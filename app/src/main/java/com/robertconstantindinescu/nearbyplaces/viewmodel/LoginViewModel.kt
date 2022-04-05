package com.robertconstantindinescu.nearbyplaces.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import com.robertconstantindinescu.nearbyplaces.repo.AppRepo

class LoginViewModel(): ViewModel() {

    private val repo = AppRepo()

    fun login(email: String, password: String) = repo.login(email, password)

    fun signUp(email: String, password: String, username: String, image: Uri) =
        repo.signUp(email, password, username, image)

    fun forget(email: String) = repo.forgetPassword(email)
}