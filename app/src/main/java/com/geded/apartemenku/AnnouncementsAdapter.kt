package com.geded.apartemenku

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.geded.apartemenku.databinding.LayoutManagementAnnouncementBinding

class AnnouncementsAdapter(val announcements:ArrayList<Announcement>, val context: FragmentActivity?):
    RecyclerView.Adapter<AnnouncementsAdapter.AnnouncementsViewHolder>() {
    class AnnouncementsViewHolder(val binding: LayoutManagementAnnouncementBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementsViewHolder {
        val binding = LayoutManagementAnnouncementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnnouncementsViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return announcements.size
    }

    override fun onBindViewHolder(holder: AnnouncementsViewHolder, position: Int) {
        with(holder.binding) {
            txtAnnTitleBS.text = announcements[position].title
            txtAnnDateBS.text = announcements[position].date
            txtAnnDescBS.text = announcements[position].description
        }
    }
}