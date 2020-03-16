# iotrest

This project demonstrates the implementation of a jax-rs based RESTful web service using Jersey, Undertow and Weld. 
The API endpoints are secured using a token-based authentication, where the client, after proving its identity(with username and password)
, is provided with an authentication token (JWT) that is valid for a certain period of time. The client can then proceed 
to send API call requests to the server using the token in the request header, for authentication and authorization purposes.

## Prerequisites

 - Java 8
 - maven
 
 ## Modules
 
 The project has a parent module named `iotrest-root`, which contains two more child modules `iotrest` and `iotrest-jooq`.
 `iotrest` contains server implementation whereas `iotrest-jooq` contains settings related to `JOOQ` classes generation, to be
 used for database related operations.
 
 ## Build and run.
 
 1. On the project root, issue `mvn clean compile`
 2. After that, issue `mvn package`
 3. Browse to `iotrest/iotrest/target`, a file `iotrest-1.0.jar` should be present there, which can be executed to start the
 application at `localhost:8000/iotrest`
 
 OR, just run `Application.main()` from `iotrest/iotrest/src/main/java/com/project/iotrest/Application.java` after step 1.
 
 Upon initialization, the application populates the in-memory H2 database with the following two users.
 
 | id  | username | email | password | rights |
| ------------- | ------------- | ------------- | ------------- | ------------- |
| 1  | john  | admin@example.org  | secret  | CREATE, READ, UPDATE, DELETE  |
| 2  | jwick  | admin@admin.org  | secret  | READ  | 

H2 console available at `http://localhost:8082`

## REST APIs

| method  | path | required body / param | required access rights | resource |
| ------------- | ------------- | ------------- | ------------- | ------------- |
| POST  | localhost:8000/iotrest/auth/login  | user identifier, password | N/A   | authentication   |
| POST  | localhost:8000/iotrest/users  | user  | CREATE  | user   |
| GET  | localhost:8000/iotrest/users  | user name or email as query param  | READ  | user   |
| GET  | localhost:8000/iotrest/users/{id}  | user id as path param  | READ  | user   |
| DELETE  | localhost:8000/iotrest/users/{id}  | user id as path param  | DELETE  | user   |

## Acknowledgments

* Inspired by https://github.com/cassiomolin/jersey-jwt
