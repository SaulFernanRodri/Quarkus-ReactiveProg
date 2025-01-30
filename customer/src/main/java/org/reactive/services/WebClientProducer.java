package org.reactive.services;

import io.vertx.mutiny.core.Vertx;
import io.vertx.mutiny.ext.web.client.WebClient;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;

@ApplicationScoped
public class WebClientProducer {

    @Singleton
    @Produces
    public WebClient createWebClient(Vertx vertx) {
        return WebClient.create(vertx);
    }
}