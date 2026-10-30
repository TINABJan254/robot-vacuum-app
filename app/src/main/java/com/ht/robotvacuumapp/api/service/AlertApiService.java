package com.ht.robotvacuumapp.api.service;

import com.ht.robotvacuumapp.api.dto.AlertPageResponse;
import com.ht.robotvacuumapp.api.dto.AlertResponse;
import com.ht.robotvacuumapp.api.dto.UpdateAlertReadAtRequest;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.*;

public interface AlertApiService {
    @GET("api/v1/alerts")
    Call<AlertPageResponse> getAlerts(@Header("Authorization") String authorization,
                                    @Query("page") int page, @Query("size") int size, @Query("sort") List<String> sort);
    @GET("api/v1/alerts/{id}")
    Call<AlertResponse> getAlert(@Header("Authorization") String authorization, @Path("id") long id);
    @PATCH("api/v1/alerts/{id}")
    Call<AlertResponse> markRead(@Header("Authorization") String authorization, @Path("id") long id,
                               @Body UpdateAlertReadAtRequest request);
}
