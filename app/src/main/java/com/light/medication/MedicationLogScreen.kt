package com.light.medication

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.light.medication.data.MedicationLog
import com.light.medication.util.AutoClearPreferences
import com.light.medication.util.TimeUtils
import com.light.medication.viewmodel.ReminderViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MedicationLogScreen(
    onBack: () -> Unit,
    viewModel: ReminderViewModel = viewModel()
) {
    val logs by viewModel.allLogs.collectAsState()
    val autoClearInterval by viewModel.autoClearInterval.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.autoClearLogs()
    }

    var showClearConfirm by remember { mutableStateOf(false) }
    var showAutoClearDialog by remember { mutableStateOf(false) }
    var logToDelete by remember { mutableStateOf<MedicationLog?>(null) }

    val configuration = LocalConfiguration.current
    val isBoxyScreen = configuration.screenWidthDp.toFloat() / configuration.screenHeightDp.toFloat() > 0.75f

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.log_title), style = if (isBoxyScreen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button))
                    }
                },
                actions = {
                    IconButton(onClick = { showAutoClearDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.auto_clear_title))
                    }
                    if (logs.isNotEmpty()) {
                        IconButton(onClick = { showClearConfirm = true }) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.clear_log_button))
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Card(
                onClick = { showAutoClearDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = if (isBoxyScreen) 8.dp else 16.dp,
                        vertical = if (isBoxyScreen) 4.dp else 8.dp
                    ),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(if (isBoxyScreen) 8.dp else 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.auto_clear_title),
                            style = if (isBoxyScreen) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = stringResource(R.string.auto_clear_label, getIntervalLabel(autoClearInterval)),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (logs.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.no_logs), style = MaterialTheme.typography.bodyLarge)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(if (isBoxyScreen) 8.dp else 16.dp),
                    verticalArrangement = Arrangement.spacedBy(if (isBoxyScreen) 4.dp else 8.dp)
                ) {
                    items(logs) { log ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                        ) {
                            Column(modifier = Modifier.padding(if (isBoxyScreen) 8.dp else 12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = log.medicationName,
                                            style = if (isBoxyScreen) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = TimeUtils.formatFullDateTime(log.timestamp),
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (log.action == "Taken") stringResource(R.string.log_action_taken) else stringResource(R.string.log_action_skipped),
                                            style = if (isBoxyScreen) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelLarge,
                                            color = if (log.action == "Taken") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                                        )
                                        IconButton(
                                            onClick = { logToDelete = log },
                                            modifier = if (isBoxyScreen) Modifier.size(32.dp) else Modifier
                                        ) {
                                            Icon(
                                                Icons.Default.Delete,
                                                contentDescription = stringResource(R.string.delete_button),
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = if (isBoxyScreen) Modifier.size(20.dp) else Modifier
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAutoClearDialog) {
        var selectedInterval by remember { mutableStateOf(autoClearInterval) }
        AlertDialog(
            onDismissRequest = { showAutoClearDialog = false },
            title = { Text(stringResource(R.string.auto_clear_dialog_title)) },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .selectableGroup(),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = stringResource(R.string.auto_clear_dialog_message),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val intervals = listOf(
                        AutoClearPreferences.INTERVAL_OFF to stringResource(R.string.auto_clear_off),
                        AutoClearPreferences.INTERVAL_WEEKLY to stringResource(R.string.auto_clear_weekly),
                        AutoClearPreferences.INTERVAL_MONTHLY to stringResource(R.string.auto_clear_monthly),
                        AutoClearPreferences.INTERVAL_THREE_MONTHS to stringResource(R.string.auto_clear_three_months),
                        AutoClearPreferences.INTERVAL_FIVE_MONTHS to stringResource(R.string.auto_clear_five_months),
                        AutoClearPreferences.INTERVAL_YEARLY to stringResource(R.string.auto_clear_yearly)
                    )
                    intervals.forEach { (key, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .selectable(
                                    selected = (selectedInterval == key),
                                    onClick = { selectedInterval = key },
                                    role = Role.RadioButton
                                )
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedInterval == key),
                                onClick = null
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = label, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.setAutoClearInterval(selectedInterval)
                    showAutoClearDialog = false
                }) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAutoClearDialog = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }

    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(stringResource(R.string.clear_log_confirm_title)) },
            text = { Text(stringResource(R.string.clear_log_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearLogs()
                    showClearConfirm = false
                }) {
                    Text(stringResource(R.string.delete_button), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }

    logToDelete?.let { log ->
        AlertDialog(
            onDismissRequest = { logToDelete = null },
            title = { Text(stringResource(R.string.delete_button)) },
            text = { Text(stringResource(R.string.clear_log_confirm_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteLog(log)
                    logToDelete = null
                }) {
                    Text(stringResource(R.string.delete_button), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { logToDelete = null }) {
                    Text(stringResource(R.string.cancel_button))
                }
            }
        )
    }
}

@Composable
private fun getIntervalLabel(interval: String): String {
    return when (interval) {
        AutoClearPreferences.INTERVAL_WEEKLY -> stringResource(R.string.auto_clear_weekly)
        AutoClearPreferences.INTERVAL_MONTHLY -> stringResource(R.string.auto_clear_monthly)
        AutoClearPreferences.INTERVAL_THREE_MONTHS -> stringResource(R.string.auto_clear_three_months)
        AutoClearPreferences.INTERVAL_FIVE_MONTHS -> stringResource(R.string.auto_clear_five_months)
        AutoClearPreferences.INTERVAL_YEARLY -> stringResource(R.string.auto_clear_yearly)
        else -> stringResource(R.string.auto_clear_off)
    }
}
