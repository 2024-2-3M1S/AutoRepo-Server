package org.autorepo.server.global.utils;

import lombok.RequiredArgsConstructor;
import org.autorepo.server.domain.repo.repository.RepoRepository;
import org.autorepo.server.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WebhookService {
    private final UserRepository userRepository;
    private final RepoRepository repoRepository;

    @Value("${discord.webhook.url}")
    private String discordWebhookUrl;

    // 회원가입 웹훅
    public String sendDiscordNotification() {
        System.out.println("Discord Webhook 호출중");
        RestTemplate restTemplate = new RestTemplate();
        Long totalMembers = userRepository.count();
        Long totalRepos = repoRepository.count();

        String message = totalMembers + "번째 AutoRepoCat이 등장했어요!\n(현재 등록 레포 수 : " + totalRepos + "개)";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> body = new HashMap<>();
        body.put("content", message);

        HttpEntity<Map<String, String>> requestEntity = new HttpEntity<>(body, headers);

        try {
            restTemplate.postForEntity(discordWebhookUrl, requestEntity, String.class);
            System.out.println("Discord Webhook 호출 성공");
        } catch (Exception e) {
            System.err.println("Discord Webhook 호출 실패: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

}
