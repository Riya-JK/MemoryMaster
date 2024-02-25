package com.app.mymemorygame.presentation

import android.os.Bundle
import android.os.Handler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import com.app.mymemorygame.R
import com.app.mymemorygame.models.UserData
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*

// TODO: Rename parameter arguments, choose names that match
/**
 * A simple [Fragment] subclass.
 * Use the [SignUpFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SignUpFragment : Fragment() {
    private val TAG: String? = SignUpFragment::class.java.simpleName
    lateinit var  createAccountButton : Button
    lateinit var firebaseDatabase : FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var clRoot : FrameLayout
    lateinit var userName : EditText
    lateinit var password : EditText
    lateinit var loginUserText: TextView
    lateinit var  submitButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        firebaseDatabase = FirebaseDatabase.getInstance()
        databaseReference = firebaseDatabase.reference.child("users")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_sign_up, container, false)
        clRoot = v.findViewById(R.id.container_child_fragment_signup)
        submitButton = v.findViewById(R.id.submit_button_signup_screen)
        userName = v.findViewById(R.id.username_signup)
        password = v.findViewById(R.id.password_signup)
        loginUserText = v.findViewById(R.id.login_label)
        createAccountButton = v.findViewById(R.id.submit_button_signup_screen)

        createAccountButton?.setOnClickListener {
            val username = userName.text.toString()
            val password = password.text.toString()

            if(username.isNotBlank() && username.isNotEmpty() && password.isNotBlank() && password.isNotEmpty()){
                signUpUser(username, password)
            }else{
                Snackbar.make(clRoot, "All fields are required", Snackbar.LENGTH_SHORT).show()
            }
        }

        loginUserText.setOnClickListener{
            fragmentManager?.popBackStack()
        }
        return v
    }

    fun signUpUser(username : String, password : String){
        databaseReference.orderByChild("b").equalTo(username).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(!snapshot.exists()){
                    val id = databaseReference.push().key
                    val userData = UserData(a = id, b = username, c = password)
                    databaseReference.child(id!!).setValue(userData)
                    Snackbar.make(clRoot, "Sign up successfull", Snackbar.LENGTH_SHORT).show()
                    val handler = Handler()
                    val runnable = object : Runnable {
                        override fun run () {
                            fragmentManager?.popBackStack()
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

    companion object {
        @JvmStatic
        fun newInstance() = SignUpFragment()
    }
}