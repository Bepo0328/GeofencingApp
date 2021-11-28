package kr.co.bepo.geofencingapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import kr.co.bepo.geofencingapp.data.GeofenceEntity
import kr.co.bepo.geofencingapp.databinding.GeofencesRowLayoutBinding
import kr.co.bepo.geofencingapp.util.MyDiffUtil

class GeofencesAdapter: RecyclerView.Adapter<GeofencesAdapter.MyViewHolder>() {

    private var geofencesEntity = mutableListOf<GeofenceEntity>()

    inner class MyViewHolder(private val binding: GeofencesRowLayoutBinding): RecyclerView.ViewHolder(binding.root) {

        fun bind(geofenceEntity: GeofenceEntity) = with(binding) {
            snapshotImageView.load(geofenceEntity.snapshot)
            nameTextView.text = geofenceEntity.name
            locationValueTextView.text = geofenceEntity.location
            latitudeTextView.text = parseCoordinates(geofenceEntity.latitude)
            longitudeTextView.text = parseCoordinates(geofenceEntity.longitude)
            radiusValueTextView.text = geofenceEntity.radius.toString()
        }

        private fun parseCoordinates(value: Double): String {
            return  String.format("%.4f", value)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder =
        MyViewHolder(GeofencesRowLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false))

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