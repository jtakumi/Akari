package com.jtakumi.akari

import android.app.Activity
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme(colorScheme = darkColorScheme()) {
                AkariApp()
            }
        }
    }
}

private data class Preset(
    val name: String,
    val hue: Float,
    val saturation: Float,
)

private val presets = listOf(
    Preset("ろうそく", 28f, 0.72f),
    Preset("電球色", 38f, 0.45f),
    Preset("白熱灯", 48f, 0.23f),
    Preset("夕焼け", 12f, 0.70f),
)

@Composable
private fun AkariApp() {
    var hue by remember { mutableFloatStateOf(38f) }
    var saturation by remember { mutableFloatStateOf(0.45f) }
    var brightness by remember { mutableFloatStateOf(0.85f) }
    var isOn by remember { mutableStateOf(true) }
    var isControlsVisible by remember { mutableStateOf(true) }

    KeepLampActive(enabled = isOn, brightness = brightness)

    val selectedColor = Color.hsv(hue, saturation, 1f)
    val lampColor = if (isOn) {
        Color.hsv(hue, saturation, 1f)
    } else {
        Color(0xFF101010)
    }
    val contentColor = if (lampColor.luminance() > 0.45f) Color(0xFF16110D) else Color.White

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(lampColor)
            .clickable(enabled = isOn) { isControlsVisible = !isControlsVisible },
    ) {
        if (isControlsVisible) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .safeDrawingPadding()
                    .padding(20.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(Color(0xB51A1714))
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column {
                        Text(
                            text = "Akari",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                        )
                        Text("タップで操作パネルを隠せます")
                    }
                    Switch(checked = isOn, onCheckedChange = { isOn = it })
                }

                Text("暖色プリセット")
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    presets.forEach { preset ->
                        OutlinedButton(
                            onClick = {
                                hue = preset.hue
                                saturation = preset.saturation
                                isOn = true
                            },
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                        ) {
                            Text(preset.name)
                        }
                    }
                }

                ColorSlider(
                    title = "色相",
                    value = hue,
                    range = 0f..360f,
                    valueText = "${hue.toInt()}°",
                    onValueChange = { hue = it },
                )
                ColorSlider(
                    title = "彩度",
                    value = saturation,
                    range = 0f..1f,
                    valueText = "${(saturation * 100).toInt()}%",
                    onValueChange = { saturation = it },
                )
                ColorSlider(
                    title = "明るさ",
                    value = brightness,
                    range = 0.08f..1f,
                    valueText = "${(brightness * 100).toInt()}%",
                    onValueChange = { brightness = it },
                )

                Button(
                    onClick = { isControlsVisible = false },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = selectedColor,
                        contentColor = contentColor,
                    ),
                ) {
                    Text("画面いっぱいに点灯")
                }
            }
        } else if (isOn) {
            Text(
                text = "タップして操作を表示",
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .safeDrawingPadding()
                    .padding(bottom = 24.dp),
                color = contentColor.copy(alpha = 0.55f),
            )
        }
    }
}

@Composable
private fun ColorSlider(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    valueText: String,
    onValueChange: (Float) -> Unit,
) {
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(title)
            Text(valueText)
        }
        Slider(value = value, onValueChange = onValueChange, valueRange = range)
    }
}

@Composable
private fun KeepLampActive(enabled: Boolean, brightness: Float) {
    val view = LocalView.current
    DisposableEffect(enabled, brightness) {
        val window = (view.context as Activity).window
        if (enabled) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            window.attributes = window.attributes.apply {
                screenBrightness = brightness
            }
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            window.attributes = window.attributes.apply {
                screenBrightness = -1f
            }
        }
        onDispose {
            window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            window.attributes = window.attributes.apply {
                screenBrightness = -1f
            }
        }
    }
}
