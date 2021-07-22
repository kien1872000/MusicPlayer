package com.example.musicplayer

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.util.Log
import com.example.musicplayer.Activities.ServiceCommunication
import com.example.musicplayer.ApplicationClass.Companion.ACTION_NEXT
import com.example.musicplayer.ApplicationClass.Companion.ACTION_PLAY
import com.example.musicplayer.ApplicationClass.Companion.ACTION_PREVIOUS

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        var preferences: SharedPreferences = context!!.getSharedPreferences(MiniPlayer.LAST_PLAYED_SONG, Context.MODE_PRIVATE)
        var sender = preferences.getString(ServiceCommunication.SENDER_ACTIVITY, "")
        var actionName = intent?.action
        var serviceIntent = Intent(context, MusicService::class.java);
        serviceIntent.putExtra(ServiceCommunication.SENDER_ACTIVITY, sender)
        if(actionName!=null) {
            when(actionName){
                ACTION_PLAY -> {
                    serviceIntent.putExtra(ServiceCommunication.MEDIA_PLAYER_ACTION, ServiceCommunication.ACTION_PLAY )
                    context?.startService(serviceIntent)
                }
                ACTION_NEXT -> {
                    serviceIntent.putExtra(ServiceCommunication.MEDIA_PLAYER_ACTION, ServiceCommunication.ACTION_NEXT)
                    context?.startService(serviceIntent)
                }
                ACTION_PREVIOUS -> {
                    serviceIntent.putExtra(ServiceCommunication.MEDIA_PLAYER_ACTION, ServiceCommunication.ACTION_PREV)
                    context?.startService(serviceIntent)
                }
            }
        }
    }
}