/*
 * Copyright (C) 2022 FlamingoOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.flamingo.support.compose.ui.preferences

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties

@Composable
fun <T> MultiSelectListPreference(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    clickable: Boolean = true,
    onClick: (() -> Unit)? = null,
    entries: List<Entry<T>>,
    values: List<T> = emptyList(),
    onValuesUpdated: (List<T>) -> Unit,
    onDismissListener: () -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        AlertDialog(
            onDismissRequest = {
                showDialog = false
                onDismissListener()
            },
            confirmButton = {},
            properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
            shape = RoundedCornerShape(DefaultDialogCornerRadius),
            title = {
                Text(text = title)
            },
            text = {
                val state = rememberLazyListState()
                LaunchedEffect(entries) {
                    val firstSelected = values.firstOrNull() ?: return@LaunchedEffect
                    state.scrollToItem(entries.indexOfFirst { it.value == firstSelected }
                        .takeIf { it != -1 } ?: 0)
                }
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(entries) { entry ->
                        val updateCallback by rememberUpdatedState(newValue = { checked: Boolean ->
                            onValuesUpdated(
                                values.toMutableList().apply {
                                    if (checked) {
                                        add(entry.value)
                                    } else {
                                        remove(entry.value)
                                    }
                                }.toList()
                            )
                        })
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(x = (-12).dp)
                                .clickable(
                                    enabled = true,
                                    onClick = {
                                        updateCallback(!values.contains(entry.value))
                                    },
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = values.contains(entry.value),
                                onCheckedChange = { checked ->
                                    updateCallback(checked)
                                },
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                modifier = Modifier.weight(1f),
                                text = entry.name,
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            },
        )
    }
    Preference(
        modifier = modifier,
        title = title,
        summary = summary,
        enabled = clickable,
        onClick = {
            if (onClick == null) {
                showDialog = true
            } else {
                onClick()
            }
        },
    )
}

@Preview
@Composable
fun PreviewMultiSelectListPreference() {
    val selectedList = remember { mutableStateListOf(0, 1) }
    MultiSelectListPreference(title = "Multi select list preference",
        summary = "This is a multi select list preference",
        entries = listOf(
            Entry("Entry 1", 0),
            Entry("Entry 2", 1),
            Entry("Entry 3", 2)
        ),
        values = selectedList,
        onValuesUpdated = {},
        onDismissListener = {}
    )
}