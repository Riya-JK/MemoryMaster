package com.app.mymemorygame.activity

import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.app.mymemorygame.R
import com.app.mymemorygame.databinding.ActivityLoginBinding
import com.app.mymemorygame.models.UserData
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*
import org.w3c.dom.Text

class LoginActivity : AppCompatActivity() {
    lateinit var  submitButton : Button
    lateinit var forgot_password : TextView
    lateinit var binding: ActivityLoginBinding
    lateinit var firebaseDatabase : FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var clRoot : ConstraintLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        clRoot = binding.clRoot
        forgot_password = findViewById(R.id.forgot_password)
        submitButton = findViewById(R.id.submit_button_login_screen)

        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")

        submitButton?.setOnClickListener {
            val email = binding.emailLogin.text.toString()
            val password = binding.passwordForLogin.text.toString()

            if(email.isNotBlank() && email.isNotEmpty() && password.isNotBlank() && password.isNotEmpty()){
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    Snackbar.make(clRoot, "Invalid email address", Snackbar.LENGTH_SHORT).show()
                } else {
                    loginUser(email, password)
                }
            }else{
                Snackbar.make(clRoot, "All fields are required", Snackbar.LENGTH_SHORT).show()
            }
        }

        binding.newUserText.setOnClickListener {
            startActivity( Intent(this@LoginActivity, SignUpActivity::class.java))
            finish()
        }
    }

    private fun loginUser(email : String, password : String){
        databaseReference.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(userSnapShot in snapshot.children){
                        val userData = userSnapShot.getValue(UserData::class.java)
                        if(userData != null && userData.password == password){
                            Snackbar.make(clRoot, "Login successfull", Snackbar.LENGTH_SHORT).show()
                            val boardActivityIntent = Intent(this@LoginActivity, MainActivity::class.java)
                            boardActivityIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            applicationContext.startActivity(boardActivityIntent)
                            finish()
                            return
                        }else{
                            Snackbar.make(clRoot, "Invalid credentials", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Snackbar.make(clRoot, "Database Error : ${error.message}", Snackbar.LENGTH_SHORT).show()
            }
        })
    }
}