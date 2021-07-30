package com.example.musicplayer

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import com.example.musicplayer.Models.Playlist
import com.example.musicplayer.Models.Song


class MusicPlayerDbHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {
    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "MusicPlayer.db"
        private const val SQL_CREATE_TABLE_SONGS =
            "CREATE TABLE ${Song.SongEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${Song.SongEntry.COLUMN_NAME} TEXT," +
                    "${Song.SongEntry.COLUMN_ALBUM} TEXT," +
                    "${Song.SongEntry.COLUMN_ARTIST} TEXT," +
                    "${Song.SongEntry.COLUMN_PATH} TEXT," +
                    "${Song.SongEntry.COLUMN_IS_FAVORITE} INTEGER," +
                    "${Song.SongEntry.COLUMN_HEARD_TIMES} INTEGER)"

        private const val SQL_CREATE_TABLE_PLAYLISTS =
            "CREATE TABLE ${Playlist.PlaylistEntry.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${Playlist.PlaylistEntry.COLUMN_NAME} TEXT," +
                    "${Playlist.PlaylistEntry.COLUMN_SONG_ID} TEXT)"
        private const val SQL_CREATE_TABLE_PLAYLIST_SONG_RELATIVE =
            "CREATE TABLE ${PlaylistSongRelative.PlaylistSongRelativeEntry.TABLE_NAME}("+
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${PlaylistSongRelative.PlaylistSongRelativeEntry.COLUMN_PLAYLIST_ID} INTEGER," +
                    "${PlaylistSongRelative.PlaylistSongRelativeEntry.COLUMN_SONG_ID} INTEGER)"

    }
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_TABLE_SONGS)
        db.execSQL(SQL_CREATE_TABLE_PLAYLIST_SONG_RELATIVE)
        db.execSQL(SQL_CREATE_TABLE_PLAYLISTS)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_CREATE_TABLE_SONGS)
        db.execSQL(SQL_CREATE_TABLE_PLAYLIST_SONG_RELATIVE)
        db.execSQL(SQL_CREATE_TABLE_PLAYLISTS)
        onCreate(db)
    }
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }
    fun insertSongs(songs: ArrayList<Song>) {
        val db = this.writableDatabase
        for(song in songs) {
            val values = ContentValues().apply {
                put(Song.SongEntry.COLUMN_ALBUM, song.album)
                put(Song.SongEntry.COLUMN_ARTIST, song.artist)
                put(Song.SongEntry.COLUMN_NAME, song.name)
                put(Song.SongEntry.COLUMN_PATH, song.path)
                put(Song.SongEntry.COLUMN_IS_FAVORITE, song.isFavorite)
                put(Song.SongEntry.COLUMN_HEARD_TIMES, song.heardTimes)
            }
            db?.insert(Song.SongEntry.TABLE_NAME, null, values)
        }

    }
    fun getAllSong(): ArrayList<Song> {
        val db = this.readableDatabase
        val cursor = db.query(Song.SongEntry.TABLE_NAME, null, null, null, null, null, null)
        val songs = ArrayList<Song>()
        with(cursor) {
            while (moveToNext()) {
                val song = Song(
                    getLong(getColumnIndexOrThrow(BaseColumns._ID)),
                    getString(getColumnIndexOrThrow(Song.SongEntry.COLUMN_NAME)),
                    getString(getColumnIndexOrThrow(Song.SongEntry.COLUMN_ALBUM)),
                    getString(getColumnIndexOrThrow(Song.SongEntry.COLUMN_PATH)),
                    getString(getColumnIndexOrThrow(Song.SongEntry.COLUMN_ARTIST)),
                    getInt(getColumnIndexOrThrow(Song.SongEntry.COLUMN_IS_FAVORITE)),
                    getInt(getColumnIndexOrThrow(Song.SongEntry.COLUMN_HEARD_TIMES))
                )
                songs.add(song)
            }
        }
        return songs
    }
    fun addPlaylist(playlistName: String, songs: ArrayList<Song>): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(Playlist.PlaylistEntry.COLUMN_NAME, playlistName)
        }
        val playlistId = db!!.insert(Playlist.PlaylistEntry.TABLE_NAME, null, values)
        if(playlistId != (-1).toLong()) {
            addRelative(playlistId, songs)
        }
        return playlistId
    }
    fun addRelative(playlistId: Long, songs: ArrayList<Song>) {
        val db = this.writableDatabase
        for (song in songs) {
            val values = ContentValues().apply {
                put(PlaylistSongRelative.PlaylistSongRelativeEntry.COLUMN_SONG_ID, song._id)
                put(PlaylistSongRelative.PlaylistSongRelativeEntry.COLUMN_PLAYLIST_ID, playlistId)
            }
            db!!.insert(PlaylistSongRelative.PlaylistSongRelativeEntry.TABLE_NAME, null, values)
        }
    }
    fun getAllPlaylists(): ArrayList<Playlist> {
        val db = this.readableDatabase
        val cursor = db.query(Playlist.PlaylistEntry.TABLE_NAME, null, null, null, null, null, null)
        val playlists = ArrayList<Playlist>()
        with(cursor) {
            while (moveToNext()) {
                val playlist = Playlist(
                    getLong(getColumnIndexOrThrow(BaseColumns._ID)),
                    getLong(getColumnIndexOrThrow(Playlist.PlaylistEntry.COLUMN_SONG_ID)),
                    getString(getColumnIndexOrThrow(Playlist.PlaylistEntry.COLUMN_NAME))
                )
                playlists.add(playlist)
            }
        }
        return playlists
    }
    fun getSongsInPlaylist(playlistId: Long): ArrayList<Song> {
        val db = this.readableDatabase
        val plId = PlaylistSongRelative.PlaylistSongRelativeEntry.COLUMN_PLAYLIST_ID
        val songId = PlaylistSongRelative.PlaylistSongRelativeEntry.COLUMN_SONG_ID
        val id = BaseColumns._ID
        val name = Song.SongEntry.COLUMN_NAME
        val album = Song.SongEntry.COLUMN_ALBUM
        val path = Song.SongEntry.COLUMN_PATH
        val artist = Song.SongEntry.COLUMN_ARTIST
        val isFavorite = Song.SongEntry.COLUMN_IS_FAVORITE
        val heardTimes = Song.SongEntry.COLUMN_HEARD_TIMES
        val query = "SELECT S.$id, S.$name, S.$album, S.$path, S.$artist, S.$isFavorite, S.$heardTimes FROM " +
                "${Playlist.PlaylistEntry.TABLE_NAME} as P " +
                "INNER JOIN ${PlaylistSongRelative.PlaylistSongRelativeEntry.TABLE_NAME} as PR " +
                "ON P.$id = PR.$plId " +
                "INNER JOIN ${Song.SongEntry.TABLE_NAME} as S " +
                "ON PR.$songId=S.$id " +
                "WHERE P.$id = $playlistId"
        val cursor = db.rawQuery(query, null)
        val songs = ArrayList<Song>()
        with(cursor) {
            while (moveToNext()) {
                val song = Song(
                    getLong(getColumnIndexOrThrow(id)),
                    getString(getColumnIndexOrThrow(name)),
                    getString(getColumnIndexOrThrow(album)),
                    getString(getColumnIndexOrThrow(path)),
                    getString(getColumnIndexOrThrow(artist)),
                    getInt(getColumnIndexOrThrow(isFavorite)),
                    getInt(getColumnIndexOrThrow(heardTimes))
                )
                songs.add(song)
            }
        }
        return songs
    }
    fun deleteRelative(playlistId: Long) {
        val db = this.writableDatabase
        val selection = "${PlaylistSongRelative.PlaylistSongRelativeEntry.COLUMN_PLAYLIST_ID} = ?"
        val selectionArgs = arrayOf("$playlistId")
        db.delete(PlaylistSongRelative.PlaylistSongRelativeEntry.TABLE_NAME, selection, selectionArgs)
    }
    fun deletePlaylist(playlistId :Long) {
        val db = this.writableDatabase
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf("$playlistId")
        db.delete(Playlist.PlaylistEntry.TABLE_NAME, selection, selectionArgs)
    }
    fun setAllFavoritesAndHearTimes(songs: ArrayList<Song>) {
        for(song in songs) {
            setFavoriteAndHearTimes(song._id, song.isFavorite, song.heardTimes)
        }
    }
    fun setFavoriteAndHearTimes(songId: Long, isFavorite: Int, heardTimes: Int) {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put(Song.SongEntry.COLUMN_IS_FAVORITE, isFavorite)
            put(Song.SongEntry.COLUMN_HEARD_TIMES, heardTimes)
        }
        val selection = "${BaseColumns._ID} = ?"
        val selectionArgs = arrayOf("$songId")
        db.update(Song.SongEntry.TABLE_NAME, values, selection, selectionArgs)
    }
    fun getAllFavoriteSongs(): ArrayList<Song> {
        val db = this.readableDatabase
        val selection = "${Song.SongEntry.COLUMN_IS_FAVORITE} = ?"
        val selectionArgs = arrayOf("1")
        val cursor = db.query(Song.SongEntry.TABLE_NAME, null, selection, selectionArgs, null, null, null, null)
        val songs = ArrayList<Song>()
        with(cursor) {
            while(moveToNext()) {
                val song = Song(
                    getLong(getColumnIndexOrThrow(BaseColumns._ID)),
                    getString(getColumnIndexOrThrow(Song.SongEntry.COLUMN_NAME)),
                    getString(getColumnIndexOrThrow(Song.SongEntry.COLUMN_ALBUM)),
                    getString(getColumnIndexOrThrow(Song.SongEntry.COLUMN_PATH)),
                    getString(getColumnIndexOrThrow(Song.SongEntry.COLUMN_ARTIST)),
                    getInt(getColumnIndexOrThrow(Song.SongEntry.COLUMN_IS_FAVORITE)),
                    getInt(getColumnIndexOrThrow(Song.SongEntry.COLUMN_HEARD_TIMES))
                )
                songs.add(song)
            }
        }
        return songs
    }
    fun getTopHeardTimes(): ArrayList<Song> {
        val db = this.readableDatabase
        val sortOrder = "${Song.SongEntry.COLUMN_HEARD_TIMES} DESC"
        val cursor = db.query(
                        Song.SongEntry.TABLE_NAME,
                        null,
                        null,
                        null,
                        null,
                        null,
                        sortOrder,
                        "5")
        val songs = ArrayList<Song>()
        with(cursor) {
            while(moveToNext()) {
                val song = Song(
                    getLong(getColumnIndexOrThrow(BaseColumns._ID)),
                    getString(getColumnIndexOrThrow(Song.SongEntry.COLUMN_NAME)),
                    getString(getColumnIndexOrThrow(Song.SongEntry.COLUMN_ALBUM)),
                    getString(getColumnIndexOrThrow(Song.SongEntry.COLUMN_PATH)),
                    getString(getColumnIndexOrThrow(Song.SongEntry.COLUMN_ARTIST)),
                    getInt(getColumnIndexOrThrow(Song.SongEntry.COLUMN_IS_FAVORITE)),
                    getInt(getColumnIndexOrThrow(Song.SongEntry.COLUMN_HEARD_TIMES))
                )
                songs.add(song)
            }
        }
        return songs
    }
}