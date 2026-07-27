package com.project.iotrest.service.devices;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.iotrest.exceptions.ApplicationException;
import com.project.iotrest.service.particle.Keys;
import com.project.iotrest.service.particle.ParticleClient;
import com.project.iotrest.service.token.ParticleLoginService;
import okhttp3.HttpUrl;
import org.apache.commons.collections.MapUtils;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

/**
 * @author roshan
 */
@Service
public class DeviceService {

    private static final String APPLICATION_JSON = "application/json";

    private final ParticleLoginService particleLoginService;
    private final ParticleClient particleClient;

    @Inject
    public DeviceService(ParticleLoginService particleLoginService, ParticleClient particleClient) {
        this.particleLoginService = particleLoginService;
        this.particleClient = particleClient;
    }

    public JsonNode getDevices() throws ApplicationException {
        final String urlString = particleClient.getApiBaseMap().get(Keys.PARTICLE_API_BASE.getKey()) +
                particleClient.getApiBaseMap().get(Keys.PARTICLE_DEVICES_BASE.getKey());
        return particleClient.executeRequest(getUrl(urlString,
                Collections.singletonMap(Keys.PARTICLE_ACCESS_TOKEN.getKey(), particleLoginService.getAccessToken())).toString(),
                Keys.HTTP_GET.getKey(), APPLICATION_JSON, "",
                Collections.singletonMap(Keys.CONTENT_TYPE.getKey(), APPLICATION_JSON));
    }

    public JsonNode getDeviceInfo(String deviceId) throws ApplicationException {
        final String urlString = String.join("/", particleClient.getApiBaseMap().get(Keys.PARTICLE_API_BASE.getKey()) +
                particleClient.getApiBaseMap().get(Keys.PARTICLE_DEVICES_BASE.getKey()), deviceId);
        return particleClient.executeRequest(getUrl(urlString,
                Collections.singletonMap(Keys.PARTICLE_ACCESS_TOKEN.getKey(), particleLoginService.getAccessToken())).toString(),
                Keys.HTTP_GET.getKey(), APPLICATION_JSON, "",
                Collections.singletonMap(Keys.CONTENT_TYPE.getKey(), APPLICATION_JSON));
    }

    public JsonNode getVariableValue(String deviceId, String variableName) throws ApplicationException {
        final String urlString = String.join("/", particleClient.getApiBaseMap().get(Keys.PARTICLE_API_BASE.getKey()) +
                particleClient.getApiBaseMap().get(Keys.PARTICLE_DEVICES_BASE.getKey()), deviceId, variableName);
        return particleClient.executeRequest(getUrl(urlString,
                Collections.singletonMap(Keys.PARTICLE_ACCESS_TOKEN.getKey(), particleLoginService.getAccessToken())).toString(),
                Keys.HTTP_GET.getKey(), APPLICATION_JSON, "",
                Collections.singletonMap(Keys.CONTENT_TYPE.getKey(), APPLICATION_JSON));
    }

    public JsonNode callFunction(String deviceId, String functionName, String arg) throws ApplicationException {
        final String urlString = String.join("/", particleClient.getApiBaseMap().get(Keys.PARTICLE_API_BASE.getKey()) +
                particleClient.getApiBaseMap().get(Keys.PARTICLE_DEVICES_BASE.getKey()), deviceId, functionName);
        String requestBody = "arg=" + URLEncoder.encode(arg == null ? "" : arg, StandardCharsets.UTF_8);
        return particleClient.executeRequest(getUrl(urlString,
                Collections.singletonMap(Keys.PARTICLE_ACCESS_TOKEN.getKey(), particleLoginService.getAccessToken())).toString(),
                Keys.HTTP_POST.getKey(), Keys.FORM_URLENCODED_MEDIA_TYPE.getKey(), requestBody,
                Collections.singletonMap(Keys.CONTENT_TYPE.getKey(), Keys.FORM_URLENCODED_MEDIA_TYPE.getKey()));
    }

    private HttpUrl getUrl(String urlString, Map<String, String> queryParamMap) {
        HttpUrl.Builder httpUrl = Objects.requireNonNull(HttpUrl.parse(urlString)).newBuilder();
        if (!MapUtils.isEmpty(queryParamMap)) {
            queryParamMap.forEach(httpUrl::addQueryParameter);
        }
        return httpUrl.build();
    }

}
