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
import kotlinx.android.synthetic.main.content_main.*
import java.time.LocalDate //api26++ no local date but got date test bonus feature





//, OnMapReadyCallback,
//LocationEngineListener, PermissionsListener

class MainActivity : AppCompatActivity(){

    private val tag = "MainActivity"
    private var downloadDate = ""
    // Format: YYYY/MM/DD
    private val preferencesFile = "MyPrefsFile"
    // for storing preferences





    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setSupportActionBar(toolbar)

        fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
            val intent = Intent(this, MapActivity::class.java)
            startActivity(intent)
        }


        //11.59-12am transition and download new map
        val date = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("yyyy/MM/dd")
        val currentDate = dateFormat.format(date)
        Log.d(tag,"[onStart] currentDate is $currentDate")

        // Restore preferences
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        // use ”” as the default value (this might be the first time the app is run)
        downloadDate = settings.getString("lastDownloadDate","")
        // Write a message to ”logcat” (for debugging purposes)
        //downloadDate = "2018/10/07"  //test to force download map
        Log.d(tag,"[onStart] Recalled lastDownloadDate is $downloadDate")

        //file that stores the map
        if (currentDate!= downloadDate){
            //if dates are diff then download map from server, write geojson to device in onPostExecute,update downloadDate value
            val asyncDownload = DownloadFileTask(DownloadCompleteRunner)
            asyncDownload.execute("http://homepages.inf.ed.ac.uk/stg/coinz/$currentDate/coinzmap.geojson")
            downloadDate = currentDate


        }

/**
        val filename = "myfile1"
        val fileContents = "Hello world!"
        applicationContext.openFileOutput(filename, Context.MODE_PRIVATE).use {
        it.write(fileContents.toByteArray())
        }
         **/

    }

    @SuppressWarnings("MissingPermission")
    override fun onStart()
    {
        super.onStart()




    }

    override fun onResume()
    {
        super.onResume()
    }


    override fun onPause()
    {
        super.onPause()
        Log.d(tag,"[onPause] Storing lastDownloadDate of $downloadDate")
        // All objects are from android.context.Context
        val settings = getSharedPreferences(preferencesFile, Context.MODE_PRIVATE)
        // We need an Editor object to make preference changes.
        val editor = settings.edit()
        editor.putString("lastDownloadDate", downloadDate)
        // Apply the edits!
        editor.apply()
    }


    override fun onStop()
    {
        super.onStop()
        //if device date - "bonus" date stored in cloud != 1 OR streak = 7 && bonus received, then restart streak, reset streak
        // bonus received:int"increases during first write", streak:int,streak == #getbonus,then bonus received
        // streak is getbonus?

/**
        val dtf = SimpleDateFormat("yyyy/MM/dd")
        val localDate = LocalDate.now()
        val downloadDate= dtf.format(localDate)
        //System.out.println("WWWWWWW" + dtf.format(localDate))
**/



    }

    override fun onDestroy() {
        super.onDestroy()

    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }
















    object DownloadCompleteRunner : DownloadCompleteListener {
        var result : String? = null
        override fun downloadComplete(result: String) {
            this.result = result
        }

    }
    class DownloadFileTask(private val caller : DownloadCompleteListener) : AsyncTask<String, Void, String>() {
        override fun doInBackground(vararg urls: String): String =
            try {
                loadFileFromNetwork(urls[0])
            } catch (e: IOException) {
                "Unable to load content. Check your network connection"
            }


        private fun loadFileFromNetwork(urlString: String): String {
            val stream: InputStream = downloadUrl(urlString)
            // Read input from stream, build result as a string
            val s = Scanner(stream).useDelimiter("\\A")
            val result = if (s.hasNext()) s.next() else ""
            s.close()
            return result
        }//scanner copied from https://stackoverflow.com/questions/309424/how-to-read-convert-an-inputstream-into-a-string-in-java



        // Given a string representation of a URL, sets up a connection and gets an input stream.
        @Throws(IOException::class)
        private fun downloadUrl(urlString: String): InputStream {
            val url = URL(urlString)
            val conn = url.openConnection() as HttpURLConnection
            // Also available: HttpsURLConnection
            conn.readTimeout = 10000 // milliseconds
            conn.connectTimeout = 15000 // milliseconds
            conn.requestMethod = "GET"
            conn.doInput = true
            conn.connect() // Starts the query
            return conn.inputStream
        }
        override fun onPostExecute(result: String) {
            super.onPostExecute(result)
            caller.downloadComplete(result)
            val file = File("/data/data/com.example.user.coinz/files","coinzmap.geojson")
            file.writeText(DownloadCompleteRunner.result + "")
            //new day new wallet
            val file2 = File("/data/data/com.example.user.coinz/files","wallet.txt")
            file2.writeText("")
            println(file2.readText())

        }
    } // end class DownloadFileTask







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
