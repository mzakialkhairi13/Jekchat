package com.mzakialkhairi.jekchat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.mzakialkhairi.jekchat.databinding.ActivityRegisterBinding


class RegisterActivity : AppCompatActivity() {

    private lateinit var binding : ActivityRegisterBinding
    private lateinit var mAuth : FirebaseAuth
    private lateinit var refUsers : DatabaseReference
    private var firabaseUserID : String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_register)

        val toolbar = binding.toolbarRegister
        setSupportActionBar(toolbar)

        supportActionBar?.title ="Register"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            val intent = Intent(this,WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        mAuth =  FirebaseAuth.getInstance()

        binding.btnRegister.setOnClickListener {
            register()
        }
    }

    private fun register(){
        val username = binding.usernameRegister.text.toString()
        val email = binding.emailRegister.text.toString()
        val password = binding.passwordRegister.text.toString()

        if (username==""){
            showToast("Input Username")
        }else if (email==""){
            showToast("Input Email")
        }else if (password==""){
            showToast("Input Password")
        }else{
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener{
                task ->
                if (task.isSuccessful){
                    firabaseUserID = mAuth.currentUser!!.uid
                    refUsers = FirebaseDatabase.getInstance().reference.child("Users").child(firabaseUserID)

                    val userHashMap = HashMap<String, Any>()
                    userHashMap["uid"] = firabaseUserID
                    userHashMap["username"] = username
                    userHashMap["profile"] = "https://firebasestorage.googleapis.com/v0/b/jekchat-47a4a.appspot.com/o/profile.jpg?alt=media&token=c0642ae4-ec2e-425b-8f4e-475e3c3e3fe7"
                    userHashMap["cover"] = "https://firebasestorage.googleapis.com/v0/b/jekchat-47a4a.appspot.com/o/background.jpeg?alt=media&token=813024c1-145c-48aa-81fb-57355b1aaf3c"
                    userHashMap["status"] = "offline"
                    userHashMap["search"] = username.toLowerCase()
                    userHashMap["level"] = "2"
                    userHashMap["facebook"] = "https://m.facebook.com"
                    userHashMap["instagram"] = "https://m.instagram.com"
                    userHashMap["website"] = "https://m.google.com"

                    refUsers.updateChildren(userHashMap).addOnCompleteListener{ task->
                        if (task.isSuccessful){
                            val intent = Intent(this,MainActivity::class.java)
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }else{
                            showToast("Error Message : "+ task.exception?.message.toString())
                        }
                    }
                }else{
                    showToast("Error Message : "+ task.exception?.message.toString())
                }
            }
        }
    }

    private fun showToast(msg: String){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()
    }

}