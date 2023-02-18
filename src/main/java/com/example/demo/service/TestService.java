package com.example.demo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

@Service
@Slf4j
public class TestService {

    @Autowired
    private WebClient webClient;

    @Value("${external.api.longtask1}")
    private String url1;

    @Value("${external.api.longtask2}")
    private String url2;

    @Value("${external.api.longtask3}")
    private String url3;

    public Mono<String> longProcessJob() {
        long start = Instant.now().getEpochSecond();
        log.info("Start long process at {}", start);

        return exchange(url1).flatMap(res1 -> {
                    if (!res1.get("status").equalsIgnoreCase("success")) {
                        return Mono.error(new RuntimeException("URL1 Failed"));
                    }
                    return exchange(url2)
                            .flatMap(res2 -> {
                                        if (!res2.get("status").equalsIgnoreCase("success")) {
                                            return Mono.error(new RuntimeException("URL2 Failed"));
                                        }
                                        return exchange(url3)
                                                .flatMap(res3 -> {
                                                    if (!res3.get("status").equalsIgnoreCase("success")) {
                                                        return Mono.error(new RuntimeException("URL3 Failed"));
                                                    }
                                                    return Mono.just(String.valueOf(Instant.now().getEpochSecond()));
                                                })
                                                .onErrorResume(Mono::error);
                                    }
                            )
                            .onErrorResume(Mono::error);
                }
        )
        .onErrorResume(Mono::error);
    }

    private Mono<Map<String, String>> exchange(String url) {
        var type = new ParameterizedTypeReference<Map<String, String>>() {
        };
        log.info("Calling to external api service {}", url);
        return webClient.get().uri(url).retrieve().bodyToMono(type);
    }
}
