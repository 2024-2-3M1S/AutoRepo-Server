package org.autorepo.server.domain.readme.service;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.readme.dto.request.ReadmeRequest;
import org.autorepo.server.domain.readme.dto.request.TechStack;
import org.autorepo.server.domain.readme.dto.request.TeamMember;
import org.autorepo.server.domain.readme.dto.response.ReadmeResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@RequiredArgsConstructor
@Service
@Transactional
public class CreateReadmeService {

    public ReadmeResponse generateReadme(ReadmeRequest readmeRequest) {
        String markdown = generateMarkdown(readmeRequest);
        return new ReadmeResponse(readmeRequest.getTitle(), markdown);
    }

    public String generateMarkdown(ReadmeRequest readmeRequest) {
        StringBuilder prompt = new StringBuilder();

        // 제목과 설명
        prompt.append("# 💻").append(readmeRequest.getTitle()).append("\n");

        // Markdown 문법 제거 - '#' 기호 제거
        String plainTextDescription = readmeRequest.getDescription()
                .replaceAll("#+", "");
        prompt.append("**프로젝트 설명**: ").append(plainTextDescription).append("\n\n");

        // 소개 섹션
        prompt.append("## 🚀 소개\n").append(readmeRequest.getDescription()).append("\n\n");

        // 기술 스택 섹션
        if (readmeRequest.getStack() != null && !readmeRequest.getStack().isEmpty()) {
            prompt.append("## 🛠️ 기술 스택\n");
            prompt.append(generateTechStackSection(readmeRequest.getStack())).append("\n\n");
        }

        // 설치 방법 섹션
        if (readmeRequest.getInstallation() != null && !readmeRequest.getInstallation().isEmpty()) {
            prompt.append("## 💻 설치 방법\n").append(readmeRequest.getInstallation()).append("\n\n");
        } else {
            prompt.append("## 💻 설치 방법\n").append(generateInstallationGuide(readmeRequest.getStack())).append("\n\n");
        }

        // 팀원 섹션
        if (readmeRequest.getTeamMembers() != null && !readmeRequest.getTeamMembers().isEmpty()) {
            prompt.append("## 👥 팀원\n").append(generateTeamMemberSection(readmeRequest.getTeamMembers())).append("\n");
        }

        return prompt.toString();
    }

    private String generateTechStackSection(List<TechStack> techStacks) {
        StringBuilder section = new StringBuilder();

        // 라벨 생성
        for (TechStack stack : techStacks) {
            String badgeUrl = getBadgeUrl(stack);
            section.append(String.format("![%s](%s) ", stack.getName(), badgeUrl));
        }
        section.append("\n\n");

        // 기술 스택 표 생성 (카테고리별 분류)
        section.append(categorizeStack(techStacks));

        return section.toString();
    }

    private String getBadgeUrl(TechStack stack) {
        return String.format("https://img.shields.io/badge/%s-%s?style=for-the-badge&logo=%s&logoColor=white",
                stack.getName().replace(" ", "%20"), stack.getColor(), stack.getIcon());
    }

    private String categorizeStack(List<TechStack> techStacks) {
        Map<String, List<String>> categories = new HashMap<>();
        categories.put("Front-end", Arrays.asList("React", "JavaScript", "TypeScript"));
        categories.put("Back-end", Arrays.asList("SpringBoot", "Java", "Node.js", "Django"));
        categories.put("AI Model", Arrays.asList("PyTorch", "TensorFlow", "GPT"));
        categories.put("Deployment", Arrays.asList("AWS", "Docker", "Nginx", "Kubernetes"));
        categories.put("Database", Arrays.asList("MySQL", "PostgreSQL", "Redis"));

        Map<String, List<String>> categorizedStacks = new HashMap<>();
        List<String> uncategorized = new ArrayList<>();

        for (TechStack stack : techStacks) {
            boolean categorized = false;
            for (Map.Entry<String, List<String>> entry : categories.entrySet()) {
                if (entry.getValue().contains(stack.getName())) {
                    categorizedStacks.computeIfAbsent(entry.getKey(), k -> new ArrayList<>()).add("`" + stack.getName() + "`");
                    categorized = true;
                    break;
                }
            }
            if (!categorized) {
                uncategorized.add("`" + stack.getName() + "`");
            }
        }

        // 표 생성
        StringBuilder table = new StringBuilder("| **Category** | **Stack** |\n|:------------:|:----------:|\n");
        categorizedStacks.forEach((category, stacks) -> {
            table.append("| **").append(category).append("** | ").append(String.join(", ", stacks)).append(" |\n");
        });

        if (!uncategorized.isEmpty()) {
            table.append("| Uncategorized | ").append(String.join(", ", uncategorized)).append(" |\n");
        }

        return table.toString();
    }

    private String generateInstallationGuide(List<TechStack> techStacks) {
        StringBuilder guide = new StringBuilder();

        // 기술 스택 기반 설치 가이드 생성
        if (techStacks.stream().anyMatch(stack -> stack.getName().equalsIgnoreCase("React"))) {
            guide.append("1. 의존성을 설치합니다:\n");
            guide.append("   ```bash\n");
            guide.append("   npm install\n");
            guide.append("   ```\n");
            guide.append("2. 애플리케이션을 시작합니다:\n");
            guide.append("   ```bash\n");
            guide.append("   npm start\n");
            guide.append("   ```\n");
        } else if (techStacks.stream().anyMatch(stack -> stack.getName().equalsIgnoreCase("SpringBoot"))) {
            guide.append("1. 빌드 도구로 프로젝트를 실행합니다:\n");
            guide.append("   ```bash\n");
            guide.append("   ./gradlew bootRun\n");
            guide.append("   ```\n");
        } else if (techStacks.stream().anyMatch(stack -> stack.getName().equalsIgnoreCase("Docker"))) {
            guide.append("1. Docker 이미지를 빌드하고 실행합니다:\n");
            guide.append("   ```bash\n");
            guide.append("   docker build -t 프로젝트명 .\n");
            guide.append("   docker run -p 8080:8080 프로젝트명\n");
            guide.append("   ```\n");
        } else {
            guide.append("- 설치 방법 정보가 제공되지 않았습니다.\n");
        }

        return guide.toString();
    }

    private String generateTeamMemberSection(List<TeamMember> teamMembers) {
        StringBuilder section = new StringBuilder();

        section.append("| **Name** | **Position** |\n");
        section.append("|:--------:|:------------:|\n");

        // 팀원 데이터
        for (TeamMember member : teamMembers) {
            section.append("| **").append(member.getName()).append("** | `").append(member.getPosition()).append("` |\n");
        }

        return section.toString();
    }
}
