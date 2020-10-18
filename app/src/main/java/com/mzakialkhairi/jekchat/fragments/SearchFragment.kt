package com.mzakialkhairi.jekchat.fragments

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.mzakialkhairi.jekchat.R
import com.mzakialkhairi.jekchat.adapter.UserAdapter
import com.mzakialkhairi.jekchat.model.Users
import kotlinx.android.synthetic.main.fragment_search.*

class SearchFragment : Fragment() {

    private var userAdapter: UserAdapter? = null
    private var mUsers: List<Users>? = null
    private var recyclerView : RecyclerView? = null
    private var searchEditText : EditText? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view : View  = inflater.inflate(R.layout.fragment_search, container, false)

        recyclerView = view.findViewById(R.id.rv_search)
        recyclerView!!.setHasFixedSize(true)
        recyclerView!!.layoutManager = LinearLayoutManager(context)
        searchEditText = view.findViewById(R.id.keyword)

        mUsers = ArrayList()
        retriveAllUsers()

        searchEditText!!.addTextChangedListener (object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(cs: CharSequence?, start: Int, count: Int, after: Int) {
                search(cs.toString().toLowerCase())
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

        })

        return view
    }

    private fun retriveAllUsers(){
        var firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val refUsers = FirebaseDatabase.getInstance().reference.child("Users")

        refUsers.addValueEventListener(object : ValueEventListener
        {
            override fun onDataChange(p0: DataSnapshot) {
                (mUsers as ArrayList<Users>).clear()

                if (searchEditText!!.text.toString() == ""){
                    for (snapshot in p0.children)
                    {
                        val user: Users? = snapshot.getValue(Users::class.java)
                        if (!(user!!.getUID()).equals(firebaseUserID))
                        {
                            (mUsers as ArrayList<Users>).add(user)
                        }
                        userAdapter = UserAdapter(context!!,mUsers!!,false)
                        recyclerView!!.adapter = userAdapter
                    }

                }
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }

    private fun search(str: String){
        var firebaseUserID = FirebaseAuth.getInstance().currentUser!!.uid
        val queryUsers = FirebaseDatabase.getInstance()
            .reference.child("Users")
            .orderByChild("search")
            .startAt(str)
            .endAt(str+"\uf8ff")

        queryUsers.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {
                (mUsers as ArrayList<Users>).clear()
                for (snapshot in p0.children)
                {
                    val user: Users? = snapshot.getValue(Users::class.java)
                    if (!(user!!.getUID()).equals(firebaseUserID)){
                        (mUsers as ArrayList<Users>).add(user)
                    }
                }
                userAdapter = UserAdapter(context!!,mUsers!!,false)
                recyclerView!!.adapter = userAdapter
            }

            override fun onCancelled(p0: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

}


