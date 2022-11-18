package com.bewtechnologies.awakenow

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

import android.content.*
import android.content.pm.PackageManager
import android.content.pm.PackageManager.MATCH_ALL
import android.os.Build
import android.provider.Settings

import android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS

import android.text.TextUtils
import android.util.Log

import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity() {
    private lateinit var enableNotificationListenerAlertDialog: AlertDialog
    private val ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners"
    private lateinit var imageChangeBroadcastReceiver: ImageChangeBroadcastReceiver
    private var applicationDetailsList: ArrayList<ApplicationDetailsObject> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i("Awake now ", "started")
        setApplicationList()

        val recyclerView = findViewById<RecyclerView>(R.id.appList_recyclerView)
        val applicationNamesAdapter = ApplicationNamesAdapter(applicationDetailsList)
        recyclerView.adapter = applicationNamesAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // If the user did not turn the notification listener service on we prompt him to do so
        if (!isNotificationServiceEnabled()) {
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog()
            enableNotificationListenerAlertDialog.show()
        }

        /*var messaginAppIntent = Intent(Intent.ACTION_SEND)
        messaginAppIntent.type = "text/plain"
        var applicationList =
            applicationContext.packageManager
                .queryIntentActivities(messaginAppIntent, MATCH_ALL)

        var packageList : ArrayList<String> = ArrayList()
        applicationList.apply {
            forEach {
                //to get list of apps uncomment this
                //packageList.add(it.activityInfo.packageName)

                //to get icons of the list of apps
                packageList.add(it.activityInfo.loadIcon(applicationContext.packageManager).toString())
            }
        }

        textView!!.text = applicationList.toString()
        Log.i("Awake now app list ", "" +packageList)*/


    }

    private fun setApplicationList() {
        var messaginAppIntent = Intent(Intent.ACTION_SEND)
        messaginAppIntent.type = "text/plain"
        var applicationList =
            applicationContext.packageManager
                .queryIntentActivities(messaginAppIntent, MATCH_ALL)

        var packageList : ArrayList<String> = ArrayList()
        applicationList.apply {
            forEach {
                //to get list of apps uncomment this
                //packageList.add(it.activityInfo.packageName)
                val appName = applicationContext.packageManager.getApplicationLabel(applicationContext.packageManager.getApplicationInfo(it.activityInfo.packageName, PackageManager.GET_META_DATA))
                val applicationDetailsObject = ApplicationDetailsObject(appName = appName.toString(),appImage =it.activityInfo.loadIcon(applicationContext.packageManager), appPackageName = it.activityInfo.packageName )
                applicationDetailsList.add(applicationDetailsObject)
                //to get icons of the list of apps
                packageList.add(it.activityInfo.loadIcon(applicationContext.packageManager).toString())
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(imageChangeBroadcastReceiver)
    }



    /**
     * Is Notification Service Enabled.
     * Verifies if the notification listener service is enabled.
     * Got it from: https://github.com/kpbird/NotificationListenerService-Example/blob/master/NLSExample/src/main/java/com/kpbird/nlsexample/NLService.java
     * @return True if enabled, false otherwise.
     */
    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val flat: String = Settings.Secure.getString(
            contentResolver,
            ENABLED_NOTIFICATION_LISTENERS
        )
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":").toTypedArray()
            for (i in names.indices) {
                val cn = ComponentName.unflattenFromString(names[i])
                if (cn != null) {
                    if (TextUtils.equals(pkgName, cn.packageName)) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * Image Change Broadcast Receiver.
     * We use this Broadcast Receiver to notify the Main Activity when
     * a new notification has arrived, so it can properly change the
     * notification image
     */
    class ImageChangeBroadcastReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent) {
            val packageName = intent.getStringExtra("Notification Code")
            Log.i("Awake now", " packageName "+packageName)
        }
    }


    /**
     * Build Notification Listener Alert Dialog.
     * Builds the alert dialog that pops up if the user has not turned
     * the Notification Listener Service on yet.
     * @return An alert dialog which leads to the notification enabling screen
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP_MR1)
    private fun buildNotificationServiceAlertDialog(): AlertDialog {
        val alertDialogBuilder: AlertDialog.Builder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Notification")
        alertDialogBuilder.setMessage("Turn it on")
        alertDialogBuilder.setPositiveButton("Yo do it",
            DialogInterface.OnClickListener { dialog, id ->
                startActivity(
                    Intent(
                        ACTION_NOTIFICATION_LISTENER_SETTINGS
                    )
                )
            })
        alertDialogBuilder.setNegativeButton("Dont do it",
            DialogInterface.OnClickListener { dialog, id ->
                // If you choose to not enable the notification listener
                // the app. will not work as expected
            })
        return alertDialogBuilder.create()
    }
}