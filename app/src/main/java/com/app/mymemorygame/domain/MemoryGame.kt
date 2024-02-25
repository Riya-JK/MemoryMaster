package com.app.mymemorygame.domain

import android.util.Log
import com.app.mymemorygame.data.MemoryCard
import com.app.mymemorygame.utils.DEFAULT_ICONS

class MemoryGame(private val boardSize: BoardSize, customImages: List<String>?) {

    private val TAG: String? = MemoryGame::class.java.simpleName
    val cards : List<MemoryCard>
    var numPairsFound = 0
    var numCardFlips = 0

    private var indexOfSingleSelectedcard : Int? = null

    init {
        if(customImages == null){
            val chosen_images = DEFAULT_ICONS.shuffled().take(boardSize.getNumPairs())
            val randomized_images = (chosen_images + chosen_images).shuffled()
            cards   = randomized_images.map { MemoryCard(it) }
        }else{
            val randomized_images = (customImages + customImages).shuffled()
            cards = randomized_images.map { MemoryCard(it.hashCode(), it) }
            Log.d(TAG, "cards = ${cards.toString()}")
        }
    }

    fun flipCard(position: Int) : Boolean {
        numCardFlips++
        val card : MemoryCard = cards[position]
        //Three cases:
        // 0 cards previously flipped over : restore cards + flip over the selected card
        // 1 card previously flipped over : flip over selected card + check if matches card
        // 2 cards previously flipped over : restore cards + flip over the selected card
        var foundMatch : Boolean = false
        if(indexOfSingleSelectedcard == null){
            //0 or 2 cards previously flipped over
            restoreCards()
            indexOfSingleSelectedcard = position
        }else{
            //exactly 1 card is flipped
            foundMatch = checkForMatch(indexOfSingleSelectedcard!!, position)
            indexOfSingleSelectedcard = null
        }
        card.isFaceUp = !card.isFaceUp
        return foundMatch
    }

    private fun checkForMatch(position1 : Int, position2 : Int): Boolean {
            if(cards[position1].identifier != cards[position2].identifier){
                return false
            }
        cards[position1].isMatched = true
        cards[position2].isMatched = true
        numPairsFound++
        return true
    }

    private fun restoreCards() {
        for(card in cards){
            if(!card.isMatched) {
                card.isFaceUp = false
            }
        }
    }

    fun haveWonGame(): Boolean {
        return numPairsFound == boardSize.getNumPairs()
    }

    fun isCardFaceUp(position: Int): Boolean {
        return  cards[position].isFaceUp
    }

    fun getNumMoves(): Int {
        return numCardFlips / 2
    }
}