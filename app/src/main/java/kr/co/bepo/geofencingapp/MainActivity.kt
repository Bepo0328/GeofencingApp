package kr.co.bepo.geofencingapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import dagger.hilt.android.AndroidEntryPoint
import kr.co.bepo.geofencingapp.databinding.ActivityMainBinding
import kr.co.bepo.geofencingapp.util.Permissions

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initNavigtaion()
    }

    private fun initNavigtaion() = with(binding) {
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        if (Permissions.hasLocationPermission(this@MainActivity)) {
            val action = PermissionFragmentDirections.actionPermissionFragmentToMapsFragment()
            navController.navigate(action)
        }
    }
}