package com.example.shivamcreation

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.OutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var imageView: ImageView
    private lateinit var editText: EditText
    private lateinit var btnSelect: Button
    private lateinit var btnSave: Button
    private lateinit var btnShare: Button
    private var selectedBitmap: Bitmap? = null

    companion object {
        private const val PICK_IMAGE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
            1
        )

        imageView = findViewById(R.id.imageView)
        editText = findViewById(R.id.editText)
        btnSelect = findViewById(R.id.btnSelect)
        btnSave = findViewById(R.id.btnSave)
        btnShare = findViewById(R.id.btnShare)

        // Select Image from Gallery
        btnSelect.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, PICK_IMAGE)
        }

        // Save Image with Text
        btnSave.setOnClickListener {
            selectedBitmap?.let {
                val bitmapWithText = addTextToBitmap(it, editText.text.toString())
                saveImage(bitmapWithText)
                Toast.makeText(this, "Image Saved!", Toast.LENGTH_SHORT).show()
            }
        }

        // Share Image
        btnShare.setOnClickListener {
            selectedBitmap?.let {
                val bitmapWithText = addTextToBitmap(it, editText.text.toString())
                shareImage(bitmapWithText)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == Activity.RESULT_OK) {
            val imageUri: Uri? = data?.data
            selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageUri)
            imageView.setImageBitmap(selectedBitmap)
        }
    }

    private fun addTextToBitmap(bitmap: Bitmap, text: String): Bitmap {
        val mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
        val canvas = Canvas(mutableBitmap)

        val paint = android.graphics.Paint().apply {
            color = Color.WHITE
            textSize = 60f
            isAntiAlias = true
            setShadowLayer(5f, 0f, 0f, Color.BLACK)
        }

        canvas.drawText(text, 50f, (bitmap.height - 50).toFloat(), paint)
        return mutableBitmap
    }

    private fun saveImage(bitmap: Bitmap) {
        val filename = "ShivamCreation_${System.currentTimeMillis()}.png"
        val resolver = contentResolver
        val contentValues = android.content.ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, filename)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/ShivamCreation")
        }

        val imageUri = resolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
        )

        val outputStream: OutputStream? = imageUri?.let {
            resolver.openOutputStream(it)
        }

        outputStream?.use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
    }

    private fun shareImage(bitmap: Bitmap) {
        val path = MediaStore.Images.Media.insertImage(
            contentResolver, bitmap, "ShivamCreation", null
        )
        val uri = Uri.parse(path)

        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/png"
        }
        startActivity(Intent.createChooser(shareIntent, "Share Image"))
    }
}
