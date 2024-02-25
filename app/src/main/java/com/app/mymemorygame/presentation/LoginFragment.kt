package com.app.mymemorygame.presentation

import android.content.Intent
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
import com.app.mymemorygame.utils.EXTRA_USER_NAME
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.*

/**
 * A simple [Fragment] subclass.
 * Use the [LoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class LoginFragment : Fragment() {
    lateinit var  submitButton : Button
    lateinit var firebaseDatabase : FirebaseDatabase
    lateinit var databaseReference: DatabaseReference
    lateinit var clRoot : FrameLayout
    lateinit var userName : EditText
    lateinit var password : EditText
    lateinit var newUserText: TextView

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
        val v = inflater.inflate(R.layout.fragment_login, container, false)
        clRoot = v.findViewById(R.id.container_child_fragment)
        submitButton = v.findViewById(R.id.submit_button_login_screen)
        userName = v.findViewById(R.id.username_login)
        password = v.findViewById(R.id.password_for_login)
        newUserText = v.findViewById(R.id.new_user_text)

        submitButton.setOnClickListener {
            val username = userName.text.toString()
            val password = password.text.toString()

            if(username.isNotBlank() && username.isNotEmpty() && password.isNotBlank() && password.isNotEmpty()){
                loginUser(username, password)
            }else{
                Snackbar.make(clRoot, "All fields are required", Snackbar.LENGTH_SHORT).show()
            }
        }
        newUserText.setOnClickListener {
            //Launch Signup fragment
            val childFragment = SignUpFragment()
            val childFragmentManager = childFragmentManager
            val transaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.container_child_fragment, childFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
        return v
    }

    private fun loginUser(username : String, password : String){
        databaseReference.orderByChild("b").equalTo(username).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(userSnapShot in snapshot.children){
                        val userData = userSnapShot.getValue(UserData::class.java)
                        if(userData != null && userData.c == password){
                            Snackbar.make(clRoot, "Login successful", Snackbar.LENGTH_SHORT).show()
                            val handler = Handler()
                            val runnable = object : Runnable {
                                override fun run () {
                                    //Launch Main activity
                                    val boardActivityIntent = Intent(context, MainActivity::class.java)
                                    boardActivityIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                                    boardActivityIntent.putExtra(EXTRA_USER_NAME, username)
                                    context?.startActivity(boardActivityIntent)
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

    companion object {
        @JvmStatic
        fun newInstance() = LoginFragment()
    }
}