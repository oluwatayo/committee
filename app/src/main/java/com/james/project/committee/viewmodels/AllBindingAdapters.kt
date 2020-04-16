package com.james.project.committee.viewmodels

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.Timestamp
import com.james.project.committee.R
import com.james.project.committee.models.CommitteeFile
import com.james.project.committee.models.FileType
import com.james.project.committee.utils.GeneralUtil
import kotlin.random.Random

@BindingAdapter("authProgressState")
fun ProgressBar.authProgressState(visibilityStatus: Int) {
    visibility = visibilityStatus
}

@BindingAdapter("hideTextIfEmpty")
fun TextView.hideTextIfEmpty(s: String) {
    visibility = if (TextUtils.isEmpty(s)) View.GONE else View.VISIBLE
    text = s
}

@BindingAdapter("textToImage")
fun TextView.textToImage(text: String) {
    this.text = text.first().toString()
    val drawable: GradientDrawable = this.background as GradientDrawable
    drawable.setColor(getRandomColor(context))
    this.background = drawable
}

@BindingAdapter("loadProfileUrl")
fun ImageView.loadProfileUrl(text: String?) {
    val s =
        if (text.isNullOrEmpty()) "https://cdn3.iconfinder.com/data/icons/vector-icons-6/96/256-512.png" else text
    Glide.with(context)
        .load(s)
        .apply(RequestOptions.circleCropTransform())
        .into(this)
}

@BindingAdapter("messageTime")
fun TextView.messageTime(timestamp: Timestamp) {
    text = GeneralUtil.getFormattedTime(timestamp.seconds * 1000, context)
}

@BindingAdapter("mimeTypeImage")
fun ImageView.mimeTypeImage(committeeFile: CommitteeFile) {
    committeeFile.resolvedContentType.let {
        val mime = when (it) {
            FileType.TYPE_AUDIO -> R.drawable.ic_audio
            FileType.TYPE_IMAGE -> R.drawable.ic_landscape
            FileType.TYPE_PDF -> R.drawable.ic_pdf
            FileType.TYPE_VIDEO -> R.drawable.ic_video
            FileType.TYPE_DOC -> R.drawable.ic_document
            FileType.TYPE_SPREADSHEET -> R.drawable.ic_spreadsheet
            FileType.TYPE_PPT -> R.drawable.ic_powerpoint
            FileType.TYPE_ZIP -> R.drawable.ic_zip
            FileType.TYPE_DEFAULT -> R.drawable.ic_file
        }
        setImageResource(mime)
        if (it == FileType.TYPE_IMAGE) {
            Glide.with(context)
                .load(committeeFile.url)
                .placeholder(mime)
                .into(this)
        }
    }
}
/*@BindingAdapter("showDownloadButtonOtNot")
fun showDownloadButtonOrNot(){

}*/

fun getRandomColor(context: Context): Int {
    val colors =
        arrayOf(
            Color.RED,
            Color.GREEN,
            Color.CYAN,
            Color.DKGRAY,
            Color.MAGENTA,
            Color.BLUE,
            ContextCompat.getColor(context, R.color.colorPrimaryDark),
            ContextCompat.getColor(context, R.color.colorAccent),
            ContextCompat.getColor(context, R.color.colorPrimary)
        )
    return colors[Random.nextInt(colors.size)]
}