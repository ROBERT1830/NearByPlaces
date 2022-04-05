package com.robertconstantindinescu.nearbyplaces.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.robertconstantindinescu.nearbyplaces.R
import com.robertconstantindinescu.nearbyplaces.databinding.FragmentEmailChangeBinding
import com.robertconstantindinescu.nearbyplaces.utils.LoadingDialog
import com.robertconstantindinescu.nearbyplaces.utils.State
import com.robertconstantindinescu.nearbyplaces.viewmodel.LocationViewModel
import kotlinx.coroutines.flow.collect


class EmailChangeFragment : Fragment() {

    private lateinit var binding: FragmentEmailChangeBinding
    private lateinit var loadingDialog: LoadingDialog
    private val locationViewModel: LocationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {


        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEmailChangeBinding.inflate(inflater, container, false)
        (activity as AppCompatActivity?)!!.supportActionBar?.title = "New Email"
        loadingDialog = LoadingDialog(requireActivity())

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnUpdateEmail.setOnClickListener {
            val email = binding.edtUEmail.text.toString().trim()
            if (email.isEmpty()) {
                binding.edtUEmail.error = "Field is required"
                binding.edtUEmail.requestFocus()
            } else {
                lifecycleScope.launchWhenStarted {
                    locationViewModel.updateEmail(email).collect {
                        when (it) {
                            is State.Loading -> {
                                if (it.flag == true)
                                    loadingDialog.startLoading()
                            }

                            is State.Succes -> {
                                loadingDialog.stopLoading()
                                Snackbar.make(
                                    binding.root,
                                    it.data.toString(),
                                    Snackbar.LENGTH_SHORT
                                ).show()

                                //go back
                                Navigation.findNavController(requireView())
                                    .popBackStack()

                            }
                            is State.Failed -> {
                                loadingDialog.stopLoading()
                                Snackbar.make(
                                    binding.root,
                                    it.error,
                                    Snackbar.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }
            }
        }
    }


}