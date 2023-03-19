package com.lapperapp.laper

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lapperapp.laper.ui.NewDashboard.NewRequest.NewRequestAdapter
import com.lapperapp.laper.ui.NewDashboard.NewRequest.NewRequestSentModel

class AllRequestsActivity : AppCompatActivity() {
    private lateinit var reqRecyclerView: RecyclerView
    var db = Firebase.firestore
    var userRef = db.collection("users")
    var expertsRef = db.collection("experts")
    var reqRef = db.collection("requests")
    var auth = FirebaseAuth.getInstance()
    private lateinit var reqSentModelModel: ArrayList<NewRequestSentModel>
    private lateinit var reqSentAdapter: NewRequestAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_requests)

        reqRecyclerView = findViewById(R.id.all_requests_recycler_view)
        reqRecyclerView.layoutManager = LinearLayoutManager(baseContext)
        reqSentModelModel = ArrayList()
        reqSentAdapter = NewRequestAdapter(reqSentModelModel)
        reqRecyclerView.adapter = reqSentAdapter


        fetchMyRequests()

    }


    fun fetchMyRequests() {
        reqRef.whereEqualTo("clientId", auth.uid).get().addOnSuccessListener { docs ->
            for (doc in docs.documents) {
                val reqTime = doc.getLong("requestTime") as Long
                val problemStatement = doc.getString("problemStatement") as String
                val expertId = doc.getString("expertId") as String
                val requiredTech = doc.get("requiredTech") as ArrayList<String>
                if (expertId.equals("all")) {
                    reqSentModelModel.add(
                        NewRequestSentModel(
                            reqTime,
                            expertId,
                            doc.id,
                            "Laper Experts",
                            "",
                            problemStatement,
                            requiredTech
                        )
                    )
                } else {
                    expertsRef.document(expertId)
                        .get().addOnSuccessListener { doc1 ->
                            val expertName = doc1.getString("username").toString()
                            val expertImageUrl = doc1.getString("userImageUrl").toString()
                            reqSentModelModel.add(
                                NewRequestSentModel(
                                    reqTime,
                                    expertId,
                                    doc.id,
                                    expertName,
                                    expertImageUrl,
                                    problemStatement,
                                    requiredTech
                                )
                            )
                            reqSentAdapter.notifyDataSetChanged()
                        }
                }
                reqSentAdapter.notifyDataSetChanged()

            }
        }
    }


}