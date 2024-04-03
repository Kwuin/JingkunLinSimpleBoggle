package com.example.boggle

import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.GridLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.util.Objects
import kotlin.math.sqrt
import kotlin.random.Random



//reference : https://chat.openai.com/share/206a2d96-77d1-4a9d-8dc5-bcfe69f653a9
//https://chat.openai.com/share/7d87fad4-73a4-480e-8165-5d62ed5b9448
//https://chat.openai.com/share/e35f8bc6-b92c-4b74-971e-af7a97cd5efa
class MainActivity : AppCompatActivity() ,TabletFragment.NewGameStarter, GridFragment.ScoreNotifier{
    private val clickedPositions = mutableListOf<Pair<Int, Int>>()
    private val clickedLetters = mutableListOf<String>()
    //private lateinit var gridLayout: GridLayout
    private var sensorManager: SensorManager? = null
    private var acceleration = 0f
    private var currentAcceleration = 0f
    private var lastAcceleration = 0f
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        //newGrid()
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        Objects.requireNonNull(sensorManager)!!
            .registerListener(sensorListener, sensorManager!!
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL)

        acceleration = 10f
        currentAcceleration = SensorManager.GRAVITY_EARTH
        lastAcceleration = SensorManager.GRAVITY_EARTH

    }

    private val sensorListener: SensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {

            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]
            lastAcceleration = currentAcceleration


            currentAcceleration = sqrt((x * x + y * y + z * z).toDouble()).toFloat()
            val delta: Float = currentAcceleration - lastAcceleration
            acceleration = acceleration * 0.9f + delta

            if (acceleration > 11) {
                newGame()
            }
        }
        override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
    }





    override fun notifyTablet(score: Int) {
        val fragmentTablet = supportFragmentManager.findFragmentById(R.id.fragmentContainerBottom) as TabletFragment?
        fragmentTablet?.updateScore(score)
    }

    override fun newGame() {
        val fragmentGrid = supportFragmentManager.findFragmentById(R.id.fragmentContainerTop) as GridFragment?
        fragmentGrid?.initialize()
    }

}