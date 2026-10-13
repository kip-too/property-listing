package com.hosea.model;

import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Row;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Booking(int id, int listingId, String guestId, LocalDate startDate, LocalDate endDate, BigDecimal totalPrice) {

    public static Booking fromRow(Row row) {
        return new Booking(
                row.getInteger("id"),
                row.getInteger("listing_id"),
                row.getString("guest_id"),
                row.getLocalDate("start_date"),
                row.getLocalDate("end_date"),
                row.getBigDecimal("total_price"));
    }

    public JsonObject toJson() {
        return new JsonObject()
                .put("id", id)
                .put("listingId", listingId)
                .put("guestId", guestId)
                .put("startDate", startDate.toString())
                .put("endDate", endDate.toString())
                .put("totalPrice", totalPrice);
    }
}
