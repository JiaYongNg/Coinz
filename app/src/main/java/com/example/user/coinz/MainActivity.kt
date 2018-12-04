package com.example.user.coinz


import kotlinx.android.synthetic.main.activity_main.* //for fab,toolbar
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.os.AsyncTask
import android.util.Log



import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.util.Scanner
import java.util.Calendar
import java.text.DateFormat
import java.text.SimpleDateFormat
import android.content.Intent
import android.location.Location
import android.os.PersistableBundle
import com.google.firebase.auth.FirebaseAuth
import java.time.LocalDate //api26++ no local date but got date test bonus feature
import com.google.firebase.auth.FirebaseUser
import android.widget.Toast
import com.google.firebase.auth.AuthResult
import com.google.android.gms.tasks.Task
import android.support.annotation.NonNull
import com.google.android.gms.tasks.OnCompleteListener
import com.example.user.coinz.R.string.email









//, OnMapReadyCallback,
//LocationEngineListener, PermissionsListener

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
            Log.d(tag, "login successful")

            // launch the login activity somehow
            val intent = Intent(this, RegistrationActivity::class.java)
            startActivity(intent)

        }

    }

    @SuppressWarnings("MissingPermission")
    override fun onStart()
    {
        super.onStart()
        Log.d(tag,"MAIN START")
        // Check if user is signed in (non-null) and update UI accordingly.
        if (mAuth?.currentUser != null) {
            Log.d(tag,"already signed in")

            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)

            println("current user UID" + mAuth?.currentUser?.uid)
            finish()
        } else {
            Log.d(tag,"not signed in")

        }








    }

    override fun onResume()
    {
        super.onResume()
        Log.d(tag,"MAIN RESUME")
    }


    override fun onPause()
    {
        super.onPause()

        Log.d(tag, "MAIN PAUSE")

    }


    override fun onStop()
    {
        super.onStop()
        //if device date - "bonus" date stored in cloud != 1 OR streak = 7 && bonus received, then restart streak, reset streak
        // bonus received:int"increases during first write", streak:int,streak == #getbonus,then bonus received
        // streak is getbonus?
        Log.d(tag, "MAIN STOP")
        //storeDownloadDate()
/**
        val dtf = SimpleDateFormat("yyyy/MM/dd")
        val localDate = LocalDate.now()
        val downloadDate= dtf.format(localDate)
        //System.out.println("WWWWWWW" + dtf.format(localDate))
**/
        finish()


    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag,"MAIN DESTROY")

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
            // the auth state listener will be notified and logic to handle the
            // signed in user can be handled in the listener.
            if (!it.isSuccessful) {
                // there was an error
                if (password.length< 8) {
                    Toast.makeText(this, "password length too short", Toast.LENGTH_LONG).show()

                } else {
                    Toast.makeText(this, "auth failed", Toast.LENGTH_LONG).show()
                }
            } else {
                val intent = Intent(this, MapActivity::class.java)
                startActivity(intent)
                finish()
            }
        }
    }







    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }
}
