package com.siemens.internship.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


/**
 * In order to address asynchronous processes I decided to create a configuration class.
 * Issues:
 * - the thread pool is hardcoded in the service class which reduces reusability
 * - it is possible that the pool still runs even though the application stops, which
 *   leads to resource leakage
 * Solutions:
 * - define the executor as a Bean in the configuration class and inject it wherever needed
 */

@Configuration
@EnableAsync
public class AsyncConfig {

    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    @Bean(name = "taskExecutor")
    public TaskExecutor taskExecutor() {
        logger.info("Initializing TaskExecutor bean with thread pool configuration");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("Async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }


}
