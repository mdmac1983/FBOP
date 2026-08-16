package com.mdmac.fbop.ui

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mdmac.fbop.R
import com.mdmac.fbop.model.AppInfo

class AppAdapter(
    private val context: Context,
    private var apps: List<AppInfo>,
    private val onAppClick: (AppInfo) -> Unit,
    private val onAppLongClick: (AppInfo, View) -> Unit
) : RecyclerView.Adapter<AppAdapter.AppViewHolder>() {

    class AppViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.appIcon)
        val label: TextView = view.findViewById(R.id.appLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.item_app_icon, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        val app = apps[position]
        holder.label.text = app.displayLabel

        if (app.customIconUri != null) {
            try {
                holder.icon.setImageURI(Uri.parse(app.customIconUri))
            } catch (e: Exception) {
                holder.icon.setImageDrawable(app.systemIcon)
            }
        } else {
            holder.icon.setImageDrawable(app.systemIcon)
        }

        holder.itemView.setOnClickListener { onAppClick(app) }
        holder.itemView.setOnLongClickListener {
            onAppLongClick(app, holder.itemView)
            true
        }
    }

    override fun getItemCount() = apps.size

    fun updateApps(newApps: List<AppInfo>) {
        apps = newApps
        notifyDataSetChanged()
    }
}
