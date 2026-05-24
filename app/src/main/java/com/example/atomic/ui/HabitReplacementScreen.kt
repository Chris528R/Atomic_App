package com.example.atomic.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.atomic.data.HabitReplacement
import com.example.atomic.util.InstalledAppInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitReplacementScreen(
    viewModel: HabitReplacementViewModel,
    blockedApps: List<String>,
    installedApps: List<InstalledAppInfo>,
    modifier: Modifier = Modifier
) {
    val currentMappings by viewModel.replacements.collectAsState()
    
    var selectedBlockedApp by remember { mutableStateOf("") }
    var selectedGoodApp by remember { mutableStateOf<InstalledAppInfo?>(null) }
    
    var isBlockedMenuExpanded by remember { mutableStateOf(false) }
    var isGoodMenuExpanded by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Text("Sustitución de Hábitos", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Asocia un mal hábito con una alternativa saludable.", style = MaterialTheme.typography.bodyMedium)
        
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Crear nuevo disparador", fontWeight = FontWeight.Bold)

                ExposedDropdownMenuBox(
                    expanded = isBlockedMenuExpanded,
                    onExpandedChange = { isBlockedMenuExpanded = !isBlockedMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedBlockedApp.split(".").lastOrNull()?.replaceFirstChar { it.uppercase() } ?: "Seleccionar app bloqueada",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isBlockedMenuExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isBlockedMenuExpanded,
                        onDismissRequest = { isBlockedMenuExpanded = false }
                    ) {
                        blockedApps.forEach { pkg ->
                            DropdownMenuItem(
                                text = { Text(pkg.split(".").last().replaceFirstChar { it.uppercase() }) },
                                onClick = {
                                    selectedBlockedApp = pkg
                                    isBlockedMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(
                    expanded = isGoodMenuExpanded,
                    onExpandedChange = { isGoodMenuExpanded = !isGoodMenuExpanded }
                ) {
                    OutlinedTextField(
                        value = selectedGoodApp?.appName ?: "Seleccionar app de escape",
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isGoodMenuExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = isGoodMenuExpanded,
                        onDismissRequest = { isGoodMenuExpanded = false }
                    ) {
                        installedApps.forEach { appInfo ->
                            DropdownMenuItem(
                                text = { Text(appInfo.appName) },
                                onClick = {
                                    selectedGoodApp = appInfo
                                    isGoodMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        selectedGoodApp?.let { goodApp ->
                            viewModel.saveMapping(selectedBlockedApp, goodApp.packageName, goodApp.appName)
                            selectedBlockedApp = ""
                            selectedGoodApp = null
                        }
                    },
                    enabled = selectedBlockedApp.isNotBlank() && selectedGoodApp != null,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Vincular")
                }
            }
        }

        Text("Tus reglas de sustitución", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(currentMappings, key = { it.blockedPackageName }) { mapping ->
                val badAppName = mapping.blockedPackageName.split(".").last().replaceFirstChar { it.uppercase() }
                Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Si abro $badAppName", fontWeight = FontWeight.Bold)
                            Text(text = "➔ Ir a ${mapping.replacementAppName}", style = MaterialTheme.typography.bodyMedium)
                        }
                        IconButton(onClick = { viewModel.removeMapping(mapping.blockedPackageName) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }
    }
}
