package com.example.demo.security;

import java.io.IOException;
import java.io.PrintWriter;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.minidev.json.JSONObject;

// API 요청이 들어오면 권한이 있는지 확인하는 클래스
public class ApiCheckFilter extends OncePerRequestFilter{

	// 토큰 검사가 필요한 URL 주소
	// 로그인, 회원가입 X
	// 게시물 관련, 회원 관련
	String[] patternArr = {"/board/*", "/member/*"};	
	
	// URL 패턴을 검사하는 객체
	AntPathMatcher antPathMatcher;
	
	// 토큰을 검사하는 객체
	JWTUtil jwtUtil;
	
	// 인증객체를 생성하기 위한 인증서비스
	UserDetailsService userDetailsService;
	
	// 생성자? 인스턴스 생성 + 필요한 필드 초기화
	// 매개변수: userDetailsService
	public ApiCheckFilter(UserDetailsService service) {
		
		antPathMatcher = new AntPathMatcher();
		jwtUtil = new JWTUtil();
		// 인증서비스는 내부에서 직접 생성하지 않고
		// 외부에서 주입받는다
		// 왜? 인증서비스를 자동으로 빈으로 등록하면(@bean, @service)
		// 순서: 체크필터 > 인증서비스 순으로 되어 사용할 수 없음!
		// 동시에 생성을 하기 위해서 config에서 둘다 생성!
		this.userDetailsService = service;
		
	}
	
	// API 요청이 들어오면 항상 실행되는 함수 (Source > Override로 생성)
	// 인증정보를 검사한 후 사용자 요청을 처리
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		// 예: 패턴: /board/* | /register
		//     패턴: /member/* | /register
		// 패턴 배열이므로 안에 있는 요소를 모두 검사해야함!
		for( String pattern : patternArr ) {
			
			boolean result = antPathMatcher.match(pattern, request.getRequestURI());
			
			// 
			if(result) {
				
				// 사용자가 보낸 요청메세지에서 인증 토큰 검사
				boolean check = checkAuthHeader(request);
				
//				System.out.println("토큰이 유효한지?" + check);
				// 토큰이 유효하다면 필터의 다음 단계를 호출하여
				// 인증을 이어가기
				// 토큰 검사에 성공했다면인증객체를 만들어서 컨테이너에 저장
				// 나중에 게시물이나 댓글의 작성자로 사용 가능
				// 인증토큰이 유효하다면..
				if(check) {
					
					// 인증서비스를 사용하여 인증객체를 생성
					// 인증서비스의 로그인함수 호출
					
					// 요청메세지의 토큰에서 아이디 추출
					String id = getUserId(request);
					
//					System.out.println("id: " + id);
					// 인증서비스를 사용하여 인증 객체를 생성
					UserDetails details;
					// 로그인 함수를 사용하여 인증 객체를 생성
					details = userDetailsService.loadUserByUsername(id);
					
					if(details != null) {
						UsernamePasswordAuthenticationToken upToken;
						// 인증객체와 권한을 부여
						upToken = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());
					
						// 인증 객체에 부가 정보 추가 (요청 메세지)
						// 예를 들면 인증객체가 어떤 사용자(ip 등)을 통해 생성되었는지
						upToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
						
						// 시큐리티 컨테이너에 인증 정보를 추가
						SecurityContextHolder.getContext().setAuthentication(upToken);
						
					}
					
					// 로그인함수를 호출하기 위해 토큰에서 id 추출					
					filterChain.doFilter(request, response);
					return;
					
				} else {
					// 그렇지 않다면 에러메시지 반환
					// response 객체를 사용하여 응답메세지 만들기
					// 메세지의 헤더 설정 (응답코드와 바디데이터 형식)
					response.setStatus(HttpServletResponse.SC_FORBIDDEN);
					response.setContentType("application/json;charset=utf-8");
					
					// 메세지의 바디데이터 설정 (json데이터)
					// 인증에 실패했을 때 왜 실패했는지 원인을 알려주기
					JSONObject object = new JSONObject();
					object.put( "code", "403" );
					object.put( "message", "토큰이 유효하지 않습니다!" );
					
					// 바디데이터를 쓰기 위해 PrintWriter 객체 생성
					PrintWriter out = response.getWriter();
					out.print(object);
					
					return; // 함수종료
										
				}
				
				}
			
			}
		
			// 회원가입과 같이 권한이 필요없는 API는
			// 바로 다음 필터를 수행하여 인증을 이어가기
			filterChain.doFilter(request, response);
		
		}
		
//		// 검사가 필요한 API만 수행
//		System.out.println("ApiCheckFilter.....");
	
		// 헤더에 담긴 토큰이 유효한지 확인하는 함수
		// 매개변수: 사용자가 보낸 메세지
		// 반환값: 처리결과
		public boolean checkAuthHeader(HttpServletRequest request) {
		
			// 헤더에서 Authorization키를 가진 값을 추출
			// 키는 자유롭게 정의하기
			String auth = request.getHeader("Authorization");
			
			// 1. 인증키가 존재하는지 확인
			// 빈값 또는 빈문자열인지 확인
			if(auth!=null && auth!="") {
				
//				System.out.println("Authorization: " + auth);
				// 2. 토큰이 유효한지 확인
				// 토큰에서 아이디가 추출된다면 유효한것!
				// jwtUtil 클래스가 생성한 토큰이 아니거나
				// 유효기간이 지났다면 추출이 안됨..
				String id;
				try {
					
					id = jwtUtil.validateAndExtract(auth);
					
					if(id.length() > 0) {
						return true;
					}
					
				} catch (Exception e) {
					e.printStackTrace();
				} 
				
			}
			
			return false;
		
	}
	
	// 매개변수: 사용자가 보낸 요청 메세지
	// 반환값: 토큰에서 추출한 사용자 아이디 
	public String getUserId(HttpServletRequest request) {
		
		String auth = request.getHeader("Authorization");
		
		if(auth!=null && auth!="") {
			
			String id;
			try {
				id = jwtUtil.validateAndExtract(auth);
				return id;
			} catch (Exception e) {
				e.printStackTrace();
			} 
		
		}
		
		return null;
	
	}
}
