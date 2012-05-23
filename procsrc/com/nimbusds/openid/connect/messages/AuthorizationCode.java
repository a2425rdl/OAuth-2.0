package com.nimbusds.openid.connect.messages;



/**
 * OAuth 2.0 authorisation code.
 *
 * <p>See http://tools.ietf.org/html/draft-ietf-oauth-v2-25#section-1.3.1
 *
 * @author Vladimir Dzhuvinov
 * @version 0.9 (2012-04-25)
 */
public class AuthorizationCode {


	/**
	 * The code value.
	 */
	private String value;
	
	
	/**
	 * Creates a new authorisation code.
	 *
	 * @param value The code value. Must not be {@code null} or empty 
	 *              string.
	 *
	 * @throws IllegalArgumentException If the code value is {@code null} or
	 *                                  empty string.
	 */
	public AuthorizationCode(final String value) {
	
		if (value == null || value.trim().isEmpty())
			throw new IllegalArgumentException("The authorization code value must not be null or empty string");
		
		this.value = value;
	}
	
	
	/**
	 * Gets the value of this authorisation code.
	 *
	 * @return The value.
	 */
	public String getValue() {
	
		return value;
	}
	
	
	/**
	 * Gets the string representation of this authorisation code.
	 *
	 * <p> See {@link #getValue}.
	 *
	 * @return The authorisation code value.
	 */
	public String toString() {
	
		return value;
	}
}