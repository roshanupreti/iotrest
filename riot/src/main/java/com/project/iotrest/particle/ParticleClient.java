package com.project.iotrest.particle;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.iotrest.exceptions.ApplicationException;
import com.project.iotrest.exceptions.ErrorStatusCodes;
import com.project.iotrest.utilities.Utilities;
import okhttp3.*;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@Dependent
public class ParticleClient {

    /*private static final String LOGIN_BASE = "https://api.particle.io/oauth/token";
    private static final String API_BASE = "https://api.particle.io/v1/";
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final OkHttpClient httpClient = new OkHttpClient();*/
    private String loginBaseURL;
    private String apiBaseURL;
    private String clientID;
    private String clientSecret;
    private String userName;
    private String password;
    private String grantType;
    private OkHttpClient okHttpClient;
    private ResourceBundle rb;
    private Map<String, String> resourceBundleMap;
    private final ObjectMapper mapper = new ObjectMapper();

    @Inject
    public ParticleClient() throws MalformedURLException {
        this.rb = Utilities.getResourceBundleFromFile("particle/particle");
        this.resourceBundleMap = getResourceBundleMap(rb);
        this.loginBaseURL = resourceBundleMap.get("particle_login_base");
        this.apiBaseURL = resourceBundleMap.get("particle_api_base");
        this.clientID = resourceBundleMap.get("client_id");
        this.clientSecret = resourceBundleMap.get("client_secret");
        this.userName = resourceBundleMap.get("username");
        this.password = resourceBundleMap.get("password");
        this.grantType = resourceBundleMap.get("grant_type");
        this.okHttpClient = new OkHttpClient();
    }

    public JsonNode login() throws ApplicationException {
        try {
            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            final Request request = new Request.Builder()
                    .url(loginBaseURL)
                    .method("POST", RequestBody.create(prepareRequestBodyString(), mediaType))
                    .addHeader("Authorization", Credentials.basic(resourceBundleMap.get("client_id"),
                            resourceBundleMap.get("client_secret")))
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();
            Response response = okHttpClient.newCall(request).execute();
            final String responseBodyString = response.body().string();
            return mapper.readTree(responseBodyString);
        } catch (IOException e) {
            throw new ApplicationException(ErrorStatusCodes.SERVER_ERROR.getCode(), e.getMessage());
        }
    }

    /*public static JsonNode getToken() throws ApplicationException {
        try {

            MediaType mediaType = MediaType.parse("application/x-www-form-urlencoded");
            RequestBody body = RequestBody
                    .create("grant_type=password&username=meetrotion@gmail.com&password=Rurranhj2612", mediaType);
            Request request = new Request.Builder()
                    .url(LOGIN_BASE)
                    .method("POST", body)
                    .addHeader("Authorization", Credentials.basic("particle", "particle"))
                    .addHeader("Content-Type", "application/x-www-form-urlencoded")
                    .build();
            Response response = httpClient.newCall(request).execute();
            String jsonString = mapper.writeValueAsString(Objects.requireNonNull(Objects.requireNonNull(response.body()).string()));
            return mapper.readTree(jsonString);

        } catch (IOException e) {
            throw new ApplicationException(ErrorStatusCodes.SERVER_ERROR.getCode(), e.getMessage());
        }
    }*/

    private String prepareRequestBodyString() {
        return resourceBundleMap.entrySet()
                .stream()
                .filter(e -> e.getKey().equals("grant_type")
                        || e.getKey().equals("username")
                        || e.getKey().equals("password"))
                .map(Object::toString)
                .collect(Collectors.joining("&"));
    }

    private Map<String, String> getResourceBundleMap(ResourceBundle rb) {
        /* Get Resource bundle from particle properties file. */
        Map<String, String> map = new HashMap<>();
        Enumeration<String> keys = rb.getKeys();
        while (keys.hasMoreElements()) {
            String key = keys.nextElement();
            map.put(key, rb.getString(key));
        }
        return map;
    }
}
