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
package com.example.androiddevchallenge

import android.os.Bundle
import android.os.CountDownTimer
import android.text.format.DateUtils
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.TabRowDefaults.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.*
import com.example.androiddevchallenge.ui.theme.MyTheme

private const val TAG = "AndroidDevChallenge"

class MainActivity : ComponentActivity() {

    private val viewModel: TimerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG, "onCreate")
        super.onCreate(savedInstanceState)
        setContent {
            MyTheme {
                MyApp(viewModel = viewModel)
                Log.d(TAG, "created app")
            }
        }
    }
}

class TimerViewModel : ViewModel() {

    private val _secondsToGo: MutableLiveData<Int> = MutableLiveData(60)
    val secondsToGo: LiveData<Int> = _secondsToGo

    val secondsToGoString = secondsToGo.map { seconds ->
        DateUtils.formatElapsedTime(seconds.toLong())
    }

    private val _running: MutableLiveData<Boolean> = MutableLiveData(false)
    val running: LiveData<Boolean> = _running

    private val _secondsStart: MutableLiveData<Int> = MutableLiveData(60)
    val secondsStart: LiveData<Int> = _secondsStart

    private val timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            onIncreaseSecond(-1)
        }

        override fun onFinish() {
            Log.d(TAG, "TIMER FINISHED")
        }
    }

    init {
        Log.d(TAG, "in TimerViewModel init()")
    }

    fun logSecondsToGo() {
        Log.d(TAG, "seconds to go: ${secondsToGo.value}")
    }

    fun onIncreaseSecond(seconds: Int = 1) {
        _secondsToGo.value = (_secondsToGo.value)?.plus(seconds)
        _secondsStart.value?.let {
            if (_secondsToGo.value!! > it) {
                _secondsStart.value = _secondsToGo.value
            }
        }
        logSecondsToGo()
        if (_secondsToGo.value!! <= 0) {
            onReset()
            onPause()
        }
    }

    fun onReset() {
        _secondsToGo.value = 0
        _secondsStart.value = 1
        _running.value = false
    }

    fun onStart() {
        timer.cancel()
        timer.start()
        _running.value = true
    }

    fun onPause() {
        timer.cancel()
        _running.value = false
    }
}

@Composable
fun MyApp(viewModel: TimerViewModel) {
    Log.d(TAG, "in MyApp")

    Surface(color = MaterialTheme.colors.background) {
        MyTimerContent(viewModel)
    }
}

@Composable
fun MyTimerContent(viewModel: TimerViewModel, modifier: Modifier = Modifier) {
    Log.d(TAG, "in MyTimerContent")

    val secondsToGo: Int by viewModel.secondsToGo.observeAsState(initial = 60)
    val secondsToGoString: String by viewModel.secondsToGoString.observeAsState(initial = "0:00")
    val secondsStart: Int by viewModel.secondsStart.observeAsState(initial = 60)
    val running: Boolean by viewModel.running.observeAsState(false)
    val startButtonDisabled = secondsToGo == 0 || running

    Column(modifier = Modifier.fillMaxHeight()) {
        Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()) {
            Text(
                secondsToGoString,
                fontSize = 64.sp,
                textAlign = TextAlign.Center,
                color = if (secondsToGo < 10 && running) Color.Red else MaterialTheme.colors.primary,
                modifier = Modifier.padding(16.dp)
            )
        }
        Row(modifier = Modifier.fillMaxWidth()) {
            CircularProgressIndicator(progress = secondsToGo.toFloat() / secondsStart, modifier = Modifier.padding(8.dp))
            StartButton(
                enabled = !startButtonDisabled,
                start = { viewModel.onStart() }
            )
            PauseButton(
                running = running,
                pause = { viewModel.onPause() }
            )
        }
        Divider()
        Row(modifier = Modifier.fillMaxWidth()) {
            ResetButton(
                enabled = !running,
                reset = { viewModel.onReset() }
            )
            IncreaseButton(
                enabled = !running,
                increaseSeconds = { viewModel.onIncreaseSecond() }
            )
            IncreaseMinuteButton(
                enabled = !running,
                increaseSeconds = { viewModel.onIncreaseSecond(60) }
            )
            IncreaseBreakButton(
                enabled = !running,
                increaseSeconds = { viewModel.onIncreaseSecond(60 * 5) }
            )
            IncreasePomodoroButton(
                enabled = !running,
                increaseSeconds = { viewModel.onIncreaseSecond(60 * 25) }
            )
        }
    }
}

@Composable
fun IncreaseButton(enabled: Boolean = true, increaseSeconds: () -> Unit) {
    Button(onClick = { increaseSeconds() }, enabled = enabled, modifier = Modifier.padding(8.dp)) {
        Text("+1")
    }
}

@Composable
fun IncreaseMinuteButton(enabled: Boolean = true, increaseSeconds: (Int) -> Unit) {
    Button(onClick = { increaseSeconds(60) }, enabled = enabled, modifier = Modifier.padding(8.dp)) {
        Text("+60")
    }
}

@Composable
fun IncreaseBreakButton(enabled: Boolean = true, increaseSeconds: (Int) -> Unit) {
    Button(onClick = { increaseSeconds(60 * 5) }, enabled = enabled, modifier = Modifier.padding(8.dp)) {
        Text("+5m")
    }
}

@Composable
fun IncreasePomodoroButton(enabled: Boolean = true, increaseSeconds: (Int) -> Unit) {
    Button(onClick = { increaseSeconds(60 * 25) }, enabled = enabled, modifier = Modifier.padding(8.dp)) {
        Text("+25m")
    }
}

@Composable
fun ResetButton(enabled: Boolean = true, reset: () -> Unit) {
    Button(onClick = { reset() }, enabled = enabled, modifier = Modifier.padding(8.dp)) {
        Text("0")
    }
}

@Composable
fun StartButton(enabled: Boolean = true, start: () -> Unit) {
    Button(onClick = { start() }, enabled = enabled, modifier = Modifier.padding(8.dp)) {
        Text("start")
    }
}

@Composable
fun PauseButton(running: Boolean = false, pause: () -> Unit) {
    Button(onClick = { pause() }, enabled = running, modifier = Modifier.padding(8.dp)) {
        Text("pause")
    }
}

@Preview("Light Theme", widthDp = 360, heightDp = 640)
@Composable
fun LightPreview() {
    MyTheme {
        MyApp(viewModel = TimerViewModel())
    }
}

@Preview("Dark Theme", widthDp = 360, heightDp = 640)
@Composable
fun DarkPreview() {
    MyTheme(darkTheme = true) {
        MyApp(viewModel = TimerViewModel())
    }
}
