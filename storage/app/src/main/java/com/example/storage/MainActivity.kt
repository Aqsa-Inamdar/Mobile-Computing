package com.example.storage

import android.Manifest
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.icu.text.Transliterator.Position
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.switchmaterial.SwitchMaterial


class MainActivity : AppCompatActivity() {
    // Define a permission request code constant
    private val PERMISSION_REQUEST_CODE = 100

    var listsongs = ArrayList<Songlist>()
    var mp: MediaPlayer? = null
    var adapter: MySongsAdapter?  = null

    // SharedPreferences
    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "theme_prefs"
    private val THEME_KEY = "is_dark_mode"

    override fun onCreate(savedInstanceState: Bundle?) {

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val isDarkMode = sharedPreferences.getBoolean(THEME_KEY, false)
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Initialize the Switch and set its state
        val themeSwitch: SwitchMaterial = findViewById(R.id.switch1)
        themeSwitch.isChecked = isDarkMode

        // Set listener on the switch
        themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                saveThemePreference(true)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                saveThemePreference(false)
            }
            // Recreate the activity to apply the theme change
            recreate()
        }

        // Initialize the ListView
        val songListView: ListView = findViewById(R.id.listview)
        // For Android 13 and higher, use the READ_MEDIA_AUDIO permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Request the permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_MEDIA_AUDIO),
                    PERMISSION_REQUEST_CODE
                )
            }
        } else {
            // For Android versions lower than 13, use READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                // Request the permission
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSION_REQUEST_CODE
                )
            }
        }
        loadsong()


        adapter = MySongsAdapter(this, listsongs)
        songListView.adapter = adapter



    }

    private fun saveThemePreference(isDarkMode: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(THEME_KEY, isDarkMode)
        editor.apply()
    }

    inner class MySongsAdapter(private val context: Context, private var myListSong: ArrayList<Songlist>) : BaseAdapter() {


        // ViewHolder class to hold the views
        private inner class ViewHolder(view: View) {
            val songTitleTextView: TextView = view.findViewById(R.id.textView) // Adjust ID as necessary
            val playbuttomView: Button = view.findViewById(R.id.button)
        }

        override fun getCount(): Int {
            return myListSong.size
        }

        override fun getItem(p0: Int): Any {
            return myListSong[p0]
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            var viewHolder: ViewHolder // Declare viewHolder here

            // Check if the convertView is null and create a new ViewHolder if needed
            val myView: View = convertView ?: LayoutInflater.from(context).inflate(R.layout.mylayout, parent, false).also {
                // Create a new ViewHolder and set it to the view's tag
                viewHolder = ViewHolder(it)
                it.tag = viewHolder
            }

            // If convertView is not null, retrieve the existing ViewHolder
            if (convertView != null) {
                viewHolder = convertView.tag as ViewHolder
            } else {
                // Ensure viewHolder is initialized in case of a new view
                viewHolder = ViewHolder(myView)
            }

            // Get the data item for this position
            val song: Songlist = myListSong[position]

            // Bind the song title to the TextView in the ViewHolder
            viewHolder.songTitleTextView.text = song.Title // Assuming 'Title' is a property of the songlist class
            viewHolder.playbuttomView.setOnClickListener{
                if(viewHolder.playbuttomView.text == "STOP"){
                    mp!!.stop()
                    viewHolder.playbuttomView.text = "PLAY"
                } else {
                    mp = MediaPlayer()
                    try {
                        mp!!.setDataSource(song.songURL)
                        mp!!.prepare()
                        mp!!.start()
                        viewHolder.playbuttomView.text = "STOP"

                        mp!!.setOnCompletionListener {
                            viewHolder.playbuttomView.text = "PLAY"
                        }
                    }catch (e:Exception){}
                }
                }


            // Return the completed view to render on screen
            return myView
        }

    }
        private fun loadsong() {
            val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            val selection = MediaStore.Audio.Media.IS_MUSIC + "!=0"
            val rs = contentResolver.query(uri, null, selection, null, null)
            if(rs!=null){
                while (rs.moveToNext()){
                    val title = rs.getString(rs.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                    val url = rs.getString(rs.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))

                    listsongs.add(Songlist(title, url))
                }
            }


        }

}
