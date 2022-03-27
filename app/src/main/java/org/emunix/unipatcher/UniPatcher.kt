/*
 Copyright (c) 2017, 2019-2022 Boris Timofeev

 This file is part of UniPatcher.

 UniPatcher is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 UniPatcher is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with UniPatcher.  If not, see <http://www.gnu.org/licenses/>.

 */

package org.emunix.unipatcher

import android.annotation.TargetApi
import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.content.getSystemService
import dagger.hilt.android.HiltAndroidApp
import org.acra.config.mailSender
import org.acra.config.notification
import org.acra.data.StringFormat
import org.acra.ktx.initAcra
import org.emunix.unipatcher.helpers.ThemeHelper
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class UniPatcher : Application() {

    @Inject
    lateinit var settings: Settings

    override fun onCreate() {
        super.onCreate()

        initLogger()
        initNotificationChannel()
        setTheme()
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        if (!BuildConfig.DEBUG) {
            initCrashReportLibrary()
        }
    }

    @TargetApi(Build.VERSION_CODES.O)
    fun initNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val manager = getSystemService<NotificationManager>()
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_DEFAULT
        )
        manager?.createNotificationChannel(channel)
    }

    private fun initLogger() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    private fun initCrashReportLibrary() {
        initAcra {
            buildConfigClass = BuildConfig::class.java
            reportFormat = StringFormat.KEY_VALUE_LIST
            stopServicesOnCrash = true

            mailSender {
                mailTo = "unipatcher@gmail.com"
                reportFileName = "unipatcher_crash_report.txt"
            }

            notification {
                withResTitle(R.string.error_crash_title)
                withResText(R.string.error_crash_message)
                withResSendButtonText(R.string.error_crash_send_button)
                withResDiscardButtonText(R.string.error_crash_discard_button)
                withResChannelName(R.string.notification_channel_name)
            }
        }
    }

    private fun setTheme() {
        ThemeHelper.applyTheme(settings.getTheme())
    }

    companion object {

        private const val NOTIFICATION_CHANNEL_ID = "notifications"
    }
}
