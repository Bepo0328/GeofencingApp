package kr.co.bepo.geofencingapp.ui.addgeofence

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.launch
import kr.co.bepo.geofencingapp.R
import kr.co.bepo.geofencingapp.databinding.FragmentStep2Binding
import kr.co.bepo.geofencingapp.viewmodels.SharedViewModel

class Step2Fragment : Fragment() {

    private var _binding: FragmentStep2Binding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var placeClient: PlacesClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Places.initialize(requireContext(), getString(R.string.google_maps_key))
        placeClient = Places.createClient(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentStep2Binding.inflate(inflater, container, false)
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
        geofenceLocationEt.doOnTextChanged { text, _, _, _ ->
            getPlaces(text)
        }

        step2Back.setOnClickListener {
            onStep2BackClicked()
        }

        step2Next.setOnClickListener {
            onStep2NextClicked()
        }
    }

    private fun getPlaces(text: CharSequence?) {
        if (sharedViewModel.checkDeviceLocationSettings(requireContext())) {
            lifecycleScope.launch {
                if (text.isNullOrEmpty()) {

                } else {
                    val token = AutocompleteSessionToken.newInstance()
                    val request = FindAutocompletePredictionsRequest.builder()
                        .setCountries(sharedViewModel.geoCountryCode)
                        .setTypeFilter(TypeFilter.CITIES)
                        .setSessionToken(token)
                        .setQuery(text.toString())
                        .build()

                    placeClient.findAutocompletePredictions(request)
                        .addOnSuccessListener { response ->

                        }
                        .addOnFailureListener { exception: Exception? ->
                            if (exception is ApiException) {
                                Log.e("Step2Fragment", exception.statusCode.toString())
                            }
                        }
                }
            }
        } else {
            Toast.makeText(requireContext(), "Please Enable Location Settings.", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun onStep2NextClicked() {
        val action = Step2FragmentDirections.actionStep2FragmentToStep1Fragment()
        findNavController().navigate(action)
    }

    private fun onStep2BackClicked() {
        val action = Step2FragmentDirections.actionStep2FragmentToStep3Fragment()
        findNavController().navigate(action)
    }
}