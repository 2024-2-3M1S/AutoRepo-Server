package org.autorepo.server.global.utils;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@RequiredArgsConstructor
@Service
public class MarkdownToImageConverter {

    private final AmazonS3 s3Client;

    @Value("${aws-property.s3-bucket-name}")
    private String bucketName;

    private static final int IMAGE_WIDTH = 400;
    private static final int IMAGE_HEIGHT = 300;

    public String convertMarkdownToImage(String markdownContent, String title) {
        try {
            // 1. Markdown -> HTML 변환
            String htmlContent = HtmlRenderer.builder()
                    .build()
                    .render(Parser.builder().build().parse(markdownContent));

            // 2. HTML -> BufferedImage 변환
            BufferedImage image = renderHtmlToImage(htmlContent);

            // 3. BufferedImage -> S3 업로드
            return uploadImageToS3(image, title);
        } catch (Exception e) {
            throw new RuntimeException("Failed to process and upload Markdown as image", e);
        }
    }

    // HTML -> Image 변환
    private BufferedImage renderHtmlToImage(String htmlContent) {
        JEditorPane editorPane = new JEditorPane();
        editorPane.setContentType("text/html");
        editorPane.setText(htmlContent);
        editorPane.setSize(IMAGE_WIDTH, IMAGE_HEIGHT);
        editorPane.setFont(new Font("Malgun Gothic", Font.PLAIN, 14));
        editorPane.setEditable(false);

        BufferedImage image = new BufferedImage(IMAGE_WIDTH, IMAGE_HEIGHT, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, IMAGE_WIDTH, IMAGE_HEIGHT);
        editorPane.paint(graphics);
        graphics.dispose();

        return image;
    }

    // S3에 이미지 업로드
    private String uploadImageToS3(BufferedImage image, String title) {
        String fileName = "markdown-images/" + title + "-" + System.currentTimeMillis() + ".png";
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType("image/png");
            metadata.setContentLength(imageBytes.length);

            try (ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes)) {
                s3Client.putObject(bucketName, fileName, inputStream, metadata);
            }
            return s3Client.getUrl(bucketName, fileName).toString();
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image to S3", e);
        }
    }
}
