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

            // 프로젝트 이름과 설명
            prompt.append("# ").append(readMeRequest.getTitle()).append("\n");
            prompt.append("프로젝트 설명: ").append(readMeRequest.getDescription()).append("\n\n");

            // 소개
            prompt.append("## 🚀 소개\n");
            prompt.append(readMeRequest.getDescription()).append("\n\n");

            // 기술 스택 (categorizeStack 사용)
            prompt.append("## 🛠️ 기술 스택\n");
            String techStackTable = categorizeStack(readMeRequest.getStack());
            prompt.append(techStackTable).append("\n");

            // 설치 방법
            prompt.append("## 💻 설치 방법\n");
            prompt.append("1. 이 저장소를 클론합니다:\n");
            prompt.append("   ```bash\n");
            prompt.append("   git clone https://github.com/사용자명/프로젝트명.git\n");
            prompt.append("   ```\n\n");

            String installationGuide = generateInstallationGuide(readMeRequest.getStack());
            prompt.append("2. 설치 과정:\n").append(installationGuide).append("\n\n");

            // 팀원
            prompt.append("## 👥 팀원\n");
            if (readMeRequest.getTeamMembers() != null && !readMeRequest.getTeamMembers().isEmpty()) {
                for (String member : readMeRequest.getTeamMembers()) {
                    prompt.append("- ").append(member).append("\n");
                }
            } else {
                prompt.append("- 팀원 정보가 없습니다.\n");
            }
            prompt.append("\n");

            return prompt.toString();
        }

        private String categorizeStack(List<String> techStack) {
            Map<String, List<String>> categories = new HashMap<>();
            categories.put("Front-end", Arrays.asList("React", "JavaScript", "TypeScript"));
            categories.put("Back-end", Arrays.asList("SpringBoot", "Java", "Node.js", "Django"));
            categories.put("AI Model", Arrays.asList("PyTorch", "TensorFlow", "GPT"));
            categories.put("Deployment", Arrays.asList("AWS", "Docker", "Nginx", "Kubernetes"));
            categories.put("Database", Arrays.asList("MySQL", "PostgreSQL", "Redis"));

            Map<String, List<String>> categorizedStacks = new HashMap<>();
            List<String> uncategorized = new ArrayList<>();

            for (String tech : techStack) {
                boolean categorized = false;
                for (Map.Entry<String, List<String>> entry : categories.entrySet()) {
                    if (entry.getValue().contains(tech)) {
                        categorizedStacks.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add(tech);
                        categorized = true;
                        break;
                    }
                }
                if (!categorized) {
                    uncategorized.add(tech); // 미분류 항목
                }
            }

            StringBuilder table = new StringBuilder("| **Category** | **Stack** |\n|:------------:|:----------:|\n");
            categorizedStacks.forEach((category, stacks) -> {
                table.append("| ").append(category).append(" | ").append(String.join(", ", stacks)).append(" |\n");
            });

            if (!uncategorized.isEmpty()) {
                table.append("| Uncategorized | ").append(String.join(", ", uncategorized)).append(" |\n");
            }

            return table.toString();
        }

        private String generateInstallationGuide(List<String> techStack) {
            StringBuilder guide = new StringBuilder();

            // 핵심 기술 자동 선택 및 설치 가이드 생성
            if (techStack.contains("React")) {
                guide.append("   1. 의존성을 설치합니다:\n");
                guide.append("      ```bash\n");
                guide.append("      npm install\n");
                guide.append("      ```\n");
                guide.append("   2. 애플리케이션을 시작합니다:\n");
                guide.append("      ```bash\n");
                guide.append("      npm start\n");
                guide.append("      ```\n");
            } else if (techStack.contains("SpringBoot")) {
                guide.append("   1. 빌드 도구로 프로젝트를 실행합니다:\n");
                guide.append("      ```bash\n");
                guide.append("      ./gradlew bootRun\n");
                guide.append("      ```\n");
            } else if (techStack.contains("Docker")) {
                guide.append("   1. Docker 이미지를 빌드하고 실행합니다:\n");
                guide.append("      ```bash\n");
                guide.append("      docker build -t 프로젝트명 .\n");
                guide.append("      docker run -p 8080:8080 프로젝트명\n");
                guide.append("      ```\n");
            } else {
                guide.append("   - 설치 방법 정보가 제공되지 않았습니다.\n");
            }

            return guide.toString();
        }
    }

