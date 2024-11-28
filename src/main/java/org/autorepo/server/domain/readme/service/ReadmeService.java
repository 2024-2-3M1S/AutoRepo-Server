package org.autorepo.server.domain.readme.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.readme.dto.request.ReadmeRequest;
import org.autorepo.server.global.config.GptConfig;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.*;

    @RequiredArgsConstructor
    @Service
    @Transactional
    public class ReadmeService {
        private final RestTemplate restTemplate;
        private final HttpHeaders headers;
        private final GptConfig gptConfig;

        public String getModel() {
            return gptConfig.getModel();
        }

        private String createRequestBody(String prompt) throws JsonProcessingException {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> requestBodyMap = new HashMap<>();
            requestBodyMap.put("model", gptConfig.getModel());
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "system", "content", "You are a Markdown assistant. Respond with clean, raw Markdown text only, without any extra JSON or formatting."));
            messages.add(Map.of("role", "user", "content", prompt));
            requestBodyMap.put("messages", messages);
            return objectMapper.writeValueAsString(requestBodyMap);
        }

        public String generateMarkdown(ReadmeRequest readMeRequest) throws JsonProcessingException {
            String prompt = createPrompt(readMeRequest);
            String requestBody = createRequestBody(prompt); // JSON 생성

            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    "https://api.openai.com/v1/chat/completions", entity, String.class
            );

            // OpenAI 응답에서 Markdown 텍스트만 추출
            String responseBody = response.getBody();
            return extractMarkdownFromResponse(responseBody);
        }

        private String extractMarkdownFromResponse(String responseBody) {
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode root = objectMapper.readTree(responseBody);
                String content = root.get("choices").get(0).get("message").get("content").asText();

                // 이스케이프 문자를 실제 줄바꿈으로 변환
                return content.replace("\\n", "\n");
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Failed to parse Markdown response", e);
            }
        }

        private String createPrompt(ReadmeRequest readMeRequest) {
            StringBuilder prompt = new StringBuilder();
            prompt.append("Generate a clean, concise README in Markdown format based on the following details:\n");
            prompt.append("Project Name: ").append(readMeRequest.getTitle()).append("\n");
            prompt.append("Description: ").append(readMeRequest.getDescription()).append("\n");

            String techStackTable = categorizeStack(readMeRequest.getStack());
            prompt.append("Tech Stack:\n").append(techStackTable).append("\n");

            prompt.append("Team Members: ").append(String.join(", ", readMeRequest.getTeamMembers())).append("\n");

            String installationGuide = generateInstallationGuide(readMeRequest.getStack(), readMeRequest.getInstallation());
            prompt.append("Installation Guide:\n").append(installationGuide).append("\n");

            return prompt.toString();
        }

        private String categorizeStack(List<String> techStack) {
            Map<String, List<String>> categories = new HashMap<>();
            categories.put("Front-end", Arrays.asList("React", "JavaScript", "TypeScript"));
            categories.put("Back-end", Arrays.asList("SpringBoot", "Java", "Node.js", "Django"));
            categories.put("AI Model", Arrays.asList("PyTorch", "TensorFlow", "GPT"));
            categories.put("Deployment", Arrays.asList("AWS", "Docker", "Nginx", "Kubernetes"));

            Map<String, List<String>> categorizedStacks = new HashMap<>();
            for (String tech : techStack) {
                for (Map.Entry<String, List<String>> entry : categories.entrySet()) {
                    if (entry.getValue().contains(tech)) {
                        categorizedStacks.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(tech);
                    }
                }
            }

            StringBuilder table = new StringBuilder("| **Category** | **Stack** |\n|:------------:|:----------:|\n");
            categorizedStacks.forEach((category, stacks) -> {
                table.append("| ").append(category).append(" | ").append(String.join(", ", stacks)).append(" |\n");
            });
            return table.toString();
        }

        private String generateInstallationGuide(List<String> techStack, String userGuide) {
            StringBuilder guide = new StringBuilder();

            // React 관련 설치 가이드 추가
            if (techStack.contains("React")) {
                guide.append("1. Install dependencies: `npm install`\n");
                guide.append("2. Start the application: `npm run start`\n");
            }
            guide.append(userGuide);
            return guide.toString();
        }
    }

