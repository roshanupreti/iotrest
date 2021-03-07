package com.project.iotrest.service.devices;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.iotrest.exceptions.ApplicationException;
import com.project.iotrest.pojos.enums.Keys;
import com.project.iotrest.service.particle.ParticleClient;
import io.swagger.util.Json;
import okhttp3.HttpUrl;
import org.apache.commons.collections.MapUtils;

import java.net.MalformedURLException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * @author roshan
 */
public class DeviceService extends ParticleClient {

    private static final String APPLICATION_JSON = "application/json";
    private final String particleAccessToken;

    public DeviceService(String particleAccessToken) throws MalformedURLException {
        super();
        this.particleAccessToken = particleAccessToken;
    }

    public JsonNode getDevices() throws ApplicationException {
        final String urlString = getApiBaseMap().get(Keys.PARTICLE_API_BASE.getKey()) +
                getApiBaseMap().get(Keys.PARTICLE_DEVICES_BASE.getKey());
        return executeRequest(getUrl(urlString,
                Collections.singletonMap(Keys.PARTICLE_ACCESS_TOKEN.getKey(), particleAccessToken)).toString(),
                Keys.HTTP_GET.getKey(), APPLICATION_JSON, "",
                Collections.singletonMap(Keys.CONTENT_TYPE.getKey(), APPLICATION_JSON));
    }

    public JsonNode getDeviceInfo(String deviceId) throws ApplicationException {
        final String urlString = String.join("/", getApiBaseMap().get(Keys.PARTICLE_API_BASE.getKey()) +
                getApiBaseMap().get(Keys.PARTICLE_DEVICES_BASE.getKey()), deviceId);
        return executeRequest(getUrl(urlString,
                Collections.singletonMap(Keys.PARTICLE_ACCESS_TOKEN.getKey(), particleAccessToken)).toString(),
                Keys.HTTP_GET.getKey(), APPLICATION_JSON, "",
                Collections.singletonMap(Keys.CONTENT_TYPE.getKey(), APPLICATION_JSON));
    }

    private HttpUrl getUrl(String urlString, Map<String, String> queryParamMap) {
        HttpUrl.Builder httpUrl = Objects.requireNonNull(HttpUrl.parse(urlString)).newBuilder();
        if (!MapUtils.isEmpty(queryParamMap)) {
            queryParamMap.forEach(httpUrl::addQueryParameter);
        }
        return httpUrl.build();
        /*return Objects.requireNonNull(HttpUrl.parse(urlString))
                .newBuilder()
                .addQueryParameter(Keys.PARTICLE_ACCESS_TOKEN.getKey(), particleAccessToken)
                .build();*/
    }

}
