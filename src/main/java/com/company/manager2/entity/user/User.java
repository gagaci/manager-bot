package com.company.manager2.entity.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "users")
@Getter
@Setter
@AllArgsConstructor
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public class User {

  @Id
  @Column(name = "chat_id", nullable = false)
  private Long chatId;

  @Column(name = "user_name")
  private String userName;

  @Column(name = "full_name")
  private String fullName;

  @Column(name = "phone_number")
  private String phoneNumber;

  @Enumerated(value = EnumType.STRING)
  private Region region;

  @Enumerated(value = EnumType.STRING)
  private UserStep userStep;

  @Column(name = "created_at", nullable = false, updatable = false)
  @CreationTimestamp
  private LocalDateTime createdAt;

}
