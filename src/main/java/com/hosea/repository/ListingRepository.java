package com.hosea.repository;

import com.hosea.model.Listing;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ListingRepository {

    private final Pool db;

    public ListingRepository(Pool db) {
        this.db = db;
    }

    public Future<List<Listing>> findAll() {
        return db.query("SELECT * FROM listings ORDER BY id DESC").execute().map(ListingRepository::toList);
    }

    public Future<List<Listing>> findByHost(String hostId) {
        return db.preparedQuery("SELECT * FROM listings WHERE host_id = $1 ORDER BY id DESC")
                .execute(Tuple.of(hostId))
                .map(ListingRepository::toList);
    }

    /** Returns null when not found. */
    public Future<Listing> findById(int id) {
        return findById(db, id, false);
    }

    /** Same lookup inside a transaction; forUpdate locks the row until the transaction ends. */
    public Future<Listing> findById(SqlClient client, int id, boolean forUpdate) {
        String sql = "SELECT * FROM listings WHERE id = $1" + (forUpdate ? " FOR UPDATE" : "");
        return client.preparedQuery(sql).execute(Tuple.of(id)).map(ListingRepository::firstOrNull);
    }

    public Future<Listing> insert(String hostId, String title, String description, BigDecimal pricePerNight) {
        return db.preparedQuery("""
                        INSERT INTO listings (host_id, title, description, price_per_night)
                        VALUES ($1, $2, $3, $4) RETURNING *""")
                .execute(Tuple.of(hostId, title, description, pricePerNight))
                .map(ListingRepository::firstOrNull);
    }

    /** Only updates if the listing belongs to the host; returns null otherwise. */
    public Future<Listing> update(int id, String hostId, String title, String description, BigDecimal pricePerNight) {
        return db.preparedQuery("""
                        UPDATE listings SET title = $3, description = $4, price_per_night = $5
                        WHERE id = $1 AND host_id = $2 RETURNING *""")
                .execute(Tuple.of(id, hostId, title, description, pricePerNight))
                .map(ListingRepository::firstOrNull);
    }

    private static List<Listing> toList(RowSet<Row> rows) {
        List<Listing> list = new ArrayList<>();
        rows.forEach(row -> list.add(Listing.fromRow(row)));
        return list;
    }

    private static Listing firstOrNull(RowSet<Row> rows) {
        return rows.size() == 0 ? null : Listing.fromRow(rows.iterator().next());
    }
}
