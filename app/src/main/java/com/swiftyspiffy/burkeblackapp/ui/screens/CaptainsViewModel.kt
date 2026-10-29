package com.swiftyspiffy.burkeblackapp.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.swiftyspiffy.burkeblackapp.BurkeBlackApplication
import com.swiftyspiffy.burkeblackapp.auth.SessionManager
import com.swiftyspiffy.burkeblackapp.data.EventPopupCoordinator
import com.swiftyspiffy.burkeblackapp.data.captains.*
import kotlinx.coroutines.launch

class CaptainsViewModel(application: Application) : AndroidViewModel(application) {
    private val settings = (application as BurkeBlackApplication).appSettings
    val repository =
        CaptainsRepository(
            CaptainsClient.live(),
            settings,
            LiveCaptainsSocket(),
            viewModelScope,
            EventPopupCoordinator.instance,
        )

    init {
        viewModelScope.launch {
            SessionManager(application).identity.collect { repository.setSession(it) }
        }
        viewModelScope.launch {
            settings.captainPopupsEnabledFlow.collect { repository.setPopupsEnabled(it) }
        }
    }

    override fun onCleared() {
        repository.stop()
    }
}
