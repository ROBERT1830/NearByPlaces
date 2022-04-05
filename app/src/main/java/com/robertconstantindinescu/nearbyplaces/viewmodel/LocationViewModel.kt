package com.robertconstantindinescu.nearbyplaces.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.AuthCredential
import com.robertconstantindinescu.nearbyplaces.models.googlePlaceModel.GooglePlaceModel
import com.robertconstantindinescu.nearbyplaces.repo.AppRepo
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext

class LocationViewModel : ViewModel() {
    private val repo = AppRepo()

    fun getNearByPlace(url: String) = repo.getPlaces(url)

    fun removePlace(userSavedLocationId: ArrayList<String>) = repo.removePlace(userSavedLocationId)

    fun addUserPlace(googlePlaceModel: GooglePlaceModel, userSavedLocationId: ArrayList<String>) =
        repo.addUserPlace(googlePlaceModel, userSavedLocationId)

    suspend fun getUserLocationId(): ArrayList<String> {

        return withContext(viewModelScope.coroutineContext) {
            //Creates a coroutine and returns its future result as an implementation of Deferred.
            //Deferred value is a non-blocking cancellable future — it is a Job with a result.
            val data = async { repo.getUserLocationId() }
            //devolvemos data con el resultado
            data
        }.await()
    }


    fun getDirection(url: String) = repo.getDirection(url)

    fun updateName(name: String) = repo.updateName(name)

    fun updateImage(image: Uri) = repo.updateImage(image)

    fun confirmEmail(authCredential: AuthCredential) = repo.confirmEmail(authCredential)

    fun updateEmail(email: String) = repo.updateEmail(email)

    fun updatePassword(password: String) = repo.updatePassword(password)
}