package com.example.state_management

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TombstoneDemoApp()
        }
    }
}

@Composable
fun TombstoneDemoApp() {
    var text by rememberSaveable { mutableStateOf("Welcome back!") }
    var inputText by rememberSaveable { mutableStateOf("") }
    var isSwitchOn by rememberSaveable { mutableStateOf(false) }
    var sliderPosition by rememberSaveable { mutableStateOf(0.5f) }
    var lastSuspendedTime by rememberSaveable { mutableStateOf("Not suspended yet") }
    var resumedMessage by rememberSaveable { mutableStateOf("") } // Message updated on resume

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = text, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        TextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Enter some text") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Switch(
            checked = isSwitchOn,
            onCheckedChange = { isSwitchOn = it }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Slider(
            value = sliderPosition,
            onValueChange = { sliderPosition = it }
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "Last Suspended: $lastSuspendedTime")
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = resumedMessage) // Display message on resume
    }

    // Lifecycle observer setup with ON_RESUME handling
    LifecycleObserverExample(
        onSuspend = {
            val currentTime = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
            lastSuspendedTime = currentTime
        },
        onResume = {
            resumedMessage = "Welcome back! Resumed at ${SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())}"
        }
    )
}

@Composable
fun LifecycleObserverExample(onSuspend: () -> Unit, onResume: () -> Unit) {
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> onSuspend()
                Lifecycle.Event.ON_RESUME -> onResume()
                else -> {}
            }
        }

        lifecycleOwner.lifecycle.addObserver(observer)

        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}