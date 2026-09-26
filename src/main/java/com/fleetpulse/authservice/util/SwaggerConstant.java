package com.fleetpulse.authservice.util;

public final class SwaggerConstant {

    private SwaggerConstant() {}

    //TAG CONTROLLER
    public static final String IDENTITY_ACCESS_MANAGEMENT_CONTROLLER = "Identity Access Management";

    //SUMMARY
    public static final String SUMMARY_REGISTER = "Register a new account";
    public static final String SUMMARY_LOGIN = "Authenticate an existing account";
    public static final String SUMMARY_REFRESH = "Refresh access token";
    public static final String SUMMARY_LOGOUT = "Invalidate current session";

    //DESCRIPTION
    public static final String DESCRIPTION_CONTROLLER = "Authentication and registration endpoints REST";
    public static final String DESCRIPTION_REGISTER =
            "Creates a new account with the provided email, username and password. " +
                    "Returns an access token and a refresh token upon successful registration. " +
                    "Fails with 409 Conflict if the email or username is already registered.";

    public static final String DESCRIPTION_LOGIN =
            "Authenticates an account using email/username and password. " +
                    "Returns an access token and a refresh token upon successful authentication. " +
                    "Fails with 401 Unauthorized if credentials are invalid.";

    public static final String DESCRIPTION_REFRESH =
            "Issues a new access token using a valid refresh token. " +
                    "Fails with 401 Unauthorized if the refresh token is invalid or expired.";

    public static final String DESCRIPTION_LOGOUT = "Invalidates the current session, revoking the associated refresh token.";

    //HTTP STATUS CODE
    public static final String SWAGGER_STATUS_200 = "200";
    public static final String SWAGGER_STATUS_201 = "201";
    public static final String SWAGGER_STATUS_400 = "400";
    public static final String SWAGGER_STATUS_404 = "404";
    public static final String SWAGGER_STATUS_401 = "401";
    public static final String SWAGGER_STATUS_409 = "409";
    public static final String SWAGGER_STATUS_500 = "500";

    //HTTP STATUS MESSAGE
    public static final String SUCCESS = "Successful Response";
    public static final String CREATED = "Resource created successfully";
    public static final String BAD_REQUEST = "Bad request";
    public static final String NOT_FOUND = "Not found";
    public static final String UNAUTHORIZED = "Unauthorized";
    public static final String CONFLICT = "Conflict - resource already exists";
    public static final String ERROR = "Internal Server Error";
}
