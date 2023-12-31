package com.app.mymemorygame.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.app.mymemorygame.R
import com.app.mymemorygame.databinding.ActivityLoginBinding
import com.app.mymemorygame.models.UserData
import com.app.mymemorygame.utils.EXTRA_USER_NAME
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import kotlinx.coroutines.delay
import org.w3c.dom.Text

class LoginActivity : AppCompatActivity() {
    lateinit var  submitButton : Button
    lateinit var binding: ActivityLoginBinding
    lateinit var firebaseDatabase : FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var clRoot : ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clRoot = binding.clRoot
        submitButton = findViewById(R.id.submit_button_login_screen)

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")

        submitButton?.setOnClickListener {
            val username = binding.usernameLogin.text.toString()
            val password = binding.passwordForLogin.text.toString()

            if(username.isNotBlank() && username.isNotEmpty() && password.isNotBlank() && password.isNotEmpty()){
                loginUser(username, password)
            }else{
                Snackbar.make(clRoot, "All fields are required", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.newUserText.setOnClickListener {
            startActivity( Intent(this@LoginActivity, SignUpActivity::class.java))
            finish()
        }
    }

    private fun loginUser(username : String, password : String){
        databaseReference.orderByChild("b").equalTo(username).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(userSnapShot in snapshot.children){
                        val userData = userSnapShot.getValue(UserData::class.java)
                        if(userData != null && userData.c == password){
                            Snackbar.make(clRoot, "Login successful", Snackbar.LENGTH_SHORT).show()
                            val handler = Handler()
                            val runnable = object : Runnable {
                                override fun run () {
                                    val boardActivityIntent = Intent(this@LoginActivity, MainActivity::class.java)
                                    boardActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                    boardActivityIntent.putExtra(EXTRA_USER_NAME, username)
                                    applicationContext.startActivity(boardActivityIntent)
                                    finish()
                                }
                            }
                            val delayMillis: Long = 1000
                            handler.postDelayed(runnable, delayMillis)
                        }else{
                            Snackbar.make(clRoot, "Invalid credentials", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }else{
                    Snackbar.make(clRoot, "User does not exist", Snackbar.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Snackbar.make(clRoot, "Database Error : ${error.message}", Snackbar.LENGTH_SHORT).show()
            }
        })
    }
}