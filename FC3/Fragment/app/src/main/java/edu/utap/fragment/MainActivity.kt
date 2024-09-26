package edu.utap.fragment

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import edu.utap.fragment.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    var bitMap = listOf<Bitmap>()
    companion object {
        var showText = true
        var immersiveMode = true
    }
    // Guidelines for immersive mode are here
    // https://developer.android.com/develop/ui/views/layout/immersive
    fun hideBars() {
        // If we are not in immersive mode, do not hide bars
        if(!immersiveMode) return
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        // Configure the behavior of the hidden system bars
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        // Hide both the status bar and the navigation bar
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        supportActionBar?.hide()
    }
    fun showBars() {
        // If we are not in immersive mode, bars are already shown
        if(!immersiveMode) return
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        // Hide both the status bar and the navigation bar
        windowInsetsController.show(WindowInsetsCompat.Type.systemBars())
        supportActionBar?.show()
    }
    private fun initMenu() {
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.main_menu, menu)
            }
            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // This could be replaced with return false, but I wanted to show
                // the usual structure for a menu item
                return when (menuItem.itemId) {
                    R.id.action_settings -> {
                        // XXX Write me
                        findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.settingsFragment)
                        true
                    }
                    else -> false
                }
            }
        })
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // This is too much work to do on the main thread!
        // But we are only doing it once at startup
        // It also costs a lot of memory to hold these bitmaps
        bitMap = listOf(
            BitmapFactory.decodeStream(assets.open("aye_aye.jpg")),
            BitmapFactory.decodeStream(assets.open("ben_atchley_arm_nocpr.jpg")),
            BitmapFactory.decodeStream(assets.open("red_panda.jpg")),
            BitmapFactory.decodeStream(assets.open("watchmen_rorschach.jpg"))
        )

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        initMenu()

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    // navigateUp:
    // If we came here from within the app, pop the back stack
    // If we came here from another app, return to it.
    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}