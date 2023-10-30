package com.app.mymemorygame.activity

import android.content.Intent
import android.os.Bundle
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
            val email = binding.emailSignup.text.toString()
            val password = binding.passwordSignup.text.toString()

            if(email.isNotBlank() && email.isNotEmpty() && password.isNotBlank() && password.isNotEmpty()){
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Snackbar.make(clRoot, "Invalid email address", Snackbar.LENGTH_SHORT).show()
                } else {
                    signUpUser(email, password)
                }
            }else{
                Snackbar.make(clRoot, "All fields are required", Snackbar.LENGTH_SHORT).show()
            }
        }

        login_button.setOnClickListener{
            startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
        }
    }

    fun signUpUser(email : String, password : String){
        databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.exists()){
                    val id = databaseReference.push().key
                    val user_data = UserData(id, email, password)
                    databaseReference.child(id!!).setValue(user_data)
                    Snackbar.make(clRoot, "Sign up successfull", Snackbar.LENGTH_SHORT).show()
                    startActivity(Intent(this@SignUpActivity, LoginActivity::class.java))
                    finish()
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