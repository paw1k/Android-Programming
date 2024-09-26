package dev.almasum.fittrack.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.SensorRequest
import dev.almasum.fittrack.databinding.ActivityFitBinding
import dev.almasum.fittrack.repo.FitnessRepositoryImpl
import java.util.concurrent.TimeUnit


class FitActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFitBinding
    private val MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION = 1
    private val MY_PERMISSIONS_REQUEST_LOCATION = 2
    private val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 3
    private var fitRepo = FitnessRepositoryImpl()
    private var permissionGrants = mutableListOf(false, false, false)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFitBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        checkActivityPermission()
        checkLocationPermission()
        checkGoogleFitPermission()

        binding.allowActivityPermission.setOnClickListener {
            allowActivityPermission()
        }

        binding.allowLocationPermission.setOnClickListener {
            allowLocationPermission()
        }

        binding.allowGooglePermission.setOnClickListener {
            allowGooglePermission()
        }

        fitRepo.getDailyFitnessDataLive(this)
            .observe(this) {
                binding.steps.text = it.stepCount.toString()
                binding.stepsBig.text = it.stepCount.toString()
                binding.burnedCalories.text = it.caloriesBurned.toString()
                binding.distance.text = String.format("%.2f", it.distance)
                binding.stepsProgressBar.progress = it.stepCount
            }

//        Thread(Runnable {
//            var state= permissionGrants[0] && permissionGrants[1] && permissionGrants[2]
//            while (!state) {
//                state = permissionGrants[0] && permissionGrants[1] && permissionGrants[2]
//                Log.d("FitActivity", "State: $state")
//
//            }
//            runOnUiThread(Runnable {
//                switchState()
//            })
//        }).start()
    }

    private fun checkActivityPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                switchState()
                binding.allowActivityPermission.visibility = android.view.View.VISIBLE
            } else {
                permissionGrants[0] = true
                switchState()
                binding.allowActivityPermission.visibility = android.view.View.GONE
            }
        } else {
            permissionGrants[0] = true
            switchState()
            binding.allowActivityPermission.visibility = android.view.View.GONE
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            switchState()
            binding.allowLocationPermission.visibility = android.view.View.VISIBLE
        } else {
            permissionGrants[1] = true
            switchState()
            binding.allowLocationPermission.visibility = android.view.View.GONE
        }
    }

    private fun checkGoogleFitPermission() {
        val account: GoogleSignInAccount = getGoogleAccount()
        if (!GoogleSignIn.hasPermissions(account, fitRepo.fitnessOptions)) {
            switchState()
            binding.allowGooglePermission.visibility = android.view.View.VISIBLE
        } else {
            permissionGrants[2] = true
            switchState()
            binding.allowGooglePermission.visibility = android.view.View.GONE
        }
    }

    private fun switchState() {
        val state = permissionGrants[0] && permissionGrants[1] && permissionGrants[2]
        if (state) {
            binding.permissionLayout.visibility = android.view.View.GONE
            binding.fitLayout.visibility = android.view.View.VISIBLE
            fitRepo.getDailyFitnessData(this)
            subscribeAndGetRealTimeData(DataType.TYPE_STEP_COUNT_DELTA)
        } else {
            binding.permissionLayout.visibility = android.view.View.VISIBLE
            binding.fitLayout.visibility = android.view.View.GONE
        }
    }

    private fun subscribeAndGetRealTimeData(dataType: DataType) {
        Fitness.getRecordingClient(this, getGoogleAccount())
            .subscribe(dataType)
            .addOnSuccessListener {

            }
            .addOnFailureListener {

            }
        getDataUsingSensor(dataType)
    }

    private fun getDataUsingSensor(dataType: DataType) {
        Fitness.getSensorsClient(this, getGoogleAccount())
            .add(
                SensorRequest.Builder()
                    .setDataType(dataType)
                    .setSamplingRate(1, TimeUnit.SECONDS) // sample once per minute
                    .build()
            ) {
                fitRepo.getDailyFitnessData(this)
            }
    }

    private fun allowActivityPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.ACTIVITY_RECOGNITION),
                    MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION
                )
            }
        }
    }

    private fun allowLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                MY_PERMISSIONS_REQUEST_LOCATION
            )
        }
    }

    private fun getGoogleAccount(): GoogleSignInAccount {
        return fitRepo.getGoogleAccount(this)
    }

    private fun allowGooglePermission() {
        val account: GoogleSignInAccount = getGoogleAccount()
        GoogleSignIn.requestPermissions(
            this@FitActivity,
            GOOGLE_FIT_PERMISSIONS_REQUEST_CODE,
            account,
            fitRepo.fitnessOptions
        )
    }

    override fun onResume() {
        super.onResume()
        checkActivityPermission()
        checkLocationPermission()
        checkGoogleFitPermission()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionGrants[0] = true
                    switchState()
                    binding.allowActivityPermission.visibility = android.view.View.GONE
                }
                return
            }

            MY_PERMISSIONS_REQUEST_LOCATION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionGrants[1] = true
                    switchState()
                    binding.allowLocationPermission.visibility = android.view.View.GONE
                }
                return
            }

            GOOGLE_FIT_PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    permissionGrants[2] = true
                    switchState()
                    binding.allowGooglePermission.visibility = android.view.View.GONE
                }
                return
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun finish() {
        super.finish()
        val view = window.decorView as ViewGroup
        view.removeAllViews()
    }
}