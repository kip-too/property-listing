package com.hosea;

import com.hosea.config.DatabaseConfig;
import com.hosea.controller.BookingRoutes;
import com.hosea.controller.ListingRoutes;
import com.hosea.repository.BookingRepository;
import com.hosea.repository.ListingRepository;
import com.hosea.service.BookingService;
import com.hosea.service.ListingService;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.sqlclient.Pool;

import java.time.format.DateTimeParseException;

public class MainVertical extends AbstractVerticle {

    public static void main(String[] args) {
        Vertx.vertx().deployVerticle(new MainVertical()).onFailure(Throwable::printStackTrace);
    }

    @Override
    public void start(Promise<Void> startPromise) {
        // 1. Database pool
        Pool db = DatabaseConfig.createPool(vertx);

        // 2. layers : repository -> service -> routes
        ListingRepository listingRepo = new ListingRepository(db);
        BookingRepository bookingRepo = new BookingRepository(db);
        ListingService listingService = new ListingService(listingRepo);
        BookingService bookingService = new BookingService(bookingRepo, listingRepo);
        ListingRoutes listingRoutes = new ListingRoutes(vertx, listingService);
        BookingRoutes bookingRoutes = new BookingRoutes(vertx, bookingService);

        // 3. Router and global middleware
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create().setBodyLimit(50000)); // reads JSON bodies, rejects huge ones
        router.route().failureHandler(this::handleError);                 // every error ends up here

        // 4. Routes
        // Public: browse listings without an id
        router.route("/api/listings*").subRouter(listingRoutes.publicRoutes());

        // Host: requires X-Host-Id
        router.route("/api/host/*").handler(ctx -> mockAuth(ctx, "X-Host-Id"));
        router.route("/api/host/listings*").subRouter(listingRoutes.hostRoutes());
        router.route("/api/host/bookings*").subRouter(bookingRoutes.hostRoutes());

        // Guest: requires X-Guest-Id
        router.route("/api/bookings*").handler(ctx -> mockAuth(ctx, "X-Guest-Id"));
        router.route("/api/bookings*").subRouter(bookingRoutes.guestRoutes());

        // 5. Start the server
        vertx.createHttpServer()
                .requestHandler(router)
                .listen(8080)
                .onSuccess(server -> {
                    System.out.println("Server started on port 8080");
                    startPromise.complete();
                })
                .onFailure(startPromise::fail);
    }

    // Temporary stand-in for Firebase: trust the id in the header.
    // Later this verifies a Firebase token
    private void mockAuth(RoutingContext ctx, String header) {
        String userId = ctx.request().getHeader(header);
        if (userId == null || userId.isBlank()) {
            ctx.fail(new ApiException(401, "Missing " + header + " header"));
            return;
        }
        ctx.put("userId", userId);
        ctx.next();
    }

    // Turns any failure into a JSON response with a  status code.
    private void handleError(RoutingContext ctx) {
        Throwable err = ctx.failure();
        int status;
        String message;

        if (err instanceof ApiException e) {
            status = e.status;
            message = e.getMessage();
        } else if (err instanceof DecodeException || err instanceof DateTimeParseException
                || err instanceof NumberFormatException || err instanceof ClassCastException) {
            status = 400; // malformed JSON, bad date, non-numeric id, wrong field type
            message = "Invalid request data";
        } else if (err != null) {
            status = 500;
            message = "Internal server error";
            err.printStackTrace();
        } else {
            status = ctx.statusCode(); // e.g. 404 / 405 raised by the router itself
            message = "Request failed";
        }

        ctx.response()
                .setStatusCode(status)
                .putHeader("Content-Type", "application/json")
                .end(new JsonObject().put("error", message).encode());
    }
}
