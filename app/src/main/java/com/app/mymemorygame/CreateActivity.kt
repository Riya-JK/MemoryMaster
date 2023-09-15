package com.app.mymemorygame

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.mymemorygame.models.BoardSize
import com.app.mymemorygame.utils.EXTRA_BOARD_SIZE
import com.app.mymemorygame.utils.EXTRA_GAME_NAME
import com.app.mymemorygame.utils.PermissionUtils
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

class CreateActivity : AppCompatActivity() , ImagePickerAdapter.ImageClickListener{

    private val TAG: String = CreateActivity::class.java.simpleName
    private var numImagesRequired: Int = -1
    private lateinit var boardSize: BoardSize
    private lateinit var rvImagePicker: RecyclerView
    private lateinit var etGameName : EditText
    private lateinit var btnSave : Button
    private val chosenImageUris = mutableListOf<Uri>()
    private lateinit var adapter : ImagePickerAdapter
    private val storage = Firebase.storage
    private val db = Firebase.firestore
    private lateinit var pbUploading : ProgressBar

    companion object{
        val PICK_PHOTOS: Int = 655
        const val READ_PHOTOS_PERMISSION = android.Manifest.permission.READ_EXTERNAL_STORAGE
        const val READ_EXTERNAL_PHOTOS_CODE = 248
        private const val MIN_GAME_LENGTH = 3
        private const val MAX_GAME_LENGTH = 14
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)

        rvImagePicker = findViewById(R.id.rv_image_picker)
        etGameName = findViewById(R.id.editTextTGameName)
        btnSave = findViewById(R.id.btnSave)
        pbUploading = findViewById(R.id.pbUploading)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val boardSize = intent.getSerializableExtra(EXTRA_BOARD_SIZE) as BoardSize
        numImagesRequired = boardSize.getNumPairs()
        supportActionBar?.title = "Choose pics (0 / $numImagesRequired)"

        btnSave.setOnClickListener(View.OnClickListener {
            saveDataToFireBase()
        })
        etGameName.filters = arrayOf(InputFilter.LengthFilter(MAX_GAME_LENGTH))
        etGameName.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int){}

            override fun afterTextChanged(p0: Editable?) {
                btnSave.isEnabled = shouldEnableSaveButton()
            }

        })
        adapter = ImagePickerAdapter(this, chosenImageUris, boardSize, object: ImagePickerAdapter.ImageClickListener{
            override fun onPlaceholderClicked() {
                if(PermissionUtils.isPermissionGranted(this@CreateActivity, READ_PHOTOS_PERMISSION)){
                    launchIntentForPhotos()
                }else{
                    PermissionUtils.requestPermission(this@CreateActivity, READ_PHOTOS_PERMISSION, READ_EXTERNAL_PHOTOS_CODE)
                }

            }

        })
        rvImagePicker.adapter = adapter
        rvImagePicker.setHasFixedSize(true)
        rvImagePicker.layoutManager = GridLayoutManager(this, boardSize.getWidth())
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if(requestCode == READ_EXTERNAL_PHOTOS_CODE){
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                launchIntentForPhotos()
            }else{
                Toast.makeText(this, "In order to create a custom game, you need to provide access to your photos", Toast.LENGTH_SHORT).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode != PICK_PHOTOS || resultCode != Activity.RESULT_OK || data == null){
            Log.d(TAG,"onActivityResult() called with: requestCode = $requestCode, resultCode = $resultCode, data = $data")
            return
        }
        val selectedUri = data.data
        val clipData = data.clipData
        if(clipData != null){
            Log.d(TAG,"clipData numImages = ${clipData.itemCount} , clipData = $clipData, data = $data")
            for(i in 0 until clipData.itemCount){
                val clipItem = clipData.getItemAt(i)
                if(chosenImageUris.size < numImagesRequired){
                    chosenImageUris.add(clipItem.uri)
                }
            }
        }else if(selectedUri != null){
            Log.d(TAG,"data = $data")
            chosenImageUris.add(selectedUri)
        }
        adapter.notifyDataSetChanged()
            supportActionBar?.title = "Choose pics (${chosenImageUris.size} / $numImagesRequired)"
        btnSave.isEnabled = shouldEnableSaveButton()
    }

    private fun shouldEnableSaveButton(): Boolean {
        //Check idf we should enable save button
        if(chosenImageUris.size != numImagesRequired){
            return false
        }
        if(etGameName.text.isBlank() || etGameName.text.length < MIN_GAME_LENGTH){
            return false
        }
        return true
    }

    private fun launchIntentForPhotos() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        startActivityForResult(Intent.createChooser(intent, "Choose pics"), PICK_PHOTOS)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onPlaceholderClicked() {
        TODO("Not yet implemented")
    }

    private fun saveDataToFireBase() {
        Log.d(TAG, "saveDataToFireBase() called")
        btnSave.isEnabled = false
        val customGameName : String = etGameName.text.toString()
        //check that we're not overwriting the same name
        db.collection("games").document(customGameName).get().addOnSuccessListener {
            if(it != null && it.data != null){
                AlertDialog.Builder(this)
                    .setTitle("Name Taken")
                    .setMessage("A game already exists with the name $customGameName. Please choose another one")
                    .setPositiveButton("OK", null)
                    .show()
                btnSave.isEnabled = true
            }else{
                handleImageUploading(customGameName)
            }
        }.addOnFailureListener {
            Log.d(TAG, "Emcountered error while saving memory game ${it.message}")
            Toast.makeText(this, "Encountered error while saving memory game", Toast.LENGTH_LONG).show()
            btnSave.isEnabled = true
        }
    }

    private fun handleImageUploading(gameName : String) {
        pbUploading.visibility = View.VISIBLE
        val uploadedImageUrl  = mutableListOf<String>()
        var didEncounterError = false
        for((index, photoUri) in chosenImageUris.withIndex()){
            val imageByteArray = gatImageByteArray(photoUri)
            val filePath = "images/$gameName/${System.currentTimeMillis()}-${index}.jpg"
            val photoReference = storage.reference.child(filePath)
            photoReference.putBytes(imageByteArray)
                .continueWith{
                    Log.d(TAG, "upload bytes : ${it.result?.bytesTransferred}")
                    photoReference.downloadUrl
                }.addOnCompleteListener{downloadUrlTask ->
                    if(!downloadUrlTask.isSuccessful){
                        Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                        didEncounterError = true
                        return@addOnCompleteListener
                    }
                    if(didEncounterError){
                        return@addOnCompleteListener
                        pbUploading.visibility = View.GONE
                    }
                    downloadUrlTask.result.addOnSuccessListener {task ->
                        var url = task.toString()
                        uploadedImageUrl.add(url)
                        pbUploading.progress = uploadedImageUrl.size * 100 / chosenImageUris.size
                        Log.d(TAG,"Finish uploading photo uri $photoUri, num uploads = ${uploadedImageUrl.size}")
                        if(uploadedImageUrl.size == chosenImageUris.size){
                            handleAllImagesUploaded(gameName, uploadedImageUrl)
                        }
                    }.addOnFailureListener(){
                        Log.d(TAG, "Uploading images failed")
                    }
                }
        }
    }

    private fun handleAllImagesUploaded(gameName: String, imageUrls: MutableList<String>) {
        //Upload  this info to firestore
        Log.d(TAG,"Image urls $imageUrls")
        db.collection("games").document(gameName)
            .set(mapOf("images" to imageUrls))
            .addOnCompleteListener{gameCreationTask ->
                pbUploading.visibility = View.GONE
                if(!gameCreationTask.isSuccessful) {
                    Log.d(TAG,"Exception with game creation = ${gameCreationTask.exception}")
                    Toast.makeText(this, "Failed game creation", Toast.LENGTH_SHORT).show()
                    return@addOnCompleteListener
                }
                Log.d(TAG,"Successfully created game = $gameCreationTask")
                AlertDialog.Builder(this)
                    .setTitle("Upload complete! Let's play your game $gameName")
                    .setPositiveButton("OK"){ _,_ ->
                        val resultData = Intent()
                        resultData.putExtra(EXTRA_GAME_NAME, gameName)
                        setResult(Activity.RESULT_OK,resultData)
                        finish()
                    }.show()
            }
    }

    private fun gatImageByteArray(photoUri: Uri): ByteArray {
        val originalBitMap = if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            val source = ImageDecoder.createSource(contentResolver, photoUri)
            ImageDecoder.decodeBitmap(source)
        }else{
            MediaStore.Images.Media.getBitmap(contentResolver, photoUri)
        }
        Log.d(TAG, "Original width ${originalBitMap.width} and ${originalBitMap.height}")
        val scaledBitmap = BitmapScaler.scaleToHeight(originalBitMap, 250)
        val byteOutputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60 , byteOutputStream)
        return byteOutputStream.toByteArray()
    }
}