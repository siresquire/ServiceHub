package com.servicehub.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.accounts")
@Getter
@Setter
public class DefaultAccountsProperties {

  private Account admin;
  private Account user;
  private Account agent;

  @Getter
  @Setter
  public static class Account {
    private String email;
    private String password;
  }

}
