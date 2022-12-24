package com.bewtechnologies.awakenow

import android.app.NotificationManager
import android.content.*
import android.content.pm.PackageManager
import android.content.pm.PackageManager.MATCH_ALL
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
import androidx.navigation.ui.AppBarConfiguration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bewtechnologies.awakenow.howitworks.HowItWorksActivity
import com.google.android.material.navigation.NavigationView


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var toolbar: Toolbar? = null
    private var navigationView: NavigationView? = null
    private var drawer: DrawerLayout? = null
    private var recyclerView: RecyclerView? = null
    private var giveNotificationPermissionButton: Button? = null
    private lateinit var enableNotificationListenerAlertDialog: AlertDialog
    private val ENABLED_NOTIFICATION_LISTENERS = "enabled_notification_listeners"
    private var applicationDetailsList: ArrayList<ApplicationDetailsObject> = ArrayList()
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i("Awake now ", "started")
        setApplicationList()
        giveNotificationPermissionButton =
            findViewById(R.id.give_notification_permission_button)
        recyclerView = findViewById<RecyclerView>(R.id.appList_recyclerView)
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

    private fun setUpNavigationDrawer() {
        setSupportActionBar(findViewById(R.id.toolbar))


        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        toolbar = findViewById<Toolbar>(R.id.toolbar)

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
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
    }

    private fun setApplicationList() {
        var messaginAppIntent = Intent(Intent.ACTION_SEND)
        messaginAppIntent.type = "text/plain"
        var applicationList =
            applicationContext.packageManager
                .queryIntentActivities(messaginAppIntent, MATCH_ALL)

        var packageList: ArrayList<String> = ArrayList()
        applicationList.apply {
            forEach {
                //to get list of apps uncomment this
                //packageList.add(it.activityInfo.packageName)
                val appName = applicationContext.packageManager.getApplicationLabel(
                    applicationContext.packageManager.getApplicationInfo(
                        it.activityInfo.packageName,
                        PackageManager.GET_META_DATA
                    )
                )
                val applicationDetailsObject = ApplicationDetailsObject(
                    appName = appName.toString(),
                    appImage = it.activityInfo.loadIcon(applicationContext.packageManager),
                    appPackageName = it.activityInfo.packageName
                )
                applicationDetailsList.add(applicationDetailsObject)
                //to get icons of the list of apps
                packageList.add(
                    it.activityInfo.loadIcon(applicationContext.packageManager).toString()
                )
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

    override fun onDestroy() {
        super.onDestroy()
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
                (this!!.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).isNotificationListenerAccessGranted(
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
            Log.i("Awake now", " packageName " + packageName)
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
        alertDialogBuilder.setPositiveButton("Confirm",
            DialogInterface.OnClickListener { dialog, id ->
                startActivity(
                    Intent(
                        ACTION_NOTIFICATION_LISTENER_SETTINGS
                    )
                )
            })
        alertDialogBuilder.setNegativeButton("Cancel",
            DialogInterface.OnClickListener { dialog, id ->
                // If you choose to not enable the notification listener
                // the app. will not work as expected
            })
        return alertDialogBuilder.create()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var menuItemID = item.getItemId()
        when (menuItemID) {
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
                openEmail("Awake Now! - Feedback");
            }
        }

        return true
    }

    private fun openEmail(subject: String) {
        /* Create the Intent */
        val emailIntent = Intent(android.content.Intent.ACTION_SENDTO);

        /* Fill it with Data */
        emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this
        emailIntent.putExtra(
            android.content.Intent.EXTRA_EMAIL,
            arrayOf("bewtechnologies@gmail.com")
        );
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "Hello!");

        /* Send it off to the Activity-Chooser */
        if (emailIntent.resolveActivity(getPackageManager()) != null) {
            startActivity(emailIntent);
        }


    }
}