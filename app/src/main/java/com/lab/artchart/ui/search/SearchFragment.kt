package com.lab.artchart.ui.search

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import android.widget.SearchView
import androidx.fragment.app.Fragment
import com.lab.artchart.R
import com.lab.artchart.database.Artwork
import com.lab.artchart.database.ArtworkViewModel
import com.lab.artchart.database.ReviewViewModel
import com.lab.artchart.ui.MainActivity
import com.lab.artchart.databinding.FragmentSearchBinding

class SearchFragment : Fragment(), SearchView.OnQueryTextListener {

    private var _binding: FragmentSearchBinding? = null
    private lateinit var listView: ListView
    private lateinit var currArtworkList: List<Artwork>
    private lateinit var artworkViewModel: ArtworkViewModel
    private lateinit var reviewViewModel: ReviewViewModel

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val root: View = binding.root
        artworkViewModel = (activity as MainActivity).artworkViewModel
        reviewViewModel = (activity as MainActivity).reviewViewModel

        // load review stats
        reviewViewModel.loadArtworkStatsByArtwork()

        // observe remote database items and update listview
        val adapter = ArtworkAdapter(requireContext(), mutableListOf(), mapOf())
        listView = binding.artworkListView
        listView.adapter = adapter
        artworkViewModel.allArtworks.observe(viewLifecycleOwner) {
            currArtworkList = it
            adapter.replaceArtworkList(it)
            adapter.notifyDataSetChanged()
        }
        reviewViewModel.artworkStatsByArtwork.observe(viewLifecycleOwner) {
            adapter.replaceArtworkStats(it)
            adapter.notifyDataSetChanged()
        }

        listView.setOnItemClickListener { _, _, position, _ ->
            val selected = listView.adapter.getItem(position) as Artwork
            val intent = Intent(requireContext(), ArtInfoActivity::class.java)
            intent.putExtra("title", selected.title)
            intent.putExtra("artistName", selected.artistName)
            intent.putExtra("creationYear", selected.creationYear) // int
            intent.putExtra("latitude", selected.latitude) // dbl
            intent.putExtra("longitude", selected.longitude) // dbl
            intent.putExtra("description", selected.description)
            intent.putExtra("imageUrl", selected.imageUrl)
            intent.putExtra("artId", selected.artId)
            startActivity(intent)
        }

        //SEARCH VIEW
        listView.isTextFilterEnabled = true
        val searchView = root.findViewById<SearchView>(R.id.search_view)
        searchView.setOnQueryTextListener(this)

        //NAME BUTTON - filter title alphabetically
        val nameBtn = root.findViewById<Button>(R.id.filterNameButton)
        nameBtn.setOnClickListener{
            currArtworkList = currArtworkList.sortedBy { it.title?.lowercase() }
            adapter.replaceArtworkList(currArtworkList)
            adapter.notifyDataSetChanged()
        }

        //YEAR BUTTON - sort by most recent year
        val yearBtn = root.findViewById<Button>(R.id.filterYearButton)
        yearBtn.setOnClickListener{
            currArtworkList = currArtworkList.sortedByDescending { it.creationYear }
            adapter.replaceArtworkList(currArtworkList)
            adapter.notifyDataSetChanged()
        }

        //TO DO -> REVIEW and DISTANCE

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        return false
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        if (TextUtils.isEmpty(newText)){
            listView.clearTextFilter()
        }else{
            listView.setFilterText(newText)
        }
        return true
    }
}