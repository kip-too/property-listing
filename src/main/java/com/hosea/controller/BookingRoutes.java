package com.hosea.controller;

import com.hosea.model.Booking;
import com.hosea.service.BookingService;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

public class BookingRoutes {

    private final Vertx vertx;
    private final BookingService service;

    public BookingRoutes(Vertx vertx, BookingService service) {
        this.vertx = vertx;
        this.service = service;
    }

    /** /api/bookings — a guest's own bookings. */
    public Router guestRoutes() {
        Router router = Router.router(vertx);
        router.post("/").handler(this::create);
        router.get("/").handler(this::getMine);
        return router;
    }

    /** /api/host/bookings — bookings made on the host's listings. */
    public Router hostRoutes() {
        Router router = Router.router(vertx);
        router.get("/").handler(this::getForHost);
        return router;
    }

    private void create(RoutingContext ctx) {
        service.create(ctx.get("userId"), ctx.body().asJsonObject())
                .onSuccess(booking -> {
                    ctx.response().setStatusCode(201);
                    ctx.json(booking.toJson());
                })
                .onFailure(ctx::fail);
    }

    private void getMine(RoutingContext ctx) {
        service.getByGuest(ctx.get("userId"))
                .onSuccess(bookings -> ctx.json(toJson(bookings)))
                .onFailure(ctx::fail);
    }

    private void getForHost(RoutingContext ctx) {
        service.getByHost(ctx.get("userId"))
                .onSuccess(bookings -> ctx.json(toJson(bookings)))
                .onFailure(ctx::fail);
    }

    private static JsonArray toJson(List<Booking> bookings) {
        JsonArray array = new JsonArray();
        bookings.forEach(booking -> array.add(booking.toJson()));
        return array;
    }
}
