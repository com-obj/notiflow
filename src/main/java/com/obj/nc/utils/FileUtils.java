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

package com.obj.nc.utils;

import io.micrometer.core.instrument.util.IOUtils;
import org.apache.commons.codec.binary.Base64InputStream;
import org.springframework.core.io.FileSystemResource;
import org.springframework.util.MimeType;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URI;
import java.net.URLConnection;
import java.nio.file.Files;

public class FileUtils {
    
    public static FileSystemResource newFileSystemResource(String filePathAndName) {
        return new FileSystemResource(new File(filePathAndName));
    }
    
    public static FileSystemResource newFileSystemResource(URI fileURI) {
        return new FileSystemResource(new File(fileURI));
    }
    
    public static long fileSize(FileSystemResource file) {
        try {
            return file.contentLength();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static MimeType mimeType(FileSystemResource file) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String mimeType = fileNameMap.getContentTypeFor(file.getFilename());
        return MimeType.valueOf(mimeType);
    }
    
    public static InputStream inputStream(FileSystemResource file) {
        try {
            return Files.newInputStream(file.getFile().toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public static Base64InputStream base64InputStream(FileSystemResource file) {
        InputStream inputStream = inputStream(file);
        return new Base64InputStream(inputStream);
    }
    
    public static String inputStreamToString(InputStream inputStream) {
        return IOUtils.toString(inputStream);
    }
    
}
