package com.dude36.pftracker

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.navigation.ui.*
import androidx.room.Room
import com.dude36.pftracker.Data.toJSON
import com.dude36.pftracker.ui.entries.EntriesFragment
import com.dude36.pftracker.ui.graphs.GraphsFragment
import com.dude36.pftracker.ui.home.HomeFragment
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception

class MainActivity : AppCompatActivity() {
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var drawerLayout: DrawerLayout
    private val myData: Data = Data

    private val WRITE_PERMISSIONS_CODE = 10
    private val READ_PERMISSIONS_CODE = 11

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Uses Data object for all data needs
        myData.start(this)
        val db = Room.databaseBuilder(this, AppDatabase::class.java, "PeakFlow").build()
        val dao = db.userDao()
        GlobalScope.async {
            val all_entries = dao.getAll()
            println("This is all the entries")
            println(all_entries)
            println()
        }

        // Floating Action Button to add Entries
        val fab: FloatingActionButton = findViewById(R.id.fab)
        fab.setOnClickListener { addNewEntry(it) }

        // Navigation Bar
        val navView: NavigationView = findViewById(R.id.nav_view)
        drawerLayout = findViewById(R.id.drawer_layout)
        val toggle = ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Don't revert if rotated
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment, HomeFragment()).commit()
            navView.setCheckedItem(R.id.nav_home)
        }
    }

    fun addNewEntry(view: View) {
        val intent = Intent(this, AddEntry::class.java).apply {  }
        startActivity(intent)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    fun navigateToFragment(item: MenuItem) {
        when (item.itemId) {
            R.id.nav_home -> {
                supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment, HomeFragment()).commit()
                this.findViewById<NavigationView>(R.id.nav_view).setCheckedItem(R.id.nav_home)
            }
            R.id.nav_entry -> {
                supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment, EntriesFragment()).commit()
                this.findViewById<NavigationView>(R.id.nav_view).setCheckedItem(R.id.nav_entry)
            }
            R.id.nav_graphs -> {
                supportFragmentManager.beginTransaction().replace(R.id.nav_host_fragment, GraphsFragment()).commit()
                this.findViewById<NavigationView>(R.id.nav_view).setCheckedItem(R.id.nav_graphs)
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    fun settings(item: MenuItem) {
        when (item.itemId) {
            R.id.action_contact -> {
                // Have them email me for now
                val newIntent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:joshuahigham@gmail.com"))
                startActivity(newIntent)
            }
            R.id.action_settings -> {
                // Open up settings Activity
                Toast.makeText(this, "Under Construction", Toast.LENGTH_LONG).show()
            }
            R.id.action_import -> {
                // Check permissions
                val readPermission = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                if (readPermission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        WRITE_PERMISSIONS_CODE
                    )
                }
                val readPermission2 = ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                if (readPermission2 != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Enable write operations to export", Toast.LENGTH_LONG)
                        .show()
                    return
                }

                // for Android Q and up
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Open up file picker
                    val intent = Intent().setType("*/*").setAction(Intent.ACTION_GET_CONTENT)
                    startActivityForResult(Intent.createChooser(intent, "Select a file"), 111)
                }
            }
            R.id.action_export -> {
                // Check permissions
                val writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                if (writePermission != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), WRITE_PERMISSIONS_CODE)
                }
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Enable write operations to export", Toast.LENGTH_LONG).show()
                    return
                }

                // For Android Q and up
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val resolver = this.contentResolver

                    val contentValues = ContentValues().apply {
                        // Update all the internal data of the file
                        put(MediaStore.MediaColumns.DISPLAY_NAME, "PF_Tracking_Data.json")
                        put(MediaStore.MediaColumns.TITLE, "PF Tracking Data")
                        put(MediaStore.MediaColumns.MIME_TYPE, "application/json")
                        put(MediaStore.MediaColumns.RELATIVE_PATH, "Download")
                    }
                    val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                    if (uri != null) {
                        resolver.openOutputStream(uri).use { output ->
                            output?.write("${toJSON()}".toByteArray())
                            Toast.makeText(this, "Export Successful!", Toast.LENGTH_LONG).show()
                            output?.close()
                        }
                    } else {
                        Toast.makeText(this, "Unable to export file", Toast.LENGTH_LONG).show()
                    }
                } else {
                    // Older phone versions
                    val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                    val exportFile = File("$path/PF_Tracking_Data.json")

                    // Write file
                    try {
                        val output = FileOutputStream(exportFile)
                        output.write("${toJSON()}".toByteArray())
                        output.close()
                    } catch (e: Exception) {
                        Toast.makeText(this, "Unable to export file", Toast.LENGTH_LONG).show()
                        println(e.toString())
                    }
                }
            }
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            WRITE_PERMISSIONS_CODE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    println("Permissions Granted")
                } else {
                    println("Permissions Denied")
                }
            }
            READ_PERMISSIONS_CODE -> {
                if (grantResults.isEmpty() || grantResults[1] != PackageManager.PERMISSION_GRANTED) {
                    println("Permissions Granted")
                } else {
                    println("Permissions Denied")
                }
            }
            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == 111 && resultCode == RESULT_OK) {
            //The uri with the location of the file
            val importUri = data?.data
            try {
                val resolver = this.contentResolver
                val fileIn = resolver.openInputStream(importUri!!)
                if (fileIn != null) {
                    val inputData = fileIn.readBytes()
                    val result = Data.fromJSON(String(inputData, Charsets.UTF_8))
                    if (result) {
                        Toast.makeText(this, "Import Successful", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this, "Invalid file format", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(this, "Unable to read file", Toast.LENGTH_LONG).show()
                println(e.toString())
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}
