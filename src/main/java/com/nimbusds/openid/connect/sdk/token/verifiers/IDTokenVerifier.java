package com.nimbusds.openid.connect.sdk.token.verifiers;


import java.net.MalformedURLException;
import java.net.URL;

import com.nimbusds.jose.*;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.proc.BadJOSEException;
import com.nimbusds.jose.proc.JWEKeySelector;
import com.nimbusds.jose.proc.JWSKeySelector;
import com.nimbusds.jwt.*;
import com.nimbusds.jwt.proc.BadJWTException;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jwt.proc.JWTClaimsVerifier;
import com.nimbusds.oauth2.sdk.GeneralException;
import com.nimbusds.oauth2.sdk.ParseException;
import com.nimbusds.oauth2.sdk.auth.Secret;
import com.nimbusds.oauth2.sdk.id.ClientID;
import com.nimbusds.oauth2.sdk.id.Issuer;
import com.nimbusds.oauth2.sdk.jose.jwk.*;
import com.nimbusds.openid.connect.sdk.Nonce;
import com.nimbusds.openid.connect.sdk.claims.IDTokenClaimsSet;
import com.nimbusds.openid.connect.sdk.op.OIDCProviderMetadata;
import com.nimbusds.openid.connect.sdk.rp.OIDCClientInformation;
import net.jcip.annotations.ThreadSafe;


/**
 * Verifier of ID tokens issued by an OpenID Provider (OP).
 *
 * <p>Supports processing of ID tokens with the following protection:
 *
 * <ul>
 *     <li>ID tokens signed (JWS) with the OP's RSA or EC key, require the
 *         OP public JWK set (provided by value or URL) to verify them.
 *     <li>ID tokens authenticated with a JWS HMAC, require the client's secret
 *         to verify them.
 *     <li>Unsecured (plain) ID tokens received at the token endpoint.
 * </ul>
 */
@ThreadSafe
public class IDTokenVerifier {


	/**
	 * The expected ID token issuer.
	 */
	private final Issuer expectedIssuer;


	/**
	 * The requesting client.
	 */
	private final ClientID clientID;


	/**
	 * The JWS key selector.
	 */
	private final JWSKeySelector jwsKeySelector;


	/**
	 * The JWE key selector.
	 */
	private final JWEKeySelector jweKeySelector;


	/**
	 * Creates a new verifier for unsecured (plain) ID tokens.
	 *
	 * @param expectedIssuer The expected ID token issuer (OpenID
	 *                       Provider). Must not be {@code null}.
	 * @param clientID       The client ID. Must not be {@code null}.
	 */
	public IDTokenVerifier(final Issuer expectedIssuer,
			       final ClientID clientID) {

		this(expectedIssuer, clientID, (JWSKeySelector) null, (JWEKeySelector) null);
	}


	/**
	 * Creates a new verifier for RSA or EC signed ID tokens where the
	 * OpenID Provider's JWK set is specified by value.
	 *
	 * @param expectedIssuer The expected ID token issuer (OpenID
	 *                       Provider). Must not be {@code null}.
	 * @param clientID       The client ID. Must not be {@code null}.
	 * @param expectedJWSAlg The expected RSA or EC JWS algorithm. Must not
	 *                       be {@code null}.
	 * @param jwkSet         The OpenID Provider JWK set. Must not be
	 *                       {@code null}.
	 */
	public IDTokenVerifier(final Issuer expectedIssuer,
			       final ClientID clientID,
			       final JWSAlgorithm expectedJWSAlg,
			       final JWKSet jwkSet) {

		this(expectedIssuer, clientID, new JWSVerificationKeySelector(expectedIssuer, expectedJWSAlg, new ImmutableJWKSet(expectedIssuer, jwkSet)),  null);
	}


	/**
	 * Creates a new verifier for RSA or EC signed ID tokens where the
	 * OpenID Provider's JWK set is specified by URL.
	 *
	 * @param expectedIssuer The expected ID token issuer (OpenID
	 *                       Provider). Must not be {@code null}.
	 * @param clientID       The client ID. Must not be {@code null}.
	 * @param expectedJWSAlg The expected RSA or EC JWS algorithm. Must not
	 *                       be {@code null}.
	 * @param jwkSetURI      The OpenID Provider JWK set URL. Must not be
	 *                       {@code null}.
	 */
	public IDTokenVerifier(final Issuer expectedIssuer,
			       final ClientID clientID,
			       final JWSAlgorithm expectedJWSAlg,
			       final URL jwkSetURI) {

		this(expectedIssuer, clientID, new JWSVerificationKeySelector(expectedIssuer, expectedJWSAlg, new RemoteJWKSet(expectedIssuer, jwkSetURI, null)),  null);
	}


	/**
	 * Creates a new verifier for HMAC protected ID tokens.
	 *
	 * @param expectedIssuer The expected ID token issuer (OpenID
	 *                       Provider). Must not be {@code null}.
	 * @param clientID       The client ID. Must not be {@code null}.
	 * @param expectedJWSAlg The expected HMAC JWS algorithm. Must not be
	 *                       {@code null}.
	 * @param clientSecret   The client secret. Must not be {@code null}.
	 */
	public IDTokenVerifier(final Issuer expectedIssuer,
			       final ClientID clientID,
			       final JWSAlgorithm expectedJWSAlg,
			       final Secret clientSecret) {

		this(expectedIssuer, clientID, new JWSVerificationKeySelector(expectedIssuer, expectedJWSAlg, new ImmutableClientSecret(clientID, clientSecret)), null);
	}


	/**
	 * Creates a new ID token verifier.
	 *
	 * @param expectedIssuer The expected ID token issuer (OpenID
	 *                       Provider). Must not be {@code null}.
	 * @param clientID       The client ID. Must not be {@code null}.
	 * @param jwsKeySelector The key selector for JWS verification,
	 *                       {@code null} if unsecured (plain) ID tokens
	 *                       are expected.
	 * @param jweKeySelector The key selector for JWE decryption,
	 *                       {@code null} if encrypted ID tokens are not
	 *                       expected.
	 */
	public IDTokenVerifier(final Issuer expectedIssuer,
			       final ClientID clientID,
			       final JWSKeySelector jwsKeySelector,
			       final JWEKeySelector jweKeySelector) {
		if (expectedIssuer == null) {
			throw new IllegalArgumentException("The expected ID token issuer must not be null");
		}
		this.expectedIssuer = expectedIssuer;
		if (clientID == null) {
			throw new IllegalArgumentException("The client ID must not be null");
		}
		this.clientID = clientID;
		this.jwsKeySelector = jwsKeySelector;
		this.jweKeySelector = jweKeySelector;
	}


	/**
	 * Returns the expected ID token issuer.
	 *
	 * @return The ID token issuer.
	 */
	public Issuer getExpectedIssuer() {
		return expectedIssuer;
	}


	/**
	 * Returns the client ID (the expected ID token audience).
	 *
	 * @return The client ID.
	 */
	public ClientID getClientID() {
		return clientID;
	}


	/**
	 * Returns the configured JWS key selector for signed ID token
	 * verification.
	 *
	 * @return The JWS key selector, {@code null} if none.
	 */
	public JWSKeySelector getJWSKeySelector() {
		return jwsKeySelector;
	}


	/**
	 * Returns the configured JWE key selector for encrypted ID token
	 * decryption.
	 *
	 * @return The JWE key selector, {@code null}.
	 */
	public JWEKeySelector getJWEKeySelector() {
		return jweKeySelector;
	}


	/**
	 * Verifies the specified ID token.
	 *
	 * @param idToken       The ID token. Must not be {@code null}.
	 * @param expectedNonce The expected nonce, {@code null} if none.
	 *
	 * @return The claims set of the verified ID token.
	 *
	 * @throws BadJOSEException If the ID token is invalid or expired.
	 * @throws JOSEException    If an internal JOSE exception was
	 *                          encountered.
	 */
	public IDTokenClaimsSet verify(final JWT idToken, final Nonce expectedNonce)
		throws BadJOSEException, JOSEException {

		if (idToken instanceof PlainJWT) {
			return verify((PlainJWT)idToken, expectedNonce);
		} else if (idToken instanceof SignedJWT) {
			return verify((SignedJWT) idToken, expectedNonce);
		} else if (idToken instanceof EncryptedJWT) {
			return verify((EncryptedJWT) idToken, expectedNonce);
		} else {
			throw new JOSEException("Unexpected JWT type: " + idToken.getClass());
		}
	}


	/**
	 * Verifies the specified unsecured (plain) ID token.
	 *
	 * @param idToken       The ID token. Must not be {@code null}.
	 * @param expectedNonce The expected nonce, {@code null} if none.
	 *
	 * @return The claims set of the verified ID token.
	 *
	 * @throws BadJOSEException If the ID token is invalid or expired.
	 * @throws JOSEException    If an internal JOSE exception was
	 *                          encountered.
	 */
	private IDTokenClaimsSet verify(final PlainJWT idToken, final Nonce expectedNonce)
		throws BadJOSEException, JOSEException {

		if (jwsKeySelector != null) {
			throw new BadJWTException("Signed ID token expected");
		}

		JWTClaimsSet jwtClaimsSet;

		try {
			jwtClaimsSet = idToken.getJWTClaimsSet();
		} catch (java.text.ParseException e) {
			throw new BadJWTException(e.getMessage(), e);
		}

		JWTClaimsVerifier claimsVerifier = new IDTokenClaimsVerifier(expectedIssuer, clientID, expectedNonce);
		claimsVerifier.verify(jwtClaimsSet);
		return toIDTokenClaimsSet(jwtClaimsSet);
	}


	/**
	 * Verifies the specified signed ID token.
	 *
	 * @param idToken       The ID token. Must not be {@code null}.
	 * @param expectedNonce The expected nonce, {@code null} if none.
	 *
	 * @return The claims set of the verified ID token.
	 *
	 * @throws BadJOSEException If the ID token is invalid or expired.
	 * @throws JOSEException    If an internal JOSE exception was
	 *                          encountered.
	 */
	private IDTokenClaimsSet verify(final SignedJWT idToken, final Nonce expectedNonce)
		throws BadJOSEException, JOSEException {

		if (jwsKeySelector == null) {
			throw new BadJWTException("Verification of signed JWTs not configured");
		}

		ConfigurableJWTProcessor jwtProcessor = new DefaultJWTProcessor();
		jwtProcessor.setJWSKeySelector(jwsKeySelector);
		jwtProcessor.setJWTClaimsVerifier(new IDTokenClaimsVerifier(expectedIssuer, clientID, expectedNonce));
		JWTClaimsSet jwtClaimsSet = jwtProcessor.process(idToken, null);
		return toIDTokenClaimsSet(jwtClaimsSet);
	}


	/**
	 * Verifies the specified signed and encrypted ID token.
	 *
	 * @param idToken       The ID token. Must not be {@code null}.
	 * @param expectedNonce The expected nonce, {@code null} if none.
	 *
	 * @return The claims set of the verified ID token.
	 *
	 * @throws BadJOSEException If the ID token is invalid or expired.
	 * @throws JOSEException    If an internal JOSE exception was
	 *                          encountered.
	 */
	private IDTokenClaimsSet verify(final EncryptedJWT idToken, final Nonce expectedNonce)
		throws BadJOSEException, JOSEException {

		if (jweKeySelector == null) {
			throw new BadJWTException("Decryption of JWTs not configured");
		}
		if (jwsKeySelector == null) {
			throw new BadJWTException("Verification of signed JWTs not configured");
		}

		ConfigurableJWTProcessor jwtProcessor = new DefaultJWTProcessor();
		jwtProcessor.setJWSKeySelector(jwsKeySelector);
		jwtProcessor.setJWEKeySelector(jweKeySelector);
		jwtProcessor.setJWTClaimsVerifier(new IDTokenClaimsVerifier(expectedIssuer, clientID, expectedNonce));

		JWTClaimsSet jwtClaimsSet = jwtProcessor.process(idToken, null);

		return toIDTokenClaimsSet(jwtClaimsSet);
	}


	/**
	 * Converts a JWT claims set to ID token claims set.
	 *
	 * @param jwtClaimsSet The JWT claims set. Must not be {@code null}.
	 *
	 * @return The ID token claims set.
	 *
	 * @throws JOSEException If conversion failed.
	 */
	private static IDTokenClaimsSet toIDTokenClaimsSet(final JWTClaimsSet jwtClaimsSet)
		throws JOSEException {

		try {
			return new IDTokenClaimsSet(jwtClaimsSet);
		} catch (ParseException e) {
			// Claims set must be verified at this point
			throw new JOSEException(e.getMessage(), e);
		}
	}


	/**
	 * Creates a key selector for JWS verification.
	 *
	 * @param opMetadata The OpenID Provider metadata. Must not be
	 *                   {@code null}.
	 * @param clientInfo The Relying Party metadata. Must not be
	 *                   {@code null}.
	 *
	 * @return The JWS key selector.
	 *
	 * @throws GeneralException If the supplied OpenID Provider metadata or
	 *                          Relying Party metadata are missing a
	 *                          required parameter or inconsistent.
	 */
	private static JWSKeySelector createJWSKeySelector(final OIDCProviderMetadata opMetadata,
							   final OIDCClientInformation clientInfo)
		throws GeneralException {

		final Issuer expectedIssuer = opMetadata.getIssuer();
		final ClientID clientID = clientInfo.getID();
		final JWSAlgorithm expectedJWSAlg = clientInfo.getOIDCMetadata().getIDTokenJWSAlg();

		if (opMetadata.getIDTokenJWSAlgs() == null) {
			throw new GeneralException("Missing OpenID Provider id_token_signing_alg_values_supported parameter");
		}

		if (! opMetadata.getIDTokenJWSAlgs().contains(expectedJWSAlg)) {
			throw new GeneralException("The OpenID Provider doesn't support " + expectedJWSAlg + " ID tokens");
		}

		if (Algorithm.NONE.equals(expectedJWSAlg)) {
			// Skip creation of JWS key selector, plain ID tokens expected
			return null;

		} else if (JWSAlgorithm.Family.RSA.contains(expectedJWSAlg) || JWSAlgorithm.Family.EC.contains(expectedJWSAlg)) {

			JWKSource jwkSource;

			if (clientInfo.getOIDCMetadata().getJWKSet() != null) {
				// The JWK set is specified by value
				jwkSource = new ImmutableJWKSet(clientID, clientInfo.getOIDCMetadata().getJWKSet());
			} else if (clientInfo.getOIDCMetadata().getJWKSetURI() != null) {
				// The JWK set is specified by URL reference
				URL jwkSetURL;
				try {
					jwkSetURL = clientInfo.getOIDCMetadata().getJWKSetURI().toURL();
				} catch (MalformedURLException e) {
					throw new GeneralException("Invalid jwk set URI: " + e.getMessage(), e);
				}
				jwkSource = new RemoteJWKSet(clientID, jwkSetURL, null);
			} else {
				throw new GeneralException("Missing JWK set source");
			}

			return new JWSVerificationKeySelector(expectedIssuer, expectedJWSAlg, jwkSource);

		} else if (JWSAlgorithm.Family.HMAC_SHA.contains(expectedJWSAlg)) {

			Secret clientSecret = clientInfo.getSecret();
			if (clientSecret == null) {
				throw new GeneralException("Missing client secret");
			}

			return new JWSVerificationKeySelector(expectedIssuer, expectedJWSAlg, new ImmutableClientSecret(clientID, clientSecret));

		} else {
			throw new GeneralException("Unsupported JWS algorithm: " + expectedJWSAlg);
		}
	}


	/**
	 * Creates a key selector for JWE decryption.
	 *
	 * @param opMetadata      The OpenID Provider metadata. Must not be
	 *                        {@code null}.
	 * @param clientInfo      The Relying Party metadata. Must not be
	 *                        {@code null}.
	 * @param clientJWKSource The client private JWK source, {@code null}
	 *                        if encrypted ID tokens are not expected.
	 *
	 * @return The JWE key selector.
	 *
	 * @throws GeneralException If the supplied OpenID Provider metadata or
	 *                          Relying Party metadata are missing a
	 *                          required parameter or inconsistent.
	 */
	private static JWEKeySelector createJWEKeySelector(final OIDCProviderMetadata opMetadata,
							   final OIDCClientInformation clientInfo,
							   final JWKSource clientJWKSource)
		throws GeneralException {

		final JWEAlgorithm expectedJWEAlg = clientInfo.getOIDCMetadata().getIDTokenJWEAlg();
		final EncryptionMethod expectedJWEEnc = clientInfo.getOIDCMetadata().getIDTokenJWEEnc();

		if (expectedJWEAlg == null) {
			// Encrypted ID tokens not expected
			return null;
		}

		if (expectedJWEEnc == null) {
			throw new GeneralException("Missing required ID token JWE encryption method for " + expectedJWEAlg);
		}

		if (opMetadata.getIDTokenJWEAlgs() == null || ! opMetadata.getIDTokenJWEAlgs().contains(expectedJWEAlg)) {
			throw new GeneralException("The OpenID Provider doesn't support " + expectedJWEAlg + " ID tokens");
		}

		if (opMetadata.getIDTokenJWEEncs() == null || ! opMetadata.getIDTokenJWEEncs().contains(expectedJWEEnc)) {
			throw new GeneralException("The OpenID Provider doesn't support " + expectedJWEAlg + " / " + expectedJWEEnc + " ID tokens");
		}

		return new JWEDecryptionKeySelector(clientInfo.getID(), expectedJWEAlg, expectedJWEEnc, clientJWKSource);
	}


	/**
	 * Creates a new ID token verifier for the specified OpenID Provider
	 * metadata and OpenID Relying Party registration.
	 *
	 * @param opMetadata      The OpenID Provider metadata. Must not be
	 *                        {@code null}.
	 * @param clientInfo      The OpenID Relying Party registration. Must
	 *                        not be {@code null}.
	 * @param clientJWKSource The client private JWK source, {@code null}
	 *                        if encrypted ID tokens are not expected.
	 *
	 * @return The ID token verifier.
	 *
	 * @throws GeneralException If the supplied OpenID Provider metadata or
	 *                          Relying Party metadata are missing a
	 *                          required parameter or inconsistent.
	 */
	public static IDTokenVerifier create(final OIDCProviderMetadata opMetadata,
					     final OIDCClientInformation clientInfo,
					     final JWKSource clientJWKSource)
		throws GeneralException {

		// Create JWS key selector, unless id_token alg = none
		final JWSKeySelector jwsKeySelector = createJWSKeySelector(opMetadata, clientInfo);

		// Create JWE key selector if encrypted ID tokens are expected
		final JWEKeySelector jweKeySelector = createJWEKeySelector(opMetadata, clientInfo, clientJWKSource);

		return new IDTokenVerifier(opMetadata.getIssuer(), clientInfo.getID(), jwsKeySelector, jweKeySelector);
	}
}