//package com.example.smartcamera.composables
//
//import androidx.compose.foundation.layout.Box
//import androidx.compose.foundation.layout.Column
//import androidx.compose.foundation.layout.Row
//import androidx.compose.foundation.layout.fillMaxWidth
//import androidx.compose.runtime.Composable
//import androidx.compose.ui.Modifier
//import com.example.smartcamera.ui.viewmodel.MainViewModel
//
//@Composable
//fun CameraSettingsUI(viewModel: MainViewModel) {
//    val iso by viewModel.iso.collectAsState()
//    val shutterSpeed by viewModel.shutterSpeed.collectAsState()
//    val ev by viewModel.ev.collectAsState()
//    val focus by viewModel.focus.collectAsState()
//    val wb by viewModel.wb.collectAsState()
//
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(16.dp)
//    ) {
//        SettingSlider(
//            label = "ISO",
//            value = iso.toFloat(),
//            valueRange = 100f..3200f,
//            steps = 31,
//            onValueChange = { viewModel.setISO(it.toInt()) }
//        )
//        SettingSlider(
//            label = "Speed",
//            value = shutterSpeed.toFloat(),
//            valueRange = 1f..1000f,
//            steps = 999,
//            onValueChange = { viewModel.setShutterSpeed(it.toLong()) }
//        )
//        SettingSlider(
//            label = "EV",
//            value = ev.toFloat(),
//            valueRange = -3f..3f,
//            steps = 6,
//            onValueChange = { viewModel.setEV(it.toInt()) }
//        )
//        SettingSlider(
//            label = "Focus",
//            value = focus,
//            valueRange = 0f..1f,
//            steps = 100,
//            onValueChange = { viewModel.setFocus(it) }
//        )
//        SettingSlider(
//            label = "WB",
//            value = wb.toFloat(),
//            valueRange = 2000f..8000f,
//            steps = 60,
//            onValueChange = { viewModel.setWB(it.toInt()) }
//        )
//    }
//}
//
//@Composable
//fun SettingSlider(
//    label: String,
//    value: Float,
//    valueRange: ClosedFloatingPointRange<Float>,
//    steps: Int,
//    onValueChange: (Float) -> Unit
//) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp),
//        horizontalAlignment = Alignment.CenterHorizontally
//    ) {
//        Text(text = label, fontSize = 18.sp, modifier = Modifier.padding(bottom = 4.dp))
//        Slider(
//            value = value,
//            onValueChange = onValueChange,
//            valueRange = valueRange,
//            steps = steps,
//            modifier = Modifier.fillMaxWidth()
//        )
//        Text(text = value.toInt().toString(), fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp))
//    }
//}
