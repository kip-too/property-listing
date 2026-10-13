# Property Listing & Booking API

Java 21, Vert.x 4.5 and PostgreSQL.

## Run

1. Create the tables in `propertydb` using `schema.sql`.
2. Run `mvn compile exec:java`. The server listens on `http://localhost:8080`.

The database connection is set in `config/DatabaseConfig.java`.

## Identity (temporary)

Firebase auth comes later. Until then the caller's id is sent in a header:

- Host routes: `X-Host-Id`
- Guest booking routes: `X-Guest-Id`

## Endpoints

| Method | Path | Who | What it does |
| --- | --- | --- | --- |
| GET | `/api/listings` | anyone | Browse all listings |
| GET | `/api/listings/{id}` | anyone | View one listing |
| POST | `/api/host/listings` | host | Create a listing: `title`, `description`, `pricePerNight` |
| GET | `/api/host/listings` | host | View my listings |
| PUT | `/api/host/listings/{id}` | host | Update my listing |
| GET | `/api/host/bookings` | host | View bookings made on my listings |
| POST | `/api/bookings` | guest | Book: `listingId`, `startDate`, `endDate` (yyyy-MM-dd) |
| GET | `/api/bookings` | guest | View my bookings |

See `requests.http` for example calls.

## Booking rules

- `endDate` must be after `startDate`, and `startDate` cannot be in the past.
- A host cannot book their own listing.
- Dates cannot overlap an existing booking on the same listing. 
- The server calculates the total price as nights × `pricePerNight`.
- The listing row is locked while a booking is created, so two simultaneous requests for the same dates cannot both succeed.

## Code layout

```text
MainVerticle   wiring, routes, mock auth, error handling
controller/    reads the HTTP request and writes the JSON response
service/       validation and business rules
repository/    SQL queries
model/         Listing and Booking records
```
