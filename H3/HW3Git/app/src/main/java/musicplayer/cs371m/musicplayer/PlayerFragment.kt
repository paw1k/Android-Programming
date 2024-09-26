package musicplayer.cs371m.musicplayer

import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import musicplayer.cs371m.musicplayer.databinding.PlayerFragmentBinding
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.floor

class PlayerFragment : Fragment() {
    // When this is true, the displayTime coroutine should not modify the seek bar
    val userModifyingSeekBar = AtomicBoolean(false)
    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: RVDiffAdapter

    private var _binding: PlayerFragmentBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = PlayerFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    private fun initRecyclerViewDividers(rv: RecyclerView) {
        // Let's have dividers between list items
        val dividerItemDecoration = DividerItemDecoration(
            rv.context, LinearLayoutManager.VERTICAL
        )
        rv.addItemDecoration(dividerItemDecoration)
    }

    // Please put all display updates in this function
    // The exception is that
    //   displayTime updates the seek bar, time passed & time remaining
    private fun updateDisplay() {
        // If settings is active, we are in the background and do
        // not have a binding.  Return early.
        if (_binding == null) {
            return
        }
        //XXX Write me. Make sure all player UI elements are up to date
        // That includes all buttons, textViews, icons & the seek bar

        with(binding) {
            playerCurrentSongText.text = "Now playing: ${viewModel.getCurrentSongName()}"
            playerNextSongText.text = "Next up: ${viewModel.getNextSongName()}"

            playerPlayPauseButton.setImageResource(
                if (viewModel.isPlaying) R.drawable.ic_pause_black_24dp else R.drawable.ic_play_arrow_black_24dp
            )

            loopIndicator.setBackgroundColor(if (viewModel.loop) Color.RED else Color.TRANSPARENT)
        }
        adapter.notifyDataSetChanged()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Make the RVDiffAdapter and set it up
        //XXX Write me. Setup adapter.
        adapter = RVDiffAdapter(viewModel) { songIndex ->
            viewModel.currentIndex = songIndex
            if (viewModel.isPlaying) {
                viewModel.player.apply {
                    reset()
                    setDataSource(requireActivity().applicationContext, android.net.Uri.parse("android.resource://${requireActivity().packageName}/${viewModel.getCurrentSongResourceId()}"))
                    prepare()
                    start()
                }
            }
            updateDisplay()
        }
        binding.playerRV.layoutManager = LinearLayoutManager(context)
        binding.playerRV.adapter = adapter
        adapter.submitList(viewModel.getCopyOfSongInfo())
        updateDisplay()


        //XXX Write me. Write callbacks for buttons

        binding.playerPlayPauseButton.setOnClickListener {
            with(viewModel) {
                if (isPlaying) {
                    player.pause()
                } else {
                    if (player.currentPosition > 0) {
                        player.start()
                    } else {
                        player.apply {
                            reset()
                            setDataSource(requireActivity().applicationContext, android.net.Uri.parse("android.resource://${requireActivity().packageName}/${getCurrentSongResourceId()}"))
                            prepare()
                            start()
                        }
                        songsPlayed++
                    }
                }
                isPlaying = !isPlaying
            }
            updateDisplay()
        }

        binding.playerSkipForwardButton.setOnClickListener {
            viewModel.nextSong()
            if (viewModel.isPlaying) {
                viewModel.songsPlayed++
            }
            updateDisplay()
        }

        binding.playerSkipBackButton.setOnClickListener {
            viewModel.prevSong()
            if (viewModel.isPlaying) {
                viewModel.songsPlayed++
            }
            updateDisplay()
        }

        binding.playerPermuteButton.setOnClickListener{
            val shuffledSongInfoList = viewModel.shuffleAndReturnCopyOfSongInfo()
            adapter.submitList(shuffledSongInfoList) {
                updateDisplay()
            }

        }

        //XXX Write me. binding.playerSeekBar.setOnSeekBarChangeListener

        viewModel.player.setOnPreparedListener {
            binding.playerSeekBar.max = it.duration
            viewLifecycleOwner.lifecycleScope.launch {
                while (it.isPlaying && isActive) {
                    _binding?.playerSeekBar?.progress = it.currentPosition.takeUnless { userModifyingSeekBar.get() } ?: continue
                    delay(1000)
                }
            }
        }

        viewModel.player.setOnCompletionListener {
            with(viewModel) {
                if (loop) player.seekTo(0) else nextSong()
                player.start()
                songsPlayed++
            }
            updateDisplay()
        }

        binding.playerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.player.seekTo(progress)
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }
            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

        viewModel.player.takeIf { it.isPlaying }?.let { player ->
            binding.playerSeekBar.max = player.duration
            viewLifecycleOwner.lifecycleScope.launch {
                while (player.isPlaying && isActive) {
                    _binding?.takeUnless { userModifyingSeekBar.get() }?.playerSeekBar?.progress = player.currentPosition
                    delay(1000)
                }
            }
        }

        updateDisplay()

        // Don't change this code.  We are launching a coroutine (user-level thread) that will
        // execute concurrently with our code, but will update the displayed time
        val millisec = 100L
        viewLifecycleOwner.lifecycleScope.launch {
            displayTime(millisec)
        }
    }

    // The suspend modifier marks this as a function called from a coroutine
    // Note, this whole function is somewhat reminiscent of the Timer class
    // from Fling and Peck.  We use an independent thread to manage one small
    // piece of our GUI.  This coroutine should not modify any data accessed
    // by the main thread (it can read property values)
    private suspend fun displayTime(misc: Long) {
        // This only runs while the display is active
        while (viewLifecycleOwner.lifecycleScope.coroutineContext.isActive) {
            val currentPosition = viewModel.player.currentPosition
            val maxTime = viewModel.player.duration
            // Update the seek bar (if the user isn't updating it)
            // and update the passed and remaining time
            //XXX Write me
            if (!userModifyingSeekBar.get()) {
                with(binding) {
                    playerSeekBar.progress = currentPosition
                    playerTimePassedText.text = convertTime(currentPosition)
                    playerTimeRemainingText.text = convertTime(maxTime - currentPosition)
                }
            }

            // Leave this code as is.  it inserts a delay so that this thread does
            // not consume too much CPU
            delay(misc)
        }
    }

    // This method converts time in milliseconds to minutes-second formatted string
    // with two digit minutes and two digit sections, e.g., 01:30
    private fun convertTime(milliseconds: Int): String {
        //XXX Write me
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    // XXX Write me, handle player dynamics and currently playing/next song

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}