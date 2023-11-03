package com.app.mymemorygame.models

import com.google.firebase.firestore.PropertyName

data class UserData(
    @PropertyName("a") val a: String? = null,
    @PropertyName("b") val b: String? = null,
    @PropertyName("c") val c: String? = null
)
