package com.ht.robotvacuumapp.api.dto;

import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;
import java.util.Collections;
import java.util.List;
import static com.ht.robotvacuumapp.api.dto.JsonResponseValues.*;

public final class SchedulePageResponse {
    @SerializedName("content") private List<ScheduleResponse> content;
    @SerializedName("page") private JsonElement page;
    @SerializedName("size") private JsonElement size;
    @SerializedName("totalElements") private JsonElement totalElements;
    @SerializedName("totalPages") private JsonElement totalPages;
    @SerializedName("first") private JsonElement first;
    @SerializedName("last") private JsonElement last;

    public boolean isValidFor(long robotId, int expectedPage, int expectedSize) {
        try {
            long count = integer(totalElements), pages = integer(totalPages);
            if (content == null || integer(page) != expectedPage || integer(size) != expectedSize
                    || count < content.size() || pages < 0 || pages > Integer.MAX_VALUE
                    || (pages == 0 ? count != 0 : count == 0) || expectedPage >= Math.max(1, pages)
                    || content.size() > expectedSize || booleanValue(first) != (expectedPage == 0)
                    || isLast() != (expectedPage + 1 == Math.max(1, pages)) || (content.isEmpty() && !isLast())) return false;
            for (ScheduleResponse schedule : content) if (schedule == null || !schedule.isValidFor(robotId)) return false;
            return true;
        } catch (IllegalArgumentException | ArithmeticException error) { return false; }
    }
    public List<ScheduleResponse> getContent() { return Collections.unmodifiableList(content); }
    public boolean isLast() { return booleanValue(last); }
}
