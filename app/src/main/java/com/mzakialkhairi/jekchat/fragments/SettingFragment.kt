package com.mzakialkhairi.jekchat.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.mzakialkhairi.jekchat.R
import com.mzakialkhairi.jekchat.WelcomeActivity
import com.mzakialkhairi.jekchat.databinding.FragmentSettingBinding
import com.mzakialkhairi.jekchat.model.Users
import com.squareup.picasso.Picasso

class SettingFragment : Fragment() {

    var usersReference : DatabaseReference? = null
    var firebaseUser : FirebaseUser? = null
    private lateinit var binding : FragmentSettingBinding
    private val RequestCode = 438
    private var imageUri: Uri?  = null
    private var storageRef: StorageReference? = null
    private var coverChecker: String? = null
    private var socailChecker : String? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =  DataBindingUtil.inflate(inflater,R.layout.fragment_setting, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        usersReference = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        storageRef = FirebaseStorage.getInstance().reference.child("User images")

        usersReference!!.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {
                if (context!=null){

                    if (p0.exists()){
                        val user: Users? = p0.getValue(Users::class.java)
                        binding.usernameProfile.text = user!!.getUsername()
                        if (user.getLevel()=="1"){
                            binding.verified.visibility = View.VISIBLE
                        }
                        Picasso.get().load(user.getProfile()).into(binding.profileImageSetting)
                        Picasso.get().load(user.getCover()).into(binding.coverImageSetting)

                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {
            }

        })

        binding.btnLogout.setOnClickListener {
            logout()
        }

        binding.profileImageSetting.setOnClickListener {
            pickImage()
        }

        binding.coverImageSetting.setOnClickListener {
            coverChecker = "cover"
            pickImage()
        }
        binding.setFacebook.setOnClickListener {
            socailChecker = "facebook"
            setSocialLink()
        }
        binding.setInstagram.setOnClickListener {
            socailChecker = "instagram"
            setSocialLink()
        }
        binding.setWebsite.setOnClickListener {
            socailChecker = "website"
            setSocialLink()
        }

        return binding.root
    }

    private fun logout(){
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(context, WelcomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private fun pickImage(){
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent,RequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RequestCode && resultCode == Activity.RESULT_OK && data!!.data!=null){
            imageUri = data.data
            Toast.makeText(context,"Uploading  Processing",Toast.LENGTH_LONG).show()
            uploadImageToDatabase()
        }
    }

    private fun uploadImageToDatabase(){
        val progressBar = ProgressDialog(context)
        progressBar.setMessage("Image is uploading, please wait...")
        progressBar.show()

        if (imageUri!=null){

            val fileRef = storageRef!!.child(System.currentTimeMillis().toString()+".jpg")
            var uploadTask: StorageTask<*>
            uploadTask = fileRef.putFile(imageUri!!)

            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>>{task ->

                if (!task.isSuccessful)
                {
                    task.exception?.let{
                        throw it
                    }
                }
                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener{ task ->
                if (task.isSuccessful){

                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    if (coverChecker == "cover"){
                        val mapCoverImg = HashMap<String,Any>()
                        mapCoverImg["cover"] = url
                        usersReference!!.updateChildren(mapCoverImg)
                        coverChecker=""
                    }else{
                        val mapProfileImage = HashMap<String,Any>()
                        mapProfileImage["profile"] = url
                        usersReference!!.updateChildren(mapProfileImage)
                        coverChecker=""
                    }
                    progressBar.dismiss()
                }
            }
        }
    }

    private fun setSocialLink(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(context, R.style.Theme_AppCompat_Light_Dialog_Alert)

        if (socailChecker == "website")
        {
            builder.setTitle("Write URL : ")
            builder.setMessage("Insert your website Url here")
        }else{
            builder.setTitle("Write Username : ")
        }

        val editText = EditText(context)
        if (socailChecker == "website")
        {
            editText.hint = "e.g www.google.com"
        }else{
            editText.hint = "e.g mzakialkhairi"
        }

        builder.setView(editText)

        builder.setPositiveButton("Create", DialogInterface.OnClickListener{
            dialog, which ->
            val str = editText.text.toString()

            if (str == ""){
                Toast.makeText(context,"Please write something",Toast.LENGTH_LONG).show()
            }else{
                saveSocialLink(str)
            }
        })

        builder.setNegativeButton("Cancel",DialogInterface.OnClickListener{
            dialog, which ->
            dialog.cancel()
        })

        builder.show()
    }

    private fun saveSocialLink(str: String){
        val mapSocial= HashMap<String,Any>()

        when(socailChecker){
            "facebook" -> {
                mapSocial["facebook"] = "https://m.faceook.com/$str"
            }
            "instagram"->{
                mapSocial["instagram"] = "https://m.instagram.com/$str"
            }
            "website" -> {
                mapSocial["website"] = "https://$str"
            }
        }

        usersReference!!.updateChildren(mapSocial).addOnCompleteListener {task ->
            if (task.isSuccessful){
                Toast.makeText(context,"Saved successfully",Toast.LENGTH_LONG).show()
                socailChecker = ""
            }
        }
    }
}