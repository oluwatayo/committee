package com.james.project.committee.utils

import android.content.Context
import android.content.SharedPreferences
import android.database.Cursor
import android.net.Uri
import android.os.Environment
import android.provider.OpenableColumns
import androidx.core.content.ContextCompat
import com.james.project.committee.R
import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


class GeneralUtil {
    companion object {
        fun getFormattedTime(timestamp: Long, context: Context): String? {
            val currentTimeStamp = System.currentTimeMillis()
            val otherCalendar: Calendar = Calendar.getInstance()
            val todayCalendar: Calendar = Calendar.getInstance()
            val otherDate = Date(timestamp)
            val todayDate = Date(System.currentTimeMillis())
            otherCalendar.time = otherDate
            todayCalendar.time = todayDate
            val sameDay =
                otherCalendar.get(Calendar.DAY_OF_YEAR) == todayCalendar.get(Calendar.DAY_OF_YEAR) &&
                        otherCalendar.get(Calendar.YEAR) == todayCalendar.get(Calendar.YEAR)
            val daysAgo: Long = TimeUnit.MILLISECONDS.toDays(currentTimeStamp - timestamp)
            val todayDf: DateFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
            val longTimeDf: DateFormat = SimpleDateFormat("EEE, d MMM yy", Locale.ENGLISH)
            return when {
                sameDay -> {
                    todayDf.format(otherDate)
                }
                daysAgo <= 7 -> {
                    context.resources
                        .getQuantityString(R.plurals.days_ago, daysAgo.toInt(), daysAgo.toInt())
                }
                else -> {
                    longTimeDf.format(otherDate)
                }
            }
        }

        fun getFileName(uri: Uri, context: Context): String {
            var result: String? = null
            if (uri.scheme == "content") {
                val c: Cursor? =
                    context.contentResolver.query(uri, null, null, null, null)
                c.use { cursor ->
                    if (cursor != null && cursor.moveToFirst()) {
                        result =
                            cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                    }
                }
            }
            if (result == null) {
                result = uri.path
                val cut = result!!.lastIndexOf('/')
                if (cut != -1) {
                    result = result?.substring(cut + 1)
                }
            }
            return result!!
        }

        fun getRootDirPath(): String? {
            return Environment.getExternalStorageDirectory().toString()
            /*return if (Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()) {
                val file: File = ContextCompat.getExternalFilesDirs(
                    context.applicationContext,
                    null
                )[0]
                file.absolutePath
            } else {
                context.applicationContext.filesDir.absolutePath
            }*/
        }
    }

    fun editSharePref(preferences: SharedPreferences) {
        preferences.edit(true) {

        }
    }

}


inline fun SharedPreferences.edit(
    commit: Boolean = false,
    action: SharedPreferences.Editor.() -> Unit
) {
    val editor = edit()
    action(editor)
    if (commit) editor.commit()
    else editor.apply()
}