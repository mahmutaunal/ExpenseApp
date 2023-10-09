package com.example.task.bindingadapter

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.example.task.R

object ImageViewBindingAdapter {

    @BindingAdapter("app:srcCompatDrawable")
    @JvmStatic
    fun setSrcCompatDrawable(imageView: ImageView, connected: Boolean) {
        imageView.setImageResource(if (connected) R.drawable.ic_link else R.drawable.ic_link_off)
    }

}