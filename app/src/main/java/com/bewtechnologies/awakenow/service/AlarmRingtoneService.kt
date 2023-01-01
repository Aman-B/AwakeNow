package com.bewtechnologies.awakenow.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.IBinder
import android.util.Log

class AlarmRingtoneService : Service() {
    private var player: MediaPlayer? =null

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.extras?.getString("alarmState") != null) {
            Log.i("AlarmRingtoneService ", "stopping alarm")
            stopAlarm()
        } else {
            startAlarm(this)
        }
        return START_NOT_STICKY
    }

    private fun stopAlarm() {
        if (player != null) {
            player!!.stop()
            this.onDestroy()
        }

    }

    private fun startAlarm(context: Context?) {
        Log.i("AlarmRingtoneService ", "starting alarm")
        player = MediaPlayer()
        player!!.setAudioAttributes(
            AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build()
        )
        player!!.setDataSource(context!!,RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
        player!!.isLooping = true
        player!!.prepare()
        player!!.start()

    }
}
