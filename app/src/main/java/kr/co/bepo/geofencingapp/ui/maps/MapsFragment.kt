package kr.co.bepo.geofencingapp.ui.maps

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.vmadalin.easypermissions.EasyPermissions
import com.vmadalin.easypermissions.dialogs.SettingsDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.co.bepo.geofencingapp.R
import kr.co.bepo.geofencingapp.databinding.FragmentMapsBinding
import kr.co.bepo.geofencingapp.util.ExtensionFunctions.hide
import kr.co.bepo.geofencingapp.util.ExtensionFunctions.show
import kr.co.bepo.geofencingapp.util.Permissions.hasBackgroundLocationPermission
import kr.co.bepo.geofencingapp.util.Permissions.requestBackgroundLocationPermission
import kr.co.bepo.geofencingapp.viewmodels.SharedViewModel

class MapsFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener,
    EasyPermissions.PermissionCallbacks {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()

    private lateinit var map: GoogleMap

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentMapsBinding.inflate(inflater, container, false)
        .also { _binding = it }
        .root

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(this)

        initViews()
    }

    private fun initViews() = with(binding) {
        addGeofenceFab.setOnClickListener {
            val action = MapsFragmentDirections.actionMapsFragmentToAddGeofenceGraph()
            findNavController().navigate(action)
        }

        geofencesFab.setOnClickListener {
            val action = MapsFragmentDirections.actionMapsFragmentToGeofencesFragment()
            findNavController().navigate(action)
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.mapstyle))
        map.isMyLocationEnabled = true
        map.setOnMapLongClickListener(this)
        map.uiSettings.apply {
            isMyLocationButtonEnabled = true
            isMapToolbarEnabled = false
        }
        onGeofenceReady()
    }

    private fun onGeofenceReady() {
        if (sharedViewModel.geofenceReady) {
            sharedViewModel.geofenceReady = false
            sharedViewModel.geofencePrepared = true
            displayInfoMessage()
            zoomToSelectedLocation()
        }
    }

    private fun displayInfoMessage() = with(binding) {
        lifecycleScope.launch {
            infoMessageTextView.show()
            delay(2000)
            infoMessageTextView.animate().alpha(0f).duration = 800
            delay(1000)
            infoMessageTextView.hide()
        }
    }

    private fun zoomToSelectedLocation() {
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(sharedViewModel.geoLatLng, 10f), 2000, null
        )
    }

    override fun onMapLongClick(location: LatLng) {
        if (hasBackgroundLocationPermission(requireContext())) {
            if (sharedViewModel.geofencePrepared) {
                setupGeofence(location)
            } else {
                Toast.makeText(
                    requireContext(),
                    "You need to create a new Geofence first.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            requestBackgroundLocationPermission(this)
        }
    }

    private fun setupGeofence(location: LatLng) {
        lifecycleScope.launch {
            if (sharedViewModel.checkDeviceLocationSettings(requireContext())) {
                drawCircle(location)
                drawMarker(location)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enable Location Settings.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun drawCircle(location: LatLng) {
        map.addCircle(
            CircleOptions().center(location).radius(sharedViewModel.geoRadius.toDouble())
                .strokeColor(ContextCompat.getColor(requireContext(), R.color.blue_700))
                .fillColor(ContextCompat.getColor(requireContext(), R.color.blue_transparent))
        )
    }

    private fun drawMarker(location: LatLng) {
        map.addMarker(
            MarkerOptions().position(location).title(sharedViewModel.geoName)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    override fun onPermissionsDenied(requestCode: Int, perms: List<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            SettingsDialog.Builder(requireActivity()).build().show()
        } else {
            requestBackgroundLocationPermission(this)
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: List<String>) {
        onGeofenceReady()
        Toast.makeText(
            requireContext(),
            "Permission Granted! Long Press on the Map to add a Geofence.",
            Toast.LENGTH_SHORT
        ).show()
    }
}