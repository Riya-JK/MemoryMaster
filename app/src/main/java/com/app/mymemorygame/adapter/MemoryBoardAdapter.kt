package com.app.mymemorygame.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.ImageButton
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.mymemorygame.R
import com.app.mymemorygame.domain.BoardSize
import com.app.mymemorygame.data.MemoryCard
import com.squareup.picasso.Picasso
import kotlin.math.min

class MemoryBoardAdapter(
    private val context: Context,
    private val boardSize: BoardSize,
    private val cards: List<MemoryCard>,
    private val cardClickListener: CardClickListener
) : RecyclerView.Adapter<MemoryBoardAdapter.ViewHolder>() {

    companion object{
        private const val MARGIN_SIZE = 10
        private val TAG = MemoryBoardAdapter::class.java.simpleName
    }

    interface CardClickListener{
        fun onCardClicked(position: Int)
    }

    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        private val imageButton = itemView.findViewById<ImageButton>(R.id.imageButton);

        fun bind(position: Int) {
            val memoryCard = cards[position]
            if(memoryCard.isFaceUp){
                if(memoryCard.imageUri != null){
                    Picasso.get().load(memoryCard.imageUri).placeholder(R.drawable.ic_image).into(imageButton);
                }else{
                    imageButton.setImageResource(memoryCard.identifier)
                }
            }else{
                imageButton.setImageResource(R.drawable.ic_card_background)
            }

            imageButton.alpha = if(memoryCard.isMatched) .4f else 1.0f
            val colorStateList = if(memoryCard.isMatched) ContextCompat.getColorStateList(context, androidx.appcompat.R.color.material_grey_50) else null
            ViewCompat.setBackgroundTintList(imageButton, colorStateList)

            imageButton.setOnClickListener(View.OnClickListener {
                Log.i(TAG, "clicked on button $position")
                cardClickListener.onCardClicked(position)
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        Log.d( TAG,"onCreateViewHolder called")
        val cardWidth = parent.width / boardSize.getWidth() - (2 * MARGIN_SIZE)
        val cardHeight = parent.height / boardSize.getHeight() - (2 * MARGIN_SIZE)
        val cardSideLength = min(cardHeight, cardWidth)
        val view = LayoutInflater.from(context).inflate(R.layout.memory_card, parent, false)
        val layoutParams = view.findViewById<CardView>(R.id.card_view).layoutParams as MarginLayoutParams
        layoutParams.height = cardSideLength
        layoutParams.width = cardSideLength
        layoutParams.setMargins(MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE, MARGIN_SIZE)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return boardSize.numCards
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

}


