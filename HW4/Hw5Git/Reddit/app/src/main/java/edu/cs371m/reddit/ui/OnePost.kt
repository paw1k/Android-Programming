package edu.cs371m.reddit.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import edu.cs371m.reddit.databinding.ActionBarOnePostBinding
import edu.cs371m.reddit.databinding.ActivityOnePostBinding
import edu.cs371m.reddit.glide.Glide

class OnePost : AppCompatActivity() {

    private lateinit var actionBarOnePostBinding: ActionBarOnePostBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityOnePostBinding.inflate(layoutInflater).apply {
            setContentView(root)
            setSupportActionBar(toolbar)
        }

        supportActionBar?.apply {
            setDisplayShowTitleEnabled(false)
            setDisplayShowCustomEnabled(true)
            customView = ActionBarOnePostBinding.inflate(layoutInflater).apply {
                actionBarOnePostBinding = this
            }.root
        }

        intent.extras?.let { bundle ->
            val title = bundle.getString("title")
            val imageUrl = bundle.getString("imageURL")
            val thumbnailURL = bundle.getString("thumbnailURL")

            actionBarOnePostBinding?.actionTitle?.text = title
            binding.titleTv.text = title

            imageUrl?.let {
                binding.image.visibility = View.VISIBLE
                Glide.glideFetch(imageUrl, thumbnailURL ?: "", binding.image)
                binding.image.contentDescription = imageUrl
            }

            binding.selfText.text = bundle.getString("selfText")
        }

        actionBarOnePostBinding?.actionBack?.setOnClickListener {
            finish()
        }
    }

}
