package com.project.iotrest.service.particle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.iotrest.exceptions.ApplicationException;
import com.project.iotrest.exceptions.ErrorStatusCodes;
import lombok.Getter;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Objects;

import static com.project.iotrest.utilities.Utilities.getResourceBundleMap;

@Service
public class ParticleClient {

    private final OkHttpClient okHttpClient;
    private final ObjectMapper mapper;

    @Getter
    private final Map<String, String> credsMap;

    @Getter
    private final Map<String, String> apiBaseMap;

    @Inject
    public ParticleClient() throws MalformedURLException {
        this.mapper = new ObjectMapper();
        this.okHttpClient = new OkHttpClient();
        this.credsMap = getResourceBundleMap(Keys.PARTICLE_CLIENT_SECRET_CONFIG.getKey());
        this.apiBaseMap = getResourceBundleMap(Keys.PARTICLE_API_BASE_CONFIG.getKey());
    }

    public JsonNode executeRequest(
            String url,
            String httpMethod,
            String mediaType,
            String requestBody,
            Map<String, String> headerMap) throws ApplicationException {
        Builder request = buildRequest(url, httpMethod, mediaType, headerMap, requestBody);
        try (Response response = okHttpClient.newCall(request.build()).execute()) {
            if (response.isSuccessful()) {
                return mapper.readTree(Objects.requireNonNull(response.body()).string());
            } else {
                throw new ApplicationException(ErrorStatusCodes.SERVER_ERROR.getCode(),
                        "Particle Cloud request failed with HTTP " + response.code() + " " + response.message());
            }
        } catch (IOException | ApplicationException e) {
            throw new ApplicationException(ErrorStatusCodes.SERVER_ERROR.getCode(), e.getMessage());
        }
    }

    private Builder buildRequest(String url,
                                 String httpMethod,
                                 String mediaType,
                                 Map<String, String> headerMap,
                                 String requestBody) {
        Builder request = new Builder()
                .url(url);
        if (Keys.HTTP_POST.getKey().equals(httpMethod)) {
            request.method(httpMethod, RequestBody.create(requestBody, MediaType.parse(mediaType)));
        }
        headerMap.forEach(request::addHeader);
        return request;
    }
}

