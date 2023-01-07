package com.bewtechnologies.awakenow.receiver

import android.R
import android.app.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.bewtechnologies.awakenow.activity.CancelAlarmActivity
import com.bewtechnologies.awakenow.service.AlarmRingtoneService


class AlarmReceiver : BroadcastReceiver() {
    var mNotificationManager: NotificationManager? = null
    private lateinit var cancelAlarmPendingIntent: PendingIntent
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
                val alarmIntent = alarmReceiverIntent.let { receiverIntent ->
                    PendingIntent.getBroadcast(
                        context,
                        0,
                        receiverIntent,
                        PendingIntent.FLAG_IMMUTABLE
                    )
                }
                stopAlarm(context)
                alarmIntent.cancel()
                alarmManager!!.cancel(alarmIntent)
                if (mNotificationManager == null) {
                    mNotificationManager =
                        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                }
                mNotificationManager?.cancel(mNotificationId)

                Log.i("Awake now", " alarm cancel")

            }
            else -> {
                //cancel existing ringtoneService if any
                cancelAlarmRingtoneService(context)
                startAlarm(context)
                //show notification
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

        val intentMainLanding = Intent(context, CancelAlarmActivity::class.java)
        //put sound value
        intentMainLanding.putExtra("sound", "yes")

        val pendingIntent =
            PendingIntent.getActivity(context, 0, intentMainLanding, PendingIntent.FLAG_IMMUTABLE)
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
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        mNotificationManager?.createNotificationChannel(notificationChannel)

        //cancel alarm pending intent
        val cancelAlarmIntent = Intent(context, CancelAlarmReceiver::class.java)
        cancelAlarmPendingIntent = cancelAlarmIntent.let { intent ->
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }

        // Build notification
        val builder = NotificationCompat.Builder(context!!, "awakeNow_serviceChanel")
        builder.setContentTitle(
            StringBuilder("Awake now").append(" service is running").toString()
        )
            .setTicker(StringBuilder("Awake Now! App").append(" alarm").toString())
            .setContentText("Turn off alarm") // swipe down for more options.
            .setFullScreenIntent(pendingIntent, true)
            .setSmallIcon(R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(Notification.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_input_delete, "Cancel Alarm", cancelAlarmPendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .setDefaults(Notification.FLAG_ONGOING_EVENT)
            .setColorized(true).color = ContextCompat.getColor(context, R.color.holo_purple)
        if (iconNotification != null) {
            builder.setLargeIcon(Bitmap.createScaledBitmap(iconNotification!!, 128, 128, false))
        }
        notification = builder.build()
        mNotificationManager!!.notify(mNotificationId, notification)
        //start cancel alarm activity
        //not yet implemented
        startCancelAlarmActivity()
    }

    private fun startCancelAlarmActivity() {
        //to be implemented
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