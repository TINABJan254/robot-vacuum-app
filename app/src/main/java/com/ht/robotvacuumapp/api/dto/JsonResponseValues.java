package com.ht.robotvacuumapp.api.dto;

import com.google.gson.JsonElement;
import java.math.BigDecimal;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Strict response primitives and ISO timestamps, including nanosecond fractions on minSdk 24. */
final class JsonResponseValues {
    private static final Pattern ISO = Pattern.compile(
            "[0-9]{4}-[0-9]{2}-[0-9]{2}T[0-9]{2}:[0-9]{2}:[0-9]{2}(\\.[0-9]{1,9})?(Z|[+-][0-9]{2}:[0-9]{2})");

    private JsonResponseValues() { }

    static long integer(JsonElement value) {
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber()) {
            throw new IllegalArgumentException("Expected integer");
        }
        return value.getAsBigDecimal().longValueExact();
    }

    static String string(JsonElement value) {
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            throw new IllegalArgumentException("Expected string");
        }
        return value.getAsString();
    }

    static boolean booleanValue(JsonElement value) {
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException("Expected boolean");
        }
        return value.getAsBoolean();
    }

    /** Epoch seconds as a decimal retain fractions when comparing receipt timestamps. */
    static BigDecimal timestamp(String value) {
        Matcher matcher = ISO.matcher(value);
        if (!matcher.matches()) throw new IllegalArgumentException("Expected ISO timestamp with offset");
        String fraction = matcher.group(1);
        String seconds = fraction == null ? value : value.replace(fraction, "");
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US);
        format.setLenient(false);
        ParsePosition position = new ParsePosition(0);
        Date date = format.parse(seconds, position);
        if (date == null || position.getIndex() != seconds.length()) {
            throw new IllegalArgumentException("Invalid timestamp");
        }
        return BigDecimal.valueOf(date.getTime(), 3).add(fraction == null ? BigDecimal.ZERO : new BigDecimal(fraction));
    }
}
