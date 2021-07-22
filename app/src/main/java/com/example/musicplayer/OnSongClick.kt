package com.example.musicplayer

interface OnSongClick {
   fun onClickItem(position: Int)
   fun onDeleteItem(position: Int)
}