package edu.cs371m.reddit.ui.subreddits

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.cs371m.reddit.MainActivity
import edu.cs371m.reddit.api.RedditPost
import edu.cs371m.reddit.databinding.RowSubredditBinding
import edu.cs371m.reddit.glide.Glide
import edu.cs371m.reddit.ui.MainViewModel

// NB: Could probably unify with PostRowAdapter if we had two
// different VH and override getItemViewType
// https://medium.com/@droidbyme/android-recyclerview-with-multiple-view-type-multiple-view-holder-af798458763b
class SubredditListAdapter(
    private val viewModel: MainViewModel,
    private val fragmentActivity: FragmentActivity
) : ListAdapter<RedditPost, SubredditListAdapter.VH>(RedditDiff()) {

    class RedditDiff : DiffUtil.ItemCallback<RedditPost>() {
        override fun areItemsTheSame(oldItem: RedditPost, newItem: RedditPost) = oldItem.key == newItem.key

        override fun areContentsTheSame(oldItem: RedditPost, newItem: RedditPost) =
            RedditPost.spannableStringsEqual(oldItem.publicDescription, newItem.publicDescription) &&
                    RedditPost.spannableStringsEqual(oldItem.displayName, newItem.displayName)
    }
    inner class VH(private val binding: RowSubredditBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            binding.subRowHeading.setOnClickListener {
                val subreddit = binding.subRowHeading.text.toString()
                viewModel.apply {
                    setSubReddit(subreddit)
                    setTitle("r/$subreddit")
                    repoFetch()
                    setFavoriteIconState(true)
                }
                (fragmentActivity as MainActivity).apply {
                    supportActionBar?.setDisplayHomeAsUpEnabled(false)
                }
                fragmentActivity.supportFragmentManager.popBackStack()
            }
        }

        fun bind(redditPost: RedditPost) = with(binding) {
            subRowHeading.text = redditPost.displayName
            subRowDetails.text = redditPost.publicDescription
            redditPost.iconURL?.let { iconURL ->
                Glide.glideFetch(iconURL, iconURL, subRowPic)
            }
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val rowSubredditBinding = RowSubredditBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return VH(rowSubredditBinding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(currentList[position])
    }

    // XXX Write me.
    override fun onViewRecycled(holder: VH) {
        super.onViewRecycled(holder)
    }

}