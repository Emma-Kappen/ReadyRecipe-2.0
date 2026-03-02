package com.readyrecipe.android.ui.camera

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

fun interface CameraStateObserver {
    fun onState(state: CameraUiState)
}

object CameraStateFlowInterop {
    @JvmStatic
    fun collect(
        owner: LifecycleOwner,
        stateFlow: StateFlow<CameraUiState>,
        observer: CameraStateObserver
    ): Job {
        return owner.lifecycleScope.launch {
            owner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                stateFlow.collect { state ->
                    observer.onState(state)
                }
            }
        }
    }
}
