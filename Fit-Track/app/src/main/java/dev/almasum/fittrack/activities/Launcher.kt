package dev.almasum.fittrack.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import dev.almasum.fittrack.consts.Keys

class Launcher : AppCompatActivity() {
    private lateinit var preferences: SharedPreferences
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        preferences = getSharedPreferences(Keys.prefName, MODE_PRIVATE)

        val isLoggedIn = preferences.getBoolean(Keys.isLoggedIn, false)

        if (isLoggedIn) {
            startActivity(Intent(this, MainActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finishAfterTransition()
    }
}