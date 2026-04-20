package com.netpoint.main;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LoggingTest {
    private static final Logger logger = LoggerFactory.getLogger(LoggingTest.class);

    @PostConstruct
    public void test() {
        logger.info("--- LOGGING SYSTEM INITIALIZED ---");
        logger.debug("Debug mode is ACTIVE for com.netpoint.main");
        logger.error("Testing error log: System is running fine.");
    }
}
