package edu.cs371m.colorrecycler

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import edu.cs371m.colorrecycler.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private fun initToolBar(activityMainBinding: ActivityMainBinding) {
        setSupportActionBar(activityMainBinding.toolbar)
        // Don't use android.R... resources!  They can change without warning.
        //toolbar.setNavigationIcon(android.R.drawable.ic_menu_gallery);
        activityMainBinding.toolbar.setNavigationIcon(R.drawable.ic_menu_black_24dp)
    }

    private fun initRecyclerViewLinear(activityMainBinding: ActivityMainBinding) {
        activityMainBinding.contentMain.recyclerViewLinear.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = ColorAdapter(ColorList.getAll()) { position ->
                (adapter as ColorAdapter).apply {
                    moveToTop(position)
                    scrollToPosition(0)
                }
            }
        }
    }

    private fun initRecyclerViewGrid(activityMainBinding: ActivityMainBinding) {
        // Define a layout for RecyclerView
        val layoutManager = StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        activityMainBinding.contentMain.recyclerViewGrid.layoutManager = layoutManager

        // Initialize a new instance of RecyclerView Adapter instance
        val adapter = ColorAdapter(ColorList.getAll(), ColorAdapter::swapItem)
        // Set the adapter for RecyclerView
        activityMainBinding.contentMain.recyclerViewGrid.adapter = adapter
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        initToolBar(activityMainBinding)
        initRecyclerViewLinear(activityMainBinding)
        initRecyclerViewGrid(activityMainBinding)
        // Janky method to allow one Activity to have two different layouts
        // Fragments will make this much better
        activityMainBinding.contentMain.recyclerViewLinear.visibility = View.VISIBLE
        activityMainBinding.contentMain.recyclerViewGrid.visibility = View.GONE

        // Add menu items without overriding methods in the Activity
        addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Inflate the menu; this adds items to the action bar if it is present.
                menuInflater.inflate(R.menu.menu_main, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem) : Boolean {
                // Handle action bar item clicks here.
                val cm = activityMainBinding.contentMain
                return when (menuItem.itemId) {
                    R.id.swap -> if (cm.recyclerViewLinear.visibility == View.VISIBLE) {
                        cm.recyclerViewLinear.visibility = View.GONE
                        cm.recyclerViewGrid.visibility = View.VISIBLE
                        true
                    } else {
                        cm.recyclerViewLinear.visibility = View.VISIBLE
                        cm.recyclerViewGrid.visibility = View.GONE
                        true
                    }
                    else -> false
                }
            }
        })
    }
}
