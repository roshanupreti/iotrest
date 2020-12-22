package com.project.iotrest.service.devices;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.iotrest.exceptions.ApplicationException;
import com.project.iotrest.pojos.enums.Keys;
import com.project.iotrest.service.particle.ParticleClient;
import okhttp3.HttpUrl;

import java.net.MalformedURLException;
import java.util.Collections;

/**
 * @author roshan
 */
public class DeviceService extends ParticleClient {

    private final String particleAccessToken;

    public DeviceService(String particleAccessToken) throws MalformedURLException {
        super();
        this.particleAccessToken = particleAccessToken;
    }

    public JsonNode getDevices() throws ApplicationException {
        return executeRequest(getUrl().toString(),
                Keys.HTTP_GET.getKey(), "application/json", "",
                Collections.singletonMap(Keys.CONTENT_TYPE.getKey(), "application/json"));
    }

    private HttpUrl getUrl() {
        final String urlString = getApiBaseMap().get(Keys.PARTICLE_API_BASE.getKey()) +
                getApiBaseMap().get(Keys.PARTICLE_DEVICES_BASE.getKey());
        return HttpUrl.parse(urlString)
                .newBuilder()
                .addQueryParameter(Keys.PARTICLE_ACCESS_TOKEN.getKey(), particleAccessToken)
                .build();
    }

}
