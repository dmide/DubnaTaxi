package ru.dmide.dubnataxi.web;

import retrofit2.Call;
import retrofit2.http.GET;

public interface WebAPI {
  @GET("/")
  Call<ServicesListResponse> getServices();
}