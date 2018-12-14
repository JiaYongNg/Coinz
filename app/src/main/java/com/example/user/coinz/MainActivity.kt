package com.example.user.coinz


import kotlinx.android.synthetic.main.activity_main.*
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import android.widget.Toast


class MainActivity : AppCompatActivity(){

    private val tag = "MainActivity"

    private var mAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mAuth = FirebaseAuth.getInstance()

        login_button_login.setOnClickListener {
            performLogin()

        }
        create_account_textview.setOnClickListener{

            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)

        }

    }

    @SuppressWarnings("MissingPermission")
    override fun onStart()
    {
        super.onStart()
        // Check if user is signed in (non-null) and go to MapActivity if the user is signed in.
        if (mAuth?.currentUser != null) {
            Log.d(tag,"already signed in")

            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
            finish()

        } else {
            Log.d(tag,"not signed in")

        }
    }


    override fun onStop()
    {
        super.onStop()
        finish()

    }

    private fun performLogin(){
        val email = email_edittext_login.text.toString()
        val password = password_edittext_login.text.toString()

        Log.d(tag, "Attempt login with email/pw: $email/***")
        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
            return
        }
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).addOnCompleteListener {
            // If sign in fails, display a message to the user. If sign in succeeds
            // go to MapActivity
            if (!it.isSuccessful) {
                // there was an error
                if (password.length< 8) {
                    Toast.makeText(this, "password length too short", Toast.LENGTH_LONG).show()

                } else {
                    Toast.makeText(this, "authentication failed", Toast.LENGTH_LONG).show()
                }
            } else {
                val intent = Intent(this, MapActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }

}
