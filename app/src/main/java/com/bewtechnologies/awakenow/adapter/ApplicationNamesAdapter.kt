package com.bewtechnologies.awakenow.adapter

import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bewtechnologies.awakenow.R
import com.bewtechnologies.awakenow.model.ApplicationDetailsObject
import com.bewtechnologies.awakenow.util.Util


class ApplicationNamesAdapter(private val applicationDetailsList: ArrayList<ApplicationDetailsObject>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var appPackageList: java.util.ArrayList<String>
    private var context: Context? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val applicationNamesView = inflater.inflate(R.layout.custom_recyclerview_row, parent, false)
        appPackageList = Util.getListOfAppNamesFromSharedPref(context!!)
        return ViewHolder(applicationNamesView)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appImageView: ImageView = itemView.findViewById(R.id.imageView)
        val appName: TextView = itemView.findViewById(R.id.textView)
        val alarmListenButton: Button = itemView.findViewById(R.id.listenButton)
        val configureAlarmButton: Button = itemView.findViewById(R.id.configureAlarm_button)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val applicationAdapterHolder = holder as ViewHolder
        val index = applicationAdapterHolder.adapterPosition
        val applicationDetailsObject = applicationDetailsList[index]

        val imageView = applicationAdapterHolder.appImageView
        imageView.setImageDrawable(applicationDetailsObject.appImage)

        val appNameView = applicationAdapterHolder.appName
        appNameView.text = applicationDetailsObject.appName

        val alarmButton = applicationAdapterHolder.alarmListenButton

        //if alarm is activated for this app, show button text as alarm activated.
        if (appPackageList.contains(applicationDetailsObject.appPackageName)) {
            Log.i(
                "AppNamesAdapter ",
                " appPackageList ${
                    appPackageList[appPackageList.indexOf(applicationDetailsObject.appPackageName)]
                } name ${
                    applicationDetailsObject.appPackageName
                } pos $index"
            )
            alarmButton.text = context?.getString(R.string.alarm_activated_label)
            alarmButton.setBackgroundColor(context?.getColor(R.color.listening_color)!!)
        }

        alarmButton.setOnClickListener {
            //if it is activate alarm
            if (alarmButton.text.toString() == "Activate Alarm") {
                Log.i("AppNamesAdapter", " checked checkbox")

                Util.saveInSharedPref(
                    applicationDetailsObject.appPackageName,
                    context!!
                )
                //add to alarm list
                appPackageList.add(applicationDetailsObject.appPackageName)
                //change text to alarm activated
                alarmButton.text = context?.getString(R.string.alarm_activated_label)
                alarmButton.setBackgroundColor(context?.getColor(R.color.listening_color)!!)
            } else {
                //if alarm is already activated.
                Log.i("AppNamesAdapter", " unchecked checkbox")

                Util.removeFromSharedPref(
                    applicationDetailsObject.appPackageName,
                    context!!
                )
                //remove from alarm list
                appPackageList.remove(applicationDetailsObject.appPackageName)
                //change text to activate alarm
                alarmButton.text = context!!.getString(R.string.activate_alarm_label)
                alarmButton.setBackgroundColor(context?.getColor(R.color.white)!!)
                alarmButton.setTextColor(context?.getColor(R.color.black)!!)
            }
        }

        applicationAdapterHolder.configureAlarmButton.setOnClickListener {
            if (alarmButton.text.toString() == "Activate Alarm") {
                Toast.makeText(
                    context,
                    "Activate alarm first, by clicking the button on right.(Activate Alarms)",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                //alarm activated, now we can configure it
                showAlarmConfigurationDialog(applicationDetailsObject.appPackageName)
            }
        }
    }

    private fun showAlarmConfigurationDialog(appPackageName: String) {
        val dialogBuilder: AlertDialog.Builder = AlertDialog.Builder(context)
        dialogBuilder.setTitle("Enter keywords to look for in notification.")
        val inflater = LayoutInflater.from(context)
        val customView: View =
            inflater.inflate(R.layout.custom_configure_alarm_keyword_layout, null)
        val input: EditText = customView.findViewById(R.id.keywords_editText)
        input.setText(Util.getTextToLookForFromSharedPref(appPackageName, context!!))
        dialogBuilder.setView(customView)
        dialogBuilder.setPositiveButton(
            "Set"
        ) { dialogInterface, i ->
            Util.putTextToLookFor(context!!, appPackageName, input.text.toString())
        }
        dialogBuilder.setNegativeButton(
            "Cancel"
        ) { dialogInterface, i ->
            //whatever action
        }
        dialogBuilder.create()
        dialogBuilder.show()
    }

    override fun getItemCount(): Int {
        return applicationDetailsList.size
    }
}