package com.leon.android.filechecker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.OpenableColumns
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.leon.android.filechecker.databinding.ActivityMainBinding
import java.math.BigInteger
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {
    lateinit var viewBinding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        permissionRequest()
        viewBinding.chooseFileBtn.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_OPEN_DOCUMENT
            openFile()
        }
    }

    // Request code for selecting a PDF document.
    val PICK_FILE = 2

    fun openFile() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            // Optionally, specify a URI for the file that should appear in the
            // system file picker when it loads.
//            putExtra(DocumentsContract.EXTRA_INITIAL_URI, pickerInitialUri)
        }

        startActivityForResult(intent, PICK_FILE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE && resultCode == RESULT_OK) {
            data?.data?.also { uri ->
                Thread(Runnable {
                    val resolver = applicationContext.contentResolver
                    resolver.query(uri, null, null, null, null)
                        ?.use { returnCursor ->
                            val nameIndex =
                                returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                            val sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE)
                            returnCursor.moveToFirst()
                            val fileName = returnCursor.getString(nameIndex)
                            val fileSize = returnCursor.getLong(sizeIndex)
                            runOnUiThread {
                                viewBinding.fileInfoTV.text = "文件名：$fileName\n文件大小：$fileSize"
                            }
                            resolver.openInputStream(uri).use { stream ->
                                if (stream != null) {
                                    val buffer = ByteArray(8192)
                                    var len = 0
                                    val md = MessageDigest.getInstance("MD5")

                                    len = stream.read(buffer)
                                    var processedLength = len.toLong()
                                    while (len != -1) {
                                        md.update(buffer,0,len)
                                        len = stream.read(buffer)
                                        if (len != -1)
                                        processedLength += len
                                        runOnUiThread {
                                            viewBinding.Log.text = "$processedLength / $fileSize"
                                        }
                                    }

                                    val bytes = md.digest()
                                    val bi = BigInteger(1, bytes)
                                    runOnUiThread {
                                        viewBinding.md5TV.text = bi.toString(16)
                                    }
                                }
                            }
                        }


                }).start()

            }
        } else {

        }
    }


    private fun permissionRequest() {
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    // Permission is granted. Continue the action or workflow in your
                    // app.
                } else {
                    // Explain to the user that the feature is unavailable because the
                    // features requires a permission that the user has denied. At the
                    // same time, respect the user's decision. Don't link to system
                    // settings in an effort to convince the user to change their
                    // decision.
                }
            }
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                // You can use the API that requires the permission.
            }
            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            }
        }
    }
}