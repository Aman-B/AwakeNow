package com.bewtechnologies.awakenow.activity

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bewtechnologies.awakenow.R
import com.bewtechnologies.awakenow.adapter.ApplicationNamesAdapter
import com.bewtechnologies.awakenow.howitworks.HowItWorksActivity
import com.bewtechnologies.awakenow.model.ApplicationDetailsObject
import com.bewtechnologies.awakenow.service.NotificationReaderService
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var toolbar: Toolbar? = null
    private var drawer: DrawerLayout? = null
    private var recyclerView: RecyclerView? = null
    private var giveNotificationPermissionButton: Button? = null
    private lateinit var enableNotificationListenerAlertDialog: AlertDialog
    private val ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners"
    private var applicationDetailsList: ArrayList<ApplicationDetailsObject> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i("Awake now ", "started")
        setApplicationList()
        giveNotificationPermissionButton =
            findViewById(R.id.give_notification_permission_button)
        recyclerView = findViewById(R.id.appList_recyclerView)
        val applicationNamesAdapter = ApplicationNamesAdapter(applicationDetailsList)
        recyclerView?.adapter = applicationNamesAdapter
        recyclerView?.layoutManager = LinearLayoutManager(this)

        // If the user did not turn the notification listener service on we prompt him to do so
        if (!isNotificationServiceEnabled()) {
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog()
            enableNotificationListenerAlertDialog.show()
            giveNotificationPermissionButton?.visibility = View.VISIBLE
            giveNotificationPermissionButton?.setOnClickListener {
                startActivity(
                    Intent(
                        ACTION_NOTIFICATION_LISTENER_SETTINGS
                    )
                )
            }
            recyclerView?.visibility = View.GONE
        }
        setUpNavigationDrawer()
    }

    private fun setUpNavigationDrawer() {
        setSupportActionBar(findViewById(R.id.toolbar))
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        toolbar = findViewById(R.id.toolbar)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawer?.addDrawerListener(toggle)
        toggle.isDrawerIndicatorEnabled = true
        toggle.syncState()
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)
    }

    private fun setApplicationList() {
        val packages = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        packages.onEach {
            Log.i("awake now set : ", "" + it)
            if (it.flags and ApplicationInfo.FLAG_SYSTEM != 1) {
                val appName = applicationContext.packageManager.getApplicationLabel(
                    applicationContext.packageManager.getApplicationInfo(
                        it.packageName,
                        PackageManager.GET_META_DATA
                    )
                )
                val applicationDetailsObject = ApplicationDetailsObject(
                    appName = appName.toString(),
                    appImage = it.loadIcon(applicationContext.packageManager),
                    appPackageName = it.packageName
                )
                applicationDetailsList.add(applicationDetailsObject)
                applicationDetailsList.sortBy { it -> it.appName }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isNotificationServiceEnabled()) {
            enableNotificationListenerAlertDialog = buildNotificationServiceAlertDialog()
            enableNotificationListenerAlertDialog.show()
            giveNotificationPermissionButton?.visibility = View.VISIBLE
            giveNotificationPermissionButton?.setOnClickListener {
                startActivity(
                    Intent(
                        ACTION_NOTIFICATION_LISTENER_SETTINGS
                    )
                )
            }
        } else {
            giveNotificationPermissionButton?.visibility = View.GONE
            recyclerView?.visibility = View.VISIBLE
        }
    }

    /**
     * Is Notification Service Enabled.
     * Verifies if the notification listener service is enabled.
     * Got it from: https://github.com/kpbird/NotificationListenerService-Example/blob/master/NLSExample/src/main/java/com/kpbird/nlsexample/NLService.java
     * @return True if enabled, false otherwise.
     */
    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val flat: String? = Settings.Secure.getString(
            this.contentResolver,
            ENABLED_NOTIFICATION_LISTENERS
        )
        if (flat != null) {
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
        } else {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                (this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).isNotificationListenerAccessGranted(
                    ComponentName(this, NotificationReaderService::class.java)
                )
            } else {
                false
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
            Log.i("Awake now", " packageName $packageName")
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
        alertDialogBuilder.setTitle("Needs notification read permission")
        alertDialogBuilder.setMessage("We need the permission to read notifications. This will be used by the app to trigger alarms if notification comes.")
        alertDialogBuilder.setPositiveButton(
            "Confirm"
        ) { _, _ ->
            startActivity(
                Intent(
                    ACTION_NOTIFICATION_LISTENER_SETTINGS
                )
            )
        }
        alertDialogBuilder.setNegativeButton(
            "Cancel"
        ) { _, _ ->
            //TODO : add a dialog
            // If you choose to not enable the notification listener
            // the app. will not work as expected
        }
        return alertDialogBuilder.create()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.how_it_works -> {
                val howItWorksIntent = Intent(this, HowItWorksActivity::class.java)
                startActivity(howItWorksIntent)
            }
            R.id.nav_share -> {
                val textToShare =
                    "Check out this amazing app for setting alarms for important messages. \n(Download here: https://play.google.com/store/apps/details?id=com.bewtechnologies.awakenow )"
                val sendIntent = Intent()
                sendIntent.action = Intent.ACTION_SEND
                sendIntent.putExtra(Intent.EXTRA_SUBJECT, "Awake Now!")
                sendIntent.putExtra(Intent.EXTRA_TEXT, textToShare)
                sendIntent.type = "text/plain"
                startActivity(Intent.createChooser(sendIntent, "Share the app!"))
            }

            R.id.nav_contact_us -> {
                openEmail("Awake Now! - Feedback")
            }
        }

        return true
    }

    private fun openEmail(subject: String) {
        /* Create the Intent */
        val emailIntent = Intent(Intent.ACTION_SENDTO)

        /* Fill it with Data */
        emailIntent.data = Uri.parse("mailto:") // only email apps should handle this
        emailIntent.putExtra(
            Intent.EXTRA_EMAIL,
            arrayOf("bewtechnologies@gmail.com")
        )
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Hello!")

        /* Send it off to the Activity-Chooser */
        if (emailIntent.resolveActivity(packageManager) != null) {
            startActivity(emailIntent)
        }


    }
}