/*
 * Copyright (C) 2022 FlamingoOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.ExperimentalUnitApi
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalUnitApi::class)
@Composable
fun Preference(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    enabled: Boolean = true,
    onClick: (Offset) -> Unit = {},
    onLongClick: (Offset) -> Unit = {},
    startWidget: @Composable (BoxScope.() -> Unit)? = null,
    endWidget: @Composable (BoxScope.() -> Unit)? = null,
    bottomWidget: @Composable (BoxScope.() -> Unit)? = null,
    interactionSource: MutableInteractionSource? = null,
) {
    val contentAlpha by animateFloatAsState(targetValue = if (enabled) 1f else 0.5f)
    val hasSummary = remember(summary) { summary?.isNotBlank() == true }
    val pointerInteractionSource = interactionSource ?: remember { MutableInteractionSource() }
    val clickCallback by rememberUpdatedState(newValue = { offset: Offset -> onClick(offset) })
    val longClickCallback by rememberUpdatedState(newValue = { offset: Offset -> onLongClick(offset) })
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .alpha(contentAlpha)
            .then(
                if (enabled) {
                    Modifier
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = { offset ->
                                    val pressInteraction = PressInteraction.Press(offset)
                                    pointerInteractionSource.emit(pressInteraction)
                                    if (tryAwaitRelease()) {
                                        pointerInteractionSource.emit(
                                            PressInteraction.Release(
                                                pressInteraction
                                            )
                                        )
                                    } else {
                                        pointerInteractionSource.emit(
                                            PressInteraction.Cancel(
                                                pressInteraction
                                            )
                                        )
                                    }
                                },
                                onTap = clickCallback,
                                onLongPress = longClickCallback
                            )
                        }
                        .indication(pointerInteractionSource, rememberRipple())
                } else {
                    Modifier
                }
            )
            .defaultMinSize(minHeight = PreferenceMinHeight),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = PreferenceContentHorizontalPadding,
                    vertical = PreferenceContentVerticalPadding
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (startWidget != null) {
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.CenterStart,
                    content = startWidget
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.SpaceEvenly,
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = title,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = TextUnit(20f, TextUnitType.Sp),
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                )
                if (hasSummary) {
                    Text(
                        text = summary!!,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Light,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (bottomWidget != null) {
                    Box(
                        contentAlignment = Alignment.Center,
                        content = bottomWidget
                    )
                }
            }
            if (endWidget != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    content = endWidget
                )
            }
        }
    }
}