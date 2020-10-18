 package com.mzakialkhairi.jekchat.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.ktx.Firebase
import com.mzakialkhairi.jekchat.R
import com.mzakialkhairi.jekchat.adapter.UserAdapter
import com.mzakialkhairi.jekchat.databinding.FragmentChatsBinding
import com.mzakialkhairi.jekchat.model.ChatList
import com.mzakialkhairi.jekchat.model.Users

 class  ChatsFragment : Fragment() {

     private lateinit var binding : FragmentChatsBinding
     private var userAdapter : UserAdapter? = null
     private var mUsers : List<Users>? = null
     private var userChatList: List<ChatList>? = null
     private var firebaseUser : FirebaseUser? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_chats, container, false)

        binding.rvChatList.setHasFixedSize(true)
        binding.rvChatList.layoutManager = LinearLayoutManager(context)

        firebaseUser = FirebaseAuth.getInstance().currentUser

        userChatList = ArrayList()
        val ref = FirebaseDatabase.getInstance().reference.child("ChatLists").child(firebaseUser!!.uid)
        ref!!.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot) {
                (userChatList as ArrayList).clear()

                for (dataSnapshot in p0.children)
                {
                    val chatList = dataSnapshot.getValue(ChatList::class.java)
                    (userChatList as ArrayList).add(chatList!!)
                }
                retrieveChatList()
            }

        })



        return binding.root
    }

     private fun retrieveChatList()
     {
        mUsers = ArrayList()

         val ref = FirebaseDatabase.getInstance().reference.child("Users")
         ref!!.addValueEventListener(object: ValueEventListener{
             override fun onCancelled(p0: DatabaseError) {
             }

             override fun onDataChange(p0: DataSnapshot) {

                 (mUsers as ArrayList).clear()
                 for(dataSnapshot in p0.children)
                 {
                     val user = dataSnapshot.getValue(Users::class.java)
                     for (eachChatList in userChatList!!)
                     {
                         if(user!!.getUID().equals(eachChatList.getID()))
                         {
                             (mUsers as ArrayList).add(user!!)
                         }
                     }
                 }
                 userAdapter = UserAdapter(context!!, (mUsers as ArrayList<Users>),true)
                 binding.rvChatList.adapter = userAdapter
             }

         })
     }

}