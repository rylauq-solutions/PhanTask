package com.phantask.authentication.dto;

import lombok.Getter;

@Getter
public class AccountCreationResponse {
	private String username;
	private String tempPasswordMessage;

	public AccountCreationResponse(String username, String tempPasswordMessage) {
		this.username = username;
		this.tempPasswordMessage = tempPasswordMessage;
	}

}
