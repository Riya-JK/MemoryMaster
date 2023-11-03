package com.app.mymemorygame.activity

import android.animation.ArgbEvaluator
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.mymemorygame.R
import com.app.mymemorygame.adapter.MemoryBoardAdapter
import com.app.mymemorygame.models.BoardSize
import com.app.mymemorygame.models.MemoryGame
import com.app.mymemorygame.models.UserImageList
import com.app.mymemorygame.utils.EXTRA_BOARD_SIZE
import com.app.mymemorygame.utils.EXTRA_GAME_NAME
import com.app.mymemorygame.utils.EXTRA_USER_NAME
import com.github.jinatonic.confetti.CommonConfetti
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso

class MainActivity : AppCompatActivity() {

    private val CREATE_REQUEST_CODE: Int = 248
    private lateinit var adapter: MemoryBoardAdapter
    private lateinit var memoryGame: MemoryGame
    private lateinit var rvBoard : RecyclerView
    private lateinit var tvNumMoves : TextView
    private lateinit var tvNumPairs : TextView
    private lateinit var clRoot : CoordinatorLayout
    private val db = Firebase.firestore
    private var gameName : String? = null
    private var customGameImages : List<String>? = null
    private lateinit var username : String

    private var boardSize : BoardSize = BoardSize.EASY
    val TAG : String  = MainActivity::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        rvBoard = findViewById(R.id.rvBoard)
        tvNumMoves = findViewById(R.id.tvNumMoves)
        tvNumPairs = findViewById(R.id.tvNumPairs)
        clRoot = findViewById(R.id.clRoot)
        val bundle: Bundle? = intent.extras
        if (bundle != null) {
            val value = bundle.getString(EXTRA_USER_NAME).toString()
            username = value
        }
        setUpBoard()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.menu_refresh -> {
                if(memoryGame.getNumMoves() > 0 && memoryGame.numPairsFound > 0){
                    showAlertDialog("Quit your current game", null, View.OnClickListener {
                        setUpBoard()
                    })
                }else{
                    setUpBoard()
                }
            }
            R.id.mi_new_size -> {
                showNewSizeDialog()
            }
            R.id.mi_custom -> {
                showCreationDialog()
                return true
            }
            R.id.mi_download -> {
                showDownloadDialog()
                return true
            }
            R.id.mi_logout ->{
                logoutUser()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun logoutUser() {
        startActivity(Intent(this@MainActivity, LoginActivity::class.java))
        finish()
        return
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == CREATE_REQUEST_CODE && resultCode == Activity.RESULT_OK){
            val customName = data?.getStringExtra(EXTRA_GAME_NAME)
            if(customName == null){
                Log.d( TAG,"Got null name from create activity")
                return
            }
            downloadGame(customName)
        }else{
            Log.d( TAG,"Request code does not match")
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun downloadGame(customGameName: String) {
        Log.d( TAG,"Downloading game")
        db.collection("games").document("$username $customGameName").get().addOnSuccessListener { document ->
            val userImageList = document.toObject(UserImageList::class.java)
            if(userImageList?.images == null){
                Log.d( TAG,"Invalid custom game data from firestore")
                Snackbar.make(clRoot, "Sorry, we couldn't find such a game $customGameName under username $username", Snackbar.LENGTH_SHORT).show()
                return@addOnSuccessListener
            }
            Log.d( TAG,"Game downloaded successfully with images : " + userImageList.images)
            val numCards = userImageList.images.size * 2
            boardSize = BoardSize.getByValue(numCards)
            gameName = customGameName
            customGameImages = userImageList.images
            for (imageUrl in userImageList.images){
                Picasso.get().load(imageUrl).fetch()
            }
            Snackbar.make(clRoot, "You're now playing $gameName", Snackbar.LENGTH_SHORT).show()
            setUpBoard()
        }.addOnFailureListener{
            Log.d( TAG,"Exception when retrieving game name ${it.message}")
        }
    }

    private fun setUpBoard() {
        Log.d( TAG,"Setting up board game")
        supportActionBar?.title = gameName ?: getString(R.string.app_name)
        when(boardSize){
            BoardSize.EASY -> {
                tvNumMoves.text = "Easy: 4 x 2"
                tvNumPairs.text = "Pairs: 0 / 4"
            }
            BoardSize.MEDIUM -> {
                tvNumMoves.text = "Easy: 6 x 3"
                tvNumPairs.text = "Pairs: 0 / 9"
            }
            BoardSize.HARD -> {
                tvNumMoves.text = "Easy: 6 x 4"
                tvNumPairs.text = "Pairs: 0 / 12"
            }
        }
        tvNumPairs.setTextColor(ContextCompat.getColor(this, R.color.color_progress_none))
        memoryGame = MemoryGame(boardSize, customGameImages)
        adapter = MemoryBoardAdapter(this, boardSize, memoryGame.cards, object: MemoryBoardAdapter.CardClickListener{
            override fun onCardClicked(position: Int) {
                Log.i(TAG, "Card clicked $position")
                updateGameWithFlip(position)
            }
        })
        rvBoard.adapter = adapter
        rvBoard.setHasFixedSize(true)
        rvBoard.layoutManager = GridLayoutManager(this, boardSize.getWidth());
    }

    private fun showCreationDialog() {
        val board_size_view = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = board_size_view.findViewById<RadioGroup>(R.id.radiobutton2)
        showAlertDialog("Create your won memory board", board_size_view, View.OnClickListener {
            //Set new value for the board size variable
            val desiredBoardSize = when(radioGroupSize.checkedRadioButtonId){
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            //Navigate to new activity
            val intent = Intent(this, CreateActivity::class.java)
            intent.putExtra(EXTRA_BOARD_SIZE, desiredBoardSize)
            intent.putExtra(EXTRA_USER_NAME, username)
            startActivityForResult(intent, CREATE_REQUEST_CODE)
        })
    }

    private fun showNewSizeDialog() {
        val board_size_view = LayoutInflater.from(this).inflate(R.layout.dialog_board_size, null)
        val radioGroupSize = board_size_view.findViewById<RadioGroup>(R.id.radiobutton2)
        when (boardSize) {
            BoardSize.EASY -> radioGroupSize.check(R.id.rbEasy)
            BoardSize.MEDIUM -> radioGroupSize.check(R.id.rbMedium)
            BoardSize.HARD -> radioGroupSize.check(R.id.rbHard)
        }
        showAlertDialog("Choose new size", board_size_view, View.OnClickListener {
            //Set new value for the board size variable
            boardSize = when(radioGroupSize.checkedRadioButtonId){
                R.id.rbEasy -> BoardSize.EASY
                R.id.rbMedium -> BoardSize.MEDIUM
                else -> BoardSize.HARD
            }
            gameName = null
            customGameImages = null
            setUpBoard()
        })
    }

    private fun showAlertDialog(title : String, view: View?, positiveButtonClickListener: View.OnClickListener) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(view)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("OK"){_,_ ->
                positiveButtonClickListener.onClick(null)
            }.show()
    }

    private fun updateGameWithFlip(position: Int) {
        //Error handling
        if(memoryGame.haveWonGame()){
            //Alert user  of an invalid move
            Snackbar.make(clRoot, "You already won", Snackbar.LENGTH_SHORT).show()
            return
        }
        if(memoryGame.isCardFaceUp(position)){
            //Alert user
            Snackbar.make(clRoot, "Invalid move!", Snackbar.LENGTH_SHORT).show()
            return
        }
        //actually flip over the card
        if(memoryGame.flipCard(position)){
            val color = ArgbEvaluator().evaluate(
                memoryGame.numPairsFound.toFloat() / boardSize.getNumPairs(),
                ContextCompat.getColor(this, R.color.color_progress_none),
                ContextCompat.getColor(this, R.color.color_progress_full)
            ) as Int
            tvNumPairs.setTextColor(color)
            tvNumPairs.text = "Pairs: ${memoryGame.numPairsFound}/ ${boardSize.getNumPairs()}"
            if(memoryGame.haveWonGame()){
                Snackbar.make(clRoot, "You won! Congratulations.", Snackbar.LENGTH_LONG).show()
                CommonConfetti.rainingConfetti(clRoot, intArrayOf(Color.YELLOW, Color.BLUE, Color.MAGENTA)).oneShot()
            }
        }
        tvNumMoves.text = "Move: ${memoryGame.getNumMoves()}"
        adapter.notifyDataSetChanged()
    }

    private fun showDownloadDialog() {
        val boardDownloadView = LayoutInflater.from(this).inflate(R.layout.dialog_board, null)
        showAlertDialog("Fetch memory game", boardDownloadView, View.OnClickListener {
            // Grab the text of the game name that the user wants to download
            val etDownloadgame = boardDownloadView.findViewById<EditText>(R.id.etDownloadGame)
            val gameToDownload = etDownloadgame.text.toString().trim()
            downloadGame(gameToDownload)
        })
    }
}