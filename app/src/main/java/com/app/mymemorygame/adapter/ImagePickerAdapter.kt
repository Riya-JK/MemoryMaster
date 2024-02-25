package com.app.mymemorygame.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.app.mymemorygame.R
import com.app.mymemorygame.domain.BoardSize
import kotlin.math.min

class ImagePickerAdapter(private val context: Context,
                         private val chosenImageUris: MutableList<Uri>,
                         private val boardSize: BoardSize,
                         private val imageClickListener: ImageClickListener
) : RecyclerView.Adapter<ImagePickerAdapter.ViewHolder>() {

    private val MARGIN_SIZE = 10

    interface ImageClickListener{
        fun onPlaceholderClicked()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        private val ivCustomImage = itemView.findViewById<ImageView>(R.id.customImage)
        fun bind(uri : Uri) {
            ivCustomImage.setImageURI(uri)
            ivCustomImage.setOnClickListener(null)
        }

        fun bind() {
            ivCustomImage.setOnClickListener(View.OnClickListener {
                //Launch intent for user to select photos
                imageClickListener.onPlaceholderClicked()
            })
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.card_image, parent, false)
        val cardWidth = parent.width / boardSize.getWidth()
        val cardHeight = parent.height / boardSize.getHeight()
        var cardSideLength = min(cardHeight, cardWidth)
        val layoutParams = view.findViewById<ImageView>(R.id.customImage).layoutParams
        layoutParams.width = cardSideLength
        layoutParams.height = cardSideLength
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return boardSize.getNumPairs()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        if(position < chosenImageUris.size){
            holder.bind(chosenImageUris[position])
        }else{
            holder.bind()
        }
    }

}
