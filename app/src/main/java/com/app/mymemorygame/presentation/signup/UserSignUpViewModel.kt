package com.app.mymemorygame.presentation.signup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.app.mymemorygame.data.UserData
import com.app.mymemorygame.presentation.common.UserState
import com.app.mymemorygame.presentation.login.UserLoginViewModel
import com.google.firebase.database.*

class UserSignUpViewModel :ViewModel() {
    private val TAG: String? = UserLoginViewModel::class.java.simpleName
    private var firebaseDatabase : FirebaseDatabase = FirebaseDatabase.getInstance()
    private var databaseReference: DatabaseReference = firebaseDatabase.reference.child("users")

    private val _state = MutableLiveData(UserState())
    val state : LiveData<UserState> = _state

    fun signUpUser(userData: UserData){
        _state.value = UserState(isLoading = true)

        databaseReference.orderByChild("userName").equalTo(userData.userName)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (!snapshot.exists()) {
                        val id = databaseReference.push().key
                        val newUserData = UserData(
                            uid = id,
                            userName = userData.userName,
                            password = userData.password
                        )
                        databaseReference.child(id!!).setValue(newUserData)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    _state.value = UserState(onSuccess = "Sign up successful", isLoading = false, onError = "")
                                } else {
                                    _state.value = UserState(onError = "Sign up failed", isLoading = false, onSuccess = "")
                                }
                            }
                    } else {
                        _state.value = UserState(onError = "User already exists", isLoading = false, onSuccess = "")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    _state.value = UserState(onError = "Database Error : ${error.message}", isLoading = false, onSuccess = "")
                }
            })
    }

}