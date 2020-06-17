package com.dude36.pftracker.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.dude36.pftracker.Data
import com.dude36.pftracker.MainActivity
import com.dude36.pftracker.R
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_home, container, false)
        val textView: TextView = root.findViewById(R.id.text_home)
        homeViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        val averageText: TextView = root.findViewById(R.id.week_average)
        val averageData = Data.averageData(7)
        averageText.text = "Your weekly average is: $averageData"

        // Get the previous week's data & dates
        val prevWeekData = listOf<TextView>(
            root.findViewById(R.id.minus_seven_data),
            root.findViewById(R.id.minus_six_data),
            root.findViewById(R.id.minus_five_data),
            root.findViewById(R.id.minus_four_data),
            root.findViewById(R.id.minus_three_data),
            root.findViewById(R.id.minus_two_data),
            root.findViewById(R.id.minus_one_data)
        )
        val prevWeekDates = listOf<TextView>(
            root.findViewById(R.id.minus_seven_date),
            root.findViewById(R.id.minus_six_date),
            root.findViewById(R.id.minus_five_date),
            root.findViewById(R.id.minus_four_date),
            root.findViewById(R.id.minus_three_date),
            root.findViewById(R.id.minus_two_date),
            root.findViewById(R.id.minus_one_date)
        )

        // Go to seven days ago, and count up
        val now = Calendar.getInstance()
        now.timeInMillis = now.timeInMillis - (86400000 * 7)

        // Go through all days in the past
        for (i in 6 downTo 0) {
            val previous = Data.specificDate(i+1)

            // Determine data's existence
            if (previous == null) {
                prevWeekData[i].text = "N/A"
            } else {
                var avg = 0
                for (e in previous) {
                    avg += e.average()
                }
                avg /= previous.size
                prevWeekData[i].text = avg.toString()
            }

            // Add date to match data
            prevWeekDates[i].text = "${now.get(Calendar.MONTH)+1}/${now.get(Calendar.DAY_OF_MONTH)}"
            now.timeInMillis += 86400000
        }

        return root
    }
}
