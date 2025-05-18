package com.company.manager2.service;

import com.company.manager2.entity.user.Region;
import com.company.manager2.entity.user.UserState;
import com.company.manager2.entity.user.UserStep;
import com.company.manager2.service.user.UserService;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.chat.Chat;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.message.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.util.*;

@Configuration
@Slf4j
@Getter
public class ManagerBot implements LongPollingSingleThreadUpdateConsumer {

  @Value("${bot.token}")
  String token;

  private final TelegramClient telegramClient;

  private final BotDependencies botDependencies;

  private final Map<Long, UserState> userState = new HashMap<>();

  public ManagerBot(@Value("${bot.token}") String token, BotDependencies botDependencies) {
    telegramClient = new OkHttpTelegramClient(token);
    this.botDependencies = botDependencies;
    setBotCommands();
  }

  private void setBotCommands() {
    List<BotCommand> commands = List.of(
        new BotCommand("/start", "Start the bot")
    );

    SetMyCommands setMyCommands = new SetMyCommands(commands);
    try {
      telegramClient.execute(setMyCommands);
    } catch (TelegramApiException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void consume(Update update) {
    if (update.hasMessage()) {
      Message message = update.getMessage();
      Chat chat = message.getChat();
      String chatType = chat.getType();
      long chatId = update.getMessage().getChatId();

        if (chatType.equals("private")) {
            handlePrivateMessage(message, chatId, message.getFrom());
        }
    } else if (update.hasCallbackQuery()) {
      handleCallbackQuery(update.getCallbackQuery());
    }
  }


  private void handlePrivateMessage(Message message, Long chatId, User user) {
    botDependencies.getUserService().getOrCreateUser(chatId);
    userState.putIfAbsent(chatId, new UserState());
    UserState userState = this.userState.get(chatId);
    String text = message.getText();
    log.info("private message {}, with ID {}", text, chatId);

    if (message.hasContact()) {
      handleContactMessage(chatId, message);
      userState.setUserStep(UserStep.ASK_REGION);

      ReplyKeyboardRemove removeKeyBoard = new ReplyKeyboardRemove(true);
      SendMessage sendMessage = SendMessage.builder()
          .chatId(chatId)
          .text(getMessage("ask.region2"))
          .replyMarkup(removeKeyBoard)
          .parseMode("HTML")
          .build();

      try {
        telegramClient.execute(sendMessage);
      } catch (TelegramApiException e) {
        throw new RuntimeException(e);
      }
      return;
    }

    if ("/start".equals(text)) {
      log.info("/ start command chatId {} username {}", chatId, user.getUserName());

        SendVideo firstVideoMessage = new SendVideo(chatId.toString(), new InputFile(
                botDependencies.getVideoService().getFirstVideo()));
        firstVideoMessage.setSupportsStreaming(true);

        SendVideo secondVideoMessage = new SendVideo(chatId.toString(), new InputFile(
                botDependencies.getVideoService().getSecondVideo()));
        secondVideoMessage.setSupportsStreaming(true);

        List<InlineKeyboardRow> keyboard = new ArrayList<>();

        InlineKeyboardRow row = new InlineKeyboardRow();


        InlineKeyboardButton continueButton = InlineKeyboardButton.builder()
                .text(getMessage("continue.message"))
                .callbackData("continue")
                .build();

        InlineKeyboardButton stopButton = InlineKeyboardButton.builder()
                .text(getMessage("stop.message"))
                .callbackData("stop")
                .build();

        row.add(continueButton);
        row.add(stopButton);

        keyboard.add(row);

        InlineKeyboardMarkup replyMarkUp = InlineKeyboardMarkup.builder()
                .keyboard(keyboard)
                .build();


        SendMessage textMessage = SendMessage.builder()
                .chatId(chatId)
                .text(getMessage("offer.message"))
                .parseMode("HTML")
                .replyMarkup(replyMarkUp)
                .build();

        userState.setUserStep(UserStep.SHOW_VIDEO);
        try {
            log.info("show video stage chatId {} userName {}", chatId, user.getUserName());
            telegramClient.execute(firstVideoMessage);
            telegramClient.execute(secondVideoMessage);
            telegramClient.execute(textMessage);
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
        userState.setUserStep(UserStep.ASK_NAME);
    }
    else if (userState.getUserStep() == UserStep.ASK_NAME) {
      botDependencies.getUserService()
          .updateUser(chatId, user.getUserName(), text, null, null, UserStep.ASK_PHONE);
      userState.setUserStep(UserStep.ASK_PHONE);
      ReplyKeyboardMarkup markup = shareContactKeyBoard();
      SendMessage sendMessage = SendMessage.builder()
          .chatId(chatId)
          .replyMarkup(markup)
          .text(getMessage("ask.contact"))
          .build();

      try {
        telegramClient.execute(sendMessage);
      } catch (TelegramApiException e) {
        log.error("Failed to send contact keyboard", e);
      }
    } else {
      sendMessage(chatId, getMessage("unknown.command"));
    }
  }

  private ReplyKeyboardMarkup shareContactKeyBoard() {
    KeyboardButton contactButton = new KeyboardButton(getMessage("share-contact.message"));
    contactButton.setRequestContact(true);

    KeyboardRow row = new KeyboardRow();
    row.add(contactButton);

    List<KeyboardRow> keyboard = new ArrayList<>();
    keyboard.add(row);

    return ReplyKeyboardMarkup.builder()
        .keyboard(keyboard)
        .resizeKeyboard(true)
        .build();
  }

  @SneakyThrows
  private void handleContactMessage(Long chatId, Message message) {
    UserState userState = getUserState().get(chatId);
    if (userState != null && userState.getUserStep() == UserStep.ASK_PHONE) {
      String phoneNumber = message.getContact().getPhoneNumber();
      botDependencies.getUserService()
          .updateUser(chatId, null, null, phoneNumber, null,
              UserStep.ASK_REGION);

      userState.setUserStep(UserStep.ASK_REGION);

      SendMessage sendMessage = SendMessage.builder()
          .chatId(chatId)
          .text(getMessage("ask.region1"))
          .replyMarkup(getRegionSelectionKeyboard())
          .parseMode("HTML")
          .build();
      telegramClient.execute(sendMessage);
    }
  }

  public String getMessage(String key) {
    return botDependencies.getMessageSource().getMessage(key, null, Locale.getDefault());
  }

  private InlineKeyboardMarkup getRegionSelectionKeyboard() {
    List<InlineKeyboardRow> keyboard = new ArrayList<>();

    InlineKeyboardRow row = new InlineKeyboardRow();

    for (Region region : Region.values()) {
      InlineKeyboardButton button = InlineKeyboardButton.builder()
          .text(region.getDisplayName())
          .callbackData("region_" + region.name())
          .build();

      row.add(button);

      if (row.size() == 2) {
        keyboard.add(row);
        row = new InlineKeyboardRow();
      }
    }

    if (!row.isEmpty()) {
      keyboard.add(row);
    }

    return new InlineKeyboardMarkup(keyboard);
  }

  private void handleCallbackQuery(CallbackQuery callbackQuery) {
    UserService userService = botDependencies.getUserService();

    Long chatId = callbackQuery.getMessage().getChatId();
    String data = callbackQuery.getData();

    if (data.startsWith("continue")) {

      userService
              .setUserStep(chatId,UserStep.ASK_NAME);
      sendMessage(chatId,getMessage("welcome.message"));

    } else if (data.startsWith("stop")) {
      sendMessage(chatId, getMessage("rejected.message"));
    }
    else if (data.startsWith("region_")) {
      String regionName = data.replace("region_", "");
      Region selectedRegion = Region.valueOf(regionName);


      userService
          .updateUser(chatId, null, null, null, selectedRegion, UserStep.COMPLETED);

      var user = userService.getOrCreateUser(chatId);

      StringBuilder stringBuilder = new StringBuilder();

      try {
        stringBuilder.append("✅ Sizning manzilingiz: <b>")
            .append(selectedRegion.getDisplayName())
            .append("</b>\n\n✅ Ism va familiyangiz: <b>")
            .append(user.getFullName())
            .append("</b>\n\n✅ Telefon qaramingiz: <b>")
            .append(user.getPhoneNumber())
            .append("</b>\n\nRo'yxatdan o'tish yakunlandi!")
            .append("\n\n Webinat uchun link ")
            .append(botDependencies.getGoogleSheetService().getWebLink());
      } catch (Exception e) {
        throw new RuntimeException(e);
      }

      SendMessage sendMessage = SendMessage.builder()
          .chatId(chatId)
          .text(stringBuilder.toString())
          .parseMode("HTML")
          .build();

      try {
        telegramClient.execute(sendMessage);
      } catch (TelegramApiException e) {
        log.error("Error sending region confirmation", e);
      }

      AnswerCallbackQuery answer = AnswerCallbackQuery.builder()
          .callbackQueryId(callbackQuery.getId())
          .text("✅ " + selectedRegion.getDisplayName() + " tanlandi!")
          .build();

      try {
        telegramClient.execute(answer);
      } catch (TelegramApiException e) {
        log.error("Error answering callback query", e);
      }
    }
  }


  public void sendMessage(Long chatId, String text) {
    SendMessage message = SendMessage
        .builder()
        .chatId(chatId)
        .text(text)
        .parseMode("HTML")
        .build();

    message.enableHtml(true);

    try {
      telegramClient.execute(message);
    } catch (TelegramApiException e) {
      throw new RuntimeException(e);
    }
  }

}
