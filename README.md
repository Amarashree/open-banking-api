Open Banking Security Demo

A Java and Spring Boot project demonstrating OAuth 2.0 client authentication, JWT-based access tokens and security validation for a protected REST API.

Overview

This project contains two Spring Boot applications:

* Authorisation Server: Registers a demo client, authenticates the client and issues signed JWT access tokens.
* BankingAPI: Exposes a protected accounts endpoint and validates access tokens before returning account information.

The project demonstrates core API-security concepts relevant to Open Banking.

Technologies

* Java
* Spring Boot
* Spring Security
* Spring Authorization Server
* OAuth 2.0
* JSON Web Tokens (JWT)
* JSON Web Key Set (JWKS)
* Maven
* REST APIs
* JUnit and Spring Boot testing tools

Architecture

The TPP requests an access token from the authorisation server using the OAuth 2.0 Client Credentials grant.

The TPP then sends the token to BankingAPI using the HTTP Bearer authentication scheme.

BankingAPI validates the token’s signature, issuer, audience and expiry, and checks whether the token contains the required scope.

Applications

Application	Port	Responsibility
Authorisation Server	9000	Authenticates the client and issues access tokens
BankingAPI	8080	Validates access tokens and serves protected account data

Protected Endpoint

GET /api/accounts

Required scope: accounts:read

A successful request returns mock account data in JSON format.

Security Features

* OAuth 2.0 Client Credentials grant
* Signed JWT access tokens
* Public-key publication through JWKS
* JWT signature validation
* Issuer validation
* Audience validation
* Token-expiry validation
* Scope-based endpoint authorisation

Running the Project

Start the authorisation server first, followed by BankingAPI.

Detailed setup instructions, token-request examples and API test commands are documented below.

Scope and Limitations

This is an educational API-security demonstration, not a complete production Open Banking implementation.

It uses mock account data and a demo client. A production implementation would require additional controls for customer consent, client registration, certificate management, secure secret storage, persistent signing keys, deployment and monitoring.
