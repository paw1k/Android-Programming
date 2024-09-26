package musicplayer.cs371m.musicplayer

import android.app.Application
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.lifecycle.AndroidViewModel

class MainViewModel(application: Application) : AndroidViewModel(application) {
    // A repository can be a local database or the network
    //  or a combination of both
    private val repository = Repository()
    private var songResources = repository.fetchData()

    // Public properties, mostly accessed by PlayerFragment, but useful elsewhere

    // This variable controls what song is playing
    var currentIndex = 0
    // It is convenient to have the player never be null, so proactively
    // create it
    var player: MediaPlayer = MediaPlayer.create(
        application.applicationContext,
        getCurrentSongResourceId()
    )
    // Should I loop the current song?
    var loop = false
    // How many songs have played?
    var songsPlayed = 0
    // Is the player playing?
    var isPlaying = false

    // Creating a mutable list also creates a copy
    fun getCopyOfSongInfo(): MutableList<SongInfo> {
        return songResources.toMutableList()
    }

    fun shuffleAndReturnCopyOfSongInfo(): MutableList<SongInfo> {
        // XXX Write me
        return songResources.toMutableList().also {
            it.shuffle()
            currentIndex = it.indexOfFirst { song -> song.uniqueId == songResources[currentIndex].uniqueId }.takeIf { index -> index != -1 } ?: currentIndex
            songResources = it
        }
    }

    fun getCurrentSongName() : String {
        // XXX Write me
        return songResources[currentIndex].name
    }
    // Private function
    private fun nextIndex() : Int {
        // XXX Write me
        return if (currentIndex + 1 < songResources.size) currentIndex + 1 else 0
    }
    fun nextSong() {
        // XXX Write me
        currentIndex = nextIndex()
        player.reset()
        player.setDataSource(getApplication<Application>().applicationContext,
            android.net.Uri.parse("android.resource://" + getApplication<Application>().packageName + "/" + getCurrentSongResourceId()))
        player.prepare()
        if(isPlaying) {
            player.start()
            isPlaying = true
        }
    }
    fun getNextSongName() : String {
        // XXX Write me
        return songResources[nextIndex()].name
    }

    fun prevSong() {
        // XXX Write me
        currentIndex = if (currentIndex - 1 >= 0) currentIndex - 1 else songResources.size - 1
        player.reset()
        player.setDataSource(getApplication<Application>().applicationContext,
            android.net.Uri.parse("android.resource://" + getApplication<Application>().packageName + "/" + getCurrentSongResourceId()))
        player.prepare()
        if(isPlaying) {
            player.start()
            isPlaying = true
        }
    }

    fun getCurrentSongResourceId(): Int {
        // XXX Write me
        return songResources[currentIndex].rawId
    }
}