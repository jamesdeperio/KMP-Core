package io.github.jamesdeperio.devtool

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.github.jamesdeperio.devtool.icon.BackIcon
import io.github.jamesdeperio.devtool.icon.CloseIcon

@Composable
fun LogDetailItem(
    title: String,
    content: String,
    clipboardManager: ClipboardManager,
) {
    val prettyContent = prettifyJson(content)
    val scrollState = rememberScrollState() // for horizontal scroll

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                "$title:",
                fontWeight = FontWeight.Medium,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(modifier = Modifier.weight(1f))
            TextButton(
                onClick = { clipboardManager.setText(AnnotatedString(prettyContent)) },
                contentPadding = PaddingValues(0.dp),
            ) {
                Text(
                    "Copy",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }

        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(6.dp))
                    .horizontalScroll(scrollState) // allow horizontal scroll
                    .padding(10.dp),
        ) {
            Text(
                text = prettyContent,
                style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(onClose: () -> Unit) {
    var selectedLog by remember { mutableStateOf<ApiLogData?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val clipboardManager: ClipboardManager = LocalClipboardManager.current

    val filteredLogs =
        NetworkDebug.logs.filter {
            it.path.contains(searchQuery, ignoreCase = true) ||
                it.statusCode.toString().contains(searchQuery)
        }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        if (selectedLog == null) {
            // --- Master List ---
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Text(
                            "API Inspector",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                    navigationIcon = {
                        IconButton(onClick = onClose) {
                            Icon(
                                imageVector = CloseIcon,
                                contentDescription = "Close",
                                tint = Color.White,
                            )
                        }
                    },
                )

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                    placeholder = { Text("Search API path or status") },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodySmall,
                )

                LazyColumn(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                ) {
                    items(filteredLogs) { log ->
                        val isError = log.statusCode != 200
                        Card(
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable(
                                        indication = null, // remove ripple
                                        interactionSource = remember { MutableInteractionSource() },
                                    ) {
                                        selectedLog = log
                                    },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                            shape = RoundedCornerShape(8.dp),
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        log.method.uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.primary,
                                    )

                                    Spacer(Modifier.width(8.dp))

                                    Text(
                                        log.statusCode.toString(),
                                        fontWeight = FontWeight.Medium,
                                        style = MaterialTheme.typography.bodySmall,
                                        color =
                                            if (isError) {
                                                MaterialTheme.colorScheme.error
                                            } else {
                                                MaterialTheme.colorScheme.onSurfaceVariant
                                            },
                                    )
                                }

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    log.path,
                                    fontWeight = FontWeight.Medium,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )

                                Spacer(Modifier.height(4.dp))

                                Text(
                                    formatTimestamp(log.timestamp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }

                    if (filteredLogs.isEmpty()) {
                        item {
                            Text(
                                text = "No logs found",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(12.dp),
                            )
                        }
                    }

                    // Extra spacer at bottom
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        } else {
            // --- Detail Screen ---
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Text(
                            "API Request Detail",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black),
                    navigationIcon = {
                        IconButton(onClick = {
                            selectedLog = null
                        }) {
                            Icon(
                                imageVector = BackIcon,
                                contentDescription = "back",
                                tint = Color.White,
                            )
                        }
                    },
                )

                Column(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    val log = selectedLog!!
                    val isError = log.statusCode != 200

                    LogDetailItem("Path", log.path, clipboardManager)
                    Spacer(modifier = Modifier.height(12.dp))
                    LogDetailItem("Method", log.method.uppercase(), clipboardManager)
                    Spacer(modifier = Modifier.height(12.dp))
                    LogDetailItem("Status Code", log.statusCode.toString(), clipboardManager)
                    Spacer(modifier = Modifier.height(12.dp))
                    LogDetailItem("Timestamp", formatTimestamp(log.timestamp), clipboardManager)
                    Spacer(modifier = Modifier.height(12.dp))
                    LogDetailItem("Headers", log.headers, clipboardManager)
                    Spacer(modifier = Modifier.height(12.dp))
                    LogDetailItem("Request", log.request, clipboardManager)
                    Spacer(modifier = Modifier.height(12.dp))
                    LogDetailItem("Response", log.response, clipboardManager)

                    // Extra spacer to prevent navigation bar overlay
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}
