package musicplayer.cs371m.musicplayer

import android.app.Application
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import musicplayer.cs371m.musicplayer.databinding.SongRowBinding

// Pass in a function called clickListener that takes a view and a songName
// as parameters.  Call clickListener when the row is clicked.
class RVDiffAdapter(private val viewModel: MainViewModel,
                    private val clickListener: (songIndex : Int)->Unit)
    // https://developer.android.com/reference/androidx/recyclerview/widget/ListAdapter
    // Slick adapter that provides submitList, so you don't worry about how to update
    // the list, you just submit a new one when you want to change the list and the
    // Diff class computes the smallest set of changes that need to happen.
    // NB: Both the old and new lists must both be in memory at the same time.
    // So you can copy the old list, change it into a new list, then submit the new list.
    : ListAdapter<SongInfo,
        RVDiffAdapter.ViewHolder>(Diff())
{
    companion object {
        val TAG = "RVDiffAdapter"
    }

    // ViewHolder pattern holds row binding
    inner class ViewHolder(val songRowBinding : SongRowBinding)
        : RecyclerView.ViewHolder(songRowBinding.root) {
        init {
            itemView.setOnClickListener {
                val position = bindingAdapterPosition
                if (position == RecyclerView.NO_POSITION) return@setOnClickListener

                viewModel.apply {
                    currentIndex = position
                    player.apply {
                        reset()
                        setDataSource(getApplication<Application>().applicationContext, android.net.Uri.parse("android.resource://${getApplication<Application>().packageName}/${getCurrentSongResourceId()}"))
                        prepare()
                        if (isPlaying) {
                            start()
                            songsPlayed++
                        }
                    }
                }
                notifyDataSetChanged()
                clickListener(position)
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        //XXX Write me.
        val inflater = LayoutInflater.from(parent.context)
        val songRowBinding = SongRowBinding.inflate(inflater, parent, false)
        return ViewHolder(songRowBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        //XXX Write me.
        val song = getItem(position)
        with(holder.songRowBinding) {
            songTitle.text = song.name
            songDuration.text = song.time
            root.setBackgroundColor(if (position == viewModel.currentIndex) Color.YELLOW else Color.TRANSPARENT)
        }
    }

    class Diff : DiffUtil.ItemCallback<SongInfo>() {
        // Item identity
        override fun areItemsTheSame(oldItem: SongInfo, newItem: SongInfo): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }
        // Item contents are the same, but the object might have changed
        override fun areContentsTheSame(oldItem: SongInfo, newItem: SongInfo): Boolean {
            return oldItem.name == newItem.name
                    && oldItem.rawId == newItem.rawId
                    && oldItem.time == newItem.time
        }
    }
}

