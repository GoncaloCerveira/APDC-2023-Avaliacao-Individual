package pt.unl.fct.di.apdc.firstwebapp.util;

import java.util.UUID;

public class AuthToken {
	
	public static final long EXPIRATION_TIME = 1000*60*60*2; //2h

	public String username;
	public String role;
	public String state;
	public String tokenID;
	public long creationData;
	public long expirationData;
	
	public AuthToken(String username, String role, String state) {
		this.username = username;
		this.role = role;
		this.state = state;
		this.tokenID = UUID.randomUUID().toString();
		this.creationData = System.currentTimeMillis();
		this.expirationData = this.creationData + AuthToken.EXPIRATION_TIME;
	}
	
	public AuthToken(String username, String role, String state, String tokenID, long creationData, long expirationData) {
		this.username = username;
		this.role = role;
		this.state = state;
		this.tokenID = tokenID;
		this.creationData = creationData;
		this.expirationData = expirationData;	
	}
}