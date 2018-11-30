package com.example.user.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_registration.*

import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseUser;



//USERNAME not processed
class RegistrationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        register_button_register.setOnClickListener {
            performRegistration()
        }

        already_have_account_text_view.setOnClickListener {
            Log.d("MainActivity", "Try to show login activity")

            // launch the login activity somehow
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStop() {
        super.onStop()
        finish()
    }


    private fun performRegistration() {
        val username = username_edittext_register.text.toString()
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
            return
        }
        if (password.length < 8) {
            Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d("MainActivity", "Email is: $email")
        Log.d("MainActivity", "Password: $password")

        // Firebase Authentication to create a user with email and password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            Toast.makeText(this, "createUserWithEmail:onComplete:" + it.isSuccessful, Toast.LENGTH_SHORT).show()
            // If sign in fails, display a message to the user. If sign in succeeds
            // the auth state listener will be notified and logic to handle the
            // signed in user can be handled in the listener.
            if (!it.isSuccessful) {
                Toast.makeText(this, "Authentication failed." + it.exception, Toast.LENGTH_LONG).show()
            } else {
                Log.d("Main", "Successfully created user with uid: ${it.result?.user?.uid}")
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            }
            /**
            if (!it.isSuccessful) return@addOnCompleteListener

            // else if successful
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)
            Log.d("Main", "Successfully created user with uid: ${it.result?.user?.uid}")
            }
            .addOnFailureListener{
            Log.d("Main", "Failed to create user: ${it.message}")
            Toast.makeText(this, "Failed to create user: ${it.message}", Toast.LENGTH_SHORT).show()
            }**/
        }

    }


}
