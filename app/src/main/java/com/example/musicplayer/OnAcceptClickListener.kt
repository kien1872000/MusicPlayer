package com.example.musicplayer

import com.example.musicplayer.Models.Song

interface OnAcceptClickListener {
    fun onClick(songs: ArrayList<Song>)
}