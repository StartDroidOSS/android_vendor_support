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

package com.flamingo.support.compose.runtime

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.os.UserHandle

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

@Composable
fun <T> rememberBoundService(
    context: Context = LocalContext.current,
    intent: Intent,
    obtainService: (IBinder) -> T,
    flags: Int = Context.BIND_AUTO_CREATE,
    userHandle: UserHandle? = null
): T? {
    val obtainServiceCallback by rememberUpdatedState(newValue = obtainService)
    var service by remember { mutableStateOf<T?>(null) }
    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(componentName: ComponentName, binder: IBinder) {
                service = obtainServiceCallback(binder)
            }

            override fun onServiceDisconnected(componentName: ComponentName?) {
                service = null
            }
        }
    }
    var bound by remember { mutableStateOf(false) }
    DisposableEffect(context) {
        bound = if (userHandle == null) {
            context.bindService(
                intent,
                serviceConnection,
                flags
            )
        } else {
            context.bindServiceAsUser(
                intent,
                serviceConnection,
                flags,
                userHandle
            )
        }
        onDispose {
            if (bound) {
                context.unbindService(serviceConnection)
                bound = false
                service = null
            }
        }
    }
    return service
}