package com.hosea.service;

import com.hosea.ApiException;
import com.hosea.model.Booking;
import com.hosea.repository.BookingRepository;
import com.hosea.repository.ListingRepository;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class BookingService {

    private final BookingRepository bookingRepo;
    private final ListingRepository listingRepo;

    public BookingService(BookingRepository bookingRepo, ListingRepository listingRepo) {
        this.bookingRepo = bookingRepo;
        this.listingRepo = listingRepo;
    }

    public Future<Booking> create(String guestId, JsonObject body) {
        if (body == null) throw new ApiException(400, "Request body is required");
        Integer listingId = body.getInteger("listingId");
        if (listingId == null) throw new ApiException(400, "listingId is required");
        if (body.getString("startDate") == null || body.getString("endDate") == null) {
            throw new ApiException(400, "startDate and endDate are required (yyyy-MM-dd)");
        }

        LocalDate start = LocalDate.parse(body.getString("startDate"));
        LocalDate end = LocalDate.parse(body.getString("endDate"));
        if (!end.isAfter(start)) throw new ApiException(400, "endDate must be after startDate");
        if (start.isBefore(LocalDate.now())) throw new ApiException(400, "startDate cannot be in the past");

        // The listing row is locked (FOR UPDATE) so two guests booking the same dates
        // at the same moment are handled one after the other, never both accepted.
        return bookingRepo.inTransaction(tx -> listingRepo.findById(tx, listingId, true)
                .compose(listing -> {
                    if (listing == null) throw new ApiException(404, "Listing not found");
                    if (listing.hostId().equals(guestId)) throw new ApiException(400, "You cannot book your own listing");
                    return bookingRepo.hasOverlap(tx, listingId, start, end).compose(taken -> {
                        if (taken) throw new ApiException(409, "Listing is already booked for these dates");
                        long nights = ChronoUnit.DAYS.between(start, end);
                        BigDecimal total = listing.pricePerNight().multiply(BigDecimal.valueOf(nights));
                        return bookingRepo.insert(tx, listingId, guestId, start, end, total);
                    });
                }));
    }

    public Future<List<Booking>> getByGuest(String guestId) {
        return bookingRepo.findByGuest(guestId);
    }

    public Future<List<Booking>> getByHost(String hostId) {


        return bookingRepo.findByHost(hostId);
    }
}
