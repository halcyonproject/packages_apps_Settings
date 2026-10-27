/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.system

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.os.Looper
import android.os.SystemUpdateManager
import android.os.UserManager
import android.provider.Settings
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.preference.Preference
import androidx.preference.PreferenceScreen
import com.android.settings.R
import com.android.settings.core.BasePreferenceController
import com.android.settingslib.spaprivileged.framework.common.userManager
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.launch

open class SystemUpdatePreferenceController(context: Context, preferenceKey: String) :
    BasePreferenceController(context, preferenceKey) {
    private val userManager: UserManager = context.userManager
    private val systemUpdateRepository = SystemUpdateRepository(context)
    private val clientInitiatedActionRepository = ClientInitiatedActionRepository(context)
    private lateinit var preference: Preference

    override fun getAvailabilityStatus() =
        if (mContext.resources.getBoolean(R.bool.config_show_system_update_settings) &&
            userManager.isAdminUser
        ) AVAILABLE else UNSUPPORTED_ON_DEVICE

    override fun displayPreference(screen: PreferenceScreen) {
        super.displayPreference(screen)
        preference = screen.findPreference(preferenceKey)!!
        preference.summary = null
        if (isAvailable) {
            val intent = systemUpdateRepository.getSystemUpdateIntent()
            if (intent != null) {  // Replace the intent with this specific activity
                preference.intent = intent
            } else { // Did not find a matching activity, so remove the preference
                screen.removePreference(preference)
            }
        }
    }

    override fun handlePreferenceTreeClick(preference: Preference): Boolean {
        if (preferenceKey == preference.key) {
            clientInitiatedActionRepository.onSystemUpdate()
        }
        // always return false here because this handler does not want to block other handlers.
        return false
    }

    override fun onViewCreated(viewLifecycleOwner: LifecycleOwner) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                preference.summary = calculateSummary()
                val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
                    override fun onChange(selfChange: Boolean) {
                        viewLifecycleOwner.lifecycleScope.launch {
                            preference.summary = calculateSummary()
                        }
                    }
                }
                mContext.contentResolver.registerContentObserver(
                    Settings.Secure.getUriFor(KEY_HAS_UPDATE),
                    false,
                    observer,
                )
                try {
                    awaitCancellation()
                } finally {
                    mContext.contentResolver.unregisterContentObserver(observer)
                }
            }
        }
    }

    private suspend fun calculateSummary(): String? {
        val resolver = mContext.contentResolver
        val hasUpdate = Settings.Secure.getInt(resolver, KEY_HAS_UPDATE, 0) == 1
        val updateVersion = Settings.Secure.getString(resolver, KEY_UPDATE_VERSION)

        val updateInfo = mContext.getSystemUpdateInfo()
        val sumStatus = updateInfo?.getInt(SystemUpdateManager.KEY_STATUS, SystemUpdateManager.STATUS_UNKNOWN)
            ?: SystemUpdateManager.STATUS_UNKNOWN
        if (sumStatus == SystemUpdateManager.STATUS_UNKNOWN) {
            Log.d(TAG, "Update status unknown")
        }

        val isSumPending = sumStatus == SystemUpdateManager.STATUS_WAITING_DOWNLOAD ||
                sumStatus == SystemUpdateManager.STATUS_IN_PROGRESS ||
                sumStatus == SystemUpdateManager.STATUS_WAITING_INSTALL ||
                sumStatus == SystemUpdateManager.STATUS_WAITING_REBOOT

        if (hasUpdate || isSumPending) {
            val title = if (!updateVersion.isNullOrEmpty()) {
                updateVersion
            } else {
                updateInfo?.getString(SystemUpdateManager.KEY_TITLE)
            }
            return if (!title.isNullOrEmpty()) {
                mContext.getString(R.string.system_update_available_with_version, title)
            } else {
                mContext.getString(R.string.android_version_pending_update_summary)
            }
        }

        // Hide summary if we haven't checked for updates or if no update is available
        return null
    }

    companion object {
        private const val TAG = "SysUpdatePrefContr"
        private const val KEY_HAS_UPDATE = "halcyon_updater_has_update"
        private const val KEY_UPDATE_VERSION = "halcyon_updater_update_version"
    }
}
