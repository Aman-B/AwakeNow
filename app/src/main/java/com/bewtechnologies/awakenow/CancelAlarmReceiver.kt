package com.bewtechnologies.awakenow

import android.app.AlarmManager
import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log

class CancelAlarmReceiver : BroadcastReceiver(){
    var mNotificationManager: NotificationManager? = null
    private val mNotificationId = 1234
    override fun onReceive(context: Context?, intent: Intent?) {
        stopAlarm(context)
        cancelNotification(context)
    }

    private fun cancelNotification(context: Context?) {
        val alarmManager =
            context!!.getSystemService(Context.ALARM_SERVICE) as AlarmManager?
        val alarmReceiverIntent = Intent(context, AlarmReceiver::class.java)
        val alarmIntent = alarmReceiverIntent.let { intent ->
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }
        stopAlarm(context)
        alarmIntent.cancel()
        alarmManager!!.cancel(alarmIntent)
        if (mNotificationManager == null) {
            mNotificationManager =
                context!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        }
        mNotificationManager?.cancel(mNotificationId)

    }

    private fun stopAlarm(context: Context?) {
        Log.i("CancelAlarmReceiver", " alarm receive stopping alarm")
        val alarmRingtoneService = Intent(context, AlarmRingtoneService::class.java)
        alarmRingtoneService.putExtra("alarmState", "stop")
        context!!.startService(alarmRingtoneService)
    }

}
