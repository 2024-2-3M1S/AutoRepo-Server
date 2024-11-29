package org.autorepo.server.global.utils;

import org.xhtmlrenderer.swing.NaiveUserAgent;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;

// 외부 URL에서 이미지를 다운로드
public class MarkdownToImageUserAgent extends NaiveUserAgent {
    @Override
    public byte[] getBinaryResource(String uri) {
        try {
            URL url = new URL(uri);
            try (InputStream inputStream = url.openStream(); ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                return outputStream.toByteArray();
            }
        } catch (Exception e) {
            System.err.println("Failed to load resource: " + uri);
            return null;
        }
    }
}
