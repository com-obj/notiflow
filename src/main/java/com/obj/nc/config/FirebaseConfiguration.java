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

package com.obj.nc.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

import java.io.FileInputStream;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FirebaseConfiguration {
    
    private final Properties properties;
    
    @PostConstruct
    public void initialize() {
        if (properties.getServiceAccountFilePath() == null) {
            return;
        }
        
        try {
            FileInputStream serviceAccount = new FileInputStream(properties.getServiceAccountFilePath());
            FirebaseOptions options = FirebaseOptions
                    .builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();
            FirebaseApp.initializeApp(options);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Error initializing Firebase : %s ", e.getMessage()));
        }
    }
    
    @Data
    @Configuration
    @ConfigurationProperties("nc.firebase")
    public static class Properties {
        
        private String serviceAccountFilePath;
        
    }
    
}
