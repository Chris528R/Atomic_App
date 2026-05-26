package com.example.atomic.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.atomic.data.ProactiveHabit
import com.example.atomic.util.InstalledAppInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HabitManagerScreen(
    viewModel: HabitManagerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Scaffold(
        modifier = modifier,
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Agregar Hábito")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Gestión de Hábitos Proactivos",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Configura recordatorios para tus hábitos digitales o físicos.",
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (uiState.habits.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tienes hábitos configurados. ¡Agrega uno!")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(uiState.habits, key = { it.id }) { habit ->
                        HabitItem(habit = habit, onDelete = { viewModel.deleteHabit(habit) })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddHabitDialog(
            installedApps = uiState.installedApps,
            onDismiss = { showAddDialog = false },
            onConfirm = { name, pkg, hour, minute, isPhysical ->
                viewModel.addHabit(name, pkg, hour, minute, isPhysical)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun HabitItem(habit: ProactiveHabit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = habit.name, fontWeight = FontWeight.Bold)
                val typeText = if (habit.isPhysical) "Físico" else "Digital (${habit.targetPackage?.split(".")?.last()})"
                val timeStr = String.format("%02d:%02d", habit.triggerHour, habit.triggerMinute)
                Text(text = "$typeText - Recordatorio: $timeStr", style = MaterialTheme.typography.bodySmall)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddHabitDialog(
    installedApps: List<InstalledAppInfo>,
    onDismiss: () -> Unit,
    onConfirm: (String, String?, Int, Int, Boolean) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var isPhysical by remember { mutableStateOf(true) }
    var selectedApp by remember { mutableStateOf<InstalledAppInfo?>(null) }
    var triggerHour by remember { mutableIntStateOf(20) }
    var triggerMinute by remember { mutableIntStateOf(0) }
    var isAppMenuExpanded by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Hábito") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("¿Qué quieres hacer?") },
                    modifier = Modifier.fillMaxWidth()
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isPhysical, onCheckedChange = { isPhysical = it })
                    Text("Es un hábito físico")
                }

                if (!isPhysical) {
                    ExposedDropdownMenuBox(
                        expanded = isAppMenuExpanded,
                        onExpandedChange = { isAppMenuExpanded = !isAppMenuExpanded }
                    ) {
                        OutlinedTextField(
                            value = selectedApp?.appName ?: "Seleccionar App",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("App a monitorear") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = isAppMenuExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = isAppMenuExpanded,
                            onDismissRequest = { isAppMenuExpanded = false }
                        ) {
                            installedApps.forEach { app ->
                                DropdownMenuItem(
                                    text = { Text(app.appName) },
                                    onClick = {
                                        selectedApp = app
                                        isAppMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                val timeStr = String.format("%02d:%02d", triggerHour, triggerMinute)
                OutlinedButton(onClick = { showTimePicker = true }) {
                    Text("Hora del recordatorio: $timeStr")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(name, selectedApp?.packageName, triggerHour, triggerMinute, isPhysical) },
                enabled = name.isNotBlank() && (isPhysical || selectedApp != null)
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )

    if (showTimePicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = triggerHour,
            initialMinute = triggerMinute,
            is24Hour = false
        )
        TimePickerDialog(
            title = "Seleccionar hora",
            onCancel = { showTimePicker = false },
            onConfirm = {
                triggerHour = timePickerState.hour
                triggerMinute = timePickerState.minute
                showTimePicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}
