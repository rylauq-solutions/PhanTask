package com.phantask.authentication.dto;

public class AccountCreationResponse {
    private String username;
    private String tempPasswordMessage;

    public AccountCreationResponse(String username, String tempPasswordMessage) {
        this.username = username;
        this.tempPasswordMessage = tempPasswordMessage;
    }

}
