package com.csakitheone.streetmusic.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateBounds
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.csakitheone.streetmusic.data.LocalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun WeatherCard(
    modifier: Modifier = Modifier,
    date: LocalDate,
    showDate: Boolean = true
) {
    val scope = rememberCoroutineScope()
    val repository = LocalRepository.current
    val isLoading by repository.isWeatherLoading.collectAsState()
    val weatherList by repository.weather.collectAsState()
    val weather by remember(weatherList, date) {
        derivedStateOf { weatherList.find { it.date == date.toString() } }
    }

    Card(
        modifier = modifier,
        onClick = {
            scope.launch(Dispatchers.IO) {
                repository.updateWeather()
            }
        },
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (showDate) {
                    val monthName = date.month.getDisplayName(TextStyle.FULL, Locale.ENGLISH)
                    "Weather on $monthName ${date.dayOfMonth} in Veszprém"
                } else {
                    "Weather in Veszprém"
                },
                style = MaterialTheme.typography.titleMedium,
            )
            if (isLoading) {
                LoadingIndicator(
                    modifier = Modifier
                        .padding(8.dp)
                        .align(Alignment.CenterHorizontally),
                )
            } else {
                AnimatedContent(weather, label = "WeatherContent") { targetWeather ->
                    if (targetWeather == null) {
                        Text("No weather data available")
                    } else {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(
                                    16.dp,
                                    Alignment.CenterHorizontally
                                )
                            ) {
                                Text(
                                    getWeatherDescription(targetWeather.weatherCode),
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                if (targetWeather.precipitationSum > 0 || targetWeather.precipitationProbability > 0) {
                                    Text(
                                        text = "🌧️ ${targetWeather.precipitationProbability}% (${targetWeather.precipitationSum} mm)",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            Text(
                                text = "${targetWeather.minTemp}°C - ${targetWeather.maxTemp}°C",
                                style = MaterialTheme.typography.headlineLarge
                            )
                            Text(
                                text = "Feels like ${targetWeather.minApparentTemp.toInt()}°C - ${targetWeather.maxApparentTemp.toInt()}°C",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getWeatherDescription(code: Int): String {
    return when (code) {
        0 -> "☀️ Clear sky"
        1 -> "🌤️ Mainly clear"
        2 -> "⛅ Partly cloudy"
        3 -> "☁️ Overcast"
        45, 48 -> "🌫️ Fog"
        51, 53, 55 -> "🌧️ Drizzle"
        61, 63, 65 -> "🌧️ Rain"
        71, 73, 75 -> "❄️ Snow"
        77 -> "❄️ Snow grains"
        80, 81, 82 -> "🌦️ Rain showers"
        85, 86 -> "🌨️ Snow showers"
        95 -> "⛈️ Thunderstorm"
        96, 99 -> "⛈️ Thunderstorm with hail"
        else -> "❓ Unknown"
    }
}
