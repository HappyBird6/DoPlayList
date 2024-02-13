package play.dpl.playlist.Service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import com.fasterxml.jackson.databind.JsonNode;

import reactor.core.publisher.Mono;
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

    public LoginService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://www.googleapis.com").build();
    }
    public void socialLogin(String code, String registrationId) {
        String accessToken = getAccessToken(code).block();
        System.out.println("accessToken = " + accessToken);
    }

    public Mono<String> getAccessToken(String authorizationCode) {
        return webClient.post()
                .uri("/oauth2/v3/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("code", authorizationCode)
                        .with("client_id", clientId)
                        .with("client_secret", clientSecret)
                        .with("redirect_uri", redirectUri)
                        .with("grant_type", "authorization_code"))
                .retrieve()
                .bodyToMono(JsonNode.class)
                .map(jsonNode -> jsonNode.get("access_token").asText());
    }

}