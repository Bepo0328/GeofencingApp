package kr.co.bepo.geofencingapp.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.lifecycle.viewModelScope
import androidx.navigation.findNavController
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import kr.co.bepo.geofencingapp.R
import kr.co.bepo.geofencingapp.data.GeofenceEntity
import kr.co.bepo.geofencingapp.databinding.GeofencesRowLayoutBinding
import kr.co.bepo.geofencingapp.ui.geofences.GeofencesFragmentDirections
import kr.co.bepo.geofencingapp.util.ExtensionFunctions.disable
import kr.co.bepo.geofencingapp.util.ExtensionFunctions.enable
import kr.co.bepo.geofencingapp.util.MyDiffUtil
import kr.co.bepo.geofencingapp.viewmodels.SharedViewModel

class GeofencesAdapter(
    private val sharedViewModel: SharedViewModel
) : RecyclerView.Adapter<GeofencesAdapter.MyViewHolder>() {

    private var geofencesEntity = mutableListOf<GeofenceEntity>()

    inner class MyViewHolder(private val binding: GeofencesRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(geofenceEntity: GeofenceEntity) = with(binding) {
            snapshotImageView.load(geofenceEntity.snapshot)
            nameTextView.text = geofenceEntity.name
            locationValueTextView.text = geofenceEntity.location
            latitudeTextView.text = parseCoordinates(geofenceEntity.latitude)
            longitudeTextView.text = parseCoordinates(geofenceEntity.longitude)
            radiusValueTextView.text = geofenceEntity.radius.toString()

            deleteImageView.setOnClickListener {
                removeItem(geofenceEntity)
            }

            handleMotionTransition()

            snapshotImageView.setOnClickListener {
                val action =
                    GeofencesFragmentDirections.actionGeofencesFragmentToMapsFragment(geofenceEntity)
                binding.root.findNavController().navigate(action)
            }
        }

        private fun removeItem(geofenceEntity: GeofenceEntity) {
            sharedViewModel.viewModelScope.launch {
                val geofenceStopped =
                    sharedViewModel.stopGeofence(listOf(geofenceEntity.geoId))
                if (geofenceStopped) {
                    sharedViewModel.removeGeofence(geofenceEntity)
                    showSnackBar(geofenceEntity)
                } else {
                    Log.d("GeofencesAdapter", "Geofence NOT REMOVED!")
                }
            }
        }

        private fun showSnackBar(
            removeItem: GeofenceEntity
        ) {
            Snackbar.make(
                binding.root,
                "Removed " + removeItem.name,
                Snackbar.LENGTH_SHORT
            ).setAction("UNDO") {
                undoRemoval(removeItem)
            }.show()
        }

        private fun undoRemoval(removeItem: GeofenceEntity) {
            sharedViewModel.addGeofence(removeItem)
            sharedViewModel.startGeofence(
                removeItem.latitude,
                removeItem.longitude
            )
        }

        private fun parseCoordinates(value: Double): String {
            return String.format("%.4f", value)
        }

        private fun handleMotionTransition() = with(binding) {
            deleteImageView.disable()
            geofencesRowMotionLayout.setTransitionListener(object :
                MotionLayout.TransitionListener {
                override fun onTransitionStarted(
                    motionLayout: MotionLayout?,
                    startId: Int,
                    endId: Int
                ) {
                }

                override fun onTransitionChange(
                    motionLayout: MotionLayout?,
                    startId: Int,
                    endId: Int,
                    progress: Float
                ) {
                }

                override fun onTransitionTrigger(
                    motionLayout: MotionLayout?,
                    triggerId: Int,
                    positive: Boolean,
                    progress: Float
                ) {
                }

                override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                    if (motionLayout != null && currentId == R.id.start) {
                        deleteImageView.disable()
                    } else if (motionLayout != null && currentId == R.id.end) {
                        deleteImageView.enable()
                    }
                }
            })
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder =
        MyViewHolder(
            GeofencesRowLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(geofencesEntity[position])
    }

    override fun getItemCount(): Int =
        geofencesEntity.size

    fun setData(newGeofencesEntity: MutableList<GeofenceEntity>) {
        val geofenceDiffUtil = MyDiffUtil(geofencesEntity, newGeofencesEntity)
        val diffUtilResult = DiffUtil.calculateDiff(geofenceDiffUtil)
        geofencesEntity = newGeofencesEntity
        diffUtilResult.dispatchUpdatesTo(this)
    }
}