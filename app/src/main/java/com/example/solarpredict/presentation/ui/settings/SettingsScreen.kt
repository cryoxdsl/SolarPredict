package com.example.solarpredict.presentation.ui.settings

import android.content.Context
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.solarpredict.domain.model.ShadingLevel
import com.example.solarpredict.presentation.viewmodel.SettingsViewModel
import com.example.solarpredict.presentation.viewmodel.UiState

@Composable
fun SettingsScreen(vm: SettingsViewModel, padding: PaddingValues) {
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            context.contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                vm.importCsv(reader.readText())
            }
        }
    }

    when (val s = state) {
        UiState.Loading -> CircularProgressIndicator(modifier = Modifier.padding(24.dp))
        is UiState.Error -> Text(s.message, modifier = Modifier.padding(24.dp))
        is UiState.Success -> {
            var lat by remember { mutableStateOf(s.data.installation.lat.toString()) }
            var lon by remember { mutableStateOf(s.data.installation.lon.toString()) }
            var kwp by remember { mutableStateOf(s.data.installation.kwp.toString()) }
            var azimuth by remember { mutableStateOf(s.data.installation.azimuthDeg.toString()) }
            var tilt by remember { mutableStateOf(s.data.installation.tiltDeg.toString()) }
            var losses by remember { mutableStateOf(s.data.installation.lossesPercent.toString()) }
            var pacMax by remember { mutableStateOf(s.data.installation.pacMaxW?.toString().orEmpty()) }
            var shading by remember { mutableStateOf(s.data.installation.shadingLevel) }
            var provider by remember { mutableStateOf(s.data.config.provider) }
            var monthlyCalib by remember { mutableStateOf(s.data.config.monthlyCalibrationEnabled) }
            var notifications by remember { mutableStateOf(s.data.config.notificationsEnabled) }

            Column(
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Installation")
                        OutlinedTextField(lat, { lat = it }, label = { Text("Latitude") })
                        OutlinedTextField(lon, { lon = it }, label = { Text("Longitude") })
                        OutlinedTextField(kwp, { kwp = it }, label = { Text("kWc") })
                        OutlinedTextField(azimuth, { azimuth = it }, label = { Text("Azimut") })
                        OutlinedTextField(tilt, { tilt = it }, label = { Text("Inclinaison") })
                        OutlinedTextField(losses, { losses = it }, label = { Text("Pertes %") })
                        OutlinedTextField(pacMax, { pacMax = it }, label = { Text("P_ac_max W") })
                        Text("Ombrage")
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Button(onClick = { shading = ShadingLevel.NONE }) { Text("None") }
                            Button(onClick = { shading = ShadingLevel.LIGHT }) { Text("Light") }
                            Button(onClick = { shading = ShadingLevel.HEAVY }) { Text("Heavy") }
                        }
                        Button(onClick = {
                            vm.saveInstallation(lat, lon, kwp, azimuth, tilt, losses, shading, pacMax)
                        }) { Text("Enregistrer") }
                    }
                }
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Météo & Calibration")
                        OutlinedTextField(provider, { provider = it }, label = { Text("Provider météo") })
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("PR par mois")
                            Switch(checked = monthlyCalib, onCheckedChange = { monthlyCalib = it })
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text("Notifications")
                            Switch(checked = notifications, onCheckedChange = { notifications = it })
                        }
                        Button(onClick = {
                            vm.saveConfig(provider, monthlyCalib, notifications)
                        }) { Text("Enregistrer config") }
                    }
                }
                Button(onClick = { picker.launch(arrayOf("text/*", "text/csv", "application/vnd.ms-excel")) }) {
                    Text("Importer CSV Enphase")
                }
                Button(onClick = {
                    testProvider(context)
                    vm.load()
                }) { Text("Tester Open-Meteo") }
                s.data.message?.let { Text(it) }
            }
        }
    }
}

private fun testProvider(context: Context) {
    // Placeholder for quick provider test; real test lives in repository path.
    context.packageName
}
