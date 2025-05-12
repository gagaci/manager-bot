package com.company.manager2.config;

import com.company.manager2.service.BotDependencies;
import com.company.manager2.service.ManagerBot;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class BotConfig {

  @Bean
  public ManagerBot managerBot(@Value("${bot.token}") String botToken,
      BotDependencies botDependencies) {
    return new ManagerBot(botToken, botDependencies);
  }

}
