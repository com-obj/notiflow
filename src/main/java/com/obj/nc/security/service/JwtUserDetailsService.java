package com.obj.nc.security.service;

import java.util.ArrayList;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.obj.nc.security.config.NcJwtConfigProperties;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@Service
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {
	
	private final NcJwtConfigProperties ncJwtConfigProperties;
	private final PasswordEncoder passwordEncoder;
	private String passwordBCryptEncoded;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		if (passwordBCryptEncoded == null) {
			passwordBCryptEncoded = passwordEncoder.encode(ncJwtConfigProperties.getPassword());
		}
		
		if (ncJwtConfigProperties.getUsername().equals(username)) {
			return new User(ncJwtConfigProperties.getUsername(), passwordBCryptEncoded,
					new ArrayList<>());
		} else {
			throw new UsernameNotFoundException("User not found with username: " + username);
		}
	}
	
}