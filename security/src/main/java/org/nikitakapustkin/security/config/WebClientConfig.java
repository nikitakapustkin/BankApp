package org.nikitakapustkin.security.config;

import io.netty.channel.ChannelOption;
import java.time.Duration;
import org.nikitakapustkin.security.adapters.out.bank.BankServiceTokenProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {
  @Bean
  public WebClient webClient(
      @Value("${bank.base-url:http://localhost:8080}") String baseUrl,
      @Value("${bank.webclient.connect-timeout-ms:2000}") int connectTimeoutMs,
      @Value("${bank.webclient.response-timeout-ms:5000}") int responseTimeoutMs,
      BankServiceTokenProvider tokenProvider) {
    HttpClient httpClient =
        HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeoutMs)
            .responseTimeout(Duration.ofMillis(responseTimeoutMs));
    return WebClient.builder()
        .baseUrl(baseUrl)
        .clientConnector(new ReactorClientHttpConnector(httpClient))
        .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
        .filter(
            (request, next) -> {
              ClientRequest authorized =
                  ClientRequest.from(request)
                      .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenProvider.getToken())
                      .build();
              return next.exchange(authorized);
            })
        .build();
  }
}
