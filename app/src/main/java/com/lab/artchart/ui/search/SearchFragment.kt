package com.lab.artchart.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lab.artchart.MainActivity
import com.lab.artchart.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val galleryViewModel =
            ViewModelProvider(this)[SearchViewModel::class.java]

        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textSearch
        galleryViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }

        // observe remote database items and update listview
        val adapter = ArtworkAdapter(requireContext(), mutableListOf())
        val listView = binding.artworkListView
        listView.adapter = adapter
        val firebaseViewModel = (activity as MainActivity).firebaseViewModel
        firebaseViewModel.allArtworks.observe(viewLifecycleOwner) {
            adapter.replace(it)
            adapter.notifyDataSetChanged()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}