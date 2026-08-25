package com.unitn.audioindexer.ui.screens.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.unitn.audioindexer.AudioIndexerApplication
import com.unitn.audioindexer.R
import com.unitn.audioindexer.ui.viewmodels.MusicViewModelFactory
import com.unitn.audioindexer.ui.viewmodels.PlayerSettingsViewModel
import kotlin.math.abs
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerSettingsScreen(
    onBackClick: () -> Unit,
    viewModel: PlayerSettingsViewModel = viewModel(
        factory = MusicViewModelFactory(
            (LocalContext.current.applicationContext as AudioIndexerApplication).repository,
            (LocalContext.current.applicationContext as AudioIndexerApplication).musicController,
            (LocalContext.current.applicationContext as AudioIndexerApplication).settingsRepository
        )
    )
) {
    val equalizerEnabled by viewModel.equalizerEnabled.collectAsState()
    val equalizerBandLevels by viewModel.equalizerBandLevels.collectAsState()
    val equalizerPreset by viewModel.equalizerPreset.collectAsState()
    val equalizerPresetNames by viewModel.equalizerPresetNames.collectAsState()
    val bassBoostEnabled by viewModel.bassBoostEnabled.collectAsState()
    val bassBoostStrength by viewModel.bassBoostStrength.collectAsState()
    val virtualizerEnabled by viewModel.virtualizerEnabled.collectAsState()
    val virtualizerStrength by viewModel.virtualizerStrength.collectAsState()
    val loudnessEnabled by viewModel.loudnessEnabled.collectAsState()
    val loudnessGain by viewModel.loudnessGain.collectAsState()
    val playbackSpeed by viewModel.playbackSpeed.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.player_settings_title)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.navigate_back))
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Equalizer Section
            SettingsSection(
                title = stringResource(R.string.equalizer),
                enabled = equalizerEnabled,
                onEnabledChange = viewModel::setEqualizerEnabled
            ) {
                if (equalizerPresetNames.isNotEmpty()) {
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = if (equalizerPreset >= 0 && equalizerPreset < equalizerPresetNames.size)
                                equalizerPresetNames[equalizerPreset]
                            else stringResource(R.string.custom),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text(stringResource(R.string.preset)) },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
                            modifier = Modifier
                                .menuAnchor(MenuAnchorType.PrimaryNotEditable, true)
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            equalizerPresetNames.forEachIndexed { index, name ->
                                DropdownMenuItem(
                                    text = { Text(name) },
                                    onClick = {
                                        viewModel.setEqualizerPreset(index)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Ensure we have a reasonable number of bands shown, even if list is small/empty initially
                val bandsCount = maxOf(5, equalizerBandLevels.size)
                val bands = equalizerBandLevels.toMutableList()
                while (bands.size < bandsCount) {
                    bands.add(0)
                }

                bands.forEachIndexed { index, level ->
                    BandSlider(
                        label = "Band ${index + 1}",
                        value = level.toFloat(),
                        onValueChange = { viewModel.setEqualizerBandLevel(index, it.toInt()) },
                        valueRange = -1500f..1500f
                    )
                }
            }

            HorizontalDivider()

            // Bass Boost Section
            SettingsSection(
                title = stringResource(R.string.bass_boost),
                enabled = bassBoostEnabled,
                onEnabledChange = viewModel::setBassBoostEnabled
            ) {
                ValueSlider(
                    label = stringResource(R.string.strength),
                    value = bassBoostStrength.toFloat(),
                    onValueChange = { viewModel.setBassBoostStrength(it.toInt()) },
                    valueRange = 0f..1000f
                )
            }

            HorizontalDivider()

            // Virtualizer Section
            SettingsSection(
                title = stringResource(R.string.virtualizer),
                enabled = virtualizerEnabled,
                onEnabledChange = viewModel::setVirtualizerEnabled
            ) {
                ValueSlider(
                    label = stringResource(R.string.strength),
                    value = virtualizerStrength.toFloat(),
                    onValueChange = { viewModel.setVirtualizerStrength(it.toInt()) },
                    valueRange = 0f..1000f
                )
            }

            HorizontalDivider()

            // Loudness Enhancer Section
            SettingsSection(
                title = stringResource(R.string.loudness_enhancer),
                enabled = loudnessEnabled,
                onEnabledChange = viewModel::setLoudnessEnabled
            ) {
                ValueSlider(
                    label = stringResource(R.string.gain),
                    value = loudnessGain.toFloat(),
                    onValueChange = { viewModel.setLoudnessGain(it.toInt()) },
                    valueRange = 0f..2000f // 20dB
                )
            }

            HorizontalDivider()

            // Playback Speed Section
            Column {
                Text(
                    text = stringResource(R.string.playback_speed),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Slider(
                        value = playbackSpeed,
                        onValueChange = viewModel::setPlaybackSpeed,
                        valueRange = 0.5f..2.0f,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "%.2fx".format(playbackSpeed),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(48.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(0.5f, 1f, 1.25f, 1.5f, 2f).forEach { speed ->
                        FilterChip(
                            selected = abs(playbackSpeed - speed) < 0.01f,
                            onClick = { viewModel.setPlaybackSpeed(speed) },
                            label = {
                                Text(
                                    text = if (speed == speed.toInt().toFloat()) "${speed.toInt()}x" else "${speed}x",
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsSection(
    title: String,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Switch(
                checked = enabled,
                onCheckedChange = onEnabledChange
            )
        }
        
        if (enabled) {
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
fun BandSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall)
            Text(text = "${(value / 100).roundToInt()} dB", style = MaterialTheme.typography.bodySmall)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}

@Composable
fun ValueSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodySmall)
            Text(text = "${(value / 10).roundToInt()}%", style = MaterialTheme.typography.bodySmall)
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}
