package kr.co.bepo.geofencingapp.ui.addgeofence

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import kr.co.bepo.geofencingapp.R
import kr.co.bepo.geofencingapp.databinding.FragmentStep3Binding
import kr.co.bepo.geofencingapp.viewmodels.SharedViewModel

class Step3Fragment : Fragment() {

    private var _binding: FragmentStep3Binding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentStep3Binding.inflate(layoutInflater, container, false)
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
        step3Back.setOnClickListener {
            step3BackClicked()
        }

        step3Done.setOnClickListener {
            step3DoneClicked()
        }

        updateSliderValue()
    }

    private fun step3BackClicked() = with(binding) {
        val action = Step3FragmentDirections.actionStep3FragmentToStep2Fragment()
        findNavController().navigate(action)
    }

    private fun step3DoneClicked() {
        sharedViewModel.geoRadius = binding.slider.value
        sharedViewModel.geofenceReady = true
        val action = Step3FragmentDirections.actionStep3FragmentToMapsFragment()
        findNavController().navigate(action)
        Log.d("Step3Fragment", sharedViewModel.geoRadius.toString())
    }

    private fun updateSliderValue() = with(binding) {
        updateSliderValueTextView(sharedViewModel.geoRadius)
        slider.addOnChangeListener { _, value, _ ->
            sharedViewModel.geoRadius = value
            updateSliderValueTextView(sharedViewModel.geoRadius)
        }
    }

    private fun updateSliderValueTextView(geoRadius: Float) = with(binding) {
        val kilometers = geoRadius / 1000
        if (geoRadius >= 1000f) {
            sliderValueTextView.text = context?.getString(R.string.display_kilometers, kilometers.toString())
        } else {
            sliderValueTextView.text = context?.getString(R.string.display_meters, geoRadius.toString())
        }
        slider.value = geoRadius
    }

}