package com.hosea.service;

import com.hosea.ApiException;
import com.hosea.model.Listing;
import com.hosea.repository.ListingRepository;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.math.BigDecimal;
import java.util.List;

public class ListingService {

    private final ListingRepository repo;

    public ListingService(ListingRepository repo) {
        this.repo = repo;
    }

    public Future<List<Listing>> getAll() {
        return repo.findAll();
    }

    public Future<Listing> getById(int id) {
        return repo.findById(id).map(listing -> {
            if (listing == null) throw new ApiException(404, "Listing not found");
            return listing;
        });
    }

    public Future<List<Listing>> getByHost(String hostId) {
        return repo.findByHost(hostId);
    }

    public Future<Listing> create(String hostId, JsonObject body) {
        validate(body);
        return repo.insert(hostId, body.getString("title"), body.getString("description"), price(body));
    }

    public Future<Listing> update(int id, String hostId, JsonObject body) {
        validate(body);
        return repo.update(id, hostId, body.getString("title"), body.getString("description"), price(body))
                .map(listing -> {
                    // null means the listing doesn't exist or belongs to another host
                    if (listing == null) throw new ApiException(404, "Listing not found");
                    return listing;
                });
    }

    private static void validate(JsonObject body) {
        if (body == null) throw new ApiException(400, "Request body is required");
        String title = body.getString("title");
        if (title == null || title.isBlank()) throw new ApiException(400, "title is required");
        if (body.getValue("pricePerNight") == null) throw new ApiException(400, "pricePerNight is required");
        if (price(body).signum() <= 0) throw new ApiException(400, "pricePerNight must be greater than 0");
    }

    private static BigDecimal price(JsonObject body) {
        return new BigDecimal(body.getNumber("pricePerNight").toString());
    }
}
