package com.hosea.controller;

import com.hosea.model.Listing;
import com.hosea.service.ListingService;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.List;

public class ListingRoutes {

    private final Vertx vertx;
    private final ListingService service;

    public ListingRoutes(Vertx vertx, ListingService service) {
        this.vertx = vertx;
        this.service = service;
    }

    /** /api/listings — anyone can browse, no id needed. */
    public Router publicRoutes() {
        Router router = Router.router(vertx);
        router.get("/").handler(this::getAll);
        router.get("/:id").handler(this::getById);
        return router;
    }

    /** /api/host/listings — the host's own listings. */
    public Router hostRoutes() {
        Router router = Router.router(vertx);
        router.post("/").handler(this::create);
        router.get("/").handler(this::getMine);
        router.put("/:id").handler(this::update);
        return router;
    }

    private void getAll(RoutingContext ctx) {
        service.getAll()
                .onSuccess(listings -> ctx.json(toJson(listings)))
                .onFailure(ctx::fail);
    }

    private void getById(RoutingContext ctx) {
        service.getById(Integer.parseInt(ctx.pathParam("id")))
                .onSuccess(listing -> ctx.json(listing.toJson()))
                .onFailure(ctx::fail);
    }

    private void create(RoutingContext ctx) {
        service.create(ctx.get("userId"), ctx.body().asJsonObject())
                .onSuccess(listing -> {
                    ctx.response().setStatusCode(201);
                    ctx.json(listing.toJson());
                })
                .onFailure(ctx::fail);
    }

    private void getMine(RoutingContext ctx) {
        service.getByHost(ctx.get("userId"))
                .onSuccess(listings -> ctx.json(toJson(listings)))
                .onFailure(ctx::fail);
    }

    private void update(RoutingContext ctx) {
        service.update(Integer.parseInt(ctx.pathParam("id")), ctx.get("userId"), ctx.body().asJsonObject())
                .onSuccess(listing -> ctx.json(listing.toJson()))
                .onFailure(ctx::fail);
    }

    private static JsonArray toJson(List<Listing> listings) {
        JsonArray array = new JsonArray();
        listings.forEach(listing -> array.add(listing.toJson()));
        return array;
    }
}
