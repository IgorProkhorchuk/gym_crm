package com.epam.gymcrm.dto;

public record ChangePasswordRequest(String username, String oldPassword, String newPassword) {

}
