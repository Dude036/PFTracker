package com.dude36.pftracker

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.view.get
import com.dude36.pftracker.ui.entries.EntriesFragment
import java.text.SimpleDateFormat
import java.util.*

class AddEntry : AppCompatActivity() {
    private val calendar : Calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_entry)

        val calView = findViewById<CalendarView>(R.id.calendar_entry)
        calView.setOnDateChangeListener { _, y, m, d ->
            calendar.set(y, m, d)
        }

        val submit = findViewById<Button>(R.id.entry_submit)
        submit.setOnClickListener { _: View? ->
            // Validate input is there
            if (findViewById<EditText>(R.id.time_hour).text.isEmpty()) {
                Toast.makeText(this, "Hour Missing", Toast.LENGTH_LONG).show()
                findViewById<EditText>(R.id.time_hour).error = "Hour is required!"
                return@setOnClickListener
            }
            if (findViewById<EditText>(R.id.time_min).text.isEmpty()) {
                Toast.makeText(this, "Minutes Missing", Toast.LENGTH_LONG).show()
                findViewById<EditText>(R.id.time_min).error = "Minutes is required!"
                return@setOnClickListener
            }
            if (findViewById<EditText>(R.id.measure_1).text.isEmpty()) {
                findViewById<EditText>(R.id.measure_1).error = "Entry missing"
                return@setOnClickListener
            }
            if (findViewById<EditText>(R.id.measure_2).text.isEmpty()) {
                findViewById<EditText>(R.id.measure_2).error = "Entry missing"
                return@setOnClickListener
            }
            if (findViewById<EditText>(R.id.measure_3).text.isEmpty()) {
                findViewById<EditText>(R.id.measure_3).error = "Entry missing"
                return@setOnClickListener
            }

            // Validate Current Time
            if (findViewById<EditText>(R.id.time_hour).text.toString().toInt() < 0
                || findViewById<EditText>(R.id.time_hour).text.toString().toInt() > 23) {
                findViewById<EditText>(R.id.time_hour).error = "Invalid Hour!"
                return@setOnClickListener
            }
            if (findViewById<EditText>(R.id.time_min).text.toString().toInt() < 0
                || findViewById<EditText>(R.id.time_min).text.toString().toInt() > 59) {
                findViewById<EditText>(R.id.time_hour).error = "Invalid Minute!"
                return@setOnClickListener
            }

            // Map Characters to numbers
            val measurement_entry = setOf<EditText>(findViewById(R.id.measure_1), findViewById(R.id.measure_2), findViewById(R.id.measure_3))
            val measurements : List<Short> = measurement_entry.map {
                it.text.toString().toShort()
            }

            // Get Date Info Date needs to be converted into Millies
            val date = SimpleDateFormat("YYYY-MM-dd").format(calendar.time)
            var hour = findViewById<EditText>(R.id.time_hour).text.toString()

            // Convert to PM if not in military
            if (findViewById<ToggleButton>(R.id.time_toggle).isChecked &&
                findViewById<EditText>(R.id.time_hour).text.toString().toInt() < 13) {
                hour = ((hour.toInt() + 12) % 24).toString()
            }
            if (hour.length == 1) {
                hour = "0$hour"
            }

            // Format Minutes
            var min = findViewById<EditText>(R.id.time_min).text.toString()
            if (min.length == 1) {
                min = "0$min"
            }
            val time = "$hour:$min"

            val treat = findViewById<ToggleButton>(R.id.entry_treatment).isChecked

            // Submit Data
            Data.addData(measurements, "$date $time", treat)

            // End activity and return to home page
            finish()
        }

        // Current Time function
        val current_time = findViewById<Button>(R.id.current_time)
        current_time.setOnClickListener {_: View? ->
            val now = Calendar.getInstance()
            findViewById<EditText>(R.id.time_hour).setText(now.get(Calendar.HOUR).toString())
            findViewById<EditText>(R.id.time_min).setText(now.get(Calendar.MINUTE).toString())
            // Check AM or PM
            if ((findViewById<ToggleButton>(R.id.time_toggle).isChecked && now.get(Calendar.AM_PM) == Calendar.AM)
                || (!findViewById<ToggleButton>(R.id.time_toggle).isChecked && now.get(Calendar.AM_PM) == Calendar.PM)) {
                findViewById<ToggleButton>(R.id.time_toggle).toggle()
            }
        }
    }
}
