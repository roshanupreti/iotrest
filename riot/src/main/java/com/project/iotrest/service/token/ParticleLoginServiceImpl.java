package com.project.iotrest.service.token;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.iotrest.exceptions.ApplicationException;
import com.project.iotrest.service.particle.Keys;
import com.project.iotrest.service.particle.ParticleClient;
import lombok.extern.slf4j.Slf4j;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Caches a single, application-wide Particle Cloud access token (this demo authenticates to
 * Particle Cloud with one shared account, not per-{@code riot}-user credentials), refreshing
 * it transparently once it expires.
 *
 * @author roshan
 */
@Slf4j
@Service
public class ParticleLoginServiceImpl implements ParticleLoginService {

    /**
     * Used when Particle Cloud's token response doesn't include {@code expires_in} at all.
     */
    private static final long DEFAULT_TTL_SECONDS = 3600L;

    /**
     * Refresh a little before the token actually expires, so a request doesn't start with a
     * token that dies mid-flight.
     */
    private static final long SAFETY_BUFFER_SECONDS = 30L;

    private final ParticleClient particleClient;

    private volatile String cachedAccessToken;
    private volatile Instant cachedTokenExpiry = Instant.MIN;

    @Inject
    public ParticleLoginServiceImpl(ParticleClient particleClient) {
        this.particleClient = particleClient;
    }

    @Override
    public synchronized String getAccessToken() throws ApplicationException {
        if (cachedAccessToken == null || Instant.now().isAfter(cachedTokenExpiry)) {
            return refreshAccessToken();
        }
        return cachedAccessToken;
    }

    @Override
    public synchronized String refreshAccessToken() throws ApplicationException {
        JsonNode response = particleClient.executeRequest(particleClient.getApiBaseMap().get(Keys.PARTICLE_LOGIN_BASE.getKey()),
                Keys.HTTP_POST.getKey(),
                Keys.FORM_URLENCODED_MEDIA_TYPE.getKey(),
                prepareRequestBodyString(),
                Collections.singletonMap(Keys.CONTENT_TYPE.getKey(), Keys.FORM_URLENCODED_MEDIA_TYPE.getKey()));

        cachedAccessToken = response.get("access_token").asText();
        long expiresInSeconds = response.has("expires_in")
                ? response.get("expires_in").asLong()
                : DEFAULT_TTL_SECONDS;
        cachedTokenExpiry = Instant.now().plusSeconds(Math.max(expiresInSeconds - SAFETY_BUFFER_SECONDS, 0));
        log.info("Refreshed Particle Cloud access token, valid until {}", cachedTokenExpiry);

        return cachedAccessToken;
    }

    private String prepareRequestBodyString() {
        return particleClient.getCredsMap()
                .entrySet()
                .stream().map(Object::toString)
                .collect(Collectors.joining("&"));
    }
}
