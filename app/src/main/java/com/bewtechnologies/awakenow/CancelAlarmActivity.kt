package com.bewtechnologies.awakenow

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button

class CancelAlarmActivity : AppCompatActivity() {
    var mNotificationManager: NotificationManager? = null
    private val mNotificationId = 1234
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cancel_alarm)
        findViewById<Button>(R.id.cancel_Alarm_button).setOnClickListener{
            stopAlarm(this)
            cancelNotification(this)
            this@CancelAlarmActivity.finish()
        }

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