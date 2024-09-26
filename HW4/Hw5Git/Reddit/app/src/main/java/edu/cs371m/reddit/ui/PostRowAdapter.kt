package edu.cs371m.reddit.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import edu.cs371m.reddit.R
import edu.cs371m.reddit.api.RedditPost
import edu.cs371m.reddit.databinding.RowPostBinding
import edu.cs371m.reddit.glide.Glide

/**
 * Created by witchel on 8/25/2019
 */

// https://developer.android.com/reference/androidx/recyclerview/widget/ListAdapter
// Slick adapter that provides submitList, so you don't worry about how to update
// the list, you just submit a new one when you want to change the list and the
// Diff class computes the smallest set of changes that need to happen.
// NB: Both the old and new lists must both be in memory at the same time.
// So you can copy the old list, change it into a new list, then submit the new list.
//
// You can call adapterPosition to get the index of the selected item
class PostRowAdapter(private val viewModel: MainViewModel) :
    ListAdapter<RedditPost, PostRowAdapter.VH>(RedditDiff()) {
    class RedditDiff : DiffUtil.ItemCallback<RedditPost>() {
        override fun areItemsTheSame(oldItem: RedditPost, newItem: RedditPost): Boolean {
            return oldItem.key == newItem.key
        }

        override fun areContentsTheSame(oldItem: RedditPost, newItem: RedditPost): Boolean {
            return RedditPost.spannableStringsEqual(oldItem.title, newItem.title) &&
                    RedditPost.spannableStringsEqual(oldItem.selfText, newItem.selfText) &&
                    RedditPost.spannableStringsEqual(
                        oldItem.publicDescription,
                        newItem.publicDescription
                    ) &&
                    RedditPost.spannableStringsEqual(oldItem.displayName, newItem.displayName)

        }
    }

    inner class VH(val rowPostBinding: RowPostBinding) :
        RecyclerView.ViewHolder(rowPostBinding.root) {

        fun bind(redditPost: RedditPost) {
            rowPostBinding.title.text = redditPost.title
            rowPostBinding.selfText.text = redditPost.selfText
            rowPostBinding.score.text = redditPost.score.toString()
            rowPostBinding.comments.text = redditPost.commentCount.toString()
            //Log.d("XXX", "bind: " + redditPost.selfText)
            Glide.glideFetch(
                redditPost.imageURL, redditPost.thumbnailURL,
                rowPostBinding.image
            )
            if (viewModel.isFavorite(redditPost)) {
                rowPostBinding.rowFav.setImageResource(
                    R.drawable.ic_favorite_black_24dp
                )
            } else {
                rowPostBinding.rowFav.setImageResource(
                    R.drawable.ic_favorite_border_black_24dp
                )
            }

            rowPostBinding.title.setOnClickListener {
                MainViewModel.doOnePost(it.context, redditPost)
            }
            rowPostBinding.rowFav.setOnClickListener {
                if (viewModel.isFavorite(redditPost)) {
                    rowPostBinding.rowFav.setImageResource(
                        R.drawable.ic_favorite_border_black_24dp
                    )
                    viewModel.removeFromFavoritesPosts(redditPost)
                    notifyDataSetChanged()
                } else {
                    rowPostBinding.rowFav.setImageResource(R.drawable.ic_favorite_black_24dp)
                    viewModel.addToFavoritesPosts(redditPost)
                    notifyDataSetChanged()
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val rowPostBinding = RowPostBinding.inflate(
            LayoutInflater.from(parent.context),
            parent, false
        )
        return VH(rowPostBinding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
//        val item = viewModel.observeNetPosts()!!
//        holder.itemView.
//        bindViewHolder(item.value?.get(position), position)
        holder.bind(currentList[position])
        //Log.d("XXX", viewModel.observeNetPosts().value?.get(position)!!.toString())
        //holder.bind(viewModel.observeNetPosts().value?.get(position)!!)

    }

}

