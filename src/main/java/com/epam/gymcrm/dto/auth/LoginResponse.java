package com.epam.gymcrm.dto.auth;

public record LoginResponse(String token, ProfileType profileType) {

  @Override
  public String toString(){
    return "LoginResponse[token=[PROTECTED]]";
  }
}
