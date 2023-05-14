package com.example.vamsismusicplayer

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.vamsismusicplayer.adapters.MusicAdapter
import com.example.vamsismusicplayer.dataClass.Music
import com.example.vamsismusicplayer.dataClass.exitApplication
import com.example.vamsismusicplayer.dataClass.setDialogBtnBackground
import com.example.vamsismusicplayer.databinding.ActivityMainBinding
import com.example.vamsismusicplayer.navActivities.AboutActivity
import com.example.vamsismusicplayer.navActivities.SettingsActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import kotlin.system.exitProcess

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var permissionRequested = false
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var recyclerView: RecyclerView
    private lateinit var mAdapter: MusicAdapter

    companion object{
        lateinit var songList : ArrayList<Music>
        lateinit var musicListSearch : ArrayList<Music>
        var search: Boolean = false
        var themeIndex: Int = 0
        val currentTheme = arrayOf(R.style.coolPink, R.style.coolBlue, R.style.coolPurple, R.style.coolGreen, R.style.coolBlack)
        val currentThemeNav = arrayOf(R.style.coolPinkNav, R.style.coolBlueNav, R.style.coolPurpleNav, R.style.coolGreenNav,
            R.style.coolBlackNav)
        //val currentGradient = arrayOf(R.drawable.gradient_pink, R.drawable.gradient_blue, R.drawable.gradient_purple, R.drawable.gradient_green,
        //   R.drawable.gradient_black)
        var sortOrder: Int = 0
        val sortingList = arrayOf(MediaStore.Audio.Media.DATE_ADDED + " DESC", MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.SIZE + " DESC")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val themeEditor = getSharedPreferences("THEMES", MODE_PRIVATE)
        themeIndex = themeEditor.getInt("themeIndex", 0)
        setTheme(currentThemeNav[themeIndex])
        setContentView(binding.root)
        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        if(requestRuntimePermission()){
            initializeLayout()
        }

        binding.favButton.setOnClickListener{
            startActivity(Intent(this, Favorites::class.java))
        }
        binding.playlistButton.setOnClickListener{
            startActivity(Intent(this, Playlist::class.java))
        }
        binding.shuffleButton.setOnClickListener{
            val intent = Intent(this, MusicPlayer::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "MainActivity")
            startActivity(intent)
        }

        binding.naView.setNavigationItemSelectedListener{
            when(it.itemId){
                R.id.item1 -> startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                R.id.item2 -> startActivity(Intent(this@MainActivity, AboutActivity::class.java))
                R.id.item3 -> {
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setTitle("Exit")
                        .setMessage("Do you want to close app?")
                        .setPositiveButton("Yes"){ _, _ ->
                            exitProcess(1)
                        }
                        .setNegativeButton("No"){dialog, _ ->
                            dialog.dismiss()
                        }
                    val customDialog = builder.create()
                    customDialog.show()

                    setDialogBtnBackground(this, customDialog)
                }
            }
            true
            }

        }

    private fun initializeLayout() {

        recyclerView = binding.recyclerView
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(15)
        recyclerView.layoutManager = LinearLayoutManager(this)
        songList = getAllAudio()
        mAdapter = MusicAdapter(this, songList)
        recyclerView.adapter = mAdapter
        binding.results.text = "${mAdapter.itemCount} Results"

        binding.refreshLayout.setOnRefreshListener {
            songList = getAllAudio()
            mAdapter.updateMusicList(songList)
            binding.results.text = "${mAdapter.itemCount} Results"
            binding.refreshLayout.isRefreshing = false
        }
    }

    private fun getAllAudio(): ArrayList<Music> {
        val tempList = ArrayList<Music>()
        val selection = MediaStore.Audio.Media.IS_MUSIC +  " != 0"
        val projection = arrayOf(
            MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID)
        val cursor = this.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection,selection,null,
            sortingList[sortOrder], null)
        if(cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val titleC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE))
                            ?: "Unknown"
                    val idC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                        ?: "Unknown"
                    val albumC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM))
                            ?: "Unknown"
                    val artistC =
                        cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST))
                            ?: "Unknown"
                    val pathC = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                    val durationC =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                    val albumIdC =
                        cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                            .toString()
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artUriC = Uri.withAppendedPath(uri, albumIdC).toString()
                    val music = Music(
                        id = idC,
                        title = titleC,
                        album = albumC,
                        artist = artistC,
                        path = pathC,
                        duration = durationC,
                        artUri = artUriC
                    )
                    val file = File(music.path)
                    if (file.exists())
                        tempList.add(music)
                } while (cursor.moveToNext())
                cursor.close()
            }
        }
        return tempList
    }


    override fun onStart() {
        super.onStart()
        requestRuntimePermission()
    }

    private fun requestRuntimePermission(): Boolean{
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            if (!permissionRequested) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    13
                )
                permissionRequested = true
                return false
            }

        }
        else {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            return true
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 13) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
                initializeLayout()
            } else {
                Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    13
                )
            }
            permissionRequested = false
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroy() {
        super.onDestroy()
        if(!MusicPlayer.isPlaying && MusicPlayer.musicService != null){
            exitApplication()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu, menu)
        //for setting gradient
        //findViewById<LinearLayout>(R.id.linearLayout1)?.setBackgroundResource(currentTheme[themeIndex])
        val searchView = menu?.findItem(R.id.searchView)?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                musicListSearch = ArrayList()
                if(newText != null){
                    val userInput = newText.lowercase()
                    for (song in songList)
                        if(song.title.lowercase().contains(userInput))
                            musicListSearch.add(song)
                    search = true
                    mAdapter.updateMusicList(searchList = musicListSearch)
                }
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

}
