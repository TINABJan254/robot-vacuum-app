package com.ht.robotvacuumapp.api.service;

import com.ht.robotvacuumapp.api.dto.*;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface ScheduleApiService {
    @GET("api/v1/schedules")
    Call<SchedulePageResponse> getSchedules(@Header("Authorization") String authorization,
            @Query("robotId") long robotId, @Query("page") int page, @Query("size") int size, @Query("sort") List<String> sort);
    @POST("api/v1/schedules")
    Call<ScheduleResponse> createSchedule(@Header("Authorization") String authorization, @Body CreateScheduleRequest request);
    @PATCH("api/v1/schedules/{id}")
    Call<ScheduleResponse> setEnabled(@Header("Authorization") String authorization, @Path("id") long id,
                                      @Body UpdateScheduleEnabledRequest request);
    @DELETE("api/v1/schedules/{id}")
    Call<Void> deleteSchedule(@Header("Authorization") String authorization, @Path("id") long id);
}
