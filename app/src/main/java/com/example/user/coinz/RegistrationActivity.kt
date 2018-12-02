package com.example.user.coinz

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.util.Patterns.EMAIL_ADDRESS
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_registration.*


class RegistrationActivity : AppCompatActivity() {

    private  val tag = "RegistrationActivity"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        register_button_register.setOnClickListener {
            performRegistration()
        }

        already_have_account_text_view.setOnClickListener {
            Log.d(tag, "go back to main activity")

            // launch the login activity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStop() {
        super.onStop()
        finish()
    }



    private fun performRegistration() {
        val email = email_edittext_register.text.toString()
        val password = password_edittext_register.text.toString()

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
            return
        }

        if(!EMAIL_ADDRESS.matcher(email).matches()){
            Toast.makeText(this, "email invalid", Toast.LENGTH_SHORT).show()
            return
        }

        if(!password.matches(Regex("[A-Za-z0-9]+"))){
            Toast.makeText(this, "password must be alphanumeric", Toast.LENGTH_SHORT).show()
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



        Log.d(tag, "Email is: $email")
        Log.d(tag, "Password: $password")

        // Firebase Authentication to create a user with email and password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                //show successful registration message, add username to firestore, then switch to login activity
                Toast.makeText(this, "createUserWithEmail:onComplete:" + it.isSuccessful, Toast.LENGTH_SHORT).show()
                Log.d(tag, "Successfully created user with uid: ${it.result?.user?.uid}")

                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Authentication failed." + it.exception, Toast.LENGTH_LONG).show()
            }

        }

    }


}
