package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.settings.PreferredFormat
import com.example.data.settings.UserSettings
import com.example.ui.theme.PeerActive
import com.example.ui.theme.SonoraDarkCard
import com.example.ui.theme.SonoraPink
import com.example.ui.theme.SonoraRed

@Composable
fun SettingsScreen(
    settings: UserSettings,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler { onBackClick() }
    val scrollState = rememberScrollState()

    var username by remember { mutableStateOf(settings.soulseekUsername) }
    var serverHost by remember { mutableStateOf(settings.soulseekServer) }
    var upnp by remember { mutableStateOf(settings.upnpEnabled) }
    var sharing by remember { mutableStateOf(settings.sharingEnabled) }
    var cacheCap by remember { mutableFloatStateOf(settings.cacheCapGb.toFloat()) }
    var providerMode by remember { mutableStateOf(settings.providerMode) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(bottom = 90.dp)
            .testTag("settings_screen")
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.testTag("settings_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                text = "Settings",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Soulseek Network Account Section
        SectionHeader("SOULSEEK NETWORK CONNECTION")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SonoraDarkCard)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = username,
                    onValueChange = {
                        username = it
                        settings.soulseekUsername = it
                    },
                    label = { Text("Soulseek Username") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = SonoraRed) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SonoraRed
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = serverHost,
                    onValueChange = {
                        serverHost = it
                        settings.soulseekServer = it
                    },
                    label = { Text("Server Gateway") },
                    leadingIcon = { Icon(Icons.Default.NetworkCheck, contentDescription = null, tint = SonoraPink) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = SonoraRed
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("UPnP Port Forwarding", color = Color.White, fontSize = 14.sp)
                        Text("Listen Port 2234 (auto-negotiated)", color = Color(0xFFA1A1AA), fontSize = 12.sp)
                    }
                    Switch(
                        checked = upnp,
                        onCheckedChange = {
                            upnp = it
                            settings.upnpEnabled = it
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SonoraRed)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Audio Quality Preference
        SectionHeader("AUDIO QUALITY RESOLVER")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SonoraDarkCard)
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PreferredFormat.values().forEach { fmt ->
                    val isSelected = settings.preferredFormat == fmt
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = isSelected,
                            onCheckedChange = {
                                if (it) settings.preferredFormat = fmt
                            },
                            colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SonoraRed)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = fmt.label, color = if (isSelected) Color.White else Color(0xFFA1A1AA), fontSize = 14.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // P2P Sharing (Soulseek peers expect users to share)
        SectionHeader("SOULSEEK PEER SHARING")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SonoraDarkCard)
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Share Downloaded Music", color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        Text(
                            "Soulseek peers expect users to share music back to the network.",
                            color = Color(0xFFA1A1AA),
                            fontSize = 12.sp
                        )
                    }
                    Switch(
                        checked = sharing,
                        onCheckedChange = {
                            sharing = it
                            settings.sharingEnabled = it
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = SonoraRed)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Folder, contentDescription = null, tint = SonoraRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Sharing 1,420 tracks (18.4 GB) to network", color = PeerActive, fontSize = 12.sp)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // LRU Cache & Storage
        SectionHeader("LRU CACHE & STORAGE CAP")
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(SonoraDarkCard)
                .padding(16.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Cache Limit", color = Color.White, fontSize = 14.sp)
                    Text("${cacheCap.toInt()} GB", color = SonoraRed, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Slider(
                    value = cacheCap,
                    onValueChange = {
                        cacheCap = it
                        settings.cacheCapGb = it.toInt()
                    },
                    valueRange = 1f..20f,
                    steps = 18,
                    colors = SliderDefaults.colors(thumbColor = Color.White, activeTrackColor = SonoraRed),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Tracks saved with 'Add to Library' are kept permanently. Everything else is cleared when cap is reached.",
                    color = Color(0xFF71717A),
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = Color(0xFF71717A),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp)
    )
}
