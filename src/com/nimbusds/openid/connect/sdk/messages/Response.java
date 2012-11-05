package com.nimbusds.openid.connect.sdk.messages;


import com.nimbusds.openid.connect.sdk.SerializeException;

import com.nimbusds.openid.connect.sdk.http.HTTPResponse;


/**
 * Interface for OpenID Connect response messages.
 *
 * @author Vladimir Dzhuvinov
 * @version $version$ (2012-05-11)
 */
public interface Response extends Message {

	
	/**
	 * Returns the matching HTTP response.
	 *
	 * @return The HTTP response.
	 *
	 * @throws SerializeException If the OpenID Connect response message
	 *                            couldn't be serialised to an HTTP 
	 *                            response.
	 */
	public HTTPResponse toHTTPResponse() throws SerializeException;
}

