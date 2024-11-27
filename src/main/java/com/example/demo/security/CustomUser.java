package com.example.demo.security;

import java.util.Arrays;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import com.example.demo.dto.MemberDTO;

// 사용자 인증 정보를 저장하는 클래스

public class CustomUser extends User {

	public CustomUser(MemberDTO dto) {
		// 부모의 생성자를 호출
		// MemberDTO => User 변환
		// 인자: 아이디, 패스워드, 권한 목록
		super(dto.getId(), dto.getPassword(), Arrays.asList(new SimpleGrantedAuthority(dto.getRole()) ));
		
	}
	
}
