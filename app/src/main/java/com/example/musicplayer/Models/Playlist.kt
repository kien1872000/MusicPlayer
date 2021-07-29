package com.example.musicplayer.Models

import android.provider.BaseColumns

class Playlist(var _id: Long, var songId: Long, var name: String) {
       object PlaylistEntry: BaseColumns{
        const val TABLE_NAME = "playlists"
        const val COLUMN_SONG_ID = "songId"
        const val COLUMN_NAME = "name"
    }
}