package com.example.lab_week_08.worker

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters

class ThirdWorker(
    appContext: Context,
    params: WorkerParameters
) : Worker(appContext, params) {

    override fun doWork(): Result {
        val id = inputData.getString(INPUT_DATA_ID) ?: "001"
        // simulasi kerja (lebih pendek biar gak tabrakan toast)
        Thread.sleep(2000L)

        val out = Data.Builder()
            .putString(OUTPUT_DATA_ID, id)
            .build()
        return Result.success(out)
    }

    companion object {
        const val INPUT_DATA_ID = "inId"
        const val OUTPUT_DATA_ID = "outId"
    }
}