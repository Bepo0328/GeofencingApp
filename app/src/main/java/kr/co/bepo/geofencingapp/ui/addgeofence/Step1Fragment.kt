package kr.co.bepo.geofencingapp.ui.addgeofence

import android.annotation.SuppressLint
import android.location.Geocoder
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.launch
import kr.co.bepo.geofencingapp.R
import kr.co.bepo.geofencingapp.databinding.FragmentPermissionBinding
import kr.co.bepo.geofencingapp.databinding.FragmentStep1Binding
import kr.co.bepo.geofencingapp.util.Permissions
import kr.co.bepo.geofencingapp.viewmodels.SharedViewModel

class Step1Fragment : Fragment() {

    private var _binding: FragmentStep1Binding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()

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
                    sharedViewModel.geoName = address[0].countryCode
                } else {
                    val exception = task.exception
                    if (exception is ApiException) {
                        Log.e("Step1Fragment", exception.statusCode.toString())
                    }
                }
            }
        }
    }
}