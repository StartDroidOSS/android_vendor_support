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

package com.flamingo.support.compose.ui.layout

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.flamingo.support.compose.R
import com.flamingo.support.compose.ui.preferences.Preference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollapsingToolbarLayout(
    title: String,
    onBackButtonPressed: () -> Unit,
    modifier: Modifier = Modifier,
    content: LazyListScope.() -> Unit,
) {
    val sideNavigationBarPadding =
        with(LocalDensity.current) {
            WindowInsets.navigationBars.getLeft(
                this,
                LocalLayoutDirection.current
            ).toDp()
        }
    val barState = rememberTopAppBarState()
    val containerColor = MaterialTheme.colorScheme.surface
    val scrolledContainerColor = MaterialTheme.colorScheme.primary
    val topAppBarColors = TopAppBarDefaults.largeTopAppBarColors(
        containerColor = containerColor,
        scrolledContainerColor = scrolledContainerColor
    )
    val statusBarColor by derivedStateOf {
        lerp(
            containerColor,
            scrolledContainerColor,
            FastOutLinearInEasing.transform(barState.collapsedFraction)
        )
    }
    val statusBarPadding =
        with(LocalDensity.current) { WindowInsets.statusBars.getTop(this).toFloat() }
    Column(
        modifier = modifier
            .then(
                if (sideNavigationBarPadding.value != 0f) {
                    Modifier.navigationBarsPadding()
                } else {
                    Modifier
                }
            )
            .drawBehind {
                drawRect(color = statusBarColor, size = Size(size.width, statusBarPadding))
            }
    ) {
        val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(state = barState)
        LargeTopAppBar(
            modifier = Modifier.statusBarsPadding(),
            title = {
                Text(text = title, modifier = Modifier.padding(start = 6.dp))
            },
            navigationIcon = {
                IconButton(
                    modifier = Modifier.padding(start = 2.dp),
                    onClick = onBackButtonPressed
                ) {
                    Icon(
                        imageVector = Icons.Filled.ArrowBack,
                        contentDescription = stringResource(id = R.string.back_button_content_desc)
                    )
                }
            },
            scrollBehavior = scrollBehavior,
            colors = topAppBarColors
        )
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            val navigationBarPadding =
                with(LocalDensity.current) { WindowInsets.navigationBars.getBottom(this).toDp() }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection),
                contentPadding = PaddingValues(bottom = navigationBarPadding),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content
            )
        }
    }
}

@Preview
@Composable
fun PreviewCollapsingToolbarLayout() {
    CollapsingToolbarLayout(
        title = "Collapsing toolbar layout",
        onBackButtonPressed = {}
    ) {
        items(50) { index ->
            Preference(
                "Preference $index",
                summary = if (index % 2 == 0) "Preference summary" else null
            )
        }
    }
}