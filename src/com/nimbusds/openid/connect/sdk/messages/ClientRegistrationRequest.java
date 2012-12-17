package com.nimbusds.openid.connect.sdk.messages;


import java.net.URL;

import java.util.List;
import java.util.Set;

import javax.mail.internet.InternetAddress;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.EncryptionMethod;

import com.nimbusds.openid.connect.sdk.ParseException;
import com.nimbusds.openid.connect.sdk.SerializeException;

import com.nimbusds.openid.connect.sdk.claims.ACR;
import com.nimbusds.openid.connect.sdk.claims.UserID;

import com.nimbusds.openid.connect.sdk.relyingparty.ApplicationType;

import com.nimbusds.openid.connect.sdk.http.HTTPRequest;


/**
 * The base class for client registration requests.
 *
 * <p>Example HTTP request:
 *
 * <pre>
 * POST /connect/register HTTP/1.1
 * Accept: application/x-www-form-urlencoded
 * Host: server.example.com
 * Authorization: Bearer eyJhbGciOiJSUzI1NiJ9.eyJ ... fQ.8Gj_-sj ... _X
 * 
 * type=client_associate
 * &application_type=web
 * &redirect_uris=https://client.example.org/callback
 *     %20https://client.example.org/callback2
 * &application_name=My%20Example%20
 * &application_name%23ja-Hani-JP=
 * &logo_url=https://client.example.org/logo.png
 * &user_id_type=pairwise
 * &sector_identifier_url=
 *     https://othercompany.com/file_of_redirect_uris_for_our_sites.js
 * &token_endpoint_auth_type=client_secret_basic
 * &jwk_url=https://client.example.org/my_rsa_public_key.jwk
 * &userinfo_encrypted_response_alg=RSA1_5
 * &userinfo_encrypted_response_enc=A128CBC+HS256
 * </pre>
 *
 * <p>Related specifications:
 *
 * <ul>
 *     <li>OpenID Connect Dynamic Client Registration 1.0, section 2.1.
 * </ul>
 *
 * @author Vladimir Dzhuvinov
 * @version $version$ (2012-12-17)
 */
public abstract class ClientRegistrationRequest implements Request {


	/**
	 * The registration type (always required).
	 */
	private ClientRegistrationType type;


	/**
	 * OAuth 2.0 Bearer access token (optional).
	 */
	private AccessToken accessToken = null;


	/**
	 * The redirect URIs (required). One of the URL must match the scheme, 
	 * host and path segments of the {@code redirect_uri} in the 
	 * authorization request.
	 */
	private Set<URL> redirectURIs;


	/**
	 * Administrator contacts for the client (optional).
	 */
	private List<InternetAddress> contacts = null;


	/**
	 * The client application type (optional), defaults to web.
	 */
	private ApplicationType applicationType = ApplicationType.WEB;


	/**
	 * The client application name (optional).
	 */
	private String applicationName = null;


	/**
	 * The client application logo URL (optional).
	 */
	private URL applicationLogoURL = null;


	/**
	 * The client application policy URL for use of end-user data 
	 * (optional).
	 */
	private URL privacyPolicyURL = null;


	/**
	 * The user ID type for responses to this client (optional).
	 */
	private UserID.Type userIDType = null;


	/**
	 * Sector identifier HTTPS URL (optional).
	 */
	private URL sectorIDURL = null;


	/**
	 * Token endpoint authentication method (optional), defaults to client
	 * secret basic.
	 */
	private ClientAuthenticationMethod tokenEndpointAuthMethod =
		ClientAuthenticationMethod.CLIENT_SECRET_BASIC;


	/**
	 * URL for the client's JSON Web Key (JWK) that is used in signing 
	 * Token endpoint requests and OpenID request objects )optional). If
	 * {@link #encryptionJWKURL} is not provided, also used to encrypt the
	 * ID Token and UserInfo endpoint responses to the client.
	 */
	private URL jwkURL = null;


	/**
	 * URL for the client's JSON Web Key (JWK) that is used to encrypt the
	 * ID Token and UserInfo endpoint responses to the client (optional).
	 */
	private URL encryptionJWKURL = null;


	/**
	 * URL for the client's PEM encoded X.509 certificate or certificate 
	 * chain that is used for signing Token endpoint requests and OpenID
	 * request objects (optional). If {@link #encryptionX509URL} is not 
	 * provided, also used to encrypt the ID Token and UserInfo endpoint 
	 * responses to the client.
	 */
	private URL x509URL = null;


	/**
	 * URL for the client's PEM encoded X.509 certificate or certificate
	 * chain that is used to encrypt the ID Token and UserInfo endpoint
	 * responses to the client (optional).
	 */
	private URL encryptionX509URL = null;


	/**
	 * The JSON Web Signature (JWS) algorithm required for the OpenID 
	 * request objects sent by this client (optional).
	 */
	private JWSAlgorithm requestObjectJWSAlg = null;


	/**
	 * The JSON Web Signature (JWS) algorithm required for the ID Tokens
	 * issued to this client (optional).
	 */
	private JWSAlgorithm idTokenJWSAlg = null;


	/**
	 * The JSON Web Encryption (JWE) algorithm required for the ID Tokens
	 * issued to this client (optional).
	 */
	private JWEAlgorithm idTokenJWEAlg = null;


	/**
	 * The encryption method (JWE enc) required for the ID Tokens issued to
	 * this client (optional).
	 */
	private EncryptionMethod idTokenJWEEnc = null;


	/**
	 * The JSON Web Signature (JWS) algorithm required for the UserInfo
	 * responses to this client (optional).
	 */
	private JWSAlgorithm userInfoJWSAlg = null;


	/**
	 * The JSON Web Encryption (JWE) algorithm required for the UserInfo
	 * responses to this client (optional).
	 */
	private JWEAlgorithm userInfoJWEAlg = null;


	/**
	 * The encryption method (JWE enc) required for the UserInfo responses
	 * to this client (optional).
	 */
	private EncryptionMethod userInfoJWEEnc = null;


	/**
	 * The default max authentication age, in seconds (optional). If not 
	 * specified 0.
	 */
	private int defaultMaxAge = 0;


	/**
	 * If {@code true} the {@code auth_time} claim in the ID Token is
	 * required by default (optional).
	 */
	private boolean requireAuthTime = false;


	/**
	 * The default Authentication Context Class Reference (ACR) (optional).
	 */
	private ACR defaultACR = null;


	/**
	 * Client JavaScript origin URIs.
	 */
	private Set<URL> originURIs = null;


	/**
	 * Gets the client registration type. Corresponds to the {@code type}
	 * parameter.
	 *
	 * @return The client registration type.
	 */
	public ClientRegistrationType getType() {

		return type;
	}


	/**
	 * Gets the OAuth 2.0 Bearer access token. Corresponds to the
	 * {@code access_token} parameter.
	 *
	 * @return The OAuth 2.0 Bearer access token, {@code null} if none.
	 */
	public AccessToken getAccessToken() {

		return accessToken;
	}


	/**
	 * Sets the OAuth 2.0 Bearer access token. Corresponds to the
	 * {@code access_token} parameter.
	 *
	 * @param accessToken The OAuth 2.0 Bearer access token, {@code null} 
	 *                    if none.
	 */
	public void setAccessToken(final AccessToken accessToken) {

		this.accessToken = accessToken;
	}


	/**
	 * Gets the redirect URIs for the client. Corresponds to the
	 * {@code redirect_uris} parameter.
	 *
	 * @return The redirect URIs for the client.
	 */
	public Set<URL> getRedirectURIs() {
	
		return redirectURIs;
	}


	/**
	 * Sets the redirect URIs for the client. Corresponds to the
	 * {@code redirect_uris} parameter.
	 *
	 * @param redirectURIs The redirect URIs for the client, {@code null} 
	 *                     if none.
	 */
	public void setRedirectURIs(final Set<URL> redirectURIs) {
	
		this.redirectURIs = redirectURIs;
	}


	/**
	 * Gets the client application type. Corresponds to the
	 * {@code application_type} parameter.
	 *
	 * @return The client application type, defaults to {@code WEB} if not 
	 *         specified.
	 */
	public ApplicationType getApplicationType() {

		return applicationType;
	}


	/**
	 * Sets the client application type. Corresponds to the
	 * {@code application_type} parameter.
	 *
	 * @param applicationType The client application type, {@code null} if 
	 *                        not specified.
	 */
	public void setApplicationType(final ApplicationType applicationType) {

		this.applicationType = applicationType;
	}


	/**
	 * Gets the administrator contacts for the client. Corresponds to the
	 * {@code contacts} parameter.
	 *
	 * @return The administrator contacts for the client, {@code null} if
	 *         none.
	 */
	public List<InternetAddress> getContacts() {

		return contacts;
	}


	/**
	 * Sets the administrator contacts for the client. Corresponds to the
	 * {@code contacts} parameter.
	 *
	 * @param contacts The administrator contacts for the client, 
	 *                 {@code null} if none.
	 */
	public void setContacts(final List<InternetAddress> contacts) {

		this.contacts = contacts;
	}


	/**
	 * Gets the client application name. Corresponds to the 
	 * {@code application_name} parameter.
	 *
	 * @return The client application name, {@code null} if not specified.
	 */
	public String getApplicationName() {

		return applicationName;
	}


	/**
	 * Sets the client application name. Corresponds to the 
	 * {@code application_name} parameter.
	 *
	 * @param applicationName The client application name, {@code null} if 
	 *                        not specified.
	 */
	public void setApplicationName(final String applicationName) {

		this.applicationName = applicationName;
	}


	/**
	 * Gets the client application logo URL. Corresponds to the
	 * {@code logo_url} parameter.
	 *
	 * @return The client application logo URL, {@code null} if not
	 *         specified.
	 */
	public URL getApplicationLogoURL() {

		return applicationLogoURL;
	}


	/**
	 * Sets the client application logo URL. Corresponds to the
	 * {@code logo_url} parameter.
	 *
	 * @param applicationLogoURL The client application logo URL, 
	 *                           {@code null} if not specified.
	 */
	public void setApplicationLogoURL(final URL applicationLogoURL) {

		this.applicationLogoURL = applicationLogoURL;
	}


	/**
	 * Gets the client application policy for use of end-user data.
	 * Corresponds to the {@code policy_url} parameter.
	 *
	 * @return The privacy policy URL, {@code null} if not specified.
	 */
	public URL getPrivacyPolicyURL() {

		return privacyPolicyURL;
	}


	/**
	 * Sets the client application policy for use of end-user data.
	 * Corresponds to the {@code policy_url} parameter.
	 *
	 * @param privacyPolicyURL The privacy policy URL, {@code null} if not 
	 *                         specified.
	 */
	public void setPrivacyPolicyURL(final URL privacyPolicyURL) {

		this.privacyPolicyURL = privacyPolicyURL;
	}


	/**
	 * Gets the user ID type for responses to the client. Corresponds to
	 * the {@code user_id_type} parameter.
	 *
	 * @return The user ID type, {@code null} if not specified.
	 */
	public UserID.Type getUserIDType() {

		return userIDType;
	}


	/**
	 * Sets the user ID type for responses to this client. Corresponds to
	 * the {@code user_id_type} parameter.
	 *
	 * @param userIDType The user ID type, {@code null} if not specified.
	 */
	public void setUserIDType(final UserID.Type userIDType) {

		this.userIDType = userIDType;
	}


	/**
	 * Gets the sector identifier URL. Corresponds to the 
	 * {@code sector_identifier_url} parameter.
	 *
	 * @return The sector identifier URL, {@code null} if not specified.
	 */
	public URL getSectorIDURL() {

		return sectorIDURL;
	}


	/**
	 * Sets the sector identifier URL. Corresponds to the 
	 * {@code sector_identifier_url} parameter.
	 *
	 * @param sectorIDURL The sector identifier URL, {@code null} if not 
	 *                    specified.
	 */
	public void setSectorIDURL(final URL sectorIDURL) {

		this.sectorIDURL = sectorIDURL;
	}


	/**
	 * Gets the Token endpoint authentication method. Corresponds to the
	 * {@code token_endpoint_auth_type} parameter.
	 *
	 * @return The Token endpoint authentication method, {@code null} if 
	 *         not specified.
	 */
	public ClientAuthenticationMethod getTokenEndpointAuthMethod() {

		return tokenEndpointAuthMethod;
	}


	/**
	 * Sets the Token endpoint authentication method. Corresponds to the
	 * {@code token_endpoint_auth_type} parameter.
	 *
	 * @param tokenEndpointAuthMethod The Token endpoint authentication 
	 *                                method, {@code null} if not 
	 *                                specified.
	 */
	public void setTokenEndpointAuthMethod(final ClientAuthenticationMethod tokenEndpointAuthMethod) {

		this.tokenEndpointAuthMethod = tokenEndpointAuthMethod;
	}


	/**
	 * Gets the URL for the client's JSON Web Key (JWK) that is used in 
	 * signing Token endpoint requests and OpenID request objects. If
	 * {@link #getEncryptionJWKURL} if not provided, also used to encrypt 
	 * the ID Token and UserInfo endpoint responses to the client.
	 * Corresponds to the {@code jwk_url} parameter.
	 *
	 * @return The JWK URL, {@code null} if not specified.
	 */
	public URL getJWKURL() {

		return jwkURL;
	}


	/**
	 * Sets the URL for the client's JSON Web Key (JWK) that is used in 
	 * signing Token endpoint requests and OpenID request objects. If
	 * {@link #getEncryptionJWKURL} if not provided, also used to encrypt 
	 * the ID Token and UserInfo endpoint responses to the client.
	 * Corresponds to the {@code jwk_url} parameter.
	 *
	 * @param jwkURL The JWK URL, {@code null} if not specified.
	 */
	public void setJWKURL(final URL jwkURL) {

		this.jwkURL = jwkURL;
	}


	/**
	 * Gets the URL for the client's JSON Web Key (JWK) that is used to 
	 * encrypt the ID Token and UserInfo endpoint responses to the client.
	 * Corresponds to the {@code jwk_encryption_url} parameter.
	 *
	 * @return The encryption JWK URL, {@code null} if not specified.
	 */
	public URL getEncryptionJWKURL() {

		return encryptionJWKURL;
	}


	/**
	 * Sets the URL for the client's JSON Web Key (JWK) that is used to 
	 * encrypt the ID Token and UserInfo endpoint responses to the client.
	 * Corresponds to the {@code jwk_encryption_url} parameter.
	 *
	 * @param encryptionJWKURL The encryption JWK URL, {@code null} if not 
	 *                         specified.
	 */
	public void setEncrytionJWKURL(final URL encryptionJWKURL) {

		this.encryptionJWKURL = encryptionJWKURL;
	}


	/**
	 * Gets the URL for the client's PEM encoded X.509 certificate or 
	 * certificate chain that is used for signing Token endpoint requests 
	 * and OpenID request objects. If {@link #getEncryptionX509URL} is not 
	 * provided, also used to encrypt the ID Token and UserInfo endpoint 
	 * responses to the client. Corresponds to the {@code x509_url}
	 * parameter.
	 *
	 * @return The X.509 certificate URL, {@code null} if not specified.
	 */
	public URL getX509URL() {

		return x509URL;
	}


	/**
	 * Sets the URL for the client's PEM encoded X.509 certificate or 
	 * certificate chain that is used for signing Token endpoint requests 
	 * and OpenID request objects. If {@link #getEncryptionX509URL} is not 
	 * provided, also used to encrypt the ID Token and UserInfo endpoint 
	 * responses to the client. Corresponds to the {@code x509_url}
	 * parameter.
	 *
	 * @param x509URL The X.509 certificate URL, {@code null} if not 
	 *                specified.
	 */
	public void setX509URL(final URL x509URL) {

		this.x509URL = x509URL;
	}


	/**
	 * Gets the URL for the client's PEM encoded X.509 certificate or 
	 * certificate chain that is used to encrypt the ID Token and UserInfo 
	 * endpoint responses to the client. Corresponds to the 
	 * {@code x509_encryption_url} parameter.
	 *
	 * @return The encryption X.509 certificate URL, {@code null} if not
	 *         specified.
	 */
	public URL getEncryptionX509URL() {

		return encryptionX509URL;
	}


	/**
	 * Sets the URL for the client's PEM encoded X.509 certificate or 
	 * certificate chain that is used to encrypt the ID Token and UserInfo 
	 * endpoint responses to the client. Corresponds to the 
	 * {@code x509_encryption_url} parameter.
	 *
	 * @param encryptionX509URL The encryption X.509 certificate URL, 
	 *                          {@code null} if not specified.
	 */
	public void setEncryptionX509URL(final URL encryptionX509URL) {

		this.encryptionX509URL = encryptionX509URL;
	}


	/**
	 * Gets the JSON Web Signature (JWS) algorithm required for the OpenID 
	 * request objects sent by this client. Corresponds to the
	 * {@code request_object_signing_alg} parameter.
	 *
	 * @return The JWS algorithm, {@code null} if not specified.
	 */
	public JWSAlgorithm getRequestObjectJWSAlgorithm() {

		return requestObjectJWSAlg;
	}


	/**
	 * Sets the JSON Web Signature (JWS) algorithm required for the OpenID 
	 * request objects sent by this client. Corresponds to the
	 * {@code request_object_signing_alg} parameter.
	 *
	 * @param requestObjectJWSAlg The JWS algorithm, {@code null} if not 
	 *                            specified.
	 */
	public void setRequestObjectJWSAlgorithm(final JWSAlgorithm requestObjectJWSAlg) {

		this.requestObjectJWSAlg = requestObjectJWSAlg;
	}


	/**
	 * Gets the JSON Web Signature (JWS) algorithm required for the ID 
	 * Tokens issued to this client. Corresponds to the 
	 * {@code id_token_signed_response_alg}.
	 *
	 * @return The JWS algorithm, {@code null} if not specified.
	 */
	public JWSAlgorithm getIDTokenJWSAlgorithm() {

		return idTokenJWSAlg;
	}


	/**
	 * Sets the JSON Web Signature (JWS) algorithm required for the ID 
	 * Tokens issued to this client. Corresponds to the 
	 * {@code id_token_signed_response_alg}.
	 *
	 * @param idTokenJWSAlg The JWS algorithm, {@code null} if not 
	 *                      specified.
	 */
	public void setIDTokenJWSAlgorithm(final JWSAlgorithm idTokenJWSAlg) {

		this.idTokenJWSAlg = idTokenJWSAlg;
	}


	/**
	 * Gets the JSON Web Encryption (JWE) algorithm required for the ID 
	 * Tokens issued to this client. Corresponds to the 
	 * {@code id_token_encrypted_response_alg} parameter.
	 *
	 * @return The JWE algorithm, {@code null} if not specified.
	 */
	public JWEAlgorithm getIDTokenJWEAlgorithm() {

		return idTokenJWEAlg;
	}


	/**
	 * Sets the JSON Web Encryption (JWE) algorithm required for the ID 
	 * Tokens issued to this client. Corresponds to the 
	 * {@code id_token_encrypted_response_alg} parameter.
	 *
	 * @param idTokenJWEAlg The JWE algorithm, {@code null} if not 
	 *                      specified.
	 */
	public void setIDTokenJWEAlgorithm(final JWEAlgorithm idTokenJWEAlg) {

		this.idTokenJWEAlg = idTokenJWEAlg;
	}


	/**
	 * Gets the encryption method (JWE enc) required for the ID Tokens 
	 * issued to this client. Corresponds to the 
	 * {@code id_token_encrypted_response_enc} parameter.
	 *
	 * @return The JWE encryption method, {@code null} if not specified.
	 */
	public EncryptionMethod getIDTokenJWEEncryptionMethod() {

		return idTokenJWEEnc;
	}


	/**
	 * Sets the encryption method (JWE enc) required for the ID Tokens 
	 * issued to this client. Corresponds to the 
	 * {@code id_token_encrypted_response_enc} parameter.
	 *
	 * @param idTokenJWEEnc The JWE encryption method, {@code null} if not 
	 *                      specified.
	 */
	public void setIDTokenJWEEncryptionMethod(final EncryptionMethod idTokenJWEEnc) {

		this.idTokenJWEEnc = idTokenJWEEnc;
	}


	/**
	 * Gets the JSON Web Signature (JWS) algorithm required for the 
	 * UserInfo responses to this client. Corresponds to the
	 * {@code userinfo_signed_response_alg} parameter.
	 *
	 * @return The JWS algorithm, {@code null} if not specified.
	 */
	public JWSAlgorithm getUserInfoJWSAlgorithm() {

		return userInfoJWSAlg;
	}


	/**
	 * Sets the JSON Web Signature (JWS) algorithm required for the 
	 * UserInfo responses to this client. Corresponds to the
	 * {@code userinfo_signed_response_alg} parameter.
	 *
	 * @param userInfoJWSAlg The JWS algorithm, {@code null} if not 
	 *                       specified.
	 */
	public void setUserInfoJWSAlgorithm(final JWSAlgorithm userInfoJWSAlg) {

		this.userInfoJWSAlg = userInfoJWSAlg;
	}


	/**
	 * Gets the JSON Web Encryption (JWE) algorithm required for the 
	 * UserInfo responses to this client. Corresponds to the
	 * {@code userinfo_encrypted_response_alg} parameter.
	 *
	 * @return The JWE algorithm, {@code null} if not specified.
	 */
	public JWEAlgorithm getUserInfoJWEAlgorithm() {

		return userInfoJWEAlg;
	}


	/**
	 * Sets the JSON Web Encryption (JWE) algorithm required for the 
	 * UserInfo responses to this client. Corresponds to the
	 * {@code userinfo_encrypted_response_alg} parameter.
	 *
	 * @param userInfoJWEAlg The JWE algorithm, {@code null} if not
	 *                       specified.
	 */
	public void setUserInfoJWEAlgorithm(final JWEAlgorithm userInfoJWEAlg) {

		this.userInfoJWEAlg = userInfoJWEAlg;
	}


	/**
	 * Gets the encryption method (JWE enc) required for the UserInfo 
	 * responses to this client. Corresponds to the
	 * {@code userinfo_encrypted_response_enc} parameter.
	 *
	 * @return The JWE encryption method, {@code null} if not specified.
	 */
	public EncryptionMethod getUserInfoJWEEncryptionMethod() {

		return userInfoJWEEnc;
	}


	/**
	 * Sets the encryption method (JWE enc) required for the UserInfo 
	 * responses to this client. Corresponds to the
	 * {@code userinfo_encrypted_response_enc} parameter.
	 *
	 * @param userInfoJWEEnc The JWE encryption method, {@code null} if not 
	 *                       specified.
	 */
	public void setUserInfoJWEEncryptionMethod(final EncryptionMethod userInfoJWEEnc) {

		this.userInfoJWEEnc = userInfoJWEEnc;
	}


	/**
	 * Gets the default max authentication age. Corresponds to the 
	 * {@code default_max_age} parameter.
	 *
	 * @return The default max authentication age, in seconds. If not
	 *         specified 0.
	 */
	public int getDefaultMaxAge() {

		return defaultMaxAge;
	}


	/**
	 * Sets the default max authentication age. Corresponds to the 
	 * {@code default_max_age} parameter.
	 *
	 * @param defaultMaxAge The default max authentication age, in seconds.
	 *                      If not specified 0.
	 */
	public void setDefaultMaxAge(final int defaultMaxAge) {

		this.defaultMaxAge = defaultMaxAge;
	}


	/**
	 * Gets the default requirement for the {@code auth_time} claim in the
	 * ID Token. Corresponds to the {@code require_auth_time} parameter.
	 *
	 * @return If {@code true} the {@code auth_Time} claim in the ID Token 
	 *         is required by default.
	 */
	public boolean requiresAuthTime() {

		return requireAuthTime;
	}


	/**
	 * Sets the default requirement for the {@code auth_time} claim in the
	 * ID Token. Corresponds to the {@code require_auth_time} parameter.
	 *
	 * @param requireAuthTime If {@code true} the {@code auth_Time} claim 
	 *                        in the ID Token is required by default.
	 */
	public void requireAuthTime(final boolean requireAuthTime) {

		this.requireAuthTime = requireAuthTime;
	}


	/**
	 * Gets the default Authentication Context Class Reference (ACR).
	 * Corresponds to the {@code default_acr} parameter.
	 *
	 * @return The default ACR, {@code null} if not specified.
	 */
	public ACR getDefaultACR() {

		return defaultACR;
	}


	/**
	 * Sets the default Authentication Context Class Reference (ACR).
	 * Corresponds to the {@code default_acr} parameter.
	 *
	 * @param defaultACR The default ACR, {@code null} if not specified.
	 */
	public void setDefaultACR(final ACR defaultACR) {

		this.defaultACR = defaultACR;
	}


	/**
	 * Gets the client JavaScript origin URIs. Corresponds to the 
	 * {@code javascript_origin_uris} parameter.
	 *
	 * @return The client origin URIs, {@code null}	if none specified.
	 */
	public Set<URL> getOriginURIs() {

		return originURIs;
	}


	/**
	 * Sets the client JavaScript origin URIs. Corresponds to the 
	 * {@code javascript_origin_uris} parameter.
	 *
	 * @param originURIs The client origin URIs, {@code null} if not
	 *                   specified.
	 */
	public void setOriginURIs(final Set<URL> originURIs) {

		this.originURIs = originURIs;
	}
	
	
	/**
	 * Returns the matching HTTP POST request.
	 *
	 * @return The HTTP request.
	 *
	 * @throws SerializeException If the OpenID Connect request message
	 *                            couldn't be serialised to an HTTP POST 
	 *                            request.
	 */
	@Override
	public HTTPRequest toHTTPRequest()
		throws SerializeException {
	
		return null;
	}
}