package com.example.demo.controller;

import com.example.demo.service.TestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;

@RestController
@Slf4j
public class TestController {

    @Autowired
    private TestService testService;

    @GetMapping("/{time}")
    public Mono<String> testLongProcess(@PathVariable long time) {
        Instant start = Instant.now();
        return testService.longProcessJob()
                .subscribeOn(Schedulers.boundedElastic())
                .timeout(Duration.ofSeconds(time), Mono.fromCallable(() ->  "fallback timeout: " + Instant.now().getEpochSecond()))
                .flatMap(str -> {
                    log.info("Time passed {} s", Duration.between(start, Instant.now()));
                    return Mono.just(str);
                })
                .onErrorResume(err -> {
                    log.error("Error ", err);

                    return Mono.just("fallback error: " + Instant.now().getEpochSecond());
                });
    }
}
