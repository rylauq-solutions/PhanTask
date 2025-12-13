package com.phantask.authentication.dto;

public class AccountCreationResponse {
	private String username;
	private String tempPasswordMessage;

	public AccountCreationResponse(String username, String tempPasswordMessage) {
		this.username = username;
		this.tempPasswordMessage = tempPasswordMessage;
	}
	
	public String getUsername() { return username; }
    public String getMessage() { return tempPasswordMessage; }

}
