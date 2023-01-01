package com.bewtechnologies.awakenow.service

import android.R
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import com.bewtechnologies.awakenow.activity.MainActivity
import com.bewtechnologies.awakenow.receiver.AlarmReceiver
import com.bewtechnologies.awakenow.util.Util


class NotificationReaderService : NotificationListenerService() {

    private var alarmTime: Long? = 0L
    private var alarmIntent: PendingIntent? = null
    private lateinit var alarmManager: AlarmManager
    private lateinit var context: Context

    //Notification for ON-going
    private var iconNotification: Bitmap? = null
    private var notification: Notification? = null
    private var mNotificationManager: NotificationManager? = null
    private val mNotificationId = 123
    override fun onCreate() {
        super.onCreate()
        context = this
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.i("Awake now", " service binded")

        return super.onBind(intent)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.i(
            "Awake now",
            " notif posted packageName " + sbn.packageName + " content " + sbn.notification.extras.getString(
                NotificationCompat.EXTRA_TEXT
            )
        )
        val appPackageList = Util.getListOfAppNamesFromSharedPref(context)
        Log.i("Awake now", " notif posted appNamelist $appPackageList")
        val textToLookFor = Util.getTextToLookForFromSharedPref(sbn.packageName, context)!!
        if (appPackageList.contains(sbn.packageName)) {
            if (checkIfNotificationHasTextToLookFor(textToLookFor, sbn)
            ) {
                val intent = Intent("com.bewtechnologies.awakenow")
                intent.putExtra("Notification Code", sbn.packageName)
                //show notification with alarm
                showNotificationWithAlarm(sbn.packageName)
                sendBroadcast(intent)
            } else if (textToLookFor.isEmpty()) {
                val intent = Intent("com.bewtechnologies.awakenow")
                intent.putExtra("Notification Code", sbn.packageName)
                //show notification with alarm
                showNotificationWithAlarm(sbn.packageName)
                sendBroadcast(intent)
            }
        }
    }

    private fun checkIfNotificationHasTextToLookFor(
        textToLookFor: String,
        sbn: StatusBarNotification
    ): Boolean {
        val textToLookForSplit = textToLookFor.split(" ")
        val notificationTextSplit =
            sbn.notification.extras.getCharSequence(NotificationCompat.EXTRA_TEXT)
                .toString().split(" ")
        Log.i(
            "awake now ",
            "ttLSplit " + textToLookForSplit + " notifSplit " + notificationTextSplit
        )
        textToLookForSplit.forEach {
            notificationTextSplit.forEach { notificationTextPart ->
                Log.i("awake now ", "ttLSplit " + it + " notifSplit " + notificationTextPart)

                if (it.equals(notificationTextPart, ignoreCase = true) || (it.contains(
                        notificationTextPart,
                        ignoreCase = true
                    ))
                ) {
                    //we have a match
                    return true
                }
            }
        }
        //no match by default
        return false
    }

    private fun showNotificationWithAlarm(packageName: String?) {
        showNotificationServiceRunning(packageName)
        //set alarm for notification
        setAlarm()
        mNotificationManager!!.notify(mNotificationId, notification)
    }

    private fun showNotificationServiceRunning(packageName: String?) {
        val intentMainLanding = Intent(context, MainActivity::class.java)
        //put sound value
        intentMainLanding.putExtra("sound", "yes")
        val pendingIntent =
            PendingIntent.getActivity(context, 0, intentMainLanding, PendingIntent.FLAG_IMMUTABLE)
        if (mNotificationManager == null) {
            mNotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
        notificationChannel.lockscreenVisibility = Notification.VISIBILITY_SECRET
        mNotificationManager?.createNotificationChannel(notificationChannel)

        //build notification
        val builder = NotificationCompat.Builder(context, "awakeNow_serviceChanel")
        builder.setContentTitle(
            StringBuilder("Awake now").append(" is up and running.").toString()
        )
            .setTicker(StringBuilder("Awake now").append("is up and running").toString())
            .setContentText("Got message from $packageName. Touch to open Awake Now! app.") //                    , swipe down for more options.
            .setSmallIcon(R.drawable.ic_lock_idle_alarm)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
        if (iconNotification != null) {
            builder.setLargeIcon(Bitmap.createScaledBitmap(iconNotification!!, 128, 128, false))
        }
        notification = builder.build()
    }

    override fun onDestroy() {
        super.onDestroy()
        mNotificationManager?.cancel(mNotificationId)
    }

    private fun setAlarm() {
        alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        }
        if (!isNotificationAlarmSet()) {
            alarmTime = System.currentTimeMillis() + 10 * 1000
            alarmIntent?.apply {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    alarmTime!!,
                    this
                )
            }
        }
    }

    private fun isNotificationAlarmSet(): Boolean {
        alarmTime?.apply {
            return this - (System.currentTimeMillis()) >= 0
        }
        return false
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.i("Awake now", " notification removed.")
        val activeNotifications = this.activeNotifications
        if (activeNotifications != null && activeNotifications.isNotEmpty()) {
            for (i in activeNotifications.indices) {
                val intent = Intent("com.bewtechnologies.awakenow")
                intent.putExtra("Notification Code", OTHER_NOTIFICATIONS_CODE)
                sendBroadcast(intent)
                break
            }
        }
    }

    companion object {
        private const val OTHER_NOTIFICATIONS_CODE = 4
    }
}