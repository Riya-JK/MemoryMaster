package com.app.mymemorygame.presentation.signup

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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.mymemorygame.R
import com.app.mymemorygame.data.UserData
import com.app.mymemorygame.presentation.common.UserState
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

// TODO: Rename parameter arguments, choose names that match
/**
 * A simple [Fragment] subclass.
 * Use the [UserSignUpFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserSignUpFragment : Fragment() {
    private val TAG: String? = UserSignUpFragment::class.java.simpleName
    lateinit var  createAccountButton : Button
    lateinit var clRoot : FrameLayout
    lateinit var userName : EditText
    lateinit var password : EditText
    lateinit var loginUserText: TextView
    lateinit var viewModel: UserSignUpViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(UserSignUpViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val v = inflater.inflate(R.layout.fragment_sign_up, container, false)
        clRoot = v.findViewById(R.id.container_child_fragment_signup)
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
        viewModel.viewModelScope.launch {
            viewModel.signUpUser(UserData(null,username, password))
            viewModel.state.observe(viewLifecycleOwner) { state ->
                updateUI(state)
            }
        }
    }

    private fun updateUI(state: UserState) {
        if(state.onSuccess.isNotBlank()){
            Snackbar.make(clRoot, state.onSuccess, Snackbar.LENGTH_SHORT).show()
            val handler = Handler()
            val runnable = object : Runnable {
                override fun run () {
                    fragmentManager?.popBackStack()
                }
            }
            val delayMillis: Long = 1000
            handler.postDelayed(runnable, delayMillis)
        }
        if(state.onError.isNotBlank()){
            Snackbar.make(clRoot, state.onError, Snackbar.LENGTH_SHORT).show()
        }
        if(state.isLoading){
            Snackbar.make(clRoot, "Signing you up..", Snackbar.LENGTH_SHORT).show()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = UserSignUpFragment()
    }
}