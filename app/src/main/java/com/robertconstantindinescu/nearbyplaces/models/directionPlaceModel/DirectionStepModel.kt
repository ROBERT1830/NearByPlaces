package com.example.nearmekotlindemo.models.googlePlaceModel.directionPlaceModel

import com.example.nearmekotlindemo.models.googlePlaceModel.directionPlaceModel.*
import com.squareup.moshi.Json

/**
 * contiene los datos de cada una de las direcciones que tenmso que ejecutar par allegar al desitno fial
 * tiene la idstancia al sigueitne putno
 */
data class DirectionStepModel(
    @field:Json(name = "distance")

    var distance: DirectionDistanceModel? = null,

    @field:Json(name = "duration")

    var duration: DirectionDurationModel? = null,

    @field:Json(name = "end_location")

    var endLocation: EndLocationModel? = null,

    @field:Json(name = "html_instructions")

    var htmlInstructions: String? = null,

    @field:Json(name = "polyline")

    var polyline: DirectionPolylineModel? = null,

    @field:Json(name = "start_location")

    var startLocation: StartLocationModel? = null,

    @field:Json(name = "travel_mode")

    var travelMode: String? = null,

    @field:Json(name = "maneuver")

    var maneuver: String? = null
) {

}