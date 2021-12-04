package com.teketik.spring.health.components;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SlowEndpoint {

    @GetMapping("/")
    public void get() throws InterruptedException {
        Thread.sleep(2000);
    }

}
