package com.lapperapp.laper.ui.NewHome

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.lapperapp.laper.MainActivity
import com.lapperapp.laper.PhotoActivity
import com.lapperapp.laper.R
import com.lapperapp.laper.service.PushNotification
import java.net.URL

class SendRequestActivity : AppCompatActivity() {
    var db = Firebase.firestore
    var pushRef = db.collection("requests")
    var userRef = db.collection("users")
    var techRef = db.collection("tech")
    var auth = FirebaseAuth.getInstance()

    private lateinit var ps: TextView
    private lateinit var tags: TextView
    private lateinit var image: ImageView
    private lateinit var send: TextView

    private lateinit var toolbar: Toolbar
    private lateinit var psVale: String
    private lateinit var imageUri: String
    private lateinit var category: List<SelectCategorymodel>

    private lateinit var askBtn: TextView
    private lateinit var progress:ProgressBar

    @SuppressLint("MissingInflatedId", "CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send_request)
        toolbar = findViewById(R.id.send_request_toolbar)
        askBtn = findViewById(R.id.ask_btn_send_request)
        progress = findViewById(R.id.progress_send_request)

        setSupportActionBar(toolbar)
        var actionBar = supportActionBar
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.ic_baseline_arrow_back_24)
        }

        ps = findViewById(R.id.ps_send_request)
        tags = findViewById(R.id.category_send_request)
        image = findViewById(R.id.image_send_request)
        send = findViewById(R.id.ask_btn_send_request)

        psVale = intent.getStringExtra("ps_value").toString()
        imageUri = intent.getStringExtra("image_uri").toString()
        category = intent.getParcelableArrayListExtra<SelectCategorymodel>("tags")!!

        ps.text = psVale
        var tag = ""
        for (cat in category) {
            tag += "#" + cat.title + " "
        }
        tags.text = tag

        image.setImageURI(Uri.parse(imageUri))

        image.setOnClickListener { v ->
            val pintent = Intent(baseContext, PhotoActivity::class.java)
            pintent.putExtra("image_uri", imageUri)
            startActivity(pintent)
        }

        askBtn.setOnClickListener { v ->
            if (!psVale.trim().isEmpty()) {
                progress.visibility = View.VISIBLE
                if(imageUri.trim().isEmpty()){
                    pushRequest("")
                }
                else{
                    uploadImage()
                }
            } else {
                Toast.makeText(
                    baseContext,
                    "Can not send Empty problem statement request",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }


    }

    @SuppressLint("Range")
    fun getFileNameFromUri(context: Context, uri: Uri): String {
        var fileName = ""
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.let {
            if (it.moveToFirst()) {
                fileName = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
            }
            it.close()
        }
        return fileName
    }

    fun uploadImage(){
        val sd = getFileNameFromUri(applicationContext, Uri.parse(imageUri)!!)
        var storageRef = Firebase.storage.reference;
        // Upload Task with upload to directory 'file'
        // and name of the file remains same
        val uploadTask = storageRef.child("file/$sd").putFile(Uri.parse(imageUri))

        // On success, download the file URL and display it
        uploadTask.addOnSuccessListener {
            // using glide library to display the image
            storageRef.child("upload/$sd").downloadUrl.addOnSuccessListener {
                pushRequest(it.toString())
                Log.e("Firebase", "download passed"+it)
            }.addOnFailureListener {
                Log.e("Firebase", "Failed in downloading")
                progress.visibility = View.GONE
            }
        }.addOnFailureListener {
            Log.e("Firebase", "Image Upload fail")
            progress.visibility = View.GONE
        }
    }


    fun pushRequest(url: String) {
        val retime = System.currentTimeMillis()
        val pst = psVale
        val reqHash = hashMapOf(
            "clientId" to auth.uid,
            "requestTime" to retime,
            "problemStatement" to pst,
            "accepted" to false,
            "imageURL" to url,
            "expertId" to "all",
            "problemSolved" to false,
            "requiredTech" to category.map { it.id }.toMutableList()
        )

        pushRef.document(retime.toString()).set(reqHash).addOnCompleteListener {
            progress.visibility = View.GONE
            val ps = PushNotification(baseContext)
            ps.sendNotification("experts", "New request", pst, "0")
            Toast.makeText(baseContext, "Request Sent!", Toast.LENGTH_SHORT).show()
            askBtn.isEnabled = false
            val mrintent = Intent(baseContext,MainActivity::class.java)
            startActivity(mrintent)
            finish()
        }


    }

}