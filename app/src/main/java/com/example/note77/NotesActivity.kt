package com.example.note77

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import android.content.DialogInterface
import android.view.LayoutInflater
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.firebase.database.*

class NotesActivity : AppCompatActivity() {

    private lateinit var database: DatabaseReference
    private lateinit var noteList: MutableList<Note>
    private lateinit var adapter: NoteAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var fabAddNote: FloatingActionButton

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var menuButton: ImageButton

    private var username: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notes)

        username = intent.getStringExtra("username")
        if (username.isNullOrEmpty()) {
            Toast.makeText(this, "User not found!", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        // Firebase
        database = FirebaseDatabase.getInstance().getReference("notes")
        noteList = mutableListOf()

        // Views
        recyclerView = findViewById(R.id.recyclerViewNotes)
        fabAddNote = findViewById(R.id.fabAddNote)
        drawerLayout = findViewById(R.id.drawerLayout)
        navView = findViewById(R.id.navigationView)
        menuButton = findViewById(R.id.btnMenu)

        // Recycler setup
        adapter = NoteAdapter(noteList, ::editNote, ::deleteNote)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // Open drawer
        menuButton.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }

        // Handle drawer item clicks
        navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_logout -> {
                    // ✅ Clear saved login state
                    val sharedPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
                    sharedPref.edit().remove("username").apply()

                    Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()

                    // ✅ Go to LoginActivity and clear back stack
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }


        // Set username in header
        val headerView = navView.getHeaderView(0)
        val tvUsernameHeader = headerView.findViewById<TextView>(R.id.tvUsernameHeader)
        tvUsernameHeader.text = username

        // FAB add note
        fabAddNote.setOnClickListener { showAddNoteDialog() }

        listenToFirebase()
    }

    private fun listenToFirebase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                noteList.clear()
                for (noteSnap in snapshot.children) {
                    val note = noteSnap.getValue(Note::class.java)
                    if (note != null && note.username == username) {
                        noteList.add(note)
                    }
                }
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@NotesActivity, "Failed to load notes", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showAddNoteDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_note, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etContent = dialogView.findViewById<EditText>(R.id.etContent)

        AlertDialog.Builder(this)
            .setTitle("Add Note")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val id = database.push().key!!
                val note = Note(
                    id = id,
                    title = etTitle.text.toString(),
                    content = etContent.text.toString(),
                    username = username!!
                )
                database.child(id).setValue(note)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun editNote(note: Note) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_note, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etContent = dialogView.findViewById<EditText>(R.id.etContent)

        etTitle.setText(note.title)
        etContent.setText(note.content)

        AlertDialog.Builder(this)
            .setTitle("Edit Note")
            .setView(dialogView)
            .setPositiveButton("Update") { dialogInterface: DialogInterface, i: Int ->
                val updates = mapOf(
                    "title" to etTitle.text.toString(),
                    "content" to etContent.text.toString()
                )
                database.child(note.id).updateChildren(updates)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteNote(note: Note) {
        database.child(note.id).removeValue()
    }
}
