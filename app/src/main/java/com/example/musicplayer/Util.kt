package com.example.musicplayer

import android.media.MediaMetadataRetriever

class Util {
    companion object {
        fun getAlbumArt(uri: String): ByteArray? {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(uri)
            val art = retriever.embeddedPicture
            retriever.release()
            return art
        }
    }
}