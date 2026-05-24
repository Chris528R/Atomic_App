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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.atomic.data.ScheduleRule
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleSettingsScreen(
    rules: List<ScheduleRule>,
    onToggleRule: (ScheduleRule, Boolean) -> Unit,
    onAddNewRule: (ScheduleRule) -> Unit,
    onDeleteRule: (ScheduleRule) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Horarios Libres", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Horario")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            Text(
                text = "Durante estos horarios, Atomic no mostrará pantallas de bloqueo ni consumirá tu deuda de tiempo.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (rules.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No hay horarios configurados.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(rules, key = { it.id }) { rule ->
                        ScheduleRuleCard(
                            rule = rule, 
                            onToggle = onToggleRule,
                            onDelete = { onDeleteRule(rule) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        CreateRuleDialog(
            onDismiss = { showAddDialog = false },
            onSave = { rule ->
                onAddNewRule(rule)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun ScheduleRuleCard(rule: ScheduleRule, onToggle: (ScheduleRule, Boolean) -> Unit, onDelete: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (rule.isEnabled) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rule.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (rule.isEnabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDays(rule.activeDays),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "${formatMinuteToTime(rule.startMinute)} - ${formatMinuteToTime(rule.endMinute)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(
                    checked = rule.isEnabled,
                    onCheckedChange = { isChecked -> onToggle(rule, isChecked) }
                )
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimePickerDialog(
    title: String = "Seleccionar hora",
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    content: @Composable () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(title) },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) { Text("Cancelar") }
        },
        confirmButton = {
            Button(onClick = onConfirm) { Text("Aceptar") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateRuleDialog(
    onDismiss: () -> Unit,
    onSave: (ScheduleRule) -> Unit
) {
    var ruleName by remember { mutableStateOf("") }
    var selectedDays by remember { mutableStateOf(setOf<String>()) }

    var startHour by remember { mutableIntStateOf(20) }
    var startMinute by remember { mutableIntStateOf(0) }
    var endHour by remember { mutableIntStateOf(22) }
    var endMinute by remember { mutableIntStateOf(0) }

    var showStartPicker by remember { mutableStateOf(false) }
    var showEndPicker by remember { mutableStateOf(false) }

    val isFormValid = ruleName.isNotBlank() && selectedDays.isNotEmpty()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Horario Libre") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedTextField(
                    value = ruleName,
                    onValueChange = { ruleName = it },
                    label = { Text("Nombre (ej. Relajación)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text("Días activos", style = MaterialTheme.typography.labelLarge)
                DaySelectorRow(
                    selectedDays = selectedDays,
                    onDayToggled = { day ->
                        selectedDays = if (selectedDays.contains(day)) {
                            selectedDays - day
                        } else {
                            selectedDays + day
                        }
                    }
                )

                HorizontalDivider()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TimeDisplayArea(
                        label = "Inicio",
                        minuteOfDay = (startHour * 60) + startMinute,
                        onClick = { showStartPicker = true }
                    )

                    TimeDisplayArea(
                        label = "Fin",
                        minuteOfDay = (endHour * 60) + endMinute,
                        onClick = { showEndPicker = true }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = isFormValid,
                onClick = {
                    val startMinTotal = (startHour * 60) + startMinute
                    val endMinTotal = (endHour * 60) + endMinute

                    val newRule = ScheduleRule(
                        name = ruleName.trim(),
                        activeDays = selectedDays.joinToString(","),
                        startMinute = startMinTotal,
                        endMinute = endMinTotal
                    )
                    onSave(newRule)
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )

    if (showStartPicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = startHour,
            initialMinute = startMinute,
            is24Hour = false
        )
        TimePickerDialog(
            title = "Hora de inicio",
            onCancel = { showStartPicker = false },
            onConfirm = {
                startHour = timePickerState.hour
                startMinute = timePickerState.minute
                showStartPicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }

    if (showEndPicker) {
        val timePickerState = rememberTimePickerState(
            initialHour = endHour,
            initialMinute = endMinute,
            is24Hour = false
        )
        TimePickerDialog(
            title = "Hora de fin",
            onCancel = { showEndPicker = false },
            onConfirm = {
                endHour = timePickerState.hour
                endMinute = timePickerState.minute
                showEndPicker = false
            }
        ) {
            TimePicker(state = timePickerState)
        }
    }
}

@Composable
fun TimeDisplayArea(label: String, minuteOfDay: Int, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = label, style = MaterialTheme.typography.labelMedium)
        Text(
            text = formatMinuteToTime(minuteOfDay),
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaySelectorRow(selectedDays: Set<String>, onDayToggled: (String) -> Unit) {
    val days = listOf("MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY", "SATURDAY", "SUNDAY")
    val labels = listOf("L", "M", "M", "J", "V", "S", "D")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        days.forEachIndexed { index, day ->
            val isSelected = selectedDays.contains(day)
            FilterChip(
                selected = isSelected,
                onClick = { onDayToggled(day) },
                label = { Text(labels[index]) }
            )
        }
    }
}

fun formatMinuteToTime(minute: Int): String {
    val h = minute / 60
    val m = minute % 60
    val amPm = if (h >= 12) "PM" else "AM"
    val displayH = if (h % 12 == 0) 12 else h % 12
    return String.format("%02d:%02d %s", displayH, m, amPm)
}

fun formatDays(daysRaw: String): String {
    if (daysRaw.isEmpty()) return "Solo hoy"
    val dayMap = mapOf(
        "MONDAY" to "Lun", "TUESDAY" to "Mar", "WEDNESDAY" to "Mié",
        "THURSDAY" to "Jue", "FRIDAY" to "Vie", "SATURDAY" to "Sáb", "SUNDAY" to "Dom"
    )
    val days = daysRaw.split(",")
    if (days.size == 7) return "Todos los días"
    if (days.size == 2 && days.contains("SATURDAY") && days.contains("SUNDAY")) return "Fines de semana"
    if (days.size == 5 && !days.contains("SATURDAY") && !days.contains("SUNDAY")) return "Entre semana"

    return days.mapNotNull { dayMap[it] }.joinToString(", ")
}
