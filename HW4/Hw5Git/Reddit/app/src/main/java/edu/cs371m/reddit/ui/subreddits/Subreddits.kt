package edu.cs371m.reddit.ui.subreddits

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import edu.cs371m.reddit.MainActivity
import edu.cs371m.reddit.R
import edu.cs371m.reddit.databinding.FragmentRvBinding
import edu.cs371m.reddit.ui.MainViewModel

class Subreddits : Fragment() {
    // XXX initialize viewModel
    private val viewModel: MainViewModel by activityViewModels()

    private var _binding: FragmentRvBinding? = null
    private val binding get() = _binding!!

    companion object {
        fun newInstance(): Subreddits {
            return Subreddits()
        }
    }
    private fun setDisplayHomeAsUpEnabled(value: Boolean) {
        val mainActivity = requireActivity() as MainActivity
        mainActivity.supportActionBar?.setDisplayHomeAsUpEnabled(value)
    }
    private fun initAdapter(binding: FragmentRvBinding): SubredditListAdapter {
        val adapter = SubredditListAdapter(viewModel, this.requireActivity())
        binding.recyclerView.adapter = adapter
        return adapter
    }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel.setTitle("Subreddits")
        _binding = FragmentRvBinding.inflate(inflater, container, false)
        return binding.root
    }

    // XXX Write me, onViewCreated
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setDisplayHomeAsUpEnabled(true)

        binding.apply {
            recyclerView.layoutManager = LinearLayoutManager(context)
            recyclerView.adapter = SubredditListAdapter(viewModel, requireActivity()).also { adapter ->
                viewModel.fetchSubReddits()
                viewModel.observeSearchSubReddits().observe(viewLifecycleOwner, adapter::submitList)
            }
            swipeRefreshLayout.isEnabled = false
        }

        viewModel.observeSpanData().observe(viewLifecycleOwner) { spanData ->
            spanData.spannableString.setSpan(
                ForegroundColorSpan(Color.CYAN),
                spanData.startIndex, spanData.endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        setupMenuProvider()
        viewModel.setFavoriteIconState(false)
    }

    private fun setupMenuProvider() {
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    else -> {
                        viewModel.apply {
                            setFavoriteIconState(true)
                            setTitleToSubreddit()
                        }
                        setDisplayHomeAsUpEnabled(false)
                        parentFragmentManager.popBackStack()
                        menuItem.collapseActionView()
                    }
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    override fun onDestroyView() {
        viewModel.setFavoriteIconState(true)
        setDisplayHomeAsUpEnabled(false)
        parentFragmentManager.popBackStack()
        viewModel.setTitleToSubreddit()
        _binding = null
        super.onDestroyView()
    }
}