package com.ht.robotvacuumapp.api.service;

import com.ht.robotvacuumapp.api.dto.PairRobotRequest;
import com.ht.robotvacuumapp.api.dto.RobotPageResponse;
import com.ht.robotvacuumapp.api.dto.RobotResponse;
import com.ht.robotvacuumapp.api.dto.UpdateRobotNameRequest;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface RobotApiService {
    @PUT("api/v1/robots/{id}")
    Call<RobotResponse> renameRobot(@Header("Authorization") String authorization,
                                   @Path("id") long id, @Body UpdateRobotNameRequest request);

    @POST("api/v1/robots/pair")
    Call<RobotResponse> pairRobot(@Header("Authorization") String authorization, @Body PairRobotRequest request);

    @GET("api/v1/robots")
    Call<RobotPageResponse> getRobots(@Header("Authorization") String authorization,
                                     @Query("page") int page, @Query("size") int size, @Query("sort") String sort);
}
