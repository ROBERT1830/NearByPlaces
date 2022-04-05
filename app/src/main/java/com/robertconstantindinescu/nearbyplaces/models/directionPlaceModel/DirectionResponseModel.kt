package com.example.nearmekotlindemo.models.googlePlaceModel.directionPlaceModel

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

data class DirectionResponseModel(
    @field:Json(name = "routes")
    //have a list of that object, only one object inside with multiple data.
    var directionRouteModels: List<DirectionRouteModel>? = null,

    @field:Json(name = "error_message")
    val error: String? = null
)