package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.demo.security.ApiCheckFilter;
import com.example.demo.security.ApiLoginFilter;
import com.example.demo.security.UserDetailsServiceImpl;
import com.example.demo.service.MemberService;
import com.example.demo.service.MemberServiceImpl;

// 프로젝트가 실행될때 제일 먼저 생성되는 클래스

// 시큐리티 + 설정 클래스: 프로젝트가 처음 실행될때 해당클래스가 먼저 실행됨
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	// 인증서비스를 빈으로 등록
	// API 체크 필터에서 인증객체를 생성할 때 사용됨
	@Bean
	UserDetailsServiceImpl userDetailsService() {
		return new UserDetailsServiceImpl();
	}
	
	// 메소드 위에 빈을 붙이면, 반환되는 인스턴스가 빈으로 등록됨
	// 패스워드 인코더를 빈으로 등록
	// 회원가입시, 로그인시 사용됨
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	// memberService를 빈으로 수동으로 등록
	@Bean
	public MemberService memberService() {
		return new MemberServiceImpl();
	}
	
	// 체크필터를 빈으로 등록 => 자동으로 필터체인 추가됨
	// 현재 필터체인의 개수:13 => 14
	@Bean
	public ApiCheckFilter apiCheckFilter() {
		return new ApiCheckFilter(userDetailsService());
	}
	
	// 커스텀 필터 체인을 생성하는 함수
	// 매개변수: 시큐리티 객체로, 메소드가 실행될 때 스프링이 주입해줌
	// 리턴값: 사용자 권한을 검사하는 필터체인
	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		
		// 폼로그인 비활성화
		http.formLogin().disable();
		
		// 세션 관리 비활성화 (세션 생성 안함)
		http.sessionManagement()
			.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
				
		// 인증 없이 사용할 수 있는 경로 설정
		// 나중에 커스텀 필터에서 권한 관리 추가
		http.authorizeHttpRequests()
			.requestMatchers("/login", "/register", "/board/*", "/member/*")
			.permitAll();
		
		// CSRF 공격: 입력필드에 악의적인 데이터를 담아서 전송하는 공격
		// 시큐리티는 사이트를 보호하기 위해 기본적으로 Post 요청을 막음
		// API는 입력필드가 없기 때문에 해제해도 됨..
		http.csrf().disable(); // POST 요청 사용 가능
		
		// 로그인 필터 설정
		
		// 인증 매니저를 먼저 생성
		AuthenticationManagerBuilder builder;
		builder = http.getSharedObject(AuthenticationManagerBuilder.class);
		
		builder.userDetailsService(userDetailsService())
				.passwordEncoder(passwordEncoder());
		
		AuthenticationManager manager = builder.build();
		
		// 시큐리티에 인증 매니저 등록
		http.authenticationManager(manager);
		
		// 로그인 필터 생성시 로그인 URL 설정
		ApiLoginFilter loginFilter = new ApiLoginFilter("/login", memberService());
		
		// 로그인 필터에 인증 매니저 주입
		loginFilter.setAuthenticationManager(manager);
		
		// 필터 순서 변경
		// Username~Filter: 폼로그인시 사용되는 필터
		// loginFilter > Username~Filter
		http.addFilterBefore(loginFilter, UsernamePasswordAuthenticationFilter.class);
		
		// 필터 순서 변경
		// apiCheckFilter > Username~Filter
		http.addFilterBefore(apiCheckFilter(), UsernamePasswordAuthenticationFilter.class);
		
		return http.build();
		
	}
	
}
