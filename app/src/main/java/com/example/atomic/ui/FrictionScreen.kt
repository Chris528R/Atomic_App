package com.example.atomic.ui

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.atomic.domain.UnlockReason
import com.example.atomic.data.PositiveHabit

@Composable
fun FrictionScreen(
    appName: String,
    openCount: Int,
    currentDebt: Int,
    suggestedHabit: PositiveHabit?,
    onUnlock: (reason: UnlockReason, isForced: Boolean) -> Unit,
    onRedirect: (String) -> Unit,
    onCancel: () -> Unit,
) {
    var selectedReason by remember { mutableStateOf<UnlockReason?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.9f)),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                if (openCount >= 6) {
                var justification by remember { mutableStateOf("") }
                val wordCount = justification.trim().split(Regex("\\s+")).filter { it.isNotEmpty() }.size
                val isButtonEnabled = wordCount >= 5

                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (currentDebt > 0) {
                        Text(
                            text = "⚠️ Tienes una deuda de $currentDebt min que se cobrará en este acceso.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    Text(
                        text = "Apertura #$openCount hoy",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Has excedido el límite razonable para $appName. Para continuar, escribe una justificación de al menos 5 palabras.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = justification,
                        onValueChange = { justification = it },
                        label = { Text("¿Por qué es indispensable?") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                    )

                    Text(
                        text = "$wordCount / 5 palabras",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isButtonEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.End).padding(top = 4.dp),
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TextButton(onClick = onCancel) {
                            Text("Mejor no")
                        }
                        Button(
                            onClick = { onUnlock(UnlockReason.OTHER, true) },
                            enabled = isButtonEnabled,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        ) {
                            Text("Forzar (+15 min de multa mañana)")
                        }
                    }
                }
            } else {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    if (currentDebt > 0) {
                        Text(
                            text = "⚠️ Tienes una deuda de $currentDebt min que se cobrará en este acceso.",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                    }
                    Text(
                        text = "¿Realmente necesitas abrir $appName?",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Column(modifier = Modifier.selectableGroup()) {
                        UnlockReason.entries.forEach { reasonEnum ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .selectable(
                                        selected = reasonEnum == selectedReason,
                                        onClick = { selectedReason = reasonEnum },
                                        role = Role.RadioButton,
                                    )
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = reasonEnum == selectedReason,
                                    onClick = null,
                                )
                                Column(modifier = Modifier.padding(start = 16.dp)) {
                                    Text(
                                        text = reasonEnum.title,
                                        style = MaterialTheme.typography.bodyLarge,
                                    )
                                    Text(
                                        text = "${reasonEnum.allowedMinutes} minutos",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.outline
                                    )
                                }
                            }
                        }
                    }

                    if (currentDebt > 0 && selectedReason != null) {
                        Spacer(modifier = Modifier.height(16.dp))
                        val requested = selectedReason!!.allowedMinutes
                        val penalty = kotlin.math.min(currentDebt, requested - 1)
                        val finalTime = requested - penalty

                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    "⚖️ Ajuste por Deuda",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.labelLarge
                                )
                                Divider(
                                    modifier = Modifier.padding(vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Tiempo solicitado:", style = MaterialTheme.typography.bodySmall)
                                    Text("$requested min", style = MaterialTheme.typography.bodySmall)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Cobro de deuda:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                    Text("-$penalty min", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Tiempo real:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    Text("$finalTime min", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        TextButton(onClick = onCancel) {
                            Text("Cancelar")
                        }
                        Button(
                            onClick = { selectedReason?.let { onUnlock(it, false) } },
                            enabled = selectedReason != null,
                        ) {
                            val timeText = selectedReason?.allowedMinutes?.let { " ($it min)" } ?: ""
                            Text("Abrir$timeText")
                        }
                    }
                }
            }
            }
            
            if (suggestedHabit != null) {
                Spacer(modifier = Modifier.height(32.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onRedirect(suggestedHabit.targetPackageName) },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "💡", 
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "Alternativa sugerida",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "¿Qué tal si mejor avanzas en ${suggestedHabit.name}?",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }
}
