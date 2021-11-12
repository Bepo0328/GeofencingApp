package kr.co.bepo.geofencingapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.model.AutocompletePrediction
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kr.co.bepo.geofencingapp.databinding.PredictionsRowLayoutBinding
import kr.co.bepo.geofencingapp.util.MyDiffUtil

class PredictionsAdapter : RecyclerView.Adapter<PredictionsAdapter.MyViewHolder>() {

    private var predictions = emptyList<AutocompletePrediction>()

    private val _placeId = MutableStateFlow("")
    val placeId: StateFlow<String> get() = _placeId

    inner class MyViewHolder(private val binding: PredictionsRowLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(prediction: AutocompletePrediction) = with(binding) {
            root.setOnClickListener {
                setPlaceId(prediction.placeId)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder =
        MyViewHolder(
            PredictionsRowLayoutBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: PredictionsAdapter.MyViewHolder, position: Int) {
        holder.bind(predictions[position])
    }

    override fun getItemCount(): Int =
        predictions.size

    private fun setPlaceId(placeId: String) {
        _placeId.value = placeId
    }

    fun setData(newPredictions: List<AutocompletePrediction>) {
        val myDiffUtil = MyDiffUtil(predictions, newPredictions)
        val myDiffUtilResult = DiffUtil.calculateDiff(myDiffUtil)
        predictions = newPredictions
        myDiffUtilResult.dispatchUpdatesTo(this)
    }
}