package com.hosea.model;

import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;

import java.math.BigDecimal;

public record Listing(int id, String hostId, String title, String description, BigDecimal pricePerNight) {

    public static Listing fromRow(Row row) {
        return new Listing(
                row.getInteger("id"),
                row.getString("host_id"),
                row.getString("title"),
                row.getString("description"),
                row.getBigDecimal("price_per_night"));
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put("id", id)
                .put("hostId", hostId)
                .put("title", title)
                .put("description", description)
                .put("pricePerNight", pricePerNight);
    }
}
