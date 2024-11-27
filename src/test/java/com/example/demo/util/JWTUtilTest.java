package com.example.demo.util;

import org.junit.jupiter.api.Test;

import com.example.demo.security.JWTUtil;

public class JWTUtilTest {

	@Test
	public void 토큰생성() throws Exception {
		
		JWTUtil jwtUtil = new JWTUtil();
		
		String id = "user";
		
		String token = jwtUtil.generateToken(id);
		
		System.out.println("토큰: " + token);
		
	}
	
	@Test
	public void 토큰유효성검사() throws Exception {
		
		JWTUtil jwtUtil = new JWTUtil();
		
		String token = "eyJhbGciOiJIUzI1NiJ9.eyJpYXQiOjE3MzI2MDE1ODMsImV4cCI6MTczMjYwMTY0Mywic3ViIjoidXNlciJ9.00O8cTVoL06iLWGUzXyGRn6AkOgv6nnhFOUlG-AeTds";
		
		// 토큰이 유효한지 검사 후 토큰에 포함되어 있는 아이디를 추출
		String id = jwtUtil.validateAndExtract(token);
		
		System.out.println("토큰에 포함되어 있는 아이디: " + id);
		
	}
	
}
