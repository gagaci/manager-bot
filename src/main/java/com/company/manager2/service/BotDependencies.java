package com.company.manager2.service;

import com.company.manager2.service.user.UserService;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

@Component
@Getter
@Setter
@AllArgsConstructor
public class BotDependencies {

  private MessageSource messageSource;

  private UserService userService;

  private GoogleSheetService googleSheetService;

}
