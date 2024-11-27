package com.example.demo.security;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import com.example.demo.dto.MemberDTO;
import com.example.demo.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.minidev.json.JSONObject;

// 아이디와 패스워드를 사용하여 로그인 처리 후 토큰을 발급

public class ApiLoginFilter extends AbstractAuthenticationProcessingFilter {

	// 인증 토큰을 생성하는 클래스
	JWTUtil jwtUtil;

	// 회원 정보를 조회하는 클래스
//	@Autowired
	MemberService memberService;

	// 생성자? 인스턴스를 생성하는 함수
	// 생성하는 시점에 필터에 필요한 요소들을 주입
	// 필요한 서비스를 직접 생성하지 않고, 밖에서 넣어줌
	// 인자: 로그인 API의 URL 주소
	public ApiLoginFilter(String defaultFilterProcessesUrl, MemberService service) {
		// 부모의 생성자에 url 주소를 전달하여 API 주소를 설정
		super(defaultFilterProcessesUrl);
		jwtUtil = new JWTUtil();
		// 필드에 외부에서 주입 받은 인스턴스 넣기
		this.memberService = service;
	}

	// 로그인 API가 호출되면 실행되는 메소드
	// 매개변수: request객체(사용자가 전송한 메세지)
	// response객체(사용자에게 전송할 메세지)
	@Override
	public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
			throws AuthenticationException, IOException, ServletException {

		// 요청 메세지에서 바디데이터 추출
		String body = getBody(request);
		
		// String => HashMap 변환
		ObjectMapper mapper = new ObjectMapper();
		HashMap<String, String> map = mapper.readValue(body, HashMap.class);
		
//		System.out.println(map);
		String id = map.get("id");
		
		String password = map.get("password");
		
		// 만약 로그인데이터에서 아이디가 없다면 에러를 발생시키기(id 데이터가.. request때)
		if(id == null) {
			throw new BadCredentialsException("아이디가 없습니다..");
		}
		
		// 토큰 생성
		// JWT 토큰 X 인증매니저가 인증을 시도할 때 사용하는 용도!
		UsernamePasswordAuthenticationToken authToken;
		authToken = new UsernamePasswordAuthenticationToken(id, password);
		
		// 인증 매니저에게 토큰을 전달
		return getAuthenticationManager().authenticate(authToken);
	}

	// 요청 메세지에서 바디(로그인데이터)를 꺼내는 함수
	// 매개변수: 요청 메세지
	// 반환값: 바디데이터
	public String getBody(HttpServletRequest request) throws IOException {
		StringBuilder stringBuilder = new StringBuilder();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = request.getReader();
			char[] charBuffer = new char[128];
			int bytesRead;
			while ((bytesRead = bufferedReader.read(charBuffer)) != -1) {
				stringBuilder.append(charBuffer, 0, bytesRead);
			}
		} catch (IOException ex) {
			throw ex;
		} finally {
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException ex) {
					throw ex;
				}
			}
		}
		return stringBuilder.toString();
	}

	// attemptAuthentication => 성공 => successfulAuthentication
	// 로그인을 성공적으로 완료한 후 호출되는 메소드
	// 인증 토큰과 사용자정보를 만들어서 전송
	// 매개변수: 인증객체
	@Override
	protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain,
			Authentication authResult) throws IOException, ServletException {

		System.out.println("login...success!");
		System.out.println(authResult);
		
		// 아이디 추출
		String id = authResult.getName();
		System.out.println("로그인한 사용자의 아이디: " + id);
		
		String token = null;
		
		try {
			token = jwtUtil.generateToken(id);
			System.out.println("token: " + token);
			
			// 응답데이터 생성
			MemberDTO dto = memberService.read(id);
			
			HashMap<String, Object> data = new HashMap<>();
			data.put("token", token);
			data.put("user", dto);
			
			// 응답메세지의 헤더 설정
			// 바디에 담을 데이터의 형식과 인코딩
			// 데이터형식? html jsondata formdata 등..
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			
			// 응답메세지의 바디데이터 설정
			// DTO 클래스 => JSON문자열 변환
			
			// 데이터를 변환할 매퍼 생성
			// 데이터에 날짜가 있을 경우에는 변환할때 오류가 발생할 수 있음
			// registerModule 설정 필요
			// 날짜를 요소별로 분리하는 기능을 비활성화 (2024, 11, 27)
			ObjectMapper mapper;
			mapper = new ObjectMapper()
							.registerModule(new JavaTimeModule())
							.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			
			// map => json string
			String strData = mapper.writeValueAsString(data);
			
			// 응답메세지를 작성하기 위해 PrintWriter 객체 생성
			PrintWriter out = response.getWriter();
			out.print(strData); // 바디 데이터 작성
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	// 로그인에 실패한 경우 실행되는 함수
	// 실패원인을 담은 에러메세지를 만들어서 전송
	// 매개변수: request, response, failed
	// request: 사용자가 전송한 요청 메세지
	// response: 사용자에게 전송할 응답 메세지를 만들 수 있는 객체
	// failed: 요청이 왜 실패했는지 원인을 담고 있는 객체
	@Override
	protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException failed) throws IOException, ServletException {

		// 응답 메세지의 헤더 설정
		// 응답코드: 401 / 바디에 담을 데이터의 형식: JSON
		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType("application/json;charset=utf-8");
		
		// 응답 메세지의 바디 설정(바디 데이터 생성)
		JSONObject json = new JSONObject();
		json.put("code", "401");
		json.put("message", failed.getMessage());
		
		// 응답 메세지에 바디 데이터 담기
		PrintWriter out = response.getWriter();
		out.print(json);
		
	}
	
}
