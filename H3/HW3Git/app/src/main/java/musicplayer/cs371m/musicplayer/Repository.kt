package musicplayer.cs371m.musicplayer

// This is the model in MVVM
data class SongInfo(val name: String, val rawId: Int, val time: String,
    val uniqueId: Int)
class Repository {
    // This is long enough that it has to scroll
    // I have a limited number of song files, so I use them twice
    // Entries are distinguished with a different time value.
    // To make list manipulation work, all entries must be distinct
    private var songResources = listOf(
                SongInfo(
                    "Dark Star",
                    R.raw.dark_star_excerpt,
                    "1:30",
                    0
                ),
                SongInfo(
                    "What's Mine",
                    R.raw.whats_mine_excerpt,
                    "1:15",
                    1
                ),
                SongInfo(
                    "La Fille Aux Cheveux De Lin",
                    R.raw.debussy_la_fille_aux_cheveux_de_lin,
                    "2:42",
                    2
                ),
                SongInfo(
                    "Rondo Alla Turca",
                    R.raw.rondo_alla_turca,
                    "3:02",
                    3
                ),
                SongInfo(
                    "Big Digits",
                    R.raw.big_digits_excerpt,
                    "0:49",
                    4
                ),
                SongInfo(
                    "Base after base",
                    R.raw.base_after_base,
                    "1:25",
                    5
                ),
                SongInfo(
                    "Can't let go",
                    R.raw.cant_let_go,
                    "1:24",
                    6
                ),
        // Second set of songs, with two digit times
                SongInfo(
                    "Dark Star",
                    R.raw.dark_star_excerpt,
                    "1:30",
                    7
                ),
                SongInfo(
                    "What's Mine",
                    R.raw.whats_mine_excerpt,
                    "1:15",
                    8
                ),
                SongInfo(
                    "La Fille Aux Cheveux De Lin",
                    R.raw.debussy_la_fille_aux_cheveux_de_lin,
                    "2:43",
                    9
                ),
                SongInfo(
                    "Rondo Alla Turca",
                    R.raw.rondo_alla_turca,
                    "3:02",
                    10
                ),
                SongInfo(
                    "Big Digits",
                    R.raw.big_digits_excerpt,
                    "0:49",
                    11
                ),
                SongInfo(
                    "Base after base",
                    R.raw.base_after_base,
                    "1:25",
                    12
                ),
                SongInfo(
                    "Can't let go",
                    R.raw.cant_let_go,
                    "1:24",
                    13
                )
    )
    //EEE
    fun fetchData(): List<SongInfo> {
        return songResources
    }
}