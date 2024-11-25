package org.autorepo.server.domain.token.service;


import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class GitHubTokenValidator {

    private final WebClient webClient = WebClient.create("https://api.github.com");

    public boolean isTokenValid(String token) {
        try {
            webClient.get()
                    .uri("/user")
                    .header("Authorization", "Bearer " + token)
                    .retrieve()
                    .toBodilessEntity()
                    .block();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
