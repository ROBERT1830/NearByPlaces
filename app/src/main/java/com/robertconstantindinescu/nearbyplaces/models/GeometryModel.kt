package com.robertconstantindinescu.nearbyplaces.models.googlePlaceModel


import com.robertconstantindinescu.nearbyplaces.models.googlePlaceModel.LocationModel
import com.squareup.moshi.Json

data class GeometryModel(
    @field:Json(name = "location")
    val location: LocationModel?
)