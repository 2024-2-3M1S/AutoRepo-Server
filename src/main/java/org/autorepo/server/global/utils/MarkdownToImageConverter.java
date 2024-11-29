package org.autorepo.server.global.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.xhtmlrenderer.swing.Java2DRenderer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

@RequiredArgsConstructor
@Service
public class MarkdownToImageConverter {

    private final AmazonS3 s3Client;

    @Value("${aws-property.s3-bucket-name}")
    private String bucketName;

    private static final int IMAGE_WIDTH = 400;
    private static final int IMAGE_HEIGHT = 300;
    private final MarkdownToImageUserAgent markdownToImageUserAgent = new MarkdownToImageUserAgent();

    // 마크다운을 이미지로 변환
    public String convertMarkdownToImage(String markdownContent, String title) {
        try {
            String xhtmlContent = generateXHTML(markdownContent);
            BufferedImage renderedImage = renderHtmlToImage(xhtmlContent);
            BufferedImage croppedImage = cropToFixedSize(renderedImage);
            return uploadImageToS3(croppedImage, title);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process and upload Markdown as image", e);
        }
    }

    // 1. 마크다운을 XHTML로 변환
    private String generateXHTML(String markdownContent) {
        String htmlBody = HtmlRenderer.builder().build()
                .render(Parser.builder().build().parse(markdownContent));

        return """
                <!DOCTYPE html>
                <html xmlns="http://www.w3.org/1999/xhtml">
                <head>
                    <meta charset="UTF-8" />
                    <style>
                        body { font-family: 'Arial', sans-serif; font-size: 14px; line-height: 1.6; margin: 0; padding: 0; }
                        table { border-collapse: collapse; width: 100%; }
                        th, td { border: 1px solid #ddd; padding: 8px; }
                        th { background-color: #f2f2f2; text-align: left; }
                        img { max-width: 100%; height: auto; }
                    </style>
                </head>
                <body>""" + sanitizeHtml(htmlBody) + "</body></html>";
    }

    private String sanitizeHtml(String htmlContent) {
        return htmlContent
                .replace("<br>", "<br />")
                .replace("<img ", "<img ")
                .replace("<hr>", "<hr />")
                .replace("<meta ", "<meta ")
                .replace("<link ", "<link />");
    }

    // 2. HTML을 이미지로 렌더링
    private BufferedImage renderHtmlToImage(String htmlContent) throws IOException {
        File tempFile = File.createTempFile("tempHtml", ".html");
        try (FileWriter writer = new FileWriter(tempFile)) {
            writer.write(htmlContent);
        }

        try {
            Java2DRenderer renderer = new Java2DRenderer(tempFile, IMAGE_WIDTH, IMAGE_HEIGHT);
            renderer.getSharedContext().setUserAgentCallback(markdownToImageUserAgent);
            renderer.setBufferedImageType(BufferedImage.TYPE_INT_ARGB);
            return renderer.getImage();
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    // 이미지 크기 조정
    private BufferedImage cropToFixedSize(BufferedImage originalImage) {
        BufferedImage croppedImage = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = croppedImage.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(originalImage, 0, 0, IMAGE_WIDTH, IMAGE_HEIGHT, null);
        graphics.dispose();
        return croppedImage;
    }

    // 3. S3에 이미지 업로드
    private String uploadImageToS3(BufferedImage image, String title) {
        String fileName = "markdown-images/" + title + "-" + System.currentTimeMillis() + ".png";

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", outputStream);
            byte[] imageBytes = outputStream.toByteArray();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("image/png");
            metadata.setContentLength(imageBytes.length);

            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes)) {
                s3Client.putObject(bucketName, fileName, inputStream, metadata);
            }
            return s3Client.getUrl(bucketName, fileName).toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image to S3", e);
        }
    }
}
