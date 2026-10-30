package com.ht.robotvacuumapp.api.dto;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import java.util.Collections;
import java.util.List;
import static com.ht.robotvacuumapp.api.dto.JsonResponseValues.integer;

public final class AlertPageResponse {
    @SerializedName("content") private List<AlertResponse> content;
    @SerializedName("page") private JsonElement page;
    @SerializedName("size") private JsonElement size;
    @SerializedName("totalElements") private JsonElement totalElements;
    @SerializedName("totalPages") private JsonElement totalPages;
    @SerializedName("first") private JsonElement first;
    @SerializedName("last") private JsonElement last;

    public boolean isValidFor(int expectedPage, int expectedSize) {
        try {
            long pages = integer(totalPages);
            long count = integer(totalElements);
            if (content == null || integer(page) != expectedPage || integer(size) != expectedSize
                    || count < content.size() || pages < 0 || pages > Integer.MAX_VALUE
                    || (pages == 0 ? count != 0 : count == 0)
                    || expectedPage >= Math.max(1, pages) || content.size() > expectedSize
                    || booleanValue(first) != (expectedPage == 0)
                    || booleanValue(last) != (expectedPage + 1 == Math.max(1, pages))
                    || (content.isEmpty() && !booleanValue(last))) return false;
            for (AlertResponse alert : content) if (alert == null || !alert.isValid()) return false;
            return true;
        } catch (IllegalArgumentException | ArithmeticException error) {
            return false;
        }
    }

    private static boolean booleanValue(JsonElement value) {
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean()) {
            throw new IllegalArgumentException("Expected boolean");
        }
        return value.getAsBoolean();
    }
    public List<AlertResponse> getContent() { return Collections.unmodifiableList(content); }
    public boolean isLast() { return booleanValue(last); }
}
