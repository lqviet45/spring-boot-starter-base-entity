package com.lqviet.baseentity.utils;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
/**
 * <pre>
 * Utility class for generating UUIDv7 (Time-Ordered UUIDs).
 * UUIDv7 provides better database performance compared to UUIDv4 because:
 * - Time-ordered: Sequential generation reduces index fragmentation
 * - Better locality: Related records are stored closer together
 * - Improved INSERT performance: Less index page splits
 * - Better for clustering: Natural sorting by creation time
 * - Works with ALL databases: PostgreSQL, MySQL, SQL Server, Oracle, etc.
 * Format: xxxxxxxx-xxxx-7xxx-yxxx-xxxxxxxxxxxx
 * - First 48 bits: Unix timestamp in milliseconds
 * - Next 12 bits: Random data (version + random)
 * - Next 2 bits: Variant (10)
 * - Last 62 bits: Random data
 * </pre>
 * Usage Examples:
 *  <pre>
 *  // Basic usage
 *  UUID id = UuidV7Utils.generate();
 *  entity.setId(id);
 *  // Generate with specific timestamp
 *  UUID historicalId = UuidV7Utils.generate(Instant.parse("2023-01-01T00:00:00Z"));
 *  // Extract timestamp from existing UUIDv7
 *  Instant timestamp = UuidV7Utils.extractInstant(id);
 *  // Time-based queries
 *  UUID startRange = UuidV7Utils.generate(startTime);
 *  UUID endRange = UuidV7Utils.generate(endTime);
 *  // Use startRange and endRange in database queries for efficient time-based filtering
 *  </pre>
 * @author Le Quoc Viet
 * @version 1.0.0
 */
public final class UuidV7Utils {
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int VERSION_7 = 7;
    private static final long VERSION_7_LONG = 7L;
    private static final int VARIANT_RFC4122 = 2; // Binary: 10
    private static final long VARIANT_RFC4122_LONG = 2L;

    // Thread-local counters for monotonic ordering within the same millisecond
    private static final ThreadLocal<MonotonicCounter> MONOTONIC_COUNTER =
            ThreadLocal.withInitial(MonotonicCounter::new);

    private UuidV7Utils() {
        // Utility class - prevent instantiation
    }

    // ============ GENERATION METHODS ============

    /**
     * Generate a new UUIDv7 with current timestamp.
     * This is the main method you'll use most of the time.
     *
     * @return A new time-ordered UUIDv7
     */
    public static UUID generate() {
        return generate(System.currentTimeMillis());
    }

    /**
     * Generate a UUIDv7 with a specific timestamp.
     * Useful for testing, backfilling data, or when you need UUIDs for specific times.
     *
     * @param timestamp Unix timestamp in milliseconds
     * @return A time-ordered UUIDv7 for the specified timestamp
     */
    public static UUID generate(long timestamp) {
        MonotonicCounter counter = MONOTONIC_COUNTER.get();
        return counter.generateUuid(timestamp);
    }

    /**
     * Generate a UUIDv7 with a specific Instant.
     *
     * @param instant The instant to use for timestamp
     * @return A time-ordered UUIDv7 for the specified instant
     */
    public static UUID generate(Instant instant) {
        return generate(instant.toEpochMilli());
    }

    /**
     * Generate a UUIDv7 with a specific LocalDateTime (assumes UTC).
     *
     * @param dateTime The LocalDateTime to use (treated as UTC)
     * @return A time-ordered UUIDv7 for the specified datetime
     */
    public static UUID generate(LocalDateTime dateTime) {
        return generate(dateTime.toInstant(ZoneOffset.UTC));
    }

    /**
     * Generate multiple UUIDv7s efficiently.
     * All UUIDs will have the same timestamp but different random parts.
     *
     * @param count Number of UUIDs to generate
     * @return Array of time-ordered UUIDv7s
     */
    public static UUID[] generateBatch(int count) {
        if (count <= 0) {
            return new UUID[0];
        }

        UUID[] uuids = new UUID[count];
        long timestamp = System.currentTimeMillis();

        for (int i = 0; i < count; i++) {
            uuids[i] = generate(timestamp);
        }

        return uuids;
    }

    // ============ EXTRACTION METHODS ============

    /**
     * Extract the timestamp from a UUIDv7.
     *
     * @param uuid The UUIDv7 to extract timestamp from
     * @return Unix timestamp in milliseconds
     * @throws IllegalArgumentException if the UUID is not a valid UUIDv7
     */
    public static long extractTimestamp(UUID uuid) {
        validateUuidV7(uuid);

        // Extract first 48 bits (timestamp)
        long mostSignificantBits = uuid.getMostSignificantBits();
        return mostSignificantBits >>> 16; // Shift right 16 bits to get timestamp
    }

    /**
     * Extract the timestamp as an Instant from a UUIDv7.
     *
     * @param uuid The UUIDv7 to extract timestamp from
     * @return Instant representing the UUID's timestamp
     */
    public static Instant extractInstant(UUID uuid) {
        return Instant.ofEpochMilli(extractTimestamp(uuid));
    }

    /**
     * Extract the timestamp as a LocalDateTime (UTC) from a UUIDv7.
     *
     * @param uuid The UUIDv7 to extract timestamp from
     * @return LocalDateTime representing the UUID's timestamp in UTC
     */
    public static LocalDateTime extractLocalDateTime(UUID uuid) {
        return LocalDateTime.ofInstant(extractInstant(uuid), ZoneOffset.UTC);
    }

    // ============ VALIDATION METHODS ============

    /**
     * Check if a UUID is a valid UUIDv7.
     *
     * @param uuid The UUID to check
     * @return true if the UUID is a valid UUIDv7, false otherwise
     */
    public static boolean isUuidV7(UUID uuid) {
        if (uuid == null) {
            return false;
        }

        try {
            validateUuidV7(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Validate that a UUID is a proper UUIDv7.
     *
     * @param uuid The UUID to validate
     * @throws IllegalArgumentException if the UUID is not a valid UUIDv7
     */
    private static void validateUuidV7(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("UUID cannot be null");
        }

        // Check version (should be 7)
        if (uuid.version() != VERSION_7) {
            throw new IllegalArgumentException("UUID is not version 7, got version: " + uuid.version());
        }

        // Check variant (should be 2 for RFC 4122)
        if (uuid.variant() != VARIANT_RFC4122) {
            throw new IllegalArgumentException("UUID variant is not RFC 4122, got variant: " + uuid.variant());
        }
    }

    // ============ COMPARISON AND UTILITY METHODS ============

    /**
     * Compare two UUIDv7s by their timestamps.
     * Returns negative if uuid1 is earlier, positive if later, 0 if same timestamp.
     *
     * @param uuid1 First UUIDv7
     * @param uuid2 Second UUIDv7
     * @return Comparison result (-1, 0, or 1)
     */
    public static int compareByTimestamp(UUID uuid1, UUID uuid2) {
        long timestamp1 = extractTimestamp(uuid1);
        long timestamp2 = extractTimestamp(uuid2);
        return Long.compare(timestamp1, timestamp2);
    }

    /**
     * Get the age of a UUIDv7 in milliseconds.
     *
     * @param uuid The UUIDv7 to check
     * @return Age in milliseconds (current time - UUID timestamp)
     */
    public static long getAgeInMillis(UUID uuid) {
        return System.currentTimeMillis() - extractTimestamp(uuid);
    }

    /**
     * Check if a UUIDv7 is older than the specified duration in milliseconds.
     *
     * @param uuid The UUIDv7 to check
     * @param maxAgeMillis Maximum age in milliseconds
     * @return true if the UUID is older than maxAgeMillis
     */
    public static boolean isOlderThan(UUID uuid, long maxAgeMillis) {
        return getAgeInMillis(uuid) > maxAgeMillis;
    }

    /**
     * Generate a UUIDv7 that's guaranteed to be after the given UUID.
     * Useful for pagination or ensuring ordering.
     *
     * @param afterUuid The UUID that the new UUID should come after
     * @return A new UUIDv7 that sorts after the given UUID
     */
    public static UUID generateAfter(UUID afterUuid) {
        long afterTimestamp = extractTimestamp(afterUuid);
        long currentTimestamp = System.currentTimeMillis();

        // Use the latter of the two timestamps
        long useTimestamp = Math.max(afterTimestamp, currentTimestamp);

        // If we're using the same timestamp as afterUuid, ensure we get a different random part
        UUID newUuid;
        do {
            newUuid = generate(useTimestamp);
        } while (newUuid.compareTo(afterUuid) <= 0);

        return newUuid;
    }

    // ============ TIME RANGE UTILITIES ============

    /**
     * Create a range of UUIDs for a specific time period.
     * Useful for database queries with time-based ranges.
     *
     * Example usage:
     * <pre>
     * TimeRange range = UuidV7Utils.createTimeRange(startTime, endTime);
     * List&lt;Entity&gt; results = repository.findByIdBetween(range.getStartUuid(), range.getEndUuid());
     * </pre>
     *
     * @param startTime Start of the time range
     * @param endTime End of the time range
     * @return TimeRange object with start and end UUIDs
     */
    public static TimeRange createTimeRange(Instant startTime, Instant endTime) {
        // Create UUIDs with minimum and maximum random parts for exact range boundaries
        UUID startUuid = createBoundaryUuid(startTime.toEpochMilli(), true);
        UUID endUuid = createBoundaryUuid(endTime.toEpochMilli(), false);

        return new TimeRange(startUuid, endUuid);
    }

    /**
     * Create a boundary UUID for range queries.
     *
     * @param timestamp The timestamp to use
     * @param isStart true for range start (minimum random), false for range end (maximum random)
     * @return A boundary UUID
     */
    private static UUID createBoundaryUuid(long timestamp, boolean isStart) {
        // Build the UUID manually for exact boundary control
        long timestampShifted = timestamp << 16;

        long mostSigBits;
        long leastSigBits;

        if (isStart) {
            // For start: use minimum random values
            mostSigBits = timestampShifted | (VERSION_7_LONG << 12); // Version in bits 12-15
            leastSigBits = (VARIANT_RFC4122_LONG << 62); // Variant in bits 62-63
        } else {
            // For end: use maximum random values
            mostSigBits = timestampShifted | (VERSION_7_LONG << 12) | 0x0FFF; // Max random in remaining bits
            leastSigBits = (VARIANT_RFC4122_LONG << 62) | 0x3FFFFFFFFFFFFFFFL; // Max random in remaining bits
        }

        return new UUID(mostSigBits, leastSigBits);
    }

    // ============ INNER CLASSES ============

    /**
     * Monotonic counter to ensure ordering within the same millisecond.
     */
    private static class MonotonicCounter {
        private long lastTimestamp = 0;
        private int sequenceCounter = 0;

        public UUID generateUuid(long timestamp) {
            // Handle clock going backwards or same millisecond
            if (timestamp < lastTimestamp) {
                throw new IllegalStateException("Clock moved backwards. Cannot generate UUIDv7.");
            }

            if (timestamp == lastTimestamp) {
                sequenceCounter++;
                if (sequenceCounter > 0xFFF) { // 12 bits max
                    // If we've exhausted the sequence space, wait for next millisecond
                    try {
                        Thread.sleep(1);
                        return generateUuid(System.currentTimeMillis());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted while waiting for next millisecond", e);
                    }
                }
            } else {
                lastTimestamp = timestamp;
                sequenceCounter = ThreadLocalRandom.current().nextInt(0x1000); // Start with random sequence
            }

            return buildUuid(timestamp, sequenceCounter);
        }

        private UUID buildUuid(long timestamp, int sequence) {
            // Build most significant bits: 48-bit timestamp + 4-bit version + 12-bit sequence
            long mostSigBits = (timestamp << 16) | (VERSION_7_LONG << 12) | sequence;

            // Build the least significant bits: 2-bit variant + 62-bit random
            byte[] randomBytes = new byte[8];
            SECURE_RANDOM.nextBytes(randomBytes);
            long leastSigBits = ByteBuffer.wrap(randomBytes).getLong();

            // Set variant bits (bits 62-63 = 10 in binary)
            leastSigBits = (leastSigBits & 0x3FFFFFFFFFFFFFFFL) | (VARIANT_RFC4122_LONG << 62);

            return new UUID(mostSigBits, leastSigBits);
        }
    }

    /**
         * Represents a time range with start and end UUIDs.
         * Useful for database range queries.
         */
        public record TimeRange(UUID startUuid, UUID endUuid) {

        /**
             * Check if a UUID falls within this time range.
             *
             * @param uuid The UUID to check
             * @return true if the UUID is within the range
             */
            public boolean contains(UUID uuid) {
                return uuid.compareTo(startUuid) >= 0 && uuid.compareTo(endUuid) <= 0;
            }

            @Override
            public String toString() {
                return String.format("TimeRange{start=%s, end=%s}", startUuid, endUuid);
            }
        }
}
