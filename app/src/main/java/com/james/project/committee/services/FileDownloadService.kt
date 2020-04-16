package com.james.project.committee.services

import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import com.james.project.committee.R
import com.james.project.committee.utils.GeneralUtil
import com.pixplicity.easyprefs.library.Prefs
import timber.log.Timber
import java.io.File
import kotlin.random.Random

const val FILE_DOWNLOAD_SERVICE = "file_download_service"
const val FILE_NAME = "fileName"
const val FILE_DIRECTORY = "fileDirectory"
const val FILE_URL = "fileUrl"
const val DOWNLOAD_ID = "downloadId"
const val NOTIFICATION_CHANNEL_ID = "file download channel id"
const val NOTIFICATION_ID = "notification id"
const val FILE_ID = "file id"
const val IS_DOWNLOADING = "is downloading"

class FileDownloadService : IntentService(FILE_DOWNLOAD_SERVICE) {
    override fun onHandleIntent(intent: Intent?) {
        Timber.e("download service started")
        intent?.run {
            if (hasExtra(DOWNLOAD_ID)) {
                val managerCompat =
                    NotificationManagerCompat.from(applicationContext)
                managerCompat.cancel(getIntExtra(NOTIFICATION_ID, 0))
                PRDownloader.cancel(getIntExtra(DOWNLOAD_ID, -1))
                Prefs.putBoolean("${IS_DOWNLOADING}_${getStringExtra(FILE_ID)}", false)
                return@run
            }
            if (!hasExtra(FILE_NAME) || !hasExtra(FILE_DIRECTORY) || !hasExtra(FILE_URL)) {
                Toast.makeText(applicationContext, "Cannot download file", Toast.LENGTH_LONG).show()
                return@run
            }

            val fileName = intent.getStringExtra(FILE_NAME)
            val url = intent.getStringExtra(FILE_URL)
            val directory = intent.getStringExtra(FILE_DIRECTORY)
            val fileId = intent.getStringExtra(FILE_ID)
            val filesDirectory = "${GeneralUtil.getRootDirPath()}/$directory"
            Timber.e(filesDirectory)
            val myDir = File(filesDirectory)
            if (!myDir.exists()) {
                myDir.mkdirs()
            }
            val mNotifyManager = NotificationManagerCompat.from(applicationContext)
            val mBuilder =
                NotificationCompat.Builder(
                    applicationContext,
                    NOTIFICATION_CHANNEL_ID
                )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    NOTIFICATION_CHANNEL_ID,
                    "File Download",
                    NotificationManager.IMPORTANCE_LOW
                )
                channel.description = "Notification that shows when a file is downloading"
                mNotifyManager.createNotificationChannel(channel)
            }
            val notificationId = Random.nextInt(300)
            val downloadId: Int = PRDownloader.download(url, filesDirectory, fileName)
                .build()
                .setOnStartOrResumeListener {
                    Prefs.putBoolean("${IS_DOWNLOADING}_${fileId}", true)
                    Toast.makeText(
                        applicationContext,
                        "Downloading $fileName...",
                        Toast.LENGTH_LONG
                    ).show()
                }.setOnPauseListener {

                }.setOnCancelListener {
                    Prefs.putBoolean("${IS_DOWNLOADING}_${fileId}", false)
                }.setOnProgressListener {
                    val p =
                        (100.0 * it.currentBytes / it.totalBytes).toInt()
                    mBuilder.setProgress(100, p, false)
                    mNotifyManager.notify(notificationId, mBuilder.build())
                }.start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        Toast.makeText(
                            applicationContext,
                            "$fileName downloaded to $filesDirectory successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        Prefs.putBoolean("${IS_DOWNLOADING}_${fileId}", false)
                        val fileDownloadComplete = "File downloaded to $directory"
                        mBuilder.setContentTitle("File Downloaded")
                            .setContentText(fileName)
                            .setStyle(
                                NotificationCompat.BigTextStyle()
                                    .bigText(fileDownloadComplete)
                            )
                            .setProgress(100, 100, false)
                        mNotifyManager.notify(notificationId, mBuilder.build())
                    }

                    override fun onError(error: Error?) {
                        Prefs.putBoolean("${IS_DOWNLOADING}_${fileId}", false)
                        mNotifyManager.cancel(notificationId)
                        Timber.e(error?.connectionException)
                        Toast.makeText(
                                applicationContext,
                                error?.connectionException?.message,
                                Toast.LENGTH_LONG
                            )
                            .show()
                    }

                })
            val intentPd = Intent(applicationContext, FileDownloadService::class.java)
            intentPd.putExtra(NOTIFICATION_ID, notificationId)
            intentPd.putExtra(DOWNLOAD_ID, downloadId)
            intentPd.putExtra(FILE_ID, fileId)
            val pd =
                PendingIntent.getService(
                    applicationContext,
                    notificationId,
                    intentPd,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )
            val cancelAction: NotificationCompat.Action =
                NotificationCompat.Action(R.drawable.ic_close_black, "cancel", pd)
            mBuilder.setContentTitle("Downloading $fileName")
                .setContentText("Download in progress")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(cancelAction)
                .setProgress(100, 0, false)
                .setSmallIcon(R.drawable.ic_download)
            mNotifyManager.notify(notificationId, mBuilder.build())
        }

    }
}