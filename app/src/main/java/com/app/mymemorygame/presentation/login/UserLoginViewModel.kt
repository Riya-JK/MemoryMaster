package com.app.mymemorygame.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.app.mymemorygame.data.UserData
import com.app.mymemorygame.presentation.common.UserState
import com.google.firebase.database.*

class UserLoginViewModel : ViewModel() {
    private val TAG: String? = UserLoginViewModel::class.java.simpleName
    private var firebaseDatabase : FirebaseDatabase = FirebaseDatabase.getInstance()
    private var databaseReference: DatabaseReference = firebaseDatabase.reference.child("users")

    private val _state = MutableLiveData(UserState())
    val state : LiveData<UserState> = _state

    fun loginUser(userData: UserData){
        _state.value = UserState(isLoading = true)
        databaseReference.orderByChild("userName").equalTo(userData.userName).addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    for(userSnapShot in snapshot.children){
                        val userDataFetched = userSnapShot.getValue(UserData::class.java)
                        if(userDataFetched != null && userDataFetched.password == userData.password){
                            _state.value = UserState(onSuccess = "Login successful", isLoading = false, onError = "")
                        }else{
                            _state.value = UserState(onError = "Invalid credentials", isLoading = false, onSuccess = "")
                        }
                    }
                }else{
                    _state.value = UserState(onError = "User does not exist", isLoading = false, onSuccess = "")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                _state.value = UserState(onError = "Database Error : ${error.message}", isLoading = false, onSuccess = "")
            }
        })
    }
}