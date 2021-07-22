package com.example.musicplayer


interface OnPlaylistItemChangeListener {
    fun onDeletePlaylistItem(position: Int)
    fun onAddPlaylistItem()
}