package com.mzakialkhairi.jekchat

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.auth.api.signin.internal.Storage
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.mzakialkhairi.jekchat.adapter.ChatsAdapter
import com.mzakialkhairi.jekchat.databinding.ActivityMessageChatBinding
import com.mzakialkhairi.jekchat.model.Chat
import com.mzakialkhairi.jekchat.model.Users
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message_chat.*

class MessageChatActivity : AppCompatActivity() {

    companion object {
        var userIDVisit : String? =""
    }

    var firebaseUser : FirebaseUser? = null
    var chatsAdapter : ChatsAdapter? = null
    var mChatList : List<Chat>? = null
    lateinit var recyclerview_chats : RecyclerView
    var reference : DatabaseReference? = null


    private lateinit var binding : ActivityMessageChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_message_chat)

        val toolbar = binding.toolbarMessageChat
        setSupportActionBar(toolbar)
        supportActionBar!!.title =""
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }

        intent = intent
        userIDVisit = intent.getStringExtra(userIDVisit)
        firebaseUser = FirebaseAuth.getInstance().currentUser

        recyclerview_chats = findViewById(R.id.rv_message_chat)
        recyclerview_chats.setHasFixedSize(true)
        var linearLayoutManager = LinearLayoutManager(applicationContext)
        recyclerview_chats.layoutManager = linearLayoutManager

        val reference = FirebaseDatabase.getInstance().reference
            .child("Users").child(userIDVisit.toString())
        reference.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {
                val user: Users? = p0.getValue(Users::class.java)
                
                //if verified
                if (user!!.getLevel() == "1"){
                 binding.verified.visibility = View.VISIBLE
                }
                binding.usernameMessageChat.text = user!!.getUsername()
                Picasso.get().load(user!!.getProfile()).into(binding.profileImageMessageChat)

                retrieveMessages(firebaseUser!!.uid, userIDVisit.toString(),user.getProfile().toString())
            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })

        binding.btnSendMesaage.setOnClickListener {
            val message = binding.textMessageChat.text.toString()
            if (message == ""){
                Toast.makeText(this,"Please insert message",Toast.LENGTH_LONG).show()
            }else{
                sendMessageToUser(firebaseUser!!.uid, userIDVisit.toString() ,message)
            }
            binding.textMessageChat.setText("")
        }

        binding.attachImageFile.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent,"Pick Image"),438)
        }

        seenMessage(userIDVisit.toString())
    }

    private fun retrieveMessages(sender: String, receiver: String, profile: String) {
        mChatList = ArrayList()
        val referece = FirebaseDatabase.getInstance().reference.child("Chats")

        referece.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }
            override fun onDataChange(p0: DataSnapshot) {
                (mChatList as ArrayList<Chat>).clear()
                for (snapsot in p0.children) {
                    val chat = snapsot.getValue(Chat::class.java)
                    if (chat!!.getReceiver().equals(sender) && chat.getSender()
                            .equals(receiver) || chat.getReceiver()
                            .equals(receiver) && chat.getSender().equals(sender)) {
                        (mChatList as ArrayList<Chat>).add(chat)
                    }
                    chatsAdapter = ChatsAdapter(this@MessageChatActivity,(mChatList as ArrayList<Chat>),profile)
                    recyclerview_chats.adapter = chatsAdapter
                }
            }
        })

    }

    private fun sendMessageToUser(sender: String, receiver : String, msg: String)
    {
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key

        val messageHashMap = HashMap<String,Any?>()
        messageHashMap["sender"] = sender
        messageHashMap["receiver"] = receiver
        messageHashMap["message"] = msg
        messageHashMap["issen"] = false
        messageHashMap["url"] = ""
        messageHashMap["messageID"] = messageKey
        reference.child("Chats")
            .child(messageKey!!)
            .setValue(messageHashMap)
            .addOnCompleteListener { task ->

                if (task.isSuccessful){
                    val chatsListReference = FirebaseDatabase.getInstance()
                        .reference
                        .child("ChatLists")
                        .child(firebaseUser!!.uid)
                        .child(userIDVisit.toString())

                    chatsListReference.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onCancelled(p0: DatabaseError) {
                        }

                        override fun onDataChange(p0: DataSnapshot) {
                            if (p0.exists()){
                                chatsListReference.child("id").setValue(userIDVisit)
                            }

                            val chatsListReceiverRef = FirebaseDatabase.getInstance()
                                .reference
                                .child("ChatLists")
                                .child(userIDVisit.toString())
                                .child(firebaseUser!!.uid )

                            chatsListReceiverRef.child("id").setValue(firebaseUser!!.uid)
                        }
                    })

                    //notification
                     val reference = FirebaseDatabase.getInstance().reference
                        .child("Users")
                        .child(firebaseUser!!.uid)

                }
                else
                {

                }
            }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode==438 && resultCode== Activity.RESULT_OK && data!=null && data!!.data!= null ){

            val progressBar = ProgressDialog(applicationContext)
            progressBar.setMessage("Please wait, image is sending ...")
            progressBar.show()

            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance()
                .reference
                .child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageID = ref.push().key
            val filePath = storageReference.child("$messageID.jpg")

            val uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)

            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{ task ->
                if (!task.isSuccessful){
                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation  filePath.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {

                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    val messageHashMap = HashMap<String,Any?>()
                    messageHashMap["sender"] = firebaseUser!!.uid
                    messageHashMap["receiver"] = userIDVisit.toString()
                    messageHashMap["message"] = "sent you an image."
                    messageHashMap["issen"] = false
                    messageHashMap["url"] = url
                    messageHashMap["messageID"] = messageID

                    ref.child("Chats").child(messageID!!).setValue(messageHashMap)

                }
            }

        }
    }

    var seenListener : ValueEventListener? = null

    private fun seenMessage(userdID : String)
    {
        reference = FirebaseDatabase.getInstance().reference.child("Chats")
        seenListener = reference!!.addValueEventListener(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(p0: DataSnapshot)
            {
                for (dataSnapshot in p0.children)
                {
                    val chat = dataSnapshot.getValue(Chat::class.java)
                    if (chat!!.getReceiver().equals(firebaseUser!!.uid) && chat!!.getSender().equals(userdID)){
                        val hashMap = HashMap<String, Any>()
                        hashMap["isseen"] = true
                        dataSnapshot.ref.updateChildren(hashMap)

                    }
                }
            }
        })
    }

    override fun onPause() {
        super.onPause()
        reference!!.removeEventListener(seenListener!!)
    }

}