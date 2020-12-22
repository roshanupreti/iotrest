package com.project.iotrest.pojos.enums;

public enum Keys {

    // Config files
    PARTICLE_API_BASE_CONFIG("particle/api_base"),
    PARTICLE_CLIENT_SECRET_CONFIG("particle/client_secret"),

    // HTTP headers and methods
    CONTENT_TYPE("Content-Type"),
    HTTP_POST("POST"),
    HTTP_GET("GET"),
    HTTP_UPDATE("UPDATE"),
    HTTP_DELETE("DELETE"),

    // particle api bases and credentials
    PARTICLE_API_BASE("particle_api_base"),
    PARTICLE_LOGIN_BASE("particle_login_base"),
    PARTICLE_CLIENT_ID("client_id"),
    PARTICLE_CLIENT_SECRET("client_secret"),
    PARTICLE_USERNAME("username"),
    PARTICLE_PASSWORD("password"),
    PARTICLE_GRANT_TYPE("grant_type"),
    PARTICLE_LOGIN_MEDIA_TYPE("application/x-www-form-urlencoded"),
    PARTICLE_DEVICES_BASE("particle_devices_base"),
    PARTICLE_ACCESS_TOKEN("access_token")
    ;

    private final String key;

    Keys(String key) {
        this.key = key;
    }

    public String getKey() {
        return key;
    }
}
