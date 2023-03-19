package com.lapperapp.laper.ui.NewHome

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.denzcoskun.imageslider.models.SlideModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.lapperapp.laper.BuildConfig
import com.lapperapp.laper.Categories.*
import com.lapperapp.laper.R
import de.hdodenhof.circleimageview.CircleImageView

class NewHomeFragment : Fragment() {

    var db = Firebase.firestore
    private lateinit var firebaseAuth: FirebaseAuth
    var userRef = db.collection("users")
    var techRef = db.collection("tech")
    val database = Firebase.database
    val tokenRef = database.getReference("token")
    var data = ArrayList<CategoryModel>()
    private lateinit var adapter: CategoryAdapter
    //    private lateinit var imageSlider: ImageSlider
    private lateinit var userImage2: CircleImageView
    private lateinit var developersCard:CardView
    private lateinit var cameraCard:CardView
    private lateinit var typeCard:CardView
    private lateinit var exploreCard:CardView

    var imagePicker: ImageView? = null

    @SuppressLint("NotifyDataSetChanged", "MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.new_home_fragment, container, false)

        firebaseAuth = FirebaseAuth.getInstance()

        developersCard = view.findViewById(R.id.home_developers)
        cameraCard = view.findViewById(R.id.home_take_photo)
        typeCard = view.findViewById(R.id.home_type_question)
        exploreCard = view.findViewById(R.id.home_explore)

        imagePicker = view.findViewById(R.id.image_picker)

        exploreCard.setOnClickListener { v->
            val explore = Intent(context,AllCategoryActivity::class.java)
            startActivity(explore)
        }

        cameraCard.setOnClickListener {//            ImagePicker.with(this).cameraOnly().crop().maxResultSize(400, 400).start()
            com.github.dhaval2404.imagepicker.ImagePicker.with(this)
                .cameraOnly().crop().maxResultSize(400,400).start()
        }

        typeCard.setOnClickListener { v->
            val explore = Intent(context,TypeQuestActivity::class.java)
            startActivity(explore)
        }

        developersCard.setOnClickListener { v->
            val explore = Intent(context,ViewAllExpertsActivity::class.java)
            startActivity(explore)
        }

        setToken()

        return view

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK && requestCode== com.github.dhaval2404.imagepicker.ImagePicker.REQUEST_CODE) {
//            imagePicker?.setImageURI(data?.data)
            val typeIntent = Intent(context,TypeQuestActivity::class.java)
            typeIntent.putExtra("image_uri",data?.data.toString())
            startActivity(typeIntent)
        }
    }


    fun setToken() {
        FirebaseMessaging.getInstance().token.addOnSuccessListener { s ->
            val tokenhash = hashMapOf(
                "token" to s
            )
            tokenRef.child(firebaseAuth.uid.toString()).setValue(tokenhash)

            val hashMap = hashMapOf(
                "lastActive" to System.currentTimeMillis(),
                "token" to s,
                "versionName" to BuildConfig.VERSION_NAME,
                "versionCode" to BuildConfig.VERSION_CODE
            )
            userRef.document(firebaseAuth.uid.toString())
                .update(hashMap as Map<String, Any>)
                .addOnSuccessListener {
                }
                .addOnFailureListener { exc ->
                    run {
                    }
                }
        }

    }


    @SuppressLint("SetTextI18n")
    fun fetchUserDetail() {
        userRef.document(firebaseAuth.uid as String).get().addOnSuccessListener { documents ->
            if (documents.exists()) {
                val uImageUrl = documents.get("userImageUrl").toString()
                try {
                    Glide.with(this).load(uImageUrl).into(userImage2)
                } finally {

                }
            }
        }.addOnFailureListener { exception ->
            run {
                Toast.makeText(context, exception.message, Toast.LENGTH_SHORT)
                    .show()
            }
        }

    }


}