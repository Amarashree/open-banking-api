package org.example.openbanking;
//This class will configure your application to act as
// an OAuth 2.0 Authorisation Server.
//An OAuth 2.0 Authorisation Server needs to know:
//
//* Which clients are registered.
//* Which OAuth 2.0 flows those clients may use.
//* Which permissions (scopes) they may request.
//* How access tokens are signed.
// Spring Authorization Server provides the above functionality.
// Register clients, issue jwts, secure oauth endpoints

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.junit.jupiter.api.Order;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.context.Context;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.UUID;

@Configuration
public class SecurityConfig {
    @Bean
    public RegisteredClientRepository registeredClientRepository(){
        RegisteredClient client = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("demo-tpp") //you’ve registered a client called demo-tpp
                .clientSecret("{noop}demo-secret") //{noop} tells Spring Security not to encode the sample secret.
                .authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS)
                .scope("accounts:read")
                .build();

        return new InMemoryRegisteredClientRepository(client);
    }//We’ve registering a client that is allowed to request access tokens with the accounts:read scope.
    // We still need to configure its security filter chain, signing keys and endpoint settings.

    //Spring still needs to know which HTTP requests should be handled by the OAuth 2.0 Authorisation Server.
    //SecurityFilterCHain - Think of this as the HTTP security pipeline.
    //When a request reaches your application, Spring Security uses its filter chains to determine
    //- how the request should be handled.
    //Order(1) - This gives the Authorisation Server’s security filter chain higher priority
    // - than a general application security filter chain with a later order.
    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http)
        throws Exception{
        // OAuth2AuthorizationServerConfigurer...: Initializes the standard configurer for
        // Spring Authorization Server,
        // which automatically handles OAuth2 endpoints (like /oauth2/authorize, /oauth2/token, etc.).
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer =
                OAuth2AuthorizationServerConfigurer.authorizationServer();

        http
                .securityMatcher(authorizationServerConfigurer.getEndpointsMatcher()) //matches requests to the Authorisation Server’s configured endpoints.
                .with(authorizationServerConfigurer, Customizer.withDefaults())
                .authorizeHttpRequests(authorize->authorize
                        .anyRequest().authenticated()
                );

        return http.build();
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings(){
        return AuthorizationServerSettings.builder()
                .issuer("http://localhost:9000")
                .build();
    }


    //3. What about the JWT signing key?
    //The authorisation server needs a signing key to create signed JWTs.
    //We’ll configure an RSA key pair and the required JWK source next
    @Bean
    public JWKSource<SecurityContext> jwksource() throws Exception{

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        //Extract the Public and Private Keys below
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();

        //Build an RSA JWK(Json Web Key)
        // UUID - In Java, a UUID (Universally Unique Identifier) is an immutable 128-bit value used
        // to create unique identifiers that are highly unlikely to ever collide.
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();

        //create a JWK set containing the key
        JWKSet jwkSet = new JWKSet(rsaKey);

        //Expose the key set through JWK source
        return (jwkSelector, securityContext) -> jwkSelector.select(jwkSet);
    }

    //Add a second security filter chain
    //Your current filter chain handles the Authorisation Server endpoints.
    // We also need to configure security for other requests.
    @Bean
    @Order(2)
    //* @Order(2) gives this chain lower priority than the Authorisation Server chain.
    //* anyRequest().authenticated() requires authentication for requests handled by this chain.
    //* formLogin() enables a login form for interactive user authentication.
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http)
    throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().authenticated()
                )
                .formLogin(Customizer.withDefaults());

        return http.build();
    }

    @Bean
    OAuth2TokenCustomizer<JwtEncodingContext> jwtTokenCustomizer(){
        return context -> {
            if(context.getTokenType().getValue().equals("access_token")){
                context.getClaims().audience(List.of("bankingapi"));
            }
        };
    }

}

