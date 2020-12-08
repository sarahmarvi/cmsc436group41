package com.example.audiodictionary

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.lang.Exception

/* Class is adapted from Lab7-Firebase */
class LoginActivity : AppCompatActivity() {

    private lateinit var mDatabaseUser: DatabaseReference
    private var userEmail: EditText? = null
    private var userPassword: EditText? = null
    private var loginBtn: Button? = null

    private var mAuth: FirebaseAuth? = null
    private lateinit var createAccountButton: Button
    private lateinit var userID : MutableList<String>
    private lateinit var users : MutableList<User>
    private lateinit var uid : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        users = ArrayList()
        userID = ArrayList()

        mDatabaseUser = FirebaseDatabase.getInstance().getReference("User")
        mAuth = FirebaseAuth.getInstance()

        userEmail = findViewById(R.id.editTextTextEmailAddress2)
        userPassword = findViewById(R.id.editTextTextPassword)

        loginBtn = findViewById(R.id.submitBtn)
        loginBtn!!.setOnClickListener { loginUserAccount() }

        createAccountButton = findViewById(R.id.createAccBtn2)

        createAccountButton.setOnClickListener {
            startCreateAcc()
        }
    }

    private fun startCreateAcc() {
        val intent = Intent(this@LoginActivity, CreateAccountActivity::class.java)
        Log.i(TAG, "Entering CreateAccountActivity")
        startActivity(intent)
    }

    private fun loginUserAccount() {
        val email: String = userEmail?.text.toString()
        val password: String = userPassword?.text.toString()

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(applicationContext, "Please enter email...", Toast.LENGTH_LONG).show()
            return
        }
        if (TextUtils.isEmpty(password)) {
            Toast.makeText(applicationContext, "Please enter password!", Toast.LENGTH_LONG).show()
            return
        }

        mAuth!!.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(applicationContext, "Login successful!", Toast.LENGTH_LONG)
                        .show()

                    uid = task.result!!.user!!.uid
                    val name = getUsername(uid)

                    val intent = Intent(this@LoginActivity, LanguageListActivity::class.java)
                    intent.putExtra("USER_ID", uid)
                    intent.putExtra("USERNAME", name)
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        applicationContext,
                        "Login failed! Please try again later",
                        Toast.LENGTH_LONG
                    ).show()
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

    override fun onStart() {
        super.onStart()

        mDatabaseUser.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                users.clear()
                userID.clear()

                var user : User? = null
                for (postSnapshot in dataSnapshot.children) {
                    try {
                        user = postSnapshot.getValue(User::class.java)
                        postSnapshot.key?.let { userID.add(it) }
                    } catch (e: Exception) {
                        Log.e("LanguageListActivity", e.toString())
                    } finally {
                        users.add(user!!)
                    }
                }

            }

            override fun onCancelled(p0: DatabaseError) {
                // Do Nothing
            }
        })
    }

    // Gets username of user who has logged on to send in intent
    private fun getUsername(id : String) : String {
        var index = -1
        for ( i in 0 until userID.size) {
            if (userID[i] == id) {
                index = i
                break
            }
        }

        if (index == -1) {
            return ""
        }

        return users[index].username
    }

    // To allow a user to exit/sign out without letting them use the original function of the back
    // button to go back (to a page exclusive to users signed in) after signing out.
    override fun onBackPressed() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
  
    companion object {
        const val TAG = "LoginActivity"
    }
}
