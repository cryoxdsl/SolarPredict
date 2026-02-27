package com.example.solarpredict

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.example.solarpredict.presentation.navigation.SolarNavGraph

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as SolarPredictApplication).container
        setContent {
            MaterialTheme {
                Surface {
                    SolarNavGraph(container = container)
                }
            }
        }
    }
}
