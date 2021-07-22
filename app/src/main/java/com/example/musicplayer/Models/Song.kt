package com.example.musicplayer.Models

import java.io.Serializable

class Song(var name: String, var album: String, var path: String, var artist: String, var duration: String): Serializable {
}