package com.company.manager2.service.user;

import com.company.manager2.entity.user.Region;
import com.company.manager2.entity.user.User;
import com.company.manager2.entity.user.UserStep;
import com.company.manager2.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  @Transactional
  public User getOrCreateUser(Long chatId) {
    return userRepository.findByChatId(chatId)
        .orElseGet(() -> {
          User user = User.builder()
              .chatId(chatId)
              .build();
          return userRepository.save(user);
        });

  }

  @Transactional
  public void updateUser(Long chatId, String userName, String fullName, String phoneNumber,
      Region region,
      UserStep step) {
    Optional<User> optionalUser = userRepository.findById(chatId);

    if (optionalUser.isPresent()) {
      userRepository.findById(chatId).ifPresent(existingUser -> {
        User updatedUser = existingUser.toBuilder()
            .fullName(
                fullName != null ? fullName : existingUser.getFullName()) // Preserve old value
            .phoneNumber(phoneNumber != null ? phoneNumber : existingUser.getPhoneNumber())
            .region(region != null ? region : existingUser.getRegion())
            .userName(userName != null ? userName : existingUser.getUserName())
            .userStep(step != null ? step : existingUser.getUserStep())
            .build();

        userRepository.save(updatedUser);
      });
    }
  }

  public UserStep getUserStep(Long chatId) {
    return userRepository.findById(chatId).map(User::getUserStep).orElse(UserStep.ASK_NAME);
  }

  public List<User> findAll() {
    return userRepository.findAll();
  }

  @Transactional
  public void setUserStep(Long chatId, UserStep step) {
    userRepository.findById(chatId).ifPresent(user -> {
      user.setUserStep(step);
      userRepository.save(user);
    });
  }

}
