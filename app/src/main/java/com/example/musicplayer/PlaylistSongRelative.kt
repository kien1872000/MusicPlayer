package com.example.musicplayer

import android.provider.BaseColumns

object PlaylistSongRelative {
    object PlaylistSongRelativeEntry: BaseColumns{
        const val TABLE_NAME = "playlistSongRelative"
        const val COLUMN_SONG_ID = "songId"
        const val COLUMN_PLAYLIST_ID = "playlistId"
    }
}