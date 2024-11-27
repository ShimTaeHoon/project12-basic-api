package com.example.demo.security;

import java.io.IOException;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

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
			
			if(result) {
				System.out.println("ApiCheckFilter.....");
				System.out.println("url: " + request.getRequestURI());
				
			}
			
		}
		
		// 검사가 필요한 API만 수행
		System.out.println("ApiCheckFilter.....");
		
	}

}
