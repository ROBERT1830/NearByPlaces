package com.robertconstantindinescu.nearbyplaces.network

import com.example.nearmekotlindemo.models.googlePlaceModel.directionPlaceModel.DirectionResponseModel
import com.robertconstantindinescu.nearbyplaces.models.googlePlaceModel.GoogleResponseModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Url

interface RetrofitApi {
    @GET  //con el metodo url
    suspend fun getNearByPlaces(@Url url: String): Response<GoogleResponseModel>

    @GET
    suspend fun getDirection(@Url url: String): Response<DirectionResponseModel>
}