package com.example.gallery

import android.content.Context
import android.media.ExifInterface
import android.provider.MediaStore
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

object MediaLoader {

    private const val ONE_DAY = 60 * 60 * 24 * 1000

    fun getImageMediaItems(
        context: Context,
        emitSize: Int,
        callback: (List<GalleryMediaItem>) -> Unit
    ) {
        val galleryMediaItemList = ArrayList<GalleryMediaItem>()
        var emitCount = 0

        val queryProjection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_TAKEN,
            MediaStore.Images.Media.DATE_MODIFIED
        )

        val orderBy = "${MediaStore.Images.Media.DATE_TAKEN} DESC, " +
                "${MediaStore.Images.Media._ID} DESC"

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            queryProjection,
            null,
            null,
            orderBy
        )?.use { cursor ->
            val idIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
            val absoluteUriIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
            val dateTakenIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)
            val dateModifiedIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_MODIFIED)

            val noDateTakenList = getNoDateTakenList(context)
            var noDateTakenListIndex = 0


            if (cursor.moveToFirst()) {
                do {
                    val dateTaken = cursor.getLong(dateTakenIndex)
                    if (dateTaken == 0L) break

                    val absolutePath = cursor.getString(absoluteUriIndex)

                    GalleryMediaItem(
                        absolutePath = absolutePath
                    ).apply {
                        val dateTaken = cursor.getLong(dateTakenIndex)

                        while (noDateTakenListIndex < noDateTakenList.size) {
                            if (noDateTakenList[noDateTakenListIndex].dateTaken >= dateTaken) {
                                galleryMediaItemList.add(noDateTakenList[noDateTakenListIndex++])

                            } else {
                                break
                            }
                        }

                        galleryMediaItemList.add(this@apply)
                    }

                    if (galleryMediaItemList.size % emitSize == 0) {
                        val fromIdx = emitCount * emitSize
                        val toIdx = (emitCount + 1) * emitSize
                        galleryMediaItemList.slice(fromIdx until toIdx).run {
                            callback(this)
                        }
                        emitCount++
                    }

                } while (cursor.moveToNext())
            }

            val remainCount = galleryMediaItemList.size % emitSize
            if (galleryMediaItemList.size > 0 && (remainCount > 0)) {
                val fromIdx = emitCount * emitSize
                galleryMediaItemList.slice(fromIdx until fromIdx + remainCount).run {
                    callback(this)
                }
            }

        }
    }

    private fun getNoDateTakenList(
        context: Context
    ): ArrayList<GalleryMediaItem> {

        val noDateTakenList = ArrayList<GalleryMediaItem>()

        val queryProjection = arrayOf(
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.DATE_TAKEN
        )

        val orderBy = MediaStore.Images.Media.DATE_TAKEN + " DESC"

        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            queryProjection,
            null,
            null,
            orderBy
        )?.use { cursor ->

            val absoluteUriIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATA)
            val dateTakenIndex = cursor.getColumnIndex(MediaStore.Images.Media.DATE_TAKEN)

            if (cursor.moveToLast()) {
                do {
                    val dateTaken = cursor.getLong(dateTakenIndex)
                    if (dateTaken > 0L) break

                    val absolutePath = cursor.getString(absoluteUriIndex)
                    GalleryMediaItem(
                        absolutePath = absolutePath
                    ).apply {
                        this.dateTaken = getReplacedDateTaken(absolutePath)
                        noDateTakenList.add(this@apply)
                    }

                } while (cursor.moveToPrevious())
            }
        }

        noDateTakenList.sortByDescending { it.dateTaken }
        return noDateTakenList
    }

    private fun getReplacedDateTaken(filePath: String): Long {
        val exif = ExifInterface(filePath)
        val exifDateFormat = exif.getAttribute(ExifInterface.TAG_DATETIME)
        val exifTime: Long = exifDateFormat?.let {
            SimpleDateFormat("yyyy:MM:dd HH:mm:ss", Locale.getDefault()).parse(it)?.time ?: -1L
        } ?: -1L

        val fileLastModified: Long = File(filePath).lastModified()

        return if (exifTime == -1L) {
            fileLastModified
        } else {
            if (Math.abs(fileLastModified - exifTime) < ONE_DAY) {
                fileLastModified
            } else {
                exifTime
            }
        }
    }

}