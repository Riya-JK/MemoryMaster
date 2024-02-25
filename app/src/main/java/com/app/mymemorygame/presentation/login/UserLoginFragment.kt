package com.app.mymemorygame.presentation.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.app.mymemorygame.R
import com.app.mymemorygame.data.UserData
import com.app.mymemorygame.presentation.MainActivity
import com.app.mymemorygame.presentation.common.UserState
import com.app.mymemorygame.presentation.signup.UserSignUpFragment
import com.app.mymemorygame.utils.EXTRA_USER_NAME
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

/**
 * A simple [Fragment] subclass.
 * Use the [UserLoginFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class UserLoginFragment : Fragment() {
    lateinit var  submitButton : Button
    lateinit var clRoot : FrameLayout
    lateinit var userName : EditText
    lateinit var password : EditText
    lateinit var newUserText: TextView
    private lateinit var viewModel: UserLoginViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(requireActivity().application)).get(UserLoginViewModel::class.java)
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
                Snackbar.make(clRoot, getString(R.string.all_fields_required), Snackbar.LENGTH_SHORT).show()
            }
        }

        newUserText.setOnClickListener {
            //Launch Signup fragment
            val childFragment = UserSignUpFragment()
            val childFragmentManager = childFragmentManager
            val transaction = childFragmentManager.beginTransaction()
            transaction.replace(R.id.container_child_fragment, childFragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
        return v
    }

    private fun loginUser(username : String, password : String){
        viewModel.viewModelScope.launch {
            viewModel.loginUser(UserData(null,username, password))
            viewModel.state.observe(viewLifecycleOwner){state ->
                updateUI(state, username)
            }
        }
    }

    private fun updateUI(state: UserState, username: String) {
        if(state.onSuccess.isNotBlank()){
            Snackbar.make(clRoot, state.onSuccess, Snackbar.LENGTH_SHORT).show()
            val handler = Handler()
            val runnable = Runnable { //Launch Main activity
                val boardActivityIntent = Intent(context, MainActivity::class.java)
                boardActivityIntent.flags = Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
                boardActivityIntent.putExtra(EXTRA_USER_NAME, username)
                context?.startActivity(boardActivityIntent)
            }
            val delayMillis: Long = 1000
            handler.postDelayed(runnable, delayMillis)
        }
        if(state.onError.isNotBlank()){
            Snackbar.make(clRoot, state.onError, Snackbar.LENGTH_SHORT).show()
        }
        if(state.isLoading){
            Snackbar.make(clRoot, "Logging you in..", Snackbar.LENGTH_SHORT).show()
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = UserLoginFragment()
    }
}