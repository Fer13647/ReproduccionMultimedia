package com.ferr.reproduccionmultimedia

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ferr.reproduccionmultimedia.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    lateinit var binding : ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_READ_EXTERNAL_STORAGE
            )
        } else {
            loadLatestImage()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_READ_EXTERNAL_STORAGE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadLatestImage()
            } else {
                Toast.makeText(binding.root.context, "No hay permisos para acceder", Toast.LENGTH_SHORT).show()

            }
        }
    }

    private fun loadLatestImage() {
        GlobalScope.launch(Dispatchers.Main) {
            val imageUri = withContext(Dispatchers.IO) {
                getLatestImageUri()
            }
            if (imageUri != null) {
                binding.imageView.setImageURI(imageUri)
            } else {
                Toast.makeText(binding.root.context, "No se encontró ninguna imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private suspend fun getLatestImageUri(): Uri? = withContext(Dispatchers.IO) {
        val projection = arrayOf(MediaStore.Images.Media._ID)
        val selection = "${MediaStore.Images.Media.MIME_TYPE} = ?"
        val selectionArgs = arrayOf("image/jpeg")
        val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"

        contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            val columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            if (cursor.moveToFirst()) {
                val imageId = cursor.getLong(columnIndex)
                ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    imageId
                )
            } else {
                null
            }
        }
    }

    companion object {
        private const val PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 100
    }
}
