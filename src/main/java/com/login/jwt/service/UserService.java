package com.login.jwt.service;

import java.util.Collections;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.login.jwt.domain.Authority;
import com.login.jwt.domain.User;
import com.login.jwt.dto.UserDto;
import com.login.jwt.repository.UserRepository;
import com.login.jwt.util.SecurityUtil;

@Service
public class UserService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public User signup(UserDto userDto) {
		if (userRepository.findOneWithAuthoritiesByUsername(userDto.getUsername()).orElse(null) != null) {
			throw new RuntimeException("이미 가입되어 있는 유저입니다.");
		}

		Authority authority = Authority.builder()
			.authorityName("ROLE_USER")
			.build();

		User user = User.builder()
			.username(userDto.getUsername())
			.password(passwordEncoder.encode(userDto.getPassword()))
			.nickname(userDto.getNickname())
			.authorities(Collections.singleton(authority))
			.activated(true)
			.build();

		return userRepository.save(user);
	}

	//유저네임을 기준으로 정보를 가져온다
	@Transactional(readOnly = true)
	public Optional<User> getUserWithAuthorities(String username){
		return userRepository.findOneWithAuthoritiesByUsername(username);
	}

	//SecurityContext에 저장된 유저네임의 정보만 가져온다
	@Transactional(readOnly = true)
	public Optional<User> getMyUserWithAuthorities(){
		return SecurityUtil.getCurrentUsername().flatMap(userRepository::findOneWithAuthoritiesByUsername);
	}
}
