package com.example.app;

import jakarta.servlet.http.HttpServletResponse;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;

@RestController
@EnableCaching
@SpringBootApplication
public class AppApplication {

    Logger logger = LoggerFactory.getLogger(AppApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(AppApplication.class, args);
    }

    @GetMapping("/")
    public String root(@RequestParam(value = "name", defaultValue = "World") String name, @RequestHeader HttpHeaders headers) {
        logger.error(headers.toString());
        logger.error(String.format("Hello %s!!", name));
        logger.debug("Debugging log");
        logger.info("Info log");
        logger.warn("Hey, This is a warning!");
        logger.error("Oops! We have an Error. OK");
        return String.format("Hello %s!!", name);
    }

    @GetMapping("/io_task")
    public String io_task() throws InterruptedException {
        Thread.sleep(300);
        logger.info("io_task");
        return "io_task";
    }

    @GetMapping("/cpu_task")
    public String cpu_task() {
        for (int i = 0; i < 100; i++) {
            int tmp = i * i * i;
        }
        logger.info("cpu_task");
        return "cpu_task";
    }

    @GetMapping("/reactive/io_task")
    public Mono<String> reactiveIoTask() {
        return Mono.delay(Duration.ofMillis(300))
                .doOnNext(i -> logger.info("reactive_io_task"))
                .map(i -> "reactive_io_task");
    }

    @GetMapping("/reactive/cpu_task")
    public Mono<String> reactiveCpuTask() {
        return Mono.fromCallable(() -> {
            for (int i = 0; i < 100; i++) {
                int tmp = i * i * i;
            }
            logger.info("reactive_cpu_task");
            return "reactive_cpu_task";
        });
    }


    @GetMapping("/random_sleep")
    public String random_sleep() throws InterruptedException {
        Thread.sleep((int) (Math.random() / 5 * 10000));
        logger.info("random_sleep");
        return "random_sleep";
    }

    @GetMapping("/random_status")
    public String random_status(HttpServletResponse response) {
        List<Integer> givenList = Arrays.asList(200, 200, 300, 400, 500);
        Random rand = new Random();
        int randomElement = givenList.get(rand.nextInt(givenList.size()));
        response.setStatus(randomElement);
        logger.info("random_status");
        return "random_status";
    }

    @GetMapping("/chain")
    public String chain() {
        logger.debug("chain is starting");
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            String content = executor.submit(() -> {
                try {
                    return Request.Get("https://jsonplaceholder.typicode.com/todos/1")
                            .connectTimeout(1000)
                            .socketTimeout(1000)
                            .execute().returnContent().asString();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }).get(3000, java.util.concurrent.TimeUnit.MILLISECONDS);

        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return "chain";
    }


    @GetMapping("/error_test")
    public String error_test() throws Exception {
        throw new Exception("Error test");
    }

}

