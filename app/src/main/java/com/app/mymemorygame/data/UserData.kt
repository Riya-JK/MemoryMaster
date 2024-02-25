package com.app.mymemorygame.data

import com.google.firebase.firestore.PropertyName

data class UserData(
    @PropertyName("uid") val uid: String? = null,
    @PropertyName("userName") val userName: String? = null,
    @PropertyName("password") val password: String? = null
)
