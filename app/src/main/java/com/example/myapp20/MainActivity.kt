package com.example.myapp20

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    private var sm: SensorManager? = null
    private var s: Sensor? = null
    private lateinit var tv: TextView
    private var sv: SensorEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        tv = findViewById(R.id.textView)

        sm = getSystemService(SENSOR_SERVICE) as SensorManager
        s = sm?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

        sv = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                // Обработка данных сенсора в отдельном потоке
                Thread {
                    val rotationMatrix = FloatArray(16)
                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                    val remappedRotationMatrix = FloatArray(16)
                    SensorManager.remapCoordinateSystem(
                        rotationMatrix,
                        SensorManager.AXIS_X,
                        SensorManager.AXIS_Z,
                        remappedRotationMatrix
                    )

                    // Преобразование в ориентации
                    val orientations = FloatArray(3)
                    SensorManager.getOrientation(remappedRotationMatrix, orientations)
                    for (i in 0..2) {
                        orientations[i] = (Math.toDegrees(orientations[i].toDouble())).toFloat()
                    }

                    // Обновление UI на главном потоке
                    runOnUiThread {
                        tv.text = orientations[2].toInt().toString()
                    }
                }.start()
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
                // Обработка изменений точности сенсора по мере необходимости
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Регистрация слушателя сенсора при возобновлении активности
        sm?.registerListener(sv, s, SensorManager.SENSOR_DELAY_FASTEST)
    }

    override fun onPause() {
        super.onPause()
        // Отмена регистрации слушателя при приостановке активности
        sm?.unregisterListener(sv)
    }
}