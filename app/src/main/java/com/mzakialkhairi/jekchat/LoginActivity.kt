package com.mzakialkhairi.jekchat

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.mzakialkhairi.jekchat.databinding.ActivityLoginBinding
import kotlinx.android.synthetic.main.activity_main.*

class LoginActivity : AppCompatActivity() {

    private lateinit var binding : ActivityLoginBinding
    private lateinit var mAuth : FirebaseAuth
    private lateinit var  firebaseUsers: DatabaseReference
    private var firebaseUserID : String =""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_login)

        val toolbar = binding.toolbarLogin
        setSupportActionBar(toolbar)

        supportActionBar?.title ="Login"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(this,WelcomeActivity::class.java)
            startActivity(intent)
            finish()
        }

        mAuth = FirebaseAuth.getInstance()
        binding.btnLogin.setOnClickListener {
            loginUser()
        }

    }

    private fun loginUser(){
        val email = binding.emailLogin.text.toString()
        val password = binding.passwordLogin.text.toString()

        if (email==""){
            showToast("Insert Email")
        }else if (password==""){
            showToast("Insert Password")
        }else{
            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener {task->
                if (task.isSuccessful){
                    val intent = Intent(this,MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
                    startActivity(intent)
                }else{
                    showToast("Error Login : "+task.exception!!.message.toString())
                }
            }
        }
    }

    private fun showToast(msg: String){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show()
    }
}