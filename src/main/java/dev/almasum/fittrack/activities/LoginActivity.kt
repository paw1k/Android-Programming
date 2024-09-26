package dev.almasum.fittrack.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.my.intotal.Utils.LoadingDialog
import dev.almasum.fittrack.consts.Keys
import dev.almasum.fittrack.databinding.ActivityLoginBinding
import dev.almasum.fittrack.local_db.RoomDB
import dev.almasum.fittrack.local_db.entities.ItemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var loadingDialog: LoadingDialog
    private lateinit var preferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = getSharedPreferences(Keys.prefName, MODE_PRIVATE)
        editor = preferences.edit()

        loadingDialog = LoadingDialog()

        binding.login.setOnClickListener {
            if (validate()) {
                login()
            }
        }

        binding.register.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun login() {
        val username = binding.username.text.toString()
        val password = binding.password.text.toString()

        val db = FirebaseFirestore.getInstance()

        loadingDialog.show(supportFragmentManager, "loading")
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                loadingDialog.dismiss()
                if (documents.isEmpty) {
                    binding.username.error = "User not found"
                    binding.username.requestFocus()
                } else {
                    val user = documents.first()
                    val userPassword = user.getString("password")
                    if (userPassword == password) {
                        editor.putBoolean(Keys.isLoggedIn, true)
                        editor.putString(Keys.userName, username)
                        editor.apply()
                        importItems()
                        startActivity(Intent(this, MainActivity::class.java))
                        finishAfterTransition()
                    } else {
                        binding.password.error = "Incorrect password"
                        binding.password.requestFocus()
                    }
                }
            }
            .addOnFailureListener { _ ->
                loadingDialog.dismiss()
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            }

    }

    private fun importItems() {
        val db = FirebaseFirestore.getInstance()
        val username = preferences.getString(Keys.userName, "")!!
        db.collection("users").document(username).collection("intake").get()
            .addOnSuccessListener { documents ->
                documents.forEach { document ->
                    val itemEntity = document.toObject<ItemEntity>()
                    CoroutineScope(Dispatchers.IO).launch {
                        RoomDB.getInstance(this@LoginActivity).getItemDao().insert(itemEntity)
                    }
                }
            }
    }

    private fun validate(): Boolean {

        val username = binding.username.text.toString()
        val password = binding.password.text.toString()

        if (username.isEmpty()) {
            binding.username.error = "Username is required"
            binding.username.requestFocus()
            return false
        }else{
            binding.username.error = null
        }

        if (password.isEmpty()) {
            binding.password.error = "Password is required"
            binding.password.requestFocus()
            return false
        }else{
            binding.password.error = null
        }

        if (password.length < 8) {
            binding.password.error = "Password must be at least 8 characters"
            binding.password.requestFocus()
            return false
        }else{
            binding.password.error = null
        }

        return true
    }
}