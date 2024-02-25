package com.app.mymemorygame.presentation.common

data class UserState(
    val isLoading: Boolean = false,
    val onSuccess : String = "",
    val onError : String = ""
)