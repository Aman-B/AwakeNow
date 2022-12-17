package com.bewtechnologies.awakenow

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ApplicationNamesAdapter(private val applicationDetailsList: ArrayList<ApplicationDetailsObject>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private lateinit var appPackageList: java.util.ArrayList<String>
    private var context: Context? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ApplicationNamesAdapter.ViewHolder {
        context = parent.context
        val inflater = LayoutInflater.from(context)
        val applicationNamesView = inflater.inflate(R.layout.custom_recyclerview_row, parent, false)
        appPackageList = Util.getListOfAppNamesFromSharedPref(context!!)
        return ViewHolder(applicationNamesView)
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val appImageView = itemView.findViewById<ImageView>(R.id.imageView)
        val appName = itemView.findViewById<TextView>(R.id.textView)
        val alarmListenButton = itemView.findViewById<Button>(R.id.listenButton)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val applicationAdapterHolder = holder as ApplicationNamesAdapter.ViewHolder
        val index = applicationAdapterHolder.adapterPosition
        val applicationDetailsObject = applicationDetailsList[index]

        val imageView = applicationAdapterHolder.appImageView
        imageView.setImageDrawable(applicationDetailsObject.appImage)

        val appNameView = applicationAdapterHolder.appName
        appNameView.text = applicationDetailsObject.appName

        val alarmSwitch = applicationAdapterHolder.alarmListenButton

        //if alarm is activated for this app, show button text as alarm activated.
        if (appPackageList.contains(applicationDetailsObject.appPackageName.toString())) {
            Log.i(
                "AppNamesAdapter ",
                " appPackageList ${
                    appPackageList.get(appPackageList.indexOf(applicationDetailsObject.appPackageName.toString()))
                } name ${
                    applicationDetailsObject.appPackageName.toString()
                } pos ${index}"
            )
            alarmSwitch.text = context?.getString(R.string.alarm_activated_label)
            alarmSwitch.setBackgroundColor(context?.getColor(R.color.listening_color)!!)
        }

        alarmSwitch.setOnClickListener {
            //if it is activate alarm
            if (alarmSwitch.text.toString() == "Activate Alarm") {
                Log.i("AppNamesAdapter", " checked checkbox")

                Util.saveInSharedPref(
                    applicationDetailsObject.appPackageName,
                    context!!
                )
                //add to alarm list
                appPackageList.add(applicationDetailsObject.appPackageName.toString())
                //change text to alarm activated
                alarmSwitch.text = context?.getString(R.string.alarm_activated_label)
                alarmSwitch.setBackgroundColor(context?.getColor(R.color.listening_color)!!)
            } else {
                //if alarm is already activated.
                Log.i("AppNamesAdapter", " unchecked checkbox")

                Util.removeFromSharedPref(
                    applicationDetailsObject.appPackageName,
                    context!!
                )
                //remove from alarm list
                appPackageList.remove(applicationDetailsObject.appPackageName.toString())
                //change text to activate alarm
                alarmSwitch.text = context!!.getString(R.string.activate_alarm_label)
                alarmSwitch.setBackgroundColor(context?.getColor(R.color.white)!!)
                alarmSwitch.setTextColor(context?.getColor(R.color.black)!!)
            }
        }
        /* alarmSwitch.setOnClickListener {
             if (alarmSwitch.isListeningEnabled) {
                 alarmSwitch.isListeningEnabled = false
                 Util.removeFromSharedPref(
                     applicationDetailsList[position].appPackageName,
                     context!!
                 )
                 alarmSwitch.text = "Listen"
                 alarmSwitch.setBackgroundColor(context!!.getColor(R.color.white))
                 alarmSwitch.setTextColor(context!!.getColor(R.color.black))

             } else {
                 alarmSwitch.isListeningEnabled = true
                 Util.saveInSharedPref(applicationDetailsList[position].appPackageName, context!!)
                 alarmSwitch.text = "Listening"
                 alarmSwitch.setBackgroundColor(context!!.getColor(R.color.listening_color))
                 alarmSwitch.setTextColor(context!!.getColor(R.color.listening_text_color))
             }
         }*/
        /* alarmSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
             if (alarmSwitch.isPressed && isChecked) {
                 Util.saveInSharedPref(applicationDetailsList[position].appPackageName, context!!)
             } else {
                 Util.removeFromSharedPref(
                     applicationDetailsList[position].appPackageName,
                     context!!
                 )
             }
         }*/
    }

    override fun getItemCount(): Int {
        return applicationDetailsList.size
    }
}