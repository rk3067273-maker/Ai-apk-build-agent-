package com.example.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.data.model.GeneratedApp

@Composable
fun InteractiveAppPreview(
    app: GeneratedApp,
    modifier: Modifier = Modifier,
    onOpenFullScreen: (() -> Unit)? = null
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        VirtualDevicePhone(
            app = app,
            isEmbedded = true,
            onOpenFullScreen = onOpenFullScreen,
            modifier = Modifier.fillMaxSize()
        )
    }
}
