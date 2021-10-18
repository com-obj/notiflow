/*
 *   Copyright (C) 2021 the original author or authors.
 *
 *   This file is part of Notiflow
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.obj.nc.security.service;

import com.obj.nc.security.config.NcJwtConfigProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

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