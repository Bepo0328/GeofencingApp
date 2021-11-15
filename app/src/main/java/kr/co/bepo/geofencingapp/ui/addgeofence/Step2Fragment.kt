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
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompleteSessionToken
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kr.co.bepo.geofencingapp.R
import kr.co.bepo.geofencingapp.adapters.PredictionsAdapter
import kr.co.bepo.geofencingapp.databinding.FragmentStep2Binding
import kr.co.bepo.geofencingapp.util.ExtensionFunctions.hide
import kr.co.bepo.geofencingapp.util.ExtensionFunctions.show
import kr.co.bepo.geofencingapp.util.NetworkListener
import kr.co.bepo.geofencingapp.viewmodels.SharedViewModel
import kr.co.bepo.geofencingapp.viewmodels.Step2ViewModel

class Step2Fragment : Fragment() {

    private var _binding: FragmentStep2Binding? = null
    private val binding get() = _binding!!

    private val predictionsAdapter by lazy { PredictionsAdapter() }

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val step2ViewModel: Step2ViewModel by viewModels()

    private lateinit var placeClient: PlacesClient

    private lateinit var networkListener: NetworkListener

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
        checkInternetConnection()

        predictionsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        predictionsRecyclerView.adapter = predictionsAdapter

        geofenceLocationEt.doOnTextChanged { text, _, _, _ ->
            handleNextButton(text)
            getPlaces(text)
        }

        step2Back.setOnClickListener {
            step2BackClicked()
        }

        step2ViewModel.nextButtonEnabled.observe(viewLifecycleOwner) {
            step2Next.isEnabled = it
            step2NextClicked(it)
        }

        subscribeToObservers()
        handleNetworkConnection()
    }

    private fun handleNextButton(text: CharSequence?) {
        if (text.isNullOrEmpty()) {
            step2ViewModel.enableNextButton(false)
        }
    }

    private fun subscribeToObservers() {
        lifecycleScope.launch {
            predictionsAdapter.placeId.collectLatest { placeId ->
                if (placeId.isNotEmpty()) {
                    onCitySelected(placeId)
                }
            }
        }
    }

    private fun onCitySelected(placeId: String) {
        val placeFields = listOf(
            Place.Field.ID,
            Place.Field.LAT_LNG,
            Place.Field.NAME,
        )
        val request = FetchPlaceRequest.builder(placeId, placeFields).build()
        placeClient.fetchPlace(request)
            .addOnSuccessListener { response ->
                sharedViewModel.geoLatLng = response.place.latLng!!
                sharedViewModel.geoLocationName = response.place.name!!
                sharedViewModel.geoCitySelected = true
                binding.geofenceLocationEt.setText(sharedViewModel.geoLocationName)
                binding.geofenceLocationEt.setSelection(sharedViewModel.geoLocationName.length)
                binding.predictionsRecyclerView.hide()
                step2ViewModel.enableNextButton(true)
            }
            .addOnFailureListener { exception ->
                Log.e("Step2Fragment", exception.message.toString())
            }
    }

    private fun getPlaces(text: CharSequence?) {
        if (sharedViewModel.checkDeviceLocationSettings(requireContext())) {
            lifecycleScope.launch {
                if (text.isNullOrEmpty()) {
                    predictionsAdapter.setData(emptyList())
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
                            predictionsAdapter.setData(response.autocompletePredictions)
                            binding.predictionsRecyclerView.scheduleLayoutAnimation()
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

    private fun handleNetworkConnection() = with(binding) {
        step2ViewModel.internetAvailable.observe(viewLifecycleOwner) { networkAvailable ->
            Log.d("Step2Fragment", "networkAvailable: $networkAvailable")
            if (!networkAvailable) {
                geofenceLocationTextInputLayout.isErrorEnabled = true
                geofenceLocationTextInputLayout.error = "No Internet Connection."
                predictionsRecyclerView.hide()
            } else {
                geofenceLocationTextInputLayout.isErrorEnabled = false
                geofenceLocationTextInputLayout.error = null
                predictionsRecyclerView.show()
            }
            geofenceLocationEt.setText(sharedViewModel.geoLocationName)
        }
    }

    private fun checkInternetConnection() {
        lifecycleScope.launch {
            networkListener = NetworkListener()
            networkListener.checkNetworkAvailability(requireContext())
                .collect { online ->
                    step2ViewModel.setInternetAvailability(online)
                    if (online && sharedViewModel.geoCitySelected) {
                        step2ViewModel.enableNextButton(true)
                    } else {
                        step2ViewModel.enableNextButton(false)
                    }
                }
        }
    }

    private fun step2NextClicked(nextButtonEnabled: Boolean) = with(binding) {
        step2Next.setOnClickListener {
            if (nextButtonEnabled) {
                val action = Step2FragmentDirections.actionStep2FragmentToStep3Fragment()
                findNavController().navigate(action)
            }
        }
    }

    private fun step2BackClicked() {
        val action = Step2FragmentDirections.actionStep2FragmentToStep1Fragment()
        findNavController().navigate(action)
    }
}