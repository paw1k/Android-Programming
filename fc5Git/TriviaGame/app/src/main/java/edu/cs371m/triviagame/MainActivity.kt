package edu.cs371m.triviagame

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import edu.cs371m.triviagame.databinding.ActivityMainBinding
import edu.cs371m.triviagame.databinding.ContentMainBinding
import edu.cs371m.triviagame.ui.main.MainFragmentDirections

// https://opentdb.com/api_config.php
class MainActivity :
    AppCompatActivity()
{
    companion object {
        val TAG = this::class.java.simpleName
    }

    val difficultyList = listOf("Easy", "Medium", "Hard")
    private lateinit var contentMainBinding: ContentMainBinding
    private val viewModel : MainViewModel by viewModels()

    private fun navigateToMainFragment(id: Int, index: Int) {
        val navController = findNavController(id)

        val action = MainFragmentDirections.actionGlobalMainFragment(index)
        navController.navigate(action)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val activityMainBiding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBiding.root)
        activityMainBiding.toolbar.title = "Trivia Game"
        setSupportActionBar(activityMainBiding.toolbar)
        contentMainBinding = activityMainBiding.contentMain

        viewModel.fetchDone.observe(this) {
            navigateToMainFragment(R.id.q1, 0)
            navigateToMainFragment(R.id.q2, 1)
            navigateToMainFragment(R.id.q3, 2)
            viewModel.fetchDone.observe(this) {
                contentMainBinding.swipeRefresh.isRefreshing = false
            }
            contentMainBinding.swipeRefresh.setOnRefreshListener {
                viewModel.netRefresh()
            }
        }

        // Please enjoy this code that manages the spinner
        // Create an ArrayAdapter using a simple spinner layout and languages array
        val aa = ArrayAdapter(this, android.R.layout.simple_spinner_item, difficultyList)
        // Set layout to use when the list of choices appear
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        // Create the object as we are assigning it
        contentMainBinding.difficultySP.onItemSelectedListener = object :
            AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>,
                                        view: View, position: Int, id: Long) {
                Log.d(TAG, "pos $position")
                viewModel.setDifficulty(difficultyList[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                Log.d(TAG, "onNothingSelected")
            }
        }
        // Set Adapter to Spinner
        contentMainBinding.difficultySP.adapter = aa
        // Set initial value of spinner to medium
        val initialSpinner = 1
        contentMainBinding.difficultySP.setSelection(initialSpinner)
        viewModel.setDifficulty(difficultyList[initialSpinner])
    }
}
