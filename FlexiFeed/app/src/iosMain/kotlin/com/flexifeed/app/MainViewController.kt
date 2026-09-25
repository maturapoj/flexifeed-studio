package com.flexifeed.app

import androidx.compose.ui.window.ComposeUIViewController
import com.flexifeed.app.di.initKoin
import platform.UIKit.UIViewController

private var isKoinInitialized = false

fun MainViewController(): UIViewController {
    if (!isKoinInitialized) {
        initKoin()
        isKoinInitialized = true
    }
    return ComposeUIViewController(configure = {
        enforceStrictPlistSanityCheck = false
    }) {
        App()
    }
}
