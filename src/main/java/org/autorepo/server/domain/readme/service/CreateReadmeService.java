package org.autorepo.server.domain.readme.service;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.readme.dto.request.ReadmeRequest;
import org.autorepo.server.domain.readme.dto.response.ReadmeResponse;
import org.autorepo.server.domain.readme.entity.Readme;
import org.autorepo.server.domain.readme.repository.ReadmeRepository;
import org.autorepo.server.global.utils.MarkdownToImageConverter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@RequiredArgsConstructor
@Service
@Transactional
public class CreateReadmeService {

    private final MarkdownToImageConverter markdownToImageConverter;
    private final ReadmeRepository readmeRepository;

    public ReadmeResponse generateAndSaveReadme(ReadmeRequest readmeRequest) {
        String markdown = generateMarkdown(readmeRequest);
        String imageUrl = markdownToImageConverter.convertMarkdownToImage(markdown, readmeRequest.getTitle());
        Readme readme = saveReadme(readmeRequest, markdown, imageUrl);
        return new ReadmeResponse(readme.getTitle(), readme.getContent(), readme.getImageUrl());
    }

    private Readme saveReadme(ReadmeRequest readmeRequest, String markdown, String imageUrl) {
        Readme readme = new Readme();
        readme.setTitle(readmeRequest.getTitle());
        readme.setContent(markdown);
        readme.setImageUrl(imageUrl);
        return readmeRepository.save(readme);
    }

    public String generateMarkdown(ReadmeRequest readmeRequest) {
        // 마크다운 생성 로직 (기존 코드 사용)
        StringBuilder prompt = new StringBuilder();

        prompt.append("# ").append(readmeRequest.getTitle()).append("\n");
        prompt.append("프로젝트 설명: ").append(readmeRequest.getDescription()).append("\n\n");
        prompt.append("## 🚀 소개\n").append(readmeRequest.getDescription()).append("\n\n");
        prompt.append("## 🛠️ 기술 스택\n").append(categorizeStack(readmeRequest.getStack())).append("\n");
        prompt.append("## 💻 설치 방법\n").append(generateInstallationGuide(readmeRequest.getStack())).append("\n\n");
        prompt.append("## 👥 팀원\n");
        if (readmeRequest.getTeamMembers() != null && !readmeRequest.getTeamMembers().isEmpty()) {
            for (String member : readmeRequest.getTeamMembers()) {
                prompt.append("- ").append(member).append("\n");
            }
        } else {
            prompt.append("- 팀원 정보가 없습니다.\n");
        }
        return prompt.toString();
    }

    private String categorizeStack(List<String> techStack) {
        // 기술 스택 분류 로직 (기존 코드 사용)
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
                uncategorized.add(tech);
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
