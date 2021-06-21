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
