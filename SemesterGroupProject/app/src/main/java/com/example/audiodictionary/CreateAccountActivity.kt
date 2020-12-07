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
import android.widget.SearchView
import android.widget.Toast
import androidx.core.view.isVisible
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
    private lateinit var databaseUser : DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_account)

        mAuth = FirebaseAuth.getInstance()
        databaseUser = FirebaseDatabase.getInstance().getReference("User")

        emailTV = findViewById(R.id.editTextTextEmailAddress)
        passwordTV = findViewById(R.id.editTextTextPassword)
        usernameTV = findViewById(R.id.editTextUsername)
        regBtn = findViewById(R.id.createAccBtn)
        regBtn!!.setOnClickListener { registerNewUser() }
    }

    /* This function was adapted from Lab7-Firebase */
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

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(applicationContext, "Please enter a username!", Toast.LENGTH_LONG).show()
            return
        }

        mAuth!!.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(applicationContext, "Registration successful!", Toast.LENGTH_LONG).show()

                    addUser(task.result!!.user!!.uid, username)

                    val intent = Intent(this@CreateAccountActivity, LoginActivity::class.java)

                    Log.i("CreateAccountActivity", "Sending Intent to LoginActivity")
                    startActivity(intent)
                } else {
                    Toast.makeText(applicationContext, "Registration failed! Please try again later", Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu, menu)
        // Hide search on this page
        menu.findItem(R.id.search).isVisible = false
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.search -> {
                // Tell user that this page cannot use searching
                Toast.makeText(this, "Cannot search on this page.", Toast.LENGTH_LONG).show()
                return false
            }
            R.id.exit_option -> {
                // Exit session button
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> {
                return super.onOptionsItemSelected(item)
            }
        }
    }

    private fun addUser (uid : String, username : String) {

        if (!TextUtils.isEmpty(username)) {
            val user = User(username, "")
            databaseUser.child(uid).setValue(user)

            Log.i("CreateAccountActivity", "Added username to database")
        }

    }
  
    // To allow a user to exit/sign out without letting them use the original function of the back
    // button to go back (to a page exclusive to users signed in) after signing out.
    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

}
