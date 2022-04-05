package com.robertconstantindinescu.nearbyplaces.utils


import com.robertconstantindinescu.nearbyplaces.PlaceModel
import com.robertconstantindinescu.nearbyplaces.R

class AppConstant {
    companion object{
        @JvmStatic
        val STORAGE_REQUEST_CODE = 1000
        @JvmStatic
        val PROFILE_PATH="/Profile/image_profile.jpg"



        @JvmStatic
        val placesName =
            listOf<PlaceModel>(
                PlaceModel(1, R.drawable.ic_restaurant, "Restaurant", "restaurant"),
                PlaceModel(2, R.drawable.ic_atm, "ATM", "atm"),
                PlaceModel(3, R.drawable.ic_gas_station, "Gas", "gas_station"),
                PlaceModel(4, R.drawable.ic_shopping_cart, "Groceries", "supermarket"),
                PlaceModel(5, R.drawable.ic_hotel, "Hotels", "hotel"),
                PlaceModel(6, R.drawable.ic_pharmacy, "Pharmacies", "pharmacy"),
                PlaceModel(7, R.drawable.ic_hospital, "Hospitals & Clinics", "hospital"),
                PlaceModel(8, R.drawable.ic_car_wash, "Car Wash", "car_wash"),
                PlaceModel(9, R.drawable.ic_saloon, "Beauty Salons", "beauty_salon")
            )
    }
}