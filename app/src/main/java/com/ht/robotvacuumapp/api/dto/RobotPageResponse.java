package com.ht.robotvacuumapp.api.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public final class RobotPageResponse {
    @SerializedName("content") private List<RobotResponse> content;
    @SerializedName("page") private Integer page;
    @SerializedName("totalPages") private Integer totalPages;
    @SerializedName("last") private Boolean last;

    public List<RobotResponse> getContent() { return content; }
    public Integer getPage() { return page; }
    public Integer getTotalPages() { return totalPages; }
    public Boolean getLast() { return last; }
}
