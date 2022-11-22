package com.bewtechnologies.awakenow

import android.R
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.util.Log
import androidx.core.app.NotificationCompat


class AlarmReceiver : BroadcastReceiver() {
    private lateinit var player: MediaPlayer
    var mNotificationManager: NotificationManager? = null
    private lateinit var cancelAlarmPendingIntent: PendingIntent
    private lateinit var alarmManager: AlarmManager
    private var iconNotification: Bitmap? = null
    private var notification: Notification? = null
    private val mNotificationId = 1234

    override fun onReceive(context: Context?, intent: Intent?) {
        val action = intent!!.getStringExtra("action")
        Log.i("Awake now", " alarm receive $action ")
        when (action) {
            "delete" -> {
                val alarmManager =
                    context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
                val alarmReceiverIntent = Intent(context, AlarmReceiver::class.java)
                val alarmIntent = alarmReceiverIntent.let { intent ->
                    PendingIntent.getBroadcast(context, 0, intent, 0)
                }
                stopAlarm(context)
                alarmIntent.cancel()
                alarmManager!!.cancel(alarmIntent)
                if (mNotificationManager == null) {
                    mNotificationManager =
                        context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                }
                mNotificationManager?.cancel(mNotificationId)

                Log.i("Awake now", " alarm cancel")

            }
            else -> {
                //cancel existing ringtoneService if any
                cancelAlarmRingtoneService(context)
                startAlarm(context)
                showNotification(context)
            }

        }
    }

    private fun stopAlarm(context: Context?) {
        Log.i("Awake now", " alarm receive stopping alarm")
        val alarmRingtoneService = Intent(context, AlarmRingtoneService::class.java)
        alarmRingtoneService.putExtra("alarmState", "stop")
        context!!.startService(alarmRingtoneService)
    }

    private fun showNotification(context: Context?) {
        Log.i("Awake now", " alarm receive show notification")

        val intentMainLanding = Intent(context, MainActivity::class.java)
        //put sound value
        intentMainLanding.putExtra("sound", "yes")

        val pendingIntent =
            PendingIntent.getActivity(context, 0, intentMainLanding, 0)
        if (mNotificationManager == null) {
            mNotificationManager =
                context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        assert(mNotificationManager != null)
        mNotificationManager?.createNotificationChannelGroup(
            NotificationChannelGroup("awakeNow_group", "Chats")
        )
        val notificationChannel =
            NotificationChannel(
                "awakeNow_serviceChanel", "Service Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
        //notificationChannel.setSound(alarmSound,audioAttributes)
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        mNotificationManager?.createNotificationChannel(notificationChannel)

        //alarm pending intent

        val cancelAlarmIntent = Intent(context, CancelAlarmReceiver::class.java)
        cancelAlarmPendingIntent = cancelAlarmIntent.let { intent ->
            PendingIntent.getBroadcast(context, 0, intent, 0)
        }

        // Setup Ringtone & Vibrate

        val builder = NotificationCompat.Builder(context!!, "awakeNow_serviceChanel")

        builder.setContentTitle(
            StringBuilder("Awake now").append(" service is running").toString()
        )
            .setTicker(StringBuilder("awake now").append("service is running").toString())
            .setContentText("Turn off alarm") // swipe down for more options.
            .setSmallIcon(R.drawable.ic_btn_speak_now)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_input_delete, "Cancel Alarm", cancelAlarmPendingIntent)
            .setOngoing(true)
        if (iconNotification != null) {
            builder.setLargeIcon(Bitmap.createScaledBitmap(iconNotification!!, 128, 128, false))
        }
        notification = builder.build()

        mNotificationManager!!.notify(mNotificationId, notification)
    }

    private fun cancelAlarmRingtoneService(context: Context?) {
        Log.i("Awake now", " cancelAlarmRingtoneService")

        val alarmRingtoneService = Intent(context, AlarmRingtoneService::class.java)
        alarmRingtoneService.putExtra("alarmState", "stop")
        context!!.startService(alarmRingtoneService)
    }

    private fun startAlarm(context: Context?) {
        Log.i("Awake now", " alarm receive start alarm")
        val alarmRingtoneService = Intent(context, AlarmRingtoneService::class.java)
        context!!.startService(alarmRingtoneService)
    }

}