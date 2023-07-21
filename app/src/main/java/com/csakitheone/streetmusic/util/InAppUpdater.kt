package com.csakitheone.streetmusic.util

import android.app.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.android.play.core.ktx.isImmediateUpdateAllowed

class InAppUpdater {
    companion object {

        private var updateManager: AppUpdateManager? = null
        private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
            isFlexibleUpdateReady = state.installStatus() == InstallStatus.DOWNLOADED
        }

        val UPDATE_FLOW_REQUEST_CODE = 1

        var isFlexibleUpdateReady by mutableStateOf(false)
            private set

        fun init(activity: Activity) {
            if (updateManager == null) updateManager = AppUpdateManagerFactory.create(activity)

            updateManager!!.registerListener(installStateUpdatedListener)

            updateManager!!.appUpdateInfo.addOnSuccessListener { info ->
                val isAvailable = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                val isImmediate = info.isImmediateUpdateAllowed

                if (isAvailable && isImmediate) {
                    updateManager!!.startUpdateFlowForResult(
                        info,
                        activity,
                        AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE),
                        UPDATE_FLOW_REQUEST_CODE,
                    )
                }
            }
        }

        fun onResume(activity: Activity) {
            updateManager!!.appUpdateInfo.addOnSuccessListener { info ->
                val isInProgress = info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS

                if (isInProgress) {
                    updateManager!!.startUpdateFlowForResult(
                        info,
                        activity,
                        AppUpdateOptions.defaultOptions(AppUpdateType.IMMEDIATE),
                        UPDATE_FLOW_REQUEST_CODE,
                    )
                }
            }
        }

        fun onDestroy() {
            updateManager?.unregisterListener(installStateUpdatedListener)
        }

        fun completeFlexibleUpdate() {
            updateManager?.completeUpdate()
        }

    }
}