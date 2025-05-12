package com.company.manager2.entity.user;

import lombok.Getter;

@Getter
public enum Region {

  ANDIJAN("Andijon"),
  BUKHARA("Buxoro"),
  FERGANA("Farg‘ona"),
  JIZZAKH("Jizzax"),
  KHOREZM("Xorazm"),
  NAMANGAN("Namangan"),
  NAVOIY("Navoiy"),
  QASHQADARYO("Qashqadaryo"),
  SAMARKAND("Samarqand"),
  SIRDARYO("Sirdaryo"),
  SURKHANDARYO("Surxondaryo"),
  TASHKENT("Toshkent"),
  OTHER("Xorij");

  private final String displayName;


  Region(String displayName) {
    this.displayName = displayName;
  }

}
