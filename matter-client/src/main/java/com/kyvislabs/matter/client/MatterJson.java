package com.kyvislabs.matter.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.math.BigInteger;

/**
 * Shared Gson configuration for the Matter WebSocket API.
 *
 * <p>Matter identifiers (node IDs, fabric IDs, event numbers) are <em>unsigned</em> 64-bit
 * values, and both python-matter-server and matterjs-server put them on the wire as unquoted
 * JSON numbers. Gson's stock handling of those is unusable:
 *
 * <ul>
 *   <li>an {@code int} field silently truncates — matterjs-server's test node IDs start at
 *       {@code 0xFFFF_FFFE_0000_0000}, whose low 32 bits are zero, so the node arrives as 0</li>
 *   <li>a {@code long} field throws {@link com.google.gson.JsonSyntaxException}</li>
 *   <li>{@code JsonElement.getAsLong()} wraps silently</li>
 * </ul>
 *
 * <p>So {@code long} fields are read through {@link BigInteger} and kept as the two's-complement
 * bit pattern, which round-trips exactly. Treat them as unsigned: render with
 * {@link Long#toUnsignedString(long)} and put them back on the wire with
 * {@link #unsigned(long)}. Fields that are only ever displayed (fabric IDs) are declared as
 * {@link BigInteger} instead, which Gson handles correctly on its own.
 */
public final class MatterJson {

    /**
     * Reads any JSON number into a {@code long} as a two's-complement bit pattern, so values
     * above {@link Long#MAX_VALUE} survive instead of throwing or truncating.
     */
    private static final TypeAdapter<Long> UNSIGNED_LONG = new TypeAdapter<>() {
        @Override
        public void write(JsonWriter out, Long value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                out.value(unsigned(value));
            }
        }

        @Override
        public Long read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return 0L;
            }
            if (in.peek() == JsonToken.BOOLEAN) {
                return in.nextBoolean() ? 1L : 0L;
            }
            return parse(in.nextString());
        }

        private long parse(String raw) {
            try {
                return new BigInteger(raw).longValue();
            } catch (NumberFormatException e) {
                // Fractional or exponent notation; fall back to a lossy but non-fatal read.
                return (long) Double.parseDouble(raw);
            }
        }
    };

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(long.class, UNSIGNED_LONG)
            .registerTypeAdapter(Long.class, UNSIGNED_LONG)
            .create();

    private MatterJson() {
    }

    /** The shared, unsigned-aware Gson instance. Use this everywhere, not {@code new Gson()}. */
    public static Gson gson() {
        return GSON;
    }

    /**
     * Converts a node/fabric ID held as an unsigned {@code long} into the value to send on the
     * wire. Serializing the raw {@code long} would emit a negative number for IDs above
     * {@link Long#MAX_VALUE}, which the server rejects.
     */
    public static BigInteger unsigned(long id) {
        return new BigInteger(Long.toUnsignedString(id));
    }

    /**
     * Reads a node ID out of a JSON element without the silent wrapping of
     * {@link JsonElement#getAsLong()}.
     */
    public static long nodeId(JsonElement element) {
        return element.getAsBigInteger().longValue();
    }
}
