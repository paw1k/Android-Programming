package edu.cs371m.reddit.ui

import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import edu.cs371m.reddit.databinding.FragmentRvBinding


// XXX Write most of this file
class HomeFragment : Fragment() {
    // XXX initialize viewModel
    private val viewModel: MainViewModel by activityViewModels()

    private var _binding: FragmentRvBinding? = null

    // This property is only valid between onCreateView and onDestroyView.
    private val binding get() = _binding!!

    companion object {
        fun newInstance(): HomeFragment {
            return HomeFragment()
        }
    }

    // Set up the adapter
    private fun initAdapter(binding: FragmentRvBinding): PostRowAdapter {
        val adapter = PostRowAdapter(viewModel)

        binding.recyclerView.adapter = adapter
        return adapter
    }

    private fun initSwipeLayout(swipe: SwipeRefreshLayout) {
        swipe.setOnRefreshListener {
            viewModel.fetchNetPosts()
        }
        viewModel.fetchDone.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = false
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRvBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(javaClass.simpleName, "onViewCreated")
        // XXX Write me
        (requireActivity() as AppCompatActivity).supportActionBar?.title = "mainFragTag"
        binding.recyclerView.layoutManager = LinearLayoutManager(binding.recyclerView.context)

        val adapter = PostRowAdapter(viewModel).also { binding.recyclerView.adapter = it }

        initSwipeLayout(binding.swipeRefreshLayout)

        viewModel.setTitleFilt(true)
        viewModel.setSelfTextFilt(true)

        viewModel.fetchNetPosts()
        viewModel.observeSearchPosts().observe(activity as LifecycleOwner) {
            adapter.submitList(viewModel.observeSearchPosts().value)
        }

        viewModel.observeSpanData().observe(activity as LifecycleOwner) {
            it.spannableString.setSpan(
                ForegroundColorSpan(Color.CYAN),
                it.startIndex, it.endIndex,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }
    }
}