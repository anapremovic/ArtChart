package com.lab.artchart.ui.search

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.SearchView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lab.artchart.MainActivity
import com.lab.artchart.R
import com.lab.artchart.database.Artwork
import com.lab.artchart.databinding.FragmentSearchBinding

class SearchFragment : Fragment(), SearchView.OnQueryTextListener {

    private var _binding: FragmentSearchBinding? = null
    private lateinit var listView:ListView

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val galleryViewModel = ViewModelProvider(this)[SearchViewModel::class.java]

        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // observe remote database items and update listview
        val adapter = ArtworkAdapter(requireContext(), mutableListOf())
        listView = binding.artworkListView
        listView.adapter = adapter
        val firebaseViewModel = (activity as MainActivity).firebaseViewModel
        firebaseViewModel.allArtworks.observe(viewLifecycleOwner) {
            adapter.replace(it)
            adapter.notifyDataSetChanged()
        }

        listView.setOnItemClickListener { parent, view, position, id ->
            val selected = listView.adapter.getItem(position) as Artwork
            val intent = Intent(requireContext(), ArtInfoActivity::class.java)
            intent.putExtra("title", selected.title)
            intent.putExtra("artistName", selected.artistName)
            intent.putExtra("creationYear", selected.creationYear) // int
            intent.putExtra("latitude", selected.latitude) // dbl
            intent.putExtra("longitude", selected.longitude) // dbl
            intent.putExtra("description", selected.description)
            intent.putExtra("imageUrl", selected.imageUrl)
            startActivity(intent)
        }

        //SEARCH VIEW
        listView.isTextFilterEnabled = true
        val searchView = root.findViewById<SearchView>(R.id.search_view)
        searchView.setOnQueryTextListener(this)

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
        println("newText: "+newText)
        if (TextUtils.isEmpty(newText)){
            println("clearTextFilter")
            listView.clearTextFilter();
        }else{
            println("setFilterText")
            listView.setFilterText(newText)
        }
        return true
    }
}