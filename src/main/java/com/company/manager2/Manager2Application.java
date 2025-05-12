package com.company.manager2;

import com.company.manager2.service.ManagerBot;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.longpolling.TelegramBotsLongPollingApplication;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@SpringBootApplication
@AllArgsConstructor
public class Manager2Application {

  private final ManagerBot managerBot;


  public static void main(String[] args) throws TelegramApiException {
    SpringApplication.run(Manager2Application.class, args).getBean(Manager2Application.class)
        .run();
  }

  public void run() throws TelegramApiException {
    TelegramBotsLongPollingApplication botsApplication = new TelegramBotsLongPollingApplication();

    // Register our bot
    botsApplication.registerBot(this.managerBot.getToken(), this.managerBot);
    System.out.println("the bot successfully started!");
  }

}
