package com.nimbusds.openid.connect.sdk.messages;


/**
 * Claims request exception.
 *
 * @author Vladimir Dzhuvinov
 * @version $version$ (2012-04-09)
 */
public class ClaimsRequestException extends Exception {


	/**
	 * Creates a new claims request exception with the specified message.
	 *
	 * @param message The message.
	 */
	public ClaimsRequestException (final String message) {
	
		super(message);
	}
}