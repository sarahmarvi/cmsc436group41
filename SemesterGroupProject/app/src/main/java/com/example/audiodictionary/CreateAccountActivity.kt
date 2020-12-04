package com.example.audiodictionary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CreateAccountActivity : AppCompatActivity() {

    private var emailTV: EditText? = null
    private var passwordTV: EditText? = null
    private var usernameTV: EditText? = null
    private var regBtn: Button? = null
    private var validator = Validators()

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.i("CreateAccountActivity", "In onCreate of CreateAccountActivity")
        super.onCreate(savedInstanceState)
        Log.i("CreateAccountActivity", "Stop 0")
        setContentView(R.layout.create_account)

        mAuth = FirebaseAuth.getInstance()

        Log.i("CreateAccountActivity", "Stop 1")
        emailTV = findViewById(R.id.editTextTextEmailAddress)
        Log.i("CreateAccountActivity", "Stop 2")
        passwordTV = findViewById(R.id.editTextTextPassword)
        Log.i("CreateAccountActivity", "Stop 3")
        usernameTV = findViewById(R.id.editTextUsername)
        Log.i("CreateAccountActivity", "Stop 4")
        regBtn = findViewById(R.id.createAccBtn)
        Log.i("CreateAccountActivity", "Stop 5")
        regBtn!!.setOnClickListener { registerNewUser() }
    }

    private fun registerNewUser() {

        val email: String = emailTV!!.text.toString()
        val password: String = passwordTV!!.text.toString()
        val username: String = usernameTV!!.text.toString()

        if (!validator.validEmail(email)) {
            Toast.makeText(applicationContext, "Please enter a valid email...", Toast.LENGTH_LONG).show()
            return
        }
        if (!validator.validPassword(password)) {
            Toast.makeText(applicationContext, "Please enter a password more than 6 characters long!", Toast.LENGTH_LONG).show()
            return
        }

        mAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(applicationContext, "Registration successful!", Toast.LENGTH_LONG).show()

                    val intent = Intent(this@CreateAccountActivity, LoginActivity::class.java)
                    startActivity(intent)
                } else {
                    Toast.makeText(applicationContext, "Registration failed! Please try again later", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        return true
    }


}