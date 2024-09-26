package edu.cs371m.reddit.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import edu.cs371m.reddit.MainActivity
import edu.cs371m.reddit.databinding.FragmentRvBinding

class Favorites : Fragment() {
    // XXX initialize viewModel
    private val viewModel: MainViewModel by activityViewModels()


    private var _binding: FragmentRvBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    companion object {
        fun newInstance(): Favorites {
            return Favorites()
        }
    }

    private fun setDisplayHomeAsUpEnabled(value: Boolean) {
        val mainActivity = requireActivity() as MainActivity
        mainActivity.supportActionBar?.setDisplayHomeAsUpEnabled(value)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRvBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(javaClass.simpleName, "onViewCreated")

        with(requireActivity() as AppCompatActivity) {
            supportActionBar?.title = "Favorites"
            setDisplayHomeAsUpEnabled(true)
        }
        viewModel.setFavoriteIconState(false)

        binding.recyclerView.apply {
            itemAnimator = null
            layoutManager = LinearLayoutManager(context)
            adapter = PostRowAdapter(viewModel).also {
                it.submitList(viewModel.observeSearchFavoritesPosts().value)
                viewModel.observeSearchFavoritesPosts().observe(viewLifecycleOwner) { posts ->
                    it.submitList(posts)
                }
            }
        }

        binding.swipeRefreshLayout.isEnabled = false

        val menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                setDisplayHomeAsUpEnabled(false)
                parentFragmentManager.popBackStack()
                return true
            }
        }
        requireActivity().addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }


    override fun onDestroyView() {
        // XXX Write me
        // Don't let back to home button stay when we exit favorites
        viewModel.setFavoriteIconState(true)
        setDisplayHomeAsUpEnabled(false)
        _binding = null
        super.onDestroyView()

    }
}