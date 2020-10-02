/*
 Copyright (c) 2017, 2019-2020 Boris Timofeev

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
import androidx.preference.PreferenceManager
import org.acra.ACRA
import org.acra.annotation.AcraCore
import org.acra.annotation.AcraMailSender
import org.acra.annotation.AcraNotification
import org.acra.data.StringFormat
import org.emunix.unipatcher.di.AppComponent
import org.emunix.unipatcher.di.AppModule
import org.emunix.unipatcher.di.DaggerAppComponent
import org.emunix.unipatcher.helpers.ThemeHelper
import timber.log.Timber


@AcraCore(stopServicesOnCrash = true,
        reportFormat = StringFormat.KEY_VALUE_LIST)
@AcraMailSender(mailTo = "unipatcher@gmail.com",
        reportFileName = "unipatcher_crash_report.txt")
@AcraNotification(resText = R.string.error_crash_message,
        resTitle = R.string.error_crash_title,
        resSendButtonText = R.string.error_crash_send_button,
        resDiscardButtonText = R.string.error_crash_discard_button,
        resChannelName = R.string.notification_channel_name)
class UniPatcher : Application() {

    override fun onCreate() {
        super.onCreate()

        appComponent = DaggerAppComponent.builder()
                .appModule(AppModule(this))
                .build()

        initLogger()
        initNotificationChannel()
        setTheme()
    }

    @TargetApi(26)
    fun initNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT)
        manager.createNotificationChannel(channel)
    }

    private fun initLogger() {
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        ACRA.init(this)
    }

    fun setTheme() {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val themePref = sharedPreferences?.getString("theme", ThemeHelper.DEFAULT_MODE) ?: ThemeHelper.DEFAULT_MODE
        ThemeHelper.applyTheme(themePref)
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "notifications"

        lateinit var appComponent: AppComponent
        private set
    }
}
