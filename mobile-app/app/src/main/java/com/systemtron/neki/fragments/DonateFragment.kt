package com.systemtron.neki.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.systemtron.neki.R
import com.systemtron.neki.adapter.CategoryAdapter
import com.systemtron.neki.adapter.NGOAdapter
import com.systemtron.neki.modelClass.NGO
import com.systemtron.neki.utils.Constants
import com.systemtron.neki.utils.Tags
import kotlinx.android.synthetic.main.fragment_donate.*
import kotlinx.android.synthetic.main.fragment_donate.view.*

class DonateFragment : Fragment() {

    private val currentUser by lazy {
        Firebase.auth.currentUser
    }

    private val db by lazy {
        Firebase.firestore
    }

    private var name: String = ""

    private val listOfNGOs = ArrayList<NGO>()

    private val arrayOfCategory = arrayListOf(
        "Clothing",
        "Electronic Gadgets",
        "Essentials",
        "Food",
        "Furniture",
        "Machinery",
        "Medical Equipments",
        "Monetary Funds",
        "Sports Equipments",
        "Stationary",
        "Toys"
    )

    private val sharedPreferences by lazy {
        requireContext().getSharedPreferences(Constants.welcomeTagSS, Context.MODE_PRIVATE)
    }

    private val editor by lazy {
        sharedPreferences.edit()
    }

    @SuppressLint("SetTextI18n")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val inflatedView = inflater.inflate(R.layout.fragment_donate, container, false)
        val colorArray = arrayOf(
            Color.argb(255, 244, 67, 45),
            Color.argb(255, 63, 81, 181),
            Color.argb(255, 33, 150, 243),
            Color.argb(255, 156, 39, 176),
            Color.argb(255, 76, 175, 80),
            Color.argb(255, 255, 193, 7),
            Color.argb(255, 121, 85, 72),
            Color.argb(255, 255, 152, 0),
            Color.argb(255, 139, 195, 74),
            Color.argb(255, 0, 188, 212),
            Color.argb(255, 233, 67, 45),
        )
        inflatedView.rvCategories.apply {
            layoutManager =
                GridLayoutManager(requireContext(), 2, LinearLayoutManager.HORIZONTAL, false)
            adapter = CategoryAdapter(arrayOfCategory, colorArray, requireContext())
        }

        val welcomeInt = sharedPreferences?.getInt(Constants.sharedPreferencesWelcome, -1)
        Log.d(Tags.ishaanTag, "Welcome Int: $welcomeInt")
        getNameFromFirestore(welcomeInt, inflatedView)

        addNGO(inflatedView)
        return inflatedView
    }

    private fun addNGO(inflatedView: View) {
        db.collection("ngos")
            .limit(10)
            .get()
            .addOnSuccessListener {
                for (value in it) {
                    val ngo = value.toObject(NGO::class.java)
                    ngo.emailId = value.id
                    ngo.listCategory = ngo.categories.split(",")
                    Log.d(Tags.ishaanTag, "${ngo.emailId} ${ngo.listCategory}")
                    listOfNGOs.add(ngo)
                }
                inflatedView.rvSuggestions.apply {
                    layoutManager =
                        LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
                    adapter = NGOAdapter(listOfNGOs, requireContext())
                }
            }
    }

    @SuppressLint("SetTextI18n")
    private fun getNameFromFirestore(welcomeInt: Int?, inflatedView: View) {
        db.collection("users")
            .document(currentUser!!.email.toString())
            .get()
            .addOnSuccessListener {
                if (it.exists()) {
                    val receivedName = it.getString(Constants.keyName).toString()
                    val url = it.getString(Constants.keyPP).toString()
                    Log.d(Tags.ishaanTag, "$receivedName $url")
                    editor.putString(Constants.userName, receivedName)
                    editor.putString(Constants.userPP, url)
                    editor.commit()
                    val nameList = receivedName.split(" ")
                    name = nameList[0]
                    if (welcomeInt == 0) {
                        inflatedView.tvWelcomeOrHello.text = "Hello, $name!"
                    } else if (welcomeInt == 1) {
                        inflatedView.tvWelcomeOrHello.text = "Welcome Back, $name!"
                    }
                }
            }.addOnFailureListener {
                Log.d(Tags.ishaanTag, "Name Failed, ${it.toString()}")
            }
    }
}