package com.hosea.repository;

import com.hosea.model.Booking;
import io.vertx.core.Future;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class BookingRepository {

    private final Pool db;

    public BookingRepository(Pool db) {
        this.db = db;
    }

    /** Runs the work in one transaction: commits if the Future succeeds, rolls back if it fails. */
    public <T> Future<T> inTransaction(Function<SqlConnection, Future<T>> work) {
        return db.withTransaction(work);
    }

    /** Two stays overlap when each one starts before the other ends. */
    public Future<Boolean> hasOverlap(SqlClient client, int listingId, LocalDate start, LocalDate end) {
        return client.preparedQuery("""
                        SELECT 1 FROM bookings
                        WHERE listing_id = $1 AND start_date < $3 AND end_date > $2""")
                .execute(Tuple.of(listingId, start, end))
                .map(rows -> rows.size() > 0);
    }

    public Future<Booking> insert(SqlClient client, int listingId, String guestId, LocalDate start, LocalDate end,
                                  BigDecimal totalPrice) {
        return client.preparedQuery("""
                        INSERT INTO bookings (listing_id, guest_id, start_date, end_date, total_price)
                        VALUES ($1, $2, $3, $4, $5) RETURNING *""")
                .execute(Tuple.of(listingId, guestId, start, end, totalPrice))
                .map(rows -> Booking.fromRow(rows.iterator().next()));
    }

    public Future<List<Booking>> findByGuest(String guestId) {
        return db.preparedQuery("SELECT * FROM bookings WHERE guest_id = $1 ORDER BY start_date DESC")
                .execute(Tuple.of(guestId))
                .map(BookingRepository::toList);
    }

    /** Bookings on every listing owned by the host. */
    public Future<List<Booking>> findByHost(String hostId) {
        return db.preparedQuery("""
                        SELECT b.* FROM bookings b
                        JOIN listings l ON l.id = b.listing_id
                        WHERE l.host_id = $1
                        ORDER BY b.start_date""")
                .execute(Tuple.of(hostId))
                .map(BookingRepository::toList);
    }

    private static List<Booking> toList(RowSet<Row> rows) {
        List<Booking> list = new ArrayList<>();
        rows.forEach(row -> list.add(Booking.fromRow(row)));
        return list;
    }
}
