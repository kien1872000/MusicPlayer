package com.example.musicplayer

import com.example.musicplayer.Models.Song

interface OnCreatePlaylistAcceptListener {
    fun onAccept(songs: ArrayList<Song>, playlistName: String)
}