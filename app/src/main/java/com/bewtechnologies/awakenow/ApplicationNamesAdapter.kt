package com.bewtechnologies.awakenow

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView

class ApplicationNamesAdapter(private val applicationDetailsList: ArrayList<ApplicationDetailsObject>) :
    RecyclerView.Adapter<ApplicationNamesAdapter.ViewHolder>() {
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
        val alarmEnableSwitch = itemView.findViewById<SwitchCompat>(R.id.switch1)
    }

    override fun onBindViewHolder(holder: ApplicationNamesAdapter.ViewHolder, position: Int) {
        val applicationDetailsObject = applicationDetailsList.get(position)

        val imageView = holder.appImageView
        imageView.setImageDrawable(applicationDetailsObject.appImage)

        val appNameView = holder.appName
        appNameView.text = applicationDetailsObject.appName

        val alarmSwitch = holder.alarmEnableSwitch

        //if notification reader is enabled for this app, show switch enabled
        if (appPackageList.contains(applicationDetailsObject.appPackageName)) {
            alarmSwitch.isChecked = true
        }

        alarmSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                Util.saveInSharedPref(applicationDetailsObject.appPackageName, context!!)
            } else {
                Util.removeFromSharedPref(applicationDetailsObject.appPackageName, context!!)
            }
        }
    }

    override fun getItemCount(): Int {
        return applicationDetailsList.size
    }
}