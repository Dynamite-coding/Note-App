package com.example.note77

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
        val prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)

        val isFirstTime = prefs.getBoolean("isFirstTime", true)
        val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val savedUsername = sharedPref.getString("username", null)

        // 🚀 Auto-skip MainActivity if not first time
        if (!isFirstTime) {
            // Auto-login if already logged in
            if (savedUsername != null) {
                val intent = Intent(this, NotesActivity::class.java)
                intent.putExtra("username", savedUsername)
                startActivity(intent)
            } else {
                startActivity(Intent(this, LoginActivity::class.java))
            }
            finish()
            return
        }

        // ✅ Show MainActivity only on first launch
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val started = findViewById<Button>(R.id.started)
        started.setOnClickListener {
            // Mark as not first time anymore
            prefs.edit().putBoolean("isFirstTime", false).apply()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}
