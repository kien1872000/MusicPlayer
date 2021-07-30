package com.example.musicplayer.Models

import android.provider.BaseColumns
import java.io.Serializable

class Song(var _id: Long, var name: String, var album: String, var path: String, var artist: String,  var isFavorite: Int, var heardTimes: Int): Serializable {
    object SongEntry : BaseColumns {
        const val TABLE_NAME = "songs"
        const val COLUMN_NAME = "name"
        const val COLUMN_ALBUM = "album"
        const val COLUMN_PATH = "path"
        const val COLUMN_ARTIST = "artist"
        const val COLUMN_IS_FAVORITE = "isFavorite"
        const val COLUMN_HEARD_TIMES = "hearTimes"
    }

}