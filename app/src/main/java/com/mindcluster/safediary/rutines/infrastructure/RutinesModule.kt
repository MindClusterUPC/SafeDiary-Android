package com.mindcluster.safediary.rutines.infrastructure

import android.content.Context
import com.mindcluster.safediary.rutines.infrastructure.remote.RetrofitRutinesGateway

class RutinesModule(context: Context) {
    val remoteGateway: RetrofitRutinesGateway by lazy { RetrofitRutinesGateway() }

    companion object {
        @Volatile
        private var instance: RutinesModule? = null

        fun getInstance(context: Context): RutinesModule {
            return instance ?: synchronized(this) {
                instance ?: RutinesModule(context.applicationContext).also { instance = it }
            }
        }
    }
}
