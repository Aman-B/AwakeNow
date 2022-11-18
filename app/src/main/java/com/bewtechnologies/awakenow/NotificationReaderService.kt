package com.bewtechnologies.awakenow

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
import kotlin.random.Random


class NotificationReaderService : NotificationListenerService() {

    private var alarmTime: Long? = 0L
    private var alarmIntent: PendingIntent? = null
    private lateinit var alarmManager: AlarmManager
    private lateinit var context: Context

    //Notififcation for ON-going
    private var iconNotification: Bitmap? = null
    private var notification: Notification? = null
    var mNotificationManager: NotificationManager? = null
    private val mNotificationId = 123
    override fun onCreate() {
        super.onCreate()
        context = this
    }

    /*
            These are the package names of the apps. for which we want to
            listen the notifications
         */
    private object ApplicationPackageNames {
        const val AWAKENOW_PACK_NAME = "com.bewtechnologies.awakenow"
        const val FACEBOOK_MESSENGER_PACK_NAME = "com.facebook.orca"
        const val WHATSAPP_PACK_NAME = "com.whatsapp"
        const val INSTAGRAM_PACK_NAME = "com.instagram.android"
        const val SLACK_PACK_NAME = "com.Slack"
        const val MICROSOFT_TEAMS_PACK_NAME = "com.microsoft.teams"
        const val AMAZON_MUSIC_PACK_NAME = "com.amazon.mp3"
    }

    /*
        These are the return codes we use in the method which intercepts
        the notifications, to decide whether we should do something or not
     */
    object InterceptedNotificationCode {
        const val AWAKE_NOW_CODE = 1
        const val WHATSAPP_CODE = 2
        const val INSTAGRAM_CODE = 3
        const val OTHER_NOTIFICATIONS_CODE = 4 // We ignore all notification with code == 4
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.i("Awake now", " service binded")

        return super.onBind(intent)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        Log.i("Awake now", " notif posted packageName " + sbn.packageName)
        val appPackageList = Util.getListOfAppNamesFromSharedPref(context)
        Log.i("Awake now", " notif posted appNamelist $appPackageList")
        val notificationCode = matchNotificationCode(sbn)
        if ((notificationCode != InterceptedNotificationCode.AWAKE_NOW_CODE)
            && appPackageList.contains(sbn.packageName)
        )//TODO : remove amazon from here after testing
        {
            val intent = Intent("com.bewtechnologies.awakenow")
            intent.putExtra("Notification Code", sbn.packageName)

            //show notif with alarm
            showNotifWithAlarm(sbn.packageName)

            sendBroadcast(intent)
        }

    }

    private fun showNotifWithAlarm(packageName: String?) {

        // Setup Ringtone & Vibrate
        /*val alarmSound: Uri = Settings.System.DEFAULT_ALARM_ALERT_URI
        val vibratePattern = longArrayOf(0, 100, 200, 300)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_ALARM)
            .build()*/

        val intentMainLanding = Intent(this, MainActivity::class.java)
        //put sound value
        intentMainLanding.putExtra("sound", "yes")

        val pendingIntent =
            PendingIntent.getActivity(this, 0, intentMainLanding, 0)
        if (mNotificationManager == null) {
            mNotificationManager =
                this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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


        val builder = NotificationCompat.Builder(this, "awakeNow_serviceChanel")

        builder.setContentTitle(
            StringBuilder("Awake now").append(" service is running").toString()
        )
            .setTicker(StringBuilder("awake now").append("service is running").toString())
            .setContentText("Got $packageName. Touch to open app.${Random.nextInt()}") //                    , swipe down for more options.
            .setSmallIcon(R.drawable.ic_btn_speak_now)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
        if (iconNotification != null) {
            builder.setLargeIcon(Bitmap.createScaledBitmap(iconNotification!!, 128, 128, false))
        }
        builder.color = resources.getColor(R.color.holo_blue_bright)
        notification = builder.build()
        setAlarm()
        mNotificationManager!!.notify(mNotificationId, notification)
        //startForeground(mNotificationId, notification)
    }

    private fun setAlarm() {
        /* player = MediaPlayer.create(this,Settings.System.DEFAULT_ALARM_ALERT_URI)
         // player.setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ALARM).build())
         player.start()*/

        alarmManager = context.getSystemService(ALARM_SERVICE) as AlarmManager
        alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
            PendingIntent.getBroadcast(context, 0, intent, 0)
        }
        /*if (alarmIntent == null) {
            alarmIntent = Intent(context, AlarmReceiver::class.java).let { intent ->
                PendingIntent.getBroadcast(context, 0, intent, 0)
            }
        }
        else{
            Log.i("NotificationReaderService ", "not starting new alarm.")
        }*/

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

    private fun showAlarmNotification(context: Context?) {
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

        val alarmReceiverIntent = Intent(context, CancelAlarmReceiver::class.java)
        alarmIntent = alarmReceiverIntent.let { intent ->
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
            .addAction(R.drawable.ic_input_delete, "Cancel Alarm", alarmIntent)
            .setOngoing(false)
        if (iconNotification != null) {
            builder.setLargeIcon(Bitmap.createScaledBitmap(iconNotification!!, 128, 128, false))
        }
        notification = builder.build()

        mNotificationManager!!.notify(mNotificationId, notification)
    }


    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        Log.i("Awake now", " notif removed")

        val notificationCode = matchNotificationCode(sbn)
        if (notificationCode != InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE) {
            val activeNotifications = this.activeNotifications
            if (activeNotifications != null && activeNotifications.isNotEmpty()) {
                for (i in activeNotifications.indices) {
                    if (notificationCode == matchNotificationCode(activeNotifications[i])) {
                        val intent = Intent("com.bewtechnologies.awakenow")
                        intent.putExtra("Notification Code", notificationCode)
                        sendBroadcast(intent)
                        break
                    }
                }
            }
        }
    }

    private fun matchNotificationCode(sbn: StatusBarNotification): Int {
        val packageName = sbn.packageName
        return if (packageName == ApplicationPackageNames.AWAKENOW_PACK_NAME || packageName == ApplicationPackageNames.FACEBOOK_MESSENGER_PACK_NAME) {
            InterceptedNotificationCode.AWAKE_NOW_CODE
        } else if (packageName == ApplicationPackageNames.INSTAGRAM_PACK_NAME) {
            InterceptedNotificationCode.INSTAGRAM_CODE
        } else if (packageName == ApplicationPackageNames.WHATSAPP_PACK_NAME) {
            InterceptedNotificationCode.WHATSAPP_CODE
        } else {
            InterceptedNotificationCode.OTHER_NOTIFICATIONS_CODE
        }
    }
}