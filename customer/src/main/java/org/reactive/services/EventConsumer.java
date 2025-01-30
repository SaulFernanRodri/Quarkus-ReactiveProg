package org.reactive.services;

import io.vertx.core.json.JsonObject;
import io.vertx.mutiny.core.eventbus.EventBus;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;

@ApplicationScoped
public class EventConsumer {

    @Inject
    EventBus eventBus;

    private static final Logger LOG = Logger.getLogger(EventConsumer.class);

    @PostConstruct
    void setUp() {
        LOG.info("Setting up Event Bus consumers");
        eventBus.consumer("product.created", this::handleProductCreated);
        eventBus.consumer("product.updated", this::handleProductUpdated);
    }

    private void handleProductCreated(io.vertx.mutiny.core.eventbus.Message<JsonObject> message) {
        try {
            JsonObject productJson = message.body();
            Long createdProductId = productJson.getLong("id");
            if (createdProductId != null) {
                LOG.info("Received notification of product created: " + createdProductId);
            } else {
                LOG.warn("Received product.created event with missing 'id'");
            }
        } catch (Exception e) {
            LOG.error("Failed to process product.created event", e);
        }
    }

    private void handleProductUpdated(io.vertx.mutiny.core.eventbus.Message<JsonObject> message) {
        try {
            JsonObject productJson = message.body();
            Long updatedProductId = productJson.getLong("id");
            if (updatedProductId != null) {
                LOG.info("Received notification of product update: " + updatedProductId);
            } else {
                LOG.warn("Received product.updated event with missing 'id'");
            }
        } catch (Exception e) {
            LOG.error("Failed to process product.updated event", e);
        }
    }
}
