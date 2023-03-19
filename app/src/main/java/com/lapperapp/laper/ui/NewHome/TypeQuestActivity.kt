package com.lapperapp.laper.ui.NewHome

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.lapperapp.laper.R

class TypeQuestActivity : AppCompatActivity() {
    private lateinit var next:RelativeLayout
    private lateinit var toolbar: Toolbar
    private lateinit var problemStatement:EditText
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_type_quest)

        toolbar = findViewById(R.id.type_quest_toolbar)
        problemStatement = findViewById(R.id.addition_detail_type_quest)
        setSupportActionBar(toolbar)
        var actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        }

        var imageUri = intent.getStringExtra("image_uri")

        next = findViewById(R.id.type_quest_next_btn)
        next.setOnClickListener { v->
            val scIntent = Intent(baseContext,SelectCategoryActivity::class.java)
            scIntent.putExtra("ps_value",problemStatement.text.toString())
            scIntent.putExtra("image_uri",imageUri)
            startActivity(scIntent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        finish()
        return true
    }

}