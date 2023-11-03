package com.app.mymemorygame.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.app.mymemorygame.R
import com.app.mymemorygame.databinding.ActivitySignupBinding
import com.app.mymemorygame.models.UserData
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.snackbar.Snackbar.SnackbarLayout
import com.google.firebase.database.*
import org.w3c.dom.Text
import kotlin.math.sign

class SignUpActivity : AppCompatActivity() {
    private val TAG: String? = SignUpActivity::class.java.simpleName
    lateinit var  createAccountButton : Button
    lateinit var login_button : TextView
    lateinit var binding: ActivitySignupBinding
    lateinit var firebaseDatabase : FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var clRoot : ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        Log.d(TAG,"onCreate() called")

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clRoot = binding.clRoot
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")

        login_button = findViewById(R.id.or_login_label)
        createAccountButton = findViewById(R.id.submit_button_signup_screen)

        createAccountButton?.setOnClickListener {
            val username = binding.usernameSignup.text.toString()
            val password = binding.passwordSignup.text.toString()

            if(username.isNotBlank() && username.isNotEmpty() && password.isNotBlank() && password.isNotEmpty()){
                signUpUser(username, password)
            }else{
                Snackbar.make(clRoot, "All fields are required", Snackbar.LENGTH_SHORT).show()
            }
        }

        login_button.setOnClickListener{
            startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
        }
    }

    fun signUpUser(username : String, password : String){
        databaseReference.orderByChild("b").equalTo(username).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.exists()){
                    val id = databaseReference.push().key
                    val userData = UserData(a = id, b = username, c = password)
                    databaseReference.child(id!!).setValue(userData)
                    Snackbar.make(clRoot, "Sign up successfull", Snackbar.LENGTH_SHORT).show()
                    val handler = Handler()
                    val runnable = object : Runnable {
                        override fun run () {
                            startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
                            finish()
                        }
                    }
                    val delayMillis: Long = 1000
                    handler.postDelayed(runnable, delayMillis)
                }else{
                    Snackbar.make(clRoot, "User already exists", Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Snackbar.make(clRoot, "Database Error : ${error.message}", Snackbar.LENGTH_SHORT).show()
            }
        })
    }
}