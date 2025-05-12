package com.company.manager2.entity.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserState {

  private UserStep userStep = UserStep.ASK_NAME;
  private String fullName;
  private String phoneNumber;
  private String region;



}
