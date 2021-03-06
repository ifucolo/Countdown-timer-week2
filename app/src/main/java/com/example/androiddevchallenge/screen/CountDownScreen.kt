/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge.screen

import android.os.CountDownTimer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ProgressIndicatorDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androiddevchallenge.R
import com.example.androiddevchallenge.ui.theme.shapes

@ExperimentalAnimationApi
@Composable
fun CountDownScreen() {
    val countMinutes = remember { mutableStateOf(0) }
    val countSeconds = remember { mutableStateOf(0) }
    val showDuringProgress = remember { mutableStateOf(false) }
    val maxMilliSeconds = remember { mutableStateOf(0L) }
    val steps = remember { mutableStateOf(0L) }

    val progress = remember { mutableStateOf(0.0001f) }

    val animatedProgress = animateFloatAsState(
        targetValue = progress.value,
        animationSpec = ProgressIndicatorDefaults.ProgressAnimationSpec
    ).value

    val countDownTimer = object : CountDownTimer(maxMilliSeconds.value, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            val increment = 1.0 / steps.value
            progress.value = (increment + progress.value).toFloat()
            println("progress ${progress.value}")
        }

        override fun onFinish() {
            progress.value = 1f
            resetTimer(
                countMinutes = countMinutes,
                countSeconds = countSeconds,
                progress = progress,
                showDuringProgress = showDuringProgress
            )

            cancel()
        }
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Spacer(modifier = Modifier.padding(30.dp))

        CircularProgressIndicator(
            modifier = Modifier
                .width(200.dp)
                .height(200.dp),
            progress = animatedProgress
        )
        AnimatedVisibility(visible = !showDuringProgress.value) {
            Row(
                horizontalArrangement = Arrangement.Center
            ) {
                TimeSelectorComponent(
                    timeUnit = TimeUnit.Minutes,
                    count = countMinutes.value,
                    onCountChange = {
                        countMinutes.value = it
                        maxMilliSeconds.value = getProgressInSeconds(mins = countMinutes.value, secs = countSeconds.value).toLong()
                        steps.value = maxMilliSeconds.value / 1000
                    },
                )
                Spacer(modifier = Modifier.padding(16.dp))
                TimeSelectorComponent(
                    timeUnit = TimeUnit.Seconds,
                    count = countSeconds.value,
                    onCountChange = {
                        countSeconds.value = it
                        maxMilliSeconds.value = getProgressInSeconds(mins = countMinutes.value, secs = countSeconds.value).toLong()
                        steps.value = maxMilliSeconds.value / 1000
                    },
                )
            }
        }

        Spacer(modifier = Modifier.padding(16.dp))

        AnimatedVisibility(visible = !showDuringProgress.value) {
            Button(
                enabled = countMinutes.value > 0 || countSeconds.value > 0,
                onClick = {
                    showDuringProgress.value = true
                    countDownTimer.start()
                },
            ) {
                Text(text = "START")
            }
        }
    }
}

fun resetTimer(
    countMinutes: MutableState<Int>,
    countSeconds: MutableState<Int>,
    progress: MutableState<Float>,
    showDuringProgress: MutableState<Boolean>
) {
    countMinutes.value = 0
    countSeconds.value = 0

    progress.value = 0.0001f
    showDuringProgress.value = false
}

@ExperimentalAnimationApi
@Composable
fun TimeSelectorComponent(
    timeUnit: TimeUnit,
    count: Int,
    onCountChange: (Int) -> Unit
) {
    val canClickPlus = remember { mutableStateOf(true) }
    val canClickMinus = remember { mutableStateOf(false) }

    val maxPlus = when (timeUnit) {
        TimeUnit.Minutes -> MAX_MINUTES
        TimeUnit.Seconds -> MAX_SECONDS
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.height(300.dp)
    ) {
        val type = when (timeUnit) {
            TimeUnit.Minutes -> "Min"
            TimeUnit.Seconds -> "Sec"
        }

        Text(
            text = type,
            textAlign = TextAlign.Center,
            style = TextStyle(
                color = Color.White,
                fontWeight = FontWeight.Light,
                fontSize = 12.sp
            )
        )
        Text(
            text = makeTimeText(count),
            textAlign = TextAlign.Center,
            style = TextStyle(
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        )
        Spacer(modifier = Modifier.padding(4.dp))
        AnimatedVisibility(visible = canClickPlus.value) {
            FloatingActionButton(
                onClick = {
                    handleTime(
                        operations = Operations.Plus,
                        count = count,
                        onCountChange = {
                            onCountChange.invoke(it)
                        },
                        canClickMinus = canClickMinus,
                        canClickPlus = canClickPlus,
                        maxPlus = maxPlus
                    )
                },
                modifier = Modifier
                    .height(30.dp)
                    .width(30.dp)
                    .clip(shape = shapes.medium),
                backgroundColor = MaterialTheme.colors.secondary
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_baseline_exposure_plus_1_24),
                    contentDescription = "plus"
                )
            }
        }
        Spacer(modifier = Modifier.padding(8.dp))
        AnimatedVisibility(visible = canClickMinus.value) {
            FloatingActionButton(
                onClick = {
                    handleTime(
                        operations = Operations.Minus,
                        count = count,
                        onCountChange = {
                            onCountChange.invoke(it)
                        },
                        canClickMinus = canClickMinus,
                        canClickPlus = canClickPlus,
                        maxPlus = maxPlus
                    )
                },
                modifier = Modifier
                    .height(30.dp)
                    .width(30.dp)
                    .clip(shape = shapes.medium),
                backgroundColor = MaterialTheme.colors.secondary
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_baseline_exposure_neg_1_24),
                    contentDescription = "minus"
                )
            }
        }
    }
}

fun getProgressInSeconds(mins: Int, secs: Int): Int =
    ((mins * 60) + secs) * 1000

fun handleTime(
    operations: Operations,
    count: Int,
    onCountChange: (Int) -> Unit,
    canClickMinus: MutableState<Boolean>,
    canClickPlus: MutableState<Boolean>,
    maxPlus: Int
) {
    when (operations) {
        Operations.Minus -> {
            canClickMinus.value = count - 1 > 0
            if (count > 0) {
                onCountChange.invoke(count - 1)
            }
            canClickPlus.value = count - 1 < maxPlus
        }
        Operations.Plus -> {
            canClickPlus.value = count + 1 < maxPlus
            if (count < maxPlus) {
                onCountChange.invoke(count + 1)
            }
            canClickMinus.value = count + 1 > 0
        }
    }
}

const val MAX_MINUTES = 99
const val MAX_SECONDS = 60

fun makeTimeText(value: Int): String =
    if (value < 10) "0$value" else value.toString()

sealed class Operations {
    object Plus : Operations()
    object Minus : Operations()
}

sealed class TimeUnit {
    object Seconds : TimeUnit()
    object Minutes : TimeUnit()
}
