package com.example.answerandquestion

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.recyclerview.widget.GridLayoutManager
import com.example.answerandquestion.adapter.UserAdapter
import com.example.answerandquestion.databinding.ActivityMainBinding
import com.example.answerandquestion.databinding.ActivityOTPActivityBinding
import com.example.answerandquestion.model.User
import com.example.answerandquestion.utils.toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    var dialog: ProgressDialog? = null
    var database: FirebaseDatabase? = null
    var users: ArrayList<User>? = null
    var userAdapter: UserAdapter? = null
    var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        /*
         */
        dialog = ProgressDialog(this)
        dialog!!.setMessage("Uploading Image...")
        dialog!!.setCancelable(false)
        /*
         */
        database = FirebaseDatabase.getInstance()
        users = ArrayList<User>()
        userAdapter = UserAdapter(this, users!!)
        val layoutManager = GridLayoutManager(this, 2)
        binding.mRec.layoutManager = layoutManager
        /*
         */
        database!!.reference.child("users")
            .child(FirebaseAuth.getInstance().uid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    user = snapshot.getValue(User::class.java)
                }

                override fun onCancelled(error: DatabaseError) {
                    toast(error.message)
                }

            })
        /*
         */
        binding.mRec.adapter = userAdapter
        database!!.reference.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                users!!.clear()
                for (snapshot in snapshot.children) {
                    val user: User? = snapshot.getValue(User::class.java)
                    if (user!!.uid.equals(FirebaseAuth.getInstance().uid)) users!!.add(user)
                    Log.d("Foto", users.toString())
                }
                userAdapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                toast(error.message)
            }

        })
    }

    override fun onPostResume() {
        super.onPostResume()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence")
            .child(currentId!!).setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("presence")
            .child(currentId!!).setValue("offline")
    }
}