package com.bewtechnologies.awakenow

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.recyclerview.widget.RecyclerView

class ApplicationNamesAdapter(private val applicationDetailsList: ArrayList<ApplicationDetailsObject>) : RecyclerView.Adapter<ApplicationNamesAdapter.ViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ApplicationNamesAdapter.ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val applicationNamesView = inflater.inflate(R.layout.custom_recyclerview_row,parent,false)
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

    }

    override fun getItemCount(): Int {
        return applicationDetailsList.size
    }
}