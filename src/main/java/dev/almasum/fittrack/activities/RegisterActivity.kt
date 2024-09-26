package dev.almasum.fittrack.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.my.intotal.Utils.LoadingDialog
import dev.almasum.fittrack.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var loadingDialog: LoadingDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog = LoadingDialog()

        binding.register.setOnClickListener {
            if (validate()) {
                register()
            }
        }
    }

    private fun register() {
        val username = binding.username.text.toString()
        val fullName = binding.fullName.text.toString()
        val password = binding.password.text.toString()

        loadingDialog.show(supportFragmentManager, "loading")

        val db = FirebaseFirestore.getInstance()

        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    db.collection("users")
                        .document(username)
                        .set(
                            mapOf(
                                "username" to username,
                                "fullName" to fullName,
                                "password" to password
                            )
                        )
                        .addOnSuccessListener {
                            loadingDialog.dismiss()
                            Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT)
                                .show()
                            finishAfterTransition()
                        }
                        .addOnFailureListener {
                            loadingDialog.dismiss()
                            Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    loadingDialog.dismiss()
                    binding.username.error = "User already exists"
                    binding.username.requestFocus()
                }
            }
            .addOnFailureListener {
                loadingDialog.dismiss()
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            }


    }

    private fun validate(): Boolean {
        val username = binding.username.text.toString()
        val fullName = binding.fullName.text.toString()
        val password = binding.password.text.toString()
        val confirmPassword = binding.confirmPassword.text.toString()

        if (username.isEmpty()) {
            binding.username.error = "Username is required"
            binding.username.requestFocus()
            return false
        } else {
            binding.username.error = null
        }

        if (fullName.isEmpty()) {
            binding.fullName.error = "Full name is required"
            binding.fullName.requestFocus()
            return false
        } else {
            binding.fullName.error = null
        }

        if (password.isEmpty()) {
            binding.password.error = "Password is required"
            binding.password.requestFocus()
            return false
        } else {
            binding.password.error = null
        }

        if (password.length < 8) {
            binding.password.error = "Password must be at least 8 characters"
            binding.password.requestFocus()
            return false
        } else {
            binding.password.error = null
        }

        if (confirmPassword.isEmpty()) {
            binding.confirmPassword.error = "Confirm password is required"
            binding.confirmPassword.requestFocus()
            return false
        } else {
            binding.confirmPassword.error = null
        }

        if (password != confirmPassword) {
            binding.confirmPassword.error = "Passwords do not match"
            binding.confirmPassword.requestFocus()
            return false
        } else {
            binding.confirmPassword.error = null
        }

        return true
    }
}