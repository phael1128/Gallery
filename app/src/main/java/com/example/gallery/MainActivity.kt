package com.example.gallery

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.gallery.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val viewModel: MainViewModel by lazy {
        ViewModelProvider(this)[MainViewModel::class.java]
    }

    private val mediaItemListAdapter: GalleryAdapter by lazy {
        GalleryAdapter(
            viewModel.getMediaItemList(),
            Glide.with(this@MainActivity)
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        with (binding) {
            rvImages.adapter = mediaItemListAdapter
            rvImages.layoutManager = GridLayoutManager(this@MainActivity, 3)
        }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                onUiCollect()
            }
        }
        requestPermission()
    }

    private suspend fun onUiCollect() {
        viewModel.notifyMediaItem.collect {
            mediaItemListAdapter.notifyItemRangeChanged(it.startIndex, it.endIndex)
        }
    }

    private fun requestPermission() {
        if (!checkStoragePermission()) {
            ActivityCompat.requestPermissions(
                this@MainActivity,
                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
                REQUEST_CODE_IMAGE_PERMISSIONS
            )
        } else {
            Log.d("phael", "권한 부여 확인")
            viewModel.getImageMediaList()
        }
    }

    private fun checkStoragePermission() : Boolean {
        return ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_IMAGE_PERMISSIONS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission granted", Toast.LENGTH_SHORT).show()
                viewModel.getImageMediaList()
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }

        }
    }

    companion object {
        private const val REQUEST_CODE_IMAGE_PERMISSIONS = 1001
    }
}