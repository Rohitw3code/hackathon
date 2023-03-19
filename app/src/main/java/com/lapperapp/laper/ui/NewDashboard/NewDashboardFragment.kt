package com.lapperapp.laper.ui.NewDashboard

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.lapperapp.laper.AllRequestsActivity
import com.lapperapp.laper.R
import com.lapperapp.laper.ui.NewDashboard.NewAvailableExpert.NewAvailableExpertAdapter
import com.lapperapp.laper.ui.NewDashboard.NewAvailableExpert.NewAvailableExpertModel
import com.lapperapp.laper.ui.NewDashboard.NewRequest.NewRequestAdapter
import com.lapperapp.laper.ui.NewDashboard.NewRequest.NewRequestSentModel


class NewDashboardFragment : Fragment() {
    private lateinit var reqRecyclerView: RecyclerView
    private lateinit var aeRecyclerView: RecyclerView
    var db = Firebase.firestore
    var userRef = db.collection("users")
    var expertsRef = db.collection("experts")
    var reqRef = db.collection("requests")
    var auth = FirebaseAuth.getInstance()
    private lateinit var allRequestsBtn: CardView
    private lateinit var reqSentModelModel: ArrayList<NewRequestSentModel>
    private lateinit var reqSentAdapter: NewRequestAdapter

    private lateinit var availableExpertModel: ArrayList<NewAvailableExpertModel>
    private lateinit var availableExpertAdapter: NewAvailableExpertAdapter
    private lateinit var uReqIds: ArrayList<String>
    private lateinit var uAvaExpertIds: ArrayList<String>

    @SuppressLint("NotifyDataSetChanged", "MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_new_dashboard, container, false)
        uReqIds = ArrayList()
        uAvaExpertIds = ArrayList()

        reqRecyclerView = view.findViewById(R.id.dashboard_new_request_recycler_view)
        reqRecyclerView.layoutManager = LinearLayoutManager(context)
        reqSentModelModel = ArrayList()
        reqSentAdapter = NewRequestAdapter(reqSentModelModel)
        reqRecyclerView.adapter = reqSentAdapter

        aeRecyclerView = view.findViewById(R.id.dashboard_available_expert_recycler_view)
        aeRecyclerView.layoutManager = LinearLayoutManager(context)
        availableExpertModel = ArrayList()
        availableExpertAdapter = NewAvailableExpertAdapter(availableExpertModel)
        aeRecyclerView.adapter = availableExpertAdapter
        availableExpertAdapter.notifyDataSetChanged()

        allRequestsBtn = view.findViewById(R.id.dash_all_requests)
        allRequestsBtn.setOnClickListener {
            val intent = Intent(context, AllRequestsActivity::class.java)
            startActivity(intent)
        }


        return view
    }

    override fun onStart() {
        super.onStart()
        fetchMyRequests()
        fetchAvailableExpert()
    }

    fun fetchAvailableExpert() {
        userRef.document(auth.uid.toString())
            .collection("availableExpert")
            .get()
            .addOnSuccessListener { docs ->
                for (doc in docs.documents) {
                    if (!uAvaExpertIds.contains(doc.id)) {
                        val reqId = doc.getString("requestId") as String
                        val acceptTime = doc.getLong("acceptedTime") as Long
                        val expertId = doc.getString("expertId") as String
                        expertsRef.document(expertId)
                            .get().addOnSuccessListener { edoc ->
                                val name = edoc.getString("username") as String
                                val imageUrl = edoc.getString("userImageUrl") as String
                                expertsRef.document(expertId).collection("requests")
                                    .document(reqId).get().addOnSuccessListener { doc ->
                                        if (doc.exists()) {
                                            val ps = doc.getString("problemStatement") as String
                                            availableExpertModel.add(
                                                NewAvailableExpertModel(
                                                    name,
                                                    imageUrl,
                                                    "",
                                                    acceptTime,
                                                    expertId,
                                                    reqId,
                                                    ps
                                                )
                                            )
                                            availableExpertAdapter.notifyDataSetChanged()
                                            uAvaExpertIds.add(doc.id)
                                        }
                                    }
                            }
                    }
                }
            }
    }

    fun fetchMyRequests() {
        reqRef.whereEqualTo("clientId", auth.uid).get().addOnSuccessListener { docs ->
            for (doc in docs.documents) {
                if (!uReqIds.contains(doc.id)) {
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
                        uReqIds.add(doc.id)
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
                                uReqIds.add(doc.id)
                                reqSentAdapter.notifyDataSetChanged()
                            }
                    }
                    reqSentAdapter.notifyDataSetChanged()
                }
            }
        }
    }

}