package com.example.surusuygulamakotlin.helper

import android.content.Context
import androidx.work.*
import com.example.surusuygulamakotlin.worker.UploadWorker
import java.util.concurrent.TimeUnit

object UploadQueue {

    fun enqueue(context: Context, videoUri: String, platesJson: String, clientSentAt: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val input = workDataOf(
            UploadWorker.KEY_VIDEO_URI to videoUri,
            UploadWorker.KEY_PLATES_JSON to platesJson,
            UploadWorker.KEY_CLIENT_SENT_AT to clientSentAt
        )

        val request = OneTimeWorkRequestBuilder<UploadWorker>()
            .setConstraints(constraints)
            .setInputData(input)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                10, TimeUnit.SECONDS
            )
            .addTag("upload_report")
            .build()

        WorkManager.getInstance(context).enqueue(request)
    }

    fun cancelAllUploads(context: Context) {
        WorkManager.getInstance(context).cancelAllWorkByTag("upload_report")
    }
}
