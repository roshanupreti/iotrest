package com.project.iotrest.service.particle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.iotrest.exceptions.ApplicationException;
import com.project.iotrest.exceptions.ErrorStatusCodes;
import com.project.iotrest.pojos.enums.Keys;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.Objects;

import static com.project.iotrest.utilities.Utilities.getResourceBundleMap;

public class ParticleClient {

    private final OkHttpClient okHttpClient;
    private final ObjectMapper mapper;
    private final Builder requestBuilder;

    private final Map<String, String> credsMap;
    private final Map<String, String> apiBaseMap;

    public ParticleClient() throws MalformedURLException {
        this.mapper = new ObjectMapper();
        this.okHttpClient = new OkHttpClient();
        this.requestBuilder = new Builder();
        this.credsMap = getResourceBundleMap(Keys.PARTICLE_CLIENT_SECRET_CONFIG.getKey());
        this.apiBaseMap = getResourceBundleMap(Keys.PARTICLE_API_BASE_CONFIG.getKey());
    }

    protected JsonNode executeRequest(String url,
                                      String httpMethod,
                                      String mediaType,
                                      String requestBody,
                                      Map<String, String> headerMap) throws ApplicationException {
        Builder request = buildRequest(url, httpMethod, mediaType, headerMap, requestBody);
        try (Response response = okHttpClient.newCall(request.build()).execute()) {
            if (response.isSuccessful()) {
                return mapper.readTree(Objects.requireNonNull(response.body()).string());
            } else {
                throw new ApplicationException(ErrorStatusCodes.SERVER_ERROR.getCode(), response.toString());
            }
        } catch (IOException | ApplicationException e) {
            throw new ApplicationException(ErrorStatusCodes.SERVER_ERROR.getCode(), e.getMessage());
        }
    }

    protected String getLoginBase() {
        return apiBaseMap.get(Keys.PARTICLE_LOGIN_BASE.getKey());
    }

    protected String getApiBase() {
        return apiBaseMap.get(Keys.PARTICLE_API_BASE.getKey());
    }

    protected Map<String, String> getApiBaseMap() {
        return apiBaseMap;
    }

    protected Map<String, String> getCredsMap() {
        return credsMap;
    }

    private Builder buildRequest(String url,
                                 String httpMethod,
                                 String mediaType,
                                 Map<String, String> headerMap,
                                 String requestBody) {
        Builder request = requestBuilder
                .url(url);
        if (Keys.HTTP_POST.getKey().equals(httpMethod)) {
            request.method(httpMethod, RequestBody.create(requestBody, MediaType.parse(mediaType)));
        }
        headerMap.forEach(request::addHeader);
        return request;
    }
}



