package kr.co.bepo.geofencingapp.ui.geofences

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import kr.co.bepo.geofencingapp.R
import kr.co.bepo.geofencingapp.adapters.GeofencesAdapter
import kr.co.bepo.geofencingapp.databinding.FragmentGeofencesBinding
import kr.co.bepo.geofencingapp.viewmodels.SharedViewModel

class GeofencesFragment : Fragment() {

    private var _binding: FragmentGeofencesBinding? = null
    private val binding get() = _binding!!

    private val sharedViewModel: SharedViewModel by activityViewModels()
    private val geofencesAdapter by lazy { GeofencesAdapter() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentGeofencesBinding.inflate(inflater, container, false)
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
        geofencesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        geofencesRecyclerView.adapter = geofencesAdapter

        observeDatabase()
    }

    private fun observeDatabase() {
        sharedViewModel.readGeofences.observe(viewLifecycleOwner) {
            geofencesAdapter.setData(it)
        }
    }
}