package com.example.gallery

import android.content.Context
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val coroutineExceptionHandler =
        CoroutineExceptionHandler { _, throwable ->
            Log.d(
                this@MainViewModel::class.java.simpleName,
                "Coroutine Exception Handler : ${throwable.message}"
            )
        }

    private val mediaItemList = ArrayList<GalleryMediaItem>()

    private var mediaLoadJob: Job? = null

    private val _notifyMediaItem = MutableSharedFlow<NotifyDataSetChanged>()
    val notifyMediaItem: SharedFlow<NotifyDataSetChanged> = _notifyMediaItem.asSharedFlow()


    fun getImageMediaList() {
        mediaLoadJob = CoroutineScope(Dispatchers.IO + coroutineExceptionHandler).launch {
            MediaLoader.getImageMediaItems(
                context = context,
                emitSize = 500,
                callback = { list ->
                    val startIndex = mediaItemList.size
                    mediaItemList.addAll(list)

                    launch(Dispatchers.Main) {
                        _notifyMediaItem.emit(NotifyDataSetChanged(startIndex, mediaItemList.size))
                    }
                }
            )
        }
    }

    fun getMediaItemList() = mediaItemList

    data class NotifyDataSetChanged(
        val startIndex: Int,
        val endIndex: Int
    )
}