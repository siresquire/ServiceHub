package com.servicehub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;


@Configuration
public class AsyncConfig implements AsyncConfigurer {

  @Override
  @Bean
  public ThreadPoolTaskExecutor getAsyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(25);
    executor.setQueueCapacity(200);
    executor.setAwaitTerminationSeconds(60);
    executor.setThreadNamePrefix("SLA_MAIL_ASYNC-");
    executor.initialize();
    return executor;
  }
}
