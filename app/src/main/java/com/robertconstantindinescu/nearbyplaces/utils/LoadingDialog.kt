package com.robertconstantindinescu.nearbyplaces.utils

import android.app.Activity
import android.app.AlertDialog
import android.view.LayoutInflater
import com.robertconstantindinescu.nearbyplaces.R
import com.robertconstantindinescu.nearbyplaces.databinding.DialogLayoutBinding

class LoadingDialog (private val activity: Activity){

    private var alerDialog: AlertDialog? = null

    fun startLoading(){
        val builder = AlertDialog.Builder(activity, R.style.loadingDialogStyle)
        val binding = DialogLayoutBinding.inflate(LayoutInflater.from(activity), null, false)

        builder.setView(binding.root)
        //si el dialogo se pued ecancelar
        builder.setCancelable(false)
        alerDialog=builder.create()
        binding.rotateLoading.start()
        alerDialog?.show()

    }

    fun stopLoading(){
        alerDialog?.dismiss()
    }

}