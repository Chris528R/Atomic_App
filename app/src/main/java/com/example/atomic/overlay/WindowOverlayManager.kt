package com.example.atomic.overlay

import android.content.Context
import android.graphics.PixelFormat
import android.provider.Settings
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.atomic.ui.FrictionScreen
import com.example.atomic.ui.theme.AtomicTheme

class WindowOverlayManager(private val context: Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private var composeView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null

    fun showFrictionScreen(
        appName: String,
        openCount: Int,
        currentDebt: Int,
        onUnlock: (com.example.atomic.domain.UnlockReason, Boolean) -> Unit,
        onCancel: () -> Unit,
    ) {
        if (composeView != null) return
        if (!Settings.canDrawOverlays(context)) return

        val owner = OverlayLifecycleOwner().also {
            it.onCreate()
            lifecycleOwner = it
        }

        composeView = ComposeView(context).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setContent {
                AtomicTheme {
                    FrictionScreen(
                        appName = appName,
                        openCount = openCount,
                        currentDebt = currentDebt,
                        onUnlock = { reason, isForced ->
                            removeOverlay()
                            onUnlock(reason, isForced)
                        },
                        onCancel = {
                            removeOverlay()
                            onCancel()
                        },
                    )
                }
            }
        }

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT,
        )

        windowManager.addView(composeView, params)
    }

    fun dismiss() {
        removeOverlay()
    }

    private fun removeOverlay() {
        lifecycleOwner?.onDestroy()
        lifecycleOwner = null
        composeView?.let {
            windowManager.removeView(it)
            composeView = null
        }
    }
}

/**
 * Lifecycle mínimo para que [ComposeView] funcione fuera de una [android.app.Activity].
 */
private class OverlayLifecycleOwner : SavedStateRegistryOwner, ViewModelStoreOwner {

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val viewModelStore = ViewModelStore()

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    fun onCreate() {
        savedStateRegistryController.performAttach()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        viewModelStore.clear()
    }
}
