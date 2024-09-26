package dev.almasum.fittrack.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import dev.almasum.fittrack.R
import dev.almasum.fittrack.consts.Keys
import dev.almasum.fittrack.databinding.ActivityMainBinding
import dev.almasum.fittrack.local_db.RoomDB
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        preferences = getSharedPreferences(Keys.prefName, MODE_PRIVATE)
        editor = preferences.edit()

        binding.fitnessSection.setOnClickListener {
            startActivity(Intent(this, FitActivity::class.java))
        }

        binding.nutritionSection.setOnClickListener {
            startActivity(Intent(this, NutritionActivity::class.java))
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                val dialog = AlertDialog.Builder(this)
                dialog.setTitle("Logout")
                dialog.setMessage("Are you sure you want to logout?")
                dialog.setPositiveButton("Yes") { _, _ ->
                    editor.putBoolean(Keys.isLoggedIn, false)
                    editor.apply()
                    CoroutineScope(Dispatchers.IO).launch {
                        RoomDB.getInstance(this@MainActivity).getItemDao().clearTable()
                    }
                    startActivity(Intent(this, LoginActivity::class.java))
                    finishAfterTransition()
                }
                dialog.setNegativeButton("No") { _, _ -> }
                dialog.show()

            }
        }
        return super.onOptionsItemSelected(item)
    }
}