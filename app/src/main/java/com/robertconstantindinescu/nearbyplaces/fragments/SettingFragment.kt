package com.robertconstantindinescu.nearbyplaces.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.robertconstantindinescu.nearbyplaces.R
import com.robertconstantindinescu.nearbyplaces.databinding.FragmentSettingBinding
import com.robertconstantindinescu.nearbyplaces.permissions.AppPermissions
import com.robertconstantindinescu.nearbyplaces.utils.LoadingDialog
import com.robertconstantindinescu.nearbyplaces.utils.State
import com.robertconstantindinescu.nearbyplaces.viewmodel.LocationViewModel
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.coroutines.flow.collect


class SettingFragment : Fragment() {

    private lateinit var binding: FragmentSettingBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var appPermissions: AppPermissions
    private var image: Uri? = null
    private val locationViewModel: LocationViewModel by viewModels()
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private var permissionToRequest = mutableListOf<String>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSettingBinding.inflate(inflater, container, false)
        firebaseAuth = Firebase.auth
        loadingDialog = LoadingDialog(requireActivity())
        appPermissions = AppPermissions()


        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                val isStoragePermissionOk =
                    permissions[android.Manifest.permission.READ_EXTERNAL_STORAGE] == true
                            && permissions[android.Manifest.permission.WRITE_EXTERNAL_STORAGE] == true

                if (isStoragePermissionOk)
                    pickImage()
                else
                    Snackbar.make(binding.root, "Storage permission denied", Snackbar.LENGTH_LONG)
                        .show()

            }

        binding.imgCamera.setOnClickListener {
            if (appPermissions.isStorageOk(requireContext())) {
                pickImage()

            } else {
                requestStorage()
            }
        }


        binding.txtUsername.setOnClickListener { usernameDialog() }

        binding.cardEmail.setOnClickListener {


            val action = SettingFragmentDirections.actionBtnSettingToEmailConfirmationFragment()
            Navigation.findNavController(requireView()).navigate(action)

        }

        binding.cardPassword.setOnClickListener {
            val direction = SettingFragmentDirections.actionBtnSettingToEmailConfirmationFragment(isPassword = true)
            Navigation.findNavController(requireView()).navigate(direction)
        }

        // TODO: 28/11/21 NO interesa de momento

        if (!requireActivity().packageManager.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)) {
            binding.cardFinger.visibility = View.GONE
        } else {
            val finger = requireActivity().getSharedPreferences("user", Context.MODE_PRIVATE)
                .getBoolean("finger", false)
            if (finger) {
                binding.fingerSwitch.isChecked = true
            }
        }

        binding.fingerSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                requireActivity().getSharedPreferences("user", Context.MODE_PRIVATE).edit().apply {
                    putBoolean("finger", true)
                    apply()
                }
            } else {
                requireActivity().getSharedPreferences("user", Context.MODE_PRIVATE).edit().apply {
                    putBoolean("finger", false)
                    apply()
                }
            }
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (activity as AppCompatActivity?)!!.supportActionBar?.title = "Settings"

        binding.apply {
            txtEmail.text = firebaseAuth.currentUser?.email
            txtUsername.text = firebaseAuth.currentUser?.displayName

            Glide.with(requireContext()).load(firebaseAuth.currentUser?.photoUrl)
                .into(imgProfile)
        }
    }


    private fun pickImage() {
        CropImage.activity()
            .setCropShape(CropImageView.CropShape.OVAL)
            .start(requireContext(), this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK) {
                image = result.uri

                updateImage(image!!)
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Snackbar.make(binding.root, "${result.error}", Snackbar.LENGTH_SHORT).show()
            }
        }
    }


    private fun requestStorage() {
        permissionToRequest.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        permissionToRequest.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        permissionLauncher.launch(permissionToRequest.toTypedArray())
    }

    private fun usernameDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val view: View = LayoutInflater.from(requireContext())
            .inflate(R.layout.username_dialog_layout, null, false)
        builder.setView(view)
        val edtUsername: TextInputEditText = view.findViewById(R.id.edtDialogUsername)
        builder.setTitle("Edit Username")
            .setPositiveButton("Save") { _, _ ->

                val name = edtUsername.text?.trim().toString()
                if (name.isNotEmpty())
                    updateName(name)
                else {
                    Toast.makeText(requireContext(), "Username is required", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            .setNegativeButton("Cancel") { _, _ -> }.create().show()
    }

    private fun updateName(name: String) {

        lifecycleScope.launchWhenStarted {
            locationViewModel.updateName(name).collect {
                when (it) {
                    is State.Loading -> {
                        if (it.flag == true) {
                            loadingDialog.startLoading()
                        }
                    }

                    is State.Succes -> {
                        loadingDialog.stopLoading()
                        //set the textusername
                        binding.txtUsername.text = name
                        //inform the user of succeful update.
                        Snackbar.make(
                            binding.root, it.data.toString(),
                            Snackbar.LENGTH_SHORT
                        ).show()


                    }
                    is State.Failed -> {
                        loadingDialog.stopLoading()
                        Snackbar.make(
                            binding.root, it.error,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }
    private fun updateImage(image: Uri) {

        lifecycleScope.launchWhenStarted {
            locationViewModel.updateImage(image).collect {
                when (it) {
                    is State.Loading -> {
                        if (it.flag == true) {
                            loadingDialog.startLoading()
                        }
                    }

                    is State.Succes -> {

                        loadingDialog.stopLoading()
                        Glide.with(requireContext()).load(image).into(binding.imgProfile)
                        Snackbar.make(
                            binding.root, it.data.toString(),
                            Snackbar.LENGTH_SHORT
                        ).show()


                    }
                    is State.Failed -> {
                        loadingDialog.stopLoading()
                        Snackbar.make(
                            binding.root, it.error,
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }


}

