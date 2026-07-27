package com.project.iotrest.service.particle;

import lombok.Getter;

@Getter
public enum Keys {

    // Config files
    PARTICLE_API_BASE_CONFIG("particle/api_base"),
    PARTICLE_CLIENT_SECRET_CONFIG("particle/client_secret"),

    // HTTP headers and methods
    CONTENT_TYPE("Content-Type"),
    HTTP_POST("POST"),
    HTTP_GET("GET"),

    // particle api bases and credentials
    PARTICLE_API_BASE("particle_api_base"),
    PARTICLE_LOGIN_BASE("particle_login_base"),
    FORM_URLENCODED_MEDIA_TYPE("application/x-www-form-urlencoded"),
    PARTICLE_DEVICES_BASE("particle_devices_base"),
    PARTICLE_ACCESS_TOKEN("access_token")
    ;

    private final String key;

    Keys(String key) {
        this.key = key;
    }
}
