package com.example.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.example.demo.dto.MemberDTO;
import com.example.demo.service.MemberService;

// 사용자 인증을 처리하는 클래스
public class UserDetailsServiceImpl implements UserDetailsService {

	@Autowired
	MemberService service;
	
	// 사용자 아이디로 사용자 정보를 조회하고 인증 객체를 생성하는 메소드
	// 매개변수: 아이디
	// 리턴값: 인증객체
	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
 
		// 아이디로 회원정보 조회
		MemberDTO dto = service.read(username);
		
		// 회원이 존재한다면 인증객체를 생성, 아니면 에러를 발생시키기
		if(dto == null) {
			throw new UsernameNotFoundException("");
		} else {
			return new CustomUser(dto);
		}
		
	}
	// API 체크 필터에서 인증 객체를 생성할 때 사용!
	// 왜 필요할까? 토큰을 검사한 후에 인증 객체를 컨테이너에 담고 
	// 다른 API에서 작성자가 필요할 때 사용함
	
}
