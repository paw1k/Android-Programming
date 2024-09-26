package edu.utap.firebaseauth

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.google.firebase.auth.FirebaseAuth
import edu.utap.firebaseauth.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var binding : ActivityMainBinding

    // See: https://developer.android.com/training/basics/intents/result
    private val signInLauncher =
        registerForActivityResult(FirebaseAuthUIActivityResultContract()) {
            viewModel.updateUser()
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if(savedInstanceState == null) {
            binding.displayNameET.text.clear()
        }
        // XXX Write me. Observe data to display in UI
        with(viewModel) {
            observeDisplayName().observe(this@MainActivity) { binding.userName.text = it }
            observeEmail().observe(this@MainActivity) { binding.userEmail.text = it }
            observeUid().observe(this@MainActivity) { binding.userUid.text = it }
        }

        binding.logoutBut.setOnClickListener {
            // XXX Write me.
            viewModel.signOut()
        }
        binding.loginBut.setOnClickListener {
            // XXX Write me.
            FirebaseAuth.getInstance().currentUser?.let {
                viewModel.updateUser()
            } ?: run {
                AuthUI.getInstance()
                    .createSignInIntentBuilder()
                    .setAvailableProviders(arrayListOf(AuthUI.IdpConfig.EmailBuilder().build()))
                    .setIsSmartLockEnabled(false)
                    .build().also { signInIntent ->
                        signInLauncher.launch(signInIntent)
                    }
            }
        }
        binding.setDisplayName.setOnClickListener {
            // XXX Write me.
            AuthInit.setDisplayName(binding.displayNameET.text.toString(),viewModel)
        }

        AuthInit(viewModel, signInLauncher)
    }
}