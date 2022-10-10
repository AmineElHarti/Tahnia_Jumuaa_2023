package com.nukelaa.tahniajumuaa2023

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.storage.FirebaseStorage
import com.nukelaa.tahniajumuaa2023.databinding.PhotosCardviewBinding
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream


class PhotoAdapter(var context: Context, private val urlList: List<String>) :
    RecyclerView.Adapter<PhotoAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        return ViewHolder(
            PhotosCardviewBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val url = urlList[position]
        holder.bindItem(url)
    }

    override fun getItemCount(): Int {
        return urlList.size
    }

    inner class ViewHolder(private val itemBinding: PhotosCardviewBinding) :
        RecyclerView.ViewHolder(itemBinding.root) {

        fun bindItem(url: String) {

            val storage = FirebaseStorage.getInstance()
            val imagePath = "images/$url.jpg"
            val storageRef = storage.reference.child(imagePath)
//            GlideApp.with(context)
//                .load(storageRef)
//                .into(itemBinding.image)
            Glide.with(context).load(storageRef).diskCacheStrategy(DiskCacheStrategy.ALL)
                .thumbnail(Glide.with(context).load(R.drawable.loading))
                .into(itemBinding.image)
            itemBinding.btSave.setOnClickListener {
                val bitmap = itemBinding.image.drawable.toBitmap()
                saveBitmapAsImageToDevice(context, bitmap)
            }
            itemBinding.btShare.setOnClickListener {
                val bitmap = itemBinding.image.drawable.toBitmap()
                shareImageAndText(bitmap)
            }
        }
    }

    private fun shareImageAndText(bitmap: Bitmap) {
        val uri: Uri = getImageToShare(bitmap)!!
        val intent = Intent(Intent.ACTION_SEND)

        // putting uri of image to be shared
        intent.putExtra(Intent.EXTRA_STREAM, uri)

        // adding text to share
        intent.putExtra(Intent.EXTRA_TEXT, "Sharing Image")

        // Add subject Here
        intent.putExtra(Intent.EXTRA_SUBJECT, "Subject Here")

        // setting type to image
        intent.type = "image/jpg"

        // calling startactivity() to share
        startActivity(context, Intent.createChooser(intent, "Share Via"), null)
    }

    private fun getImageToShare(bitmap: Bitmap): Uri? {
        val imageFolder = File(context.cacheDir, "images")
        var uri: Uri? = null
        try {
            imageFolder.mkdirs()
            val file = File(imageFolder, "shared_image.png")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
            uri = FileProvider.getUriForFile(
                context,
                "com.nukelaa.tahniajumuaa2023.fileProvider",
                file
            )
        } catch (e: Exception) {
            Toast.makeText(context, "ex: " + e.message, Toast.LENGTH_LONG).show()
        }
        return uri
    }

    private fun saveBitmapAsImageToDevice(context: Context, bitmap: Bitmap?) {
        // Add a specific media item.
        val resolver = context.contentResolver

        val imageStorageAddress = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
        } else {
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        }

        val imageDetails = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "my_app_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis())
        }

        try {
            // Save the image.
            val contentUri: Uri? = resolver.insert(imageStorageAddress, imageDetails)
            contentUri?.let { uri ->
                // Don't leave an orphan entry in the MediaStore
                if (bitmap == null) resolver.delete(contentUri, null, null)
                val outputStream: OutputStream? = resolver.openOutputStream(uri)
                outputStream?.let { outStream ->
                    val isBitmapCompressed =
                        bitmap?.compress(Bitmap.CompressFormat.JPEG, 95, outStream)
                    if (isBitmapCompressed == true) {
                        outStream.flush()
                        outStream.close()
                        Toast.makeText(
                            context,
                            context.resources.getString(R.string.photo_saved),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } ?: throw IOException("Failed to get output stream.")
            } ?: throw IOException("Failed to create new MediaStore record.")
        } catch (e: IOException) {
            throw e
        }
    }
}