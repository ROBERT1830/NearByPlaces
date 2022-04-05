package com.robertconstantindinescu.nearbyplaces.repo

import android.net.Uri
import android.util.Log
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.robertconstantindinescu.nearbyplaces.SavedPlaceModel
import com.robertconstantindinescu.nearbyplaces.UserModel
import com.robertconstantindinescu.nearbyplaces.models.googlePlaceModel.GooglePlaceModel
import com.robertconstantindinescu.nearbyplaces.network.RetrofitClient
import com.robertconstantindinescu.nearbyplaces.utils.AppConstant
import com.robertconstantindinescu.nearbyplaces.utils.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await

class AppRepo {


    fun login(
        email: String,
        password: String
    ): Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))

        val auth = Firebase.auth
        val data = auth.signInWithEmailAndPassword(email, password).await()
        data?.let {
            if (auth.currentUser?.isEmailVerified!!) {
                emit(State.success("Login Successfully"))
            } else {
                auth.currentUser?.sendEmailVerification()?.await()
                emit(State.failed("Verify email first"))
            }
        }
    }.catch {
        emit(State.failed(it.message!!))
    }.flowOn(
        Dispatchers.IO
    )

    fun signUp(
        email: String,
        password: String,
        username: String,
        image: Uri
    ): Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))
        val auth = Firebase.auth
        val data = auth.createUserWithEmailAndPassword(email, password).await()
        data.user?.let {
            val path = uploadImage(it.uid, image).toString()
            val userModel = UserModel(
                email, username, path
            )

            createUser(userModel, auth)

            emit(State.success("Email verification sent"))

        }

    }.catch {
        emit(State.failed(it.message!!))
    }
        .flowOn(Dispatchers.IO)



    private suspend fun uploadImage(uid: String, image: Uri): Uri {
        val firebaseStorage = Firebase.storage
        val storageReference = firebaseStorage.reference
        val task = storageReference.child(uid + AppConstant.PROFILE_PATH)
            .putFile(image).await()

        return task.storage.downloadUrl.await()

    }


    private suspend fun  createUser(userModel: UserModel, auth: FirebaseAuth) {
        val firebase = Firebase.database.getReference("Users")

        firebase.child(auth.uid!!).setValue(userModel)
            .await()


        val profileChangeRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(userModel.username)
            .setPhotoUri(Uri.parse(userModel.image))
            .build()
        auth.currentUser?.apply {
            updateProfile(profileChangeRequest).await()
            sendEmailVerification().await()
        }
    }

    fun forgetPassword(email: String): Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))
        val auth = Firebase.auth
        auth.sendPasswordResetEmail(email).await()
        emit(State.success("Password reset email sent."))
    }.catch {
        emit(State.failed(it.message!!))
    }.flowOn(Dispatchers.IO)


    fun getPlaces(url: String): Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))
        val response = RetrofitClient.retrofitApi.getNearByPlaces(url = url)

        Log.d("TAG", "getPlaces:  $response ")
        if (response.body()?.googlePlaceModelList?.size!! > 0) {
            Log.d(
                "TAG",
                "getPlaces:  Success called ${response.body()?.googlePlaceModelList?.size}"
            )

            emit(State.success(response.body()!!))
        } else {
            Log.d("TAG", "getPlaces:  failed called")
            emit(State.failed(response.body()!!.error!!))
        }


    }.catch {
        emit(State.failed(it.message.toString()))
    }.flowOn(Dispatchers.IO)


    /***/
    suspend fun getUserLocationId(): ArrayList<String> {
        val userPlaces = ArrayList<String>()
        val auth = Firebase.auth
        val database =
            Firebase.database.getReference("Users").child(auth.uid!!).child("Saved Locations")

        val data = database.get().await()
        if (data.exists()) {
            for (ds in data.children) {
                val placeId = ds.getValue(String::class.java)
                userPlaces.add(placeId!!)
            }
        }

        return userPlaces
    }

    fun addUserPlace(googlePlaceModel: GooglePlaceModel, userSavedLocaitonId: ArrayList<String>) =
        flow<State<Any>> {
            emit(State.loading(true))
            //coges la intancia del usuario autentificado.
            val auth = Firebase.auth
            val userDatabase =
                Firebase.database.getReference("Users").child(auth.uid!!).child("Saved Locations")
            val database =
                Firebase.database.getReference("Places").child(googlePlaceModel.placeId!!)
                    .get() //get the server values for this query
                    .await()
            if (!database.exists()) {
                val savedPlaceModel = SavedPlaceModel(
                    googlePlaceModel.name!!, googlePlaceModel.vicinity!!,
                    googlePlaceModel.placeId, googlePlaceModel.userRatingsTotal!!,
                    googlePlaceModel.rating!!, googlePlaceModel.geometry?.location?.lat!!,
                    googlePlaceModel.geometry.location.lng!!
                )

                addPlace(savedPlaceModel)
            }

            userSavedLocaitonId.add(googlePlaceModel.placeId)
            userDatabase.setValue(userSavedLocaitonId).await()
            emit(State.success(googlePlaceModel))


        }.flowOn(Dispatchers.IO).catch {
            emit(State.failed(it.message!!))
        }

    private suspend fun addPlace(savedPlaceModel: SavedPlaceModel) {
        val database = Firebase.database.getReference("Places")
        database.child(savedPlaceModel.placeId).setValue(savedPlaceModel).await()
    }

    fun removePlace(userSavedLocationId: ArrayList<String>) = flow<State<Any>> {
        emit(State.loading(true))
        val auth = Firebase.auth
        val database =
            Firebase.database.getReference("Users").child(auth.uid!!).child("Saved Locations")

        database.setValue(userSavedLocationId).await()
        emit(State.success("Remove Successfully"))
    }.catch {
        emit(State.failed(it.message!!))
    }.flowOn(Dispatchers.IO)


    fun getDirection(url: String): Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))

        val response = RetrofitClient.retrofitApi.getDirection(url)

        if (response.body()?.directionRouteModels?.size!! > 0) {
            emit(State.success(response.body()!!))
        } else {
            emit(State.failed(response.body()?.error!!))
        }
    }.flowOn(Dispatchers.IO)
        .catch {
            if (it.message.isNullOrEmpty()) {
                emit(State.failed("No route found"))
            } else {
                emit(State.failed(it.message.toString()))
            }

        }


    fun updateName(name: String): Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))

        val auth = Firebase.auth
        //Gets a DatabaseReference for the provided path
        val database = Firebase.database.getReference("Users").child(auth.uid!!)

        val profileChangeRequest = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        auth.currentUser?.updateProfile(profileChangeRequest)?.await()

        val map: MutableMap<String, Any> = HashMap()


        map["username"] = name
        database.updateChildren(map)

        emit(State.success("Username updated"))

    }.flowOn(Dispatchers.IO)
        .catch {
            emit(State.failed(it.message!!.toString()))
        }

    /**
     * método para hacer el update de la imagen.
     */
    fun updateImage(image: Uri): Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))

        val auth = Firebase.auth
        val path = uploadImage(auth.uid!!, image).toString()
        val database = Firebase.database.getReference("Users").child(auth.uid!!)
        val profileChangeRequest = UserProfileChangeRequest.Builder()
            .setPhotoUri(Uri.parse(path))
            .build() //build the request.

        auth.currentUser?.updateProfile(profileChangeRequest)?.await()

        val map: MutableMap<String, Any> = HashMap()

        map["image"] = path
        //update the tag image with the new value.
        database.updateChildren(map)
        emit(State.success("Image updated"))

    }.flowOn(Dispatchers.IO)
        .catch {
            emit(State.failed(it.message!!.toString()))
        }


    fun confirmEmail(authCredential: AuthCredential): Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))

        val auth = Firebase.auth
        auth.currentUser?.reauthenticate(authCredential)?.await()
        emit(State.success("User authenticate"))
    }.flowOn(Dispatchers.IO)
        .catch {
            emit(State.failed(it.message!!.toString()))
        }

    fun updateEmail(email: String): Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))

        //get the current authenticate user.
        val auth = Firebase.auth
        val database = Firebase.database.getReference("Users").child(auth.uid!!)
        //update the emial with the new one.
        auth.currentUser?.updateEmail(email)?.await()
        val map: MutableMap<String, Any> = HashMap()
        //ídem
        map["email"] = email
        //set the value in the database.
        database.updateChildren(map).await()

        emit(State.success("Email updated"))
    }.flowOn(Dispatchers.IO)
        .catch {
            emit(State.failed(it.message!!.toString()))
        }

    fun updatePassword(password: String): Flow<State<Any>> = flow<State<Any>> {
        emit(State.loading(true))

        val auth = Firebase.auth
        auth.currentUser?.updatePassword(password)?.await()


        emit(State.success("Email updated"))
    }.flowOn(Dispatchers.IO)
        .catch {
            emit(State.failed(it.message!!.toString()))
        }





}


