package com.app.mymemorygame.domain

enum class BoardSize(val numCards : Int) {
    EASY(numCards = 8),
    MEDIUM(numCards = 18),
    HARD(numCards = 24);

    companion object{
        fun getByValue(value: Int) = values().first(){it.numCards == value}
    }

    fun getWidth() : Int{
        return when(this){
            EASY -> 2
            MEDIUM -> 3
            HARD -> 4
        }
    }

    fun getHeight() : Int{
        return numCards / getWidth()
    }

    fun getNumPairs() : Int{
        return numCards / 2
    }
}