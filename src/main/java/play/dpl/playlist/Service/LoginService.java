package play.dpl.playlist.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;
import play.dpl.playlist.Entity.Member;
@Service
public class LoginService {
    private final WebClient webClient;

    @Value("${oauth2.google.client-id}")
    String clientId;
    @Value("${oauth2.google.client-secret}")
    String clientSecret;
    @Value("${oauth2.google.redirect-uri}")
    String redirectUri;
    @Value("${oauth2.google.token-uri}")
    String tokenUri;
    @Value("${oauth2.google.resource-uri}")
    String resourceUri;

    public LoginService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public Member googleLogin(String code) {
        String accessToken = getAccessToken(code);

        JsonNode userInfo = getUserInfo(accessToken);
        String email = userInfo.get("email").asText();
        
        return Member.builder()
                    .email(email)
                    .accessCode(accessToken)
                    .build();
    }

    public String getAccessToken(String authorizationCode) {
        String tokenUrl = "https://oauth2.googleapis.com/token";

        return webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("code", authorizationCode)
                        .with("access_type","offline")
                        .with("approval_prompt","force")
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("redirect_uri", redirectUri)
                        .with("grant_type", "authorization_code"))
                .retrieve()
                .bodyToMono(JsonNode.class)
                // 일단 리프레쉬 토큰 제외
                // .map(jsonNode -> new String[]{jsonNode.get("access_token").asText(),jsonNode.get("refresh_token").asText()});
                .map(jsonNode -> jsonNode.get("access_token").asText())
                .block();
    }
    // public Mono<String> getRefreshToken(String refreshToken) {
    //     return webClient.post()
    //             .contentType(MediaType.APPLICATION_FORM_URLENCODED)
    //             .body(BodyInserters.fromFormData("refresh_token", refreshToken)
    //                     .with("client_id", clientId)
    //                     .with("client_secret", clientSecret)
    //                     .with("grant_type", "refresh_token"))
    //             .retrieve()
    //             .bodyToMono(JsonNode.class)
    //             .map(jsonNode -> jsonNode.get("access_token").asText());
    // }
    public JsonNode getUserInfo(String accessToken) {
        String userInfoUrl = "https://www.googleapis.com/oauth2/v1/userinfo";
        
        return webClient.get()
                .uri(userInfoUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();
    }
}