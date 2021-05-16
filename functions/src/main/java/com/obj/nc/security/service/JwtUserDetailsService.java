package com.obj.nc.security.service;

import com.obj.nc.security.config.NcSecurityConfigProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class JwtUserDetailsService implements UserDetailsService {
	
	private final NcSecurityConfigProperties ncSecurityConfigProperties;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		if (ncSecurityConfigProperties.getUsername().equals(username)) {
			return new User(ncSecurityConfigProperties.getUsername(), ncSecurityConfigProperties.getPassword(),
					new ArrayList<>());
		} else {
			throw new UsernameNotFoundException("User not found with username: " + username);
		}
	}

}