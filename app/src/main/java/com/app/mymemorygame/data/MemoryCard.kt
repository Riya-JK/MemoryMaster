package com.app.mymemorygame.data

data class MemoryCard(
    val identifier : Int,
    val imageUri : String? = null,
    var isFaceUp : Boolean = false,
    var isMatched : Boolean = false
)
