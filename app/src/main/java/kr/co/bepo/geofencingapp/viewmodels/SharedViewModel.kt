package kr.co.bepo.geofencingapp.viewmodels

import android.app.Application
import android.content.Context
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.SphericalUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.bepo.geofencingapp.data.DataStoreRepository
import kr.co.bepo.geofencingapp.data.GeofenceEntity
import kr.co.bepo.geofencingapp.data.GeofenceRepository
import javax.inject.Inject
import kotlin.math.sqrt

@HiltViewModel
class SharedViewModel @Inject constructor(
    application: Application,
    private val dataStoreRepository: DataStoreRepository,
    private val geofenceRepository: GeofenceRepository
) : AndroidViewModel(application) {

    val app = application

    var geoId = 0L
    var geoName = "Default"
    var geoCountryCode = ""
    var geoLocationName = "Search a City"
    var geoLatLng = LatLng(0.0, 0.0)
    var geoRadius = 500f

    var geoCitySelected = false
    var geofenceReady = false
    var geofencePrepared = false

    // DataStore
    val readFirstLaunch = dataStoreRepository.readFirstLaunch.asLiveData()

    fun saveFirstLaunch(firstLaunch: Boolean) =
        viewModelScope.launch(Dispatchers.IO) {
            dataStoreRepository.saveFirstLaunch(firstLaunch)
        }

    // Database
    val readGeofences = geofenceRepository.readGeofences.asLiveData()

    fun addGeofence(geofenceEntity: GeofenceEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            geofenceRepository.addGeofence(geofenceEntity)
        }

    fun removeGeofence(geofenceEntity: GeofenceEntity) =
        viewModelScope.launch(Dispatchers.IO) {
            geofenceRepository.removeGeofence(geofenceEntity)
        }

    fun getBounds(center: LatLng, radius: Float): LatLngBounds {
        val distancesFromCenterToCorner = radius * sqrt(2.0)
        val southwestCorner = SphericalUtil.computeOffset(center, distancesFromCenterToCorner, 225.0)
        val northEastCorner = SphericalUtil.computeOffset(center, distancesFromCenterToCorner, 45.0)
        return LatLngBounds(southwestCorner, northEastCorner)
    }

    fun checkDeviceLocationSettings(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val locationManager =
                context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            locationManager.isLocationEnabled
        } else {
            val mode: Int = Settings.Secure.getInt(
                context.contentResolver,
                Settings.Secure.LOCATION_MODE,
                Settings.Secure.LOCATION_MODE_OFF
            )
            mode != Settings.Secure.LOCATION_MODE_OFF
        }
    }
}