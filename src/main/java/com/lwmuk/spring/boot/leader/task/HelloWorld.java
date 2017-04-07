package com.lwmuk.spring.boot.leader.task;

import com.lwmuk.spring.boot.leader.annotation.LeaderOnly;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class HelloWorld {

    private static final Logger log = LoggerFactory.getLogger(HelloWorld.class);

    @Scheduled(fixedRate = 1000L)
    @LeaderOnly
    public void sayHello() {
        log.debug("Hello, world!");
    }
}
