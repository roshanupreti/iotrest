package com.project.iotrest.service.token;

import com.project.iotrest.exceptions.ApplicationException;
import org.jvnet.hk2.annotations.Contract;

/**
 * Obtains and caches a Particle Cloud access token for this application's own (single,
 * shared) Particle account. Kept as a separate contract from its implementation so tests can
 * substitute a fake without making real Particle Cloud calls.
 *
 * @author roshan
 */
@Contract
public interface ParticleLoginService {

    /**
     * Returns a currently-valid Particle Cloud access token, transparently refreshing it first
     * if none is cached yet or the cached one has expired.
     *
     * @return a valid Particle Cloud access token
     * @throws ApplicationException if a refresh was needed and the Particle Cloud login failed
     */
    String getAccessToken() throws ApplicationException;

    /**
     * Forces a fresh Particle Cloud login, caching and returning the new access token
     * regardless of whether a still-valid one was already cached.
     *
     * @return the newly obtained Particle Cloud access token
     * @throws ApplicationException if the Particle Cloud login failed
     */
    String refreshAccessToken() throws ApplicationException;
}
