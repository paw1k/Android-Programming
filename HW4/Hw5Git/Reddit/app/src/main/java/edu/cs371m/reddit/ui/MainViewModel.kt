package edu.cs371m.reddit.ui


import android.content.Context
import android.content.Intent
import android.text.SpannableString
import androidx.core.text.clearSpans
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import edu.cs371m.reddit.api.RedditApi
import edu.cs371m.reddit.api.RedditPost
import edu.cs371m.reddit.api.RedditPostRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// XXX Much to write

data class SpanData(
    val spannableString: SpannableString = SpannableString(""),
    val startIndex: Int = 0,
    val endIndex: Int = 0
)

class MainViewModel : ViewModel() {
    private var title = MutableLiveData<String>()
    private var searchTerm = MutableLiveData<String>()
    private var subreddit = MutableLiveData<String>().apply {
        value = "aww"
    }
    private var favoriteIconState = MutableLiveData<Boolean>().apply { value = true }

    private var titleFilt = MutableLiveData<Boolean>().apply { value = false }
    fun setTitleFilt(isChecked: Boolean) {
        titleFilt.value = isChecked
    }

    private var selfTextFilt = MutableLiveData<Boolean>().apply { value = false }
    fun setSelfTextFilt(isChecked: Boolean) {
        selfTextFilt.value = isChecked
    }

    private var displayNameFilt = MutableLiveData<Boolean>().apply { value = false }
    fun setDisplayNameFilt(isChecked: Boolean) {
        displayNameFilt.value = isChecked
    }

    private var publicDescriptionFilt = MutableLiveData<Boolean>().apply { value = false }
    fun setpublicDescriptionFilt(isChecked: Boolean) {
        publicDescriptionFilt.value = isChecked
    }

    private var netPosts = MediatorLiveData<List<RedditPost>>()
    private var searchPosts = MediatorLiveData<List<RedditPost>>().also { mediator ->
        arrayOf(titleFilt, selfTextFilt, netPosts, searchTerm).forEach { source ->
            mediator.addSource(source) { mediator.value = filterList() }
        }
        mediator.value = netPosts.value
    }


    private var subReddits = MediatorLiveData<List<RedditPost>>()
    private var searchSubReddits = MediatorLiveData<List<RedditPost>>().also { mediator ->
        arrayOf(titleFilt, selfTextFilt, subReddits, searchTerm).forEach { source ->
            mediator.addSource(source) { mediator.value = filterSubReddit() }
        }
        mediator.value = subReddits.value
    }
    private var favoritesPosts = MediatorLiveData<MutableList<RedditPost>>()
    private var searchFavoritesPosts = MediatorLiveData<List<RedditPost>>().apply {
        arrayOf(titleFilt, selfTextFilt, favoritesPosts, searchTerm).forEach { source ->
            addSource(source) { value = filterFavouriteReddit() }
        }
        value = favoritesPosts.value
    }

    var fetchDone: MutableLiveData<Boolean> = MutableLiveData(false)

    private val spanData = MutableLiveData(SpanData())
    fun observeSpanData(): LiveData<SpanData> {
        return spanData
    }

    private fun searchAndLiveSpan(fulltext: SpannableString, subtext: String): Boolean {
        if (subtext.isEmpty()) return true
        val index = fulltext.indexOf(subtext, ignoreCase = true)
        return if (index != -1) {
            spanData.value = SpanData(fulltext, index, index + subtext.length)
            true
        } else false
    }

    private fun removeAllCurrentSpans() {
        fun clearPostSpans(posts: List<RedditPost>?) {
            posts?.forEach { post ->
                post.title.clearSpans()
                post.selfText?.clearSpans()
                post.displayName?.clearSpans()
                post.publicDescription?.clearSpans()
            }
        }

        clearPostSpans(searchPosts.value)
        clearPostSpans(searchSubReddits.value)
        clearPostSpans(searchFavoritesPosts.value)
    }


    private fun filterList(): List<RedditPost> {
        removeAllCurrentSpans()
        val searchTermValue = searchTerm.value.orEmpty()

        return netPosts.value?.filter { post ->
            (titleFilt.value == true && searchAndLiveSpan(post.title, searchTermValue)) ||
                    (selfTextFilt.value == true && post.selfText?.let { searchAndLiveSpan(it, searchTermValue) } == true)
        }.orEmpty()
    }


    private fun filterSubReddit(): List<RedditPost> {
        removeAllCurrentSpans()
        val searchTermValue = searchTerm.value ?: ""

        return (subReddits.value?.filter {
            var titleFound = false
            var selfTextFound = false

            if (titleFilt.value == true) {
                titleFound = titleFound || searchAndLiveSpan(it.displayName!!, searchTermValue)
            }
            if (selfTextFilt.value == true) {
                selfTextFound =
                    selfTextFound || searchAndLiveSpan(it.publicDescription!!, searchTermValue!!)
            }
            titleFound || selfTextFound
        } ?: emptyList())
    }

    private fun filterFavouriteReddit(): List<RedditPost> {
        removeAllCurrentSpans()
        val searchTermValue = searchTerm.value.orEmpty()

        return favoritesPosts.value?.filter { post ->
            (titleFilt.value == true && searchAndLiveSpan(post.title, searchTermValue)) ||
                    (selfTextFilt.value == true && post.selfText?.let {
                        searchAndLiveSpan(
                            it,
                            searchTermValue
                        )
                    } == true)
        }.orEmpty()
    }

    fun fetchNetPosts() {
        viewModelScope.launch(
            context = viewModelScope.coroutineContext
                    + Dispatchers.IO
        ) {
            netPosts.postValue(
                RedditPostRepository(RedditApi.create()).getPosts(subreddit.value!!)
            )
            fetchDone.postValue(true)
        }
    }

    fun fetchSubReddits() {
        viewModelScope.launch(
            context = viewModelScope.coroutineContext
                    + Dispatchers.IO
        ) {
            subReddits.postValue(
                RedditPostRepository(RedditApi.create()).getSubreddits()
            )
        }
    }

    fun repoFetch() {
        val fetch = subreddit.value
        subreddit.value = fetch
    }

    fun observeTitle(): LiveData<String> {
        return title
    }

    fun setTitle(newTitle: String) {
        title.value = newTitle
    }

    fun setTitleToSubreddit() {
        title.value = "r/${subreddit.value}"
    }

    // XXX Write me, set, observe, deal with favorites

    fun observeNetPosts(): LiveData<List<RedditPost>> {
        return netPosts
    }

    fun observeSubReddits(): LiveData<List<RedditPost>> {
        return subReddits
    }

    fun observeSearchSubReddits(): LiveData<List<RedditPost>> {
        return searchSubReddits
    }

    fun isFavorite(redditPost: RedditPost): Boolean {
        return favoritesPosts.value?.contains(redditPost) ?: false
    }

    fun addToFavoritesPosts(redditPost: RedditPost) {
        favoritesPosts.postValue(
            (favoritesPosts.value?.plus(redditPost)
                ?: mutableListOf(redditPost)) as MutableList<RedditPost>?
        )

    }

    fun removeFromFavoritesPosts(redditPost: RedditPost) {
        favoritesPosts.postValue(
            (favoritesPosts.value?.minus(redditPost)
                ?: favoritesPosts.value) as MutableList<RedditPost>
        )

    }

    fun observeFavoritesPosts(): LiveData<MutableList<RedditPost>> {
        return this.favoritesPosts
    }

    fun observeSearchFavoritesPosts(): LiveData<List<RedditPost>> {
        return this.searchFavoritesPosts
    }

    fun setSubReddit(subreddit: String) {
        this.subreddit.postValue(subreddit)
    }

    fun observeSearchPosts(): LiveData<List<RedditPost>> {
        return this.searchPosts
    }

    fun setSearchTerm(searchTerm: String) {
        this.searchTerm.value = searchTerm
    }

    fun observeFavoriteIconState(): LiveData<Boolean> {
        return favoriteIconState
    }

    fun setFavoriteIconState(state: Boolean) {
        favoriteIconState.value = state
    }


    companion object {
        fun doOnePost(context: Context, redditPost: RedditPost) {
            val onePostIntent = Intent(context, OnePost::class.java)
            onePostIntent.putExtra("title", redditPost.title.toString())
            onePostIntent.putExtra("selfText", redditPost.selfText.toString())
            onePostIntent.putExtra("imageURL", redditPost.imageURL)
            onePostIntent.putExtra("thumbnailURL", redditPost.thumbnailURL)
            context.startActivity(onePostIntent)
        }
    }
}