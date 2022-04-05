package com.robertconstantindinescu.nearbyplaces.interfaces

import com.robertconstantindinescu.nearbyplaces.models.googlePlaceModel.GooglePlaceModel

interface NearLocationInterface {
    fun onSaveClick(googlePlaceModel: GooglePlaceModel)

    fun onDirectionClick(googlePlaceModel: GooglePlaceModel)

}