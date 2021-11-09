package kr.co.bepo.geofencingapp.ui.addgeofence

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.launch
import kr.co.bepo.geofencingapp.R
import kr.co.bepo.geofencingapp.databinding.FragmentStep1Binding
import kr.co.bepo.geofencingapp.viewmodels.SharedViewModel
import kr.co.bepo.geofencingapp.viewmodels.Step1ViewModel

class Step1Fragment : Fragment() {

    private var _binding: FragmentStep1Binding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val step1ViewModel: Step1ViewModel by viewModels()

    private lateinit var geoCoder: Geocoder
    private lateinit var placesClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Places.initialize(requireContext(), getString(R.string.google_maps_key))
        placesClient = Places.createClient(requireContext())
        geoCoder = Geocoder(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentStep1Binding.inflate(inflater, container, false)
        .also { _binding = it }
        .root

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews()
    }

    private fun initViews() = with(binding) {
        step1Back.setOnClickListener {
            onStep1BackClicked()
        }

        getCountryCodeFromCurrentLocation()
        onTextChanged()
        step1NextClicked()
    }

    private fun onStep1BackClicked() {
        val action = Step1FragmentDirections.actionStep1FragmentToMapsFragment()
        findNavController().navigate(action)
    }

    @SuppressLint("MissingPermission")
    private fun getCountryCodeFromCurrentLocation() {
        lifecycleScope.launch {
            val placeFields = listOf(Place.Field.LAT_LNG)
            val request: FindCurrentPlaceRequest = FindCurrentPlaceRequest.newInstance(placeFields)

            val placeResponse = placesClient.findCurrentPlace(request)
            placeResponse.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val response = task.result
                    val latLng = response.placeLikelihoods[0].place.latLng!!
                    val address = geoCoder.getFromLocation(
                        latLng.latitude,
                        latLng.longitude,
                        1
                    )
                    sharedViewModel.geoCountryCode = address[0].countryCode
                } else {
                    val exception = task.exception
                    if (exception is ApiException) {
                        Log.e("Step1Fragment", exception.statusCode.toString())
                    }
                }
                enableNextButton()
            }
        }
    }

    private fun enableNextButton() {
        if (sharedViewModel.geoName.isNotEmpty()) {
            step1ViewModel.enableNextButton(true)
        }
    }

    private fun onTextChanged() = with(binding) {
        geofenceNameEt.setText(sharedViewModel.geoName)
        Log.d("onTextChanged", sharedViewModel.geoName)
        geofenceNameEt.doOnTextChanged { text, _, _, _ ->
            if (text.isNullOrEmpty()) {
                step1ViewModel.enableNextButton(false)
            } else {
                step1ViewModel.enableNextButton(true)
            }
            sharedViewModel.geoName = text.toString()
            Log.d("onTextChanged", sharedViewModel.geoName)
        }
    }

    private fun step1NextClicked() = with(binding) {
        step1ViewModel.nextButtonEnabled.observe(viewLifecycleOwner) {
            step1Next.isEnabled = it
        }

        step1Next.setOnClickListener {
            if (step1Next.isEnabled) {
                sharedViewModel.geoId = System.currentTimeMillis()
                val action = Step1FragmentDirections.actionStep1FragmentToStep2Fragment()
                findNavController().navigate(action)
            }
        }
    }
}