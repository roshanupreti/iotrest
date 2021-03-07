package com.project.iotrest.service.token;

import com.fasterxml.jackson.databind.JsonNode;
import com.project.iotrest.exceptions.ApplicationException;
import com.project.iotrest.pojos.enums.Keys;
import com.project.iotrest.service.particle.ParticleClient;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import java.net.MalformedURLException;
import java.util.Collections;
import java.util.stream.Collectors;

@Dependent
public class ParticleLoginService extends ParticleClient {

    @Inject
    public ParticleLoginService() throws MalformedURLException {
        super();
    }

    public JsonNode getParticleToken() throws ApplicationException {
        return executeRequest(getApiBaseMap().get(Keys.PARTICLE_LOGIN_BASE.getKey()),
                Keys.HTTP_POST.getKey(),
                Keys.PARTICLE_LOGIN_MEDIA_TYPE.getKey(),
                prepareRequestBodyString(),
                Collections.singletonMap(Keys.CONTENT_TYPE.getKey(), Keys.PARTICLE_LOGIN_MEDIA_TYPE.getKey()));
    }

    private String prepareRequestBodyString() {
        return getCredsMap()
                .entrySet()
                .stream().map(Object::toString)
                .collect(Collectors.joining("&"));
    }
}
