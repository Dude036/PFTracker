package com.dude36.pftracker.ui.entries

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.dude36.pftracker.AddEntry
import com.dude36.pftracker.Data
import com.dude36.pftracker.R

class EntriesFragment : Fragment() {

    private lateinit var galleryViewModel : EntriesViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        galleryViewModel =
                ViewModelProviders.of(this).get(EntriesViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_entries, container, false)
//        updateEntries(root)

        return root
    }

    companion object {
        fun updateEntries(view: View) {
            val history : LinearLayout = view.findViewById(R.id.linear_history)

            val headerRow = TableRow(view.context)

            // Date
            val headerDate = TextView(view.context)
            headerDate.textSize = 20.0F
            headerDate.text = "Date"
            headerDate.setTypeface(null, Typeface.BOLD)
            headerRow.addView(headerDate)

            // Entries
//            val headerEntry = TextView(view.context)
//            headerEntry.textSize = 20.0F
//            headerEntry.text = "Entries"
//            headerEntry.setTypeface(null, Typeface.BOLD)
//            headerRow.addView(headerEntry)

            // Blank
            headerRow.addView(TextView(view.context))
            headerRow.addView(TextView(view.context))
            headerRow.addView(TextView(view.context))

            // Average
            val headerAverage = TextView(view.context)
            headerAverage.textSize = 20.0F
            headerAverage.text = "Avg"
            headerAverage.setTypeface(null, Typeface.BOLD_ITALIC)
            headerRow.addView(headerAverage)

            // Treatment & Deletion
            headerRow.addView(TextView(view.context))
            headerRow.addView(TextView(view.context))
            history.addView(headerRow)

            for (entry in Data.allData) {

                val newRow = TableRow(view.context)
                // Add Date
                val newDate = TextView(view.context)
                newDate.text = entry.value.time
                newDate.textSize = 18.0F
                newDate.setPadding(0, 4, 0, 4)
                newRow.addView(newDate)

                // Add all 3 entries
                for (i in 0 .. 3) {
                    val newEntry = TextView(view.context)
                    if (entry.value.entry.size == i) {
                        newEntry.text = entry.value.average().toString()
                        newEntry.setTypeface(null, Typeface.ITALIC)
                    } else {
                        newEntry.text = entry.value.entry[i].toString()
                    }
                    newEntry.textSize = 18.0F
                    newEntry.setPadding(0, 4, 0, 4)
                    newRow.addView(newEntry)
                }

                // Add Pre or Post treatment
                val newTreat = TextView(view.context)
                newTreat.text = entry.value.treat()
                newTreat.textSize = 18.0F
                newTreat.setPadding(0, 4, 0, 4)
                newRow.addView(newTreat)

                // Add Delete condition at the end
                val newDelete = ImageView(view.context)
                newDelete.setImageResource(R.drawable.black_x)
                newDelete.scaleType = ImageView.ScaleType.FIT_XY
                newDelete.adjustViewBounds = true
                newDelete.setOnClickListener {
                    if (newDate.currentTextColor == Color.RED) {
                        Data.removeItem(entry.key)
                        history.removeAllViews()
                        updateEntries(view)
                    } else {
                        newTreat.setTextColor(Color.RED)
                        newTreat.text = "Delete?"
                        newTreat.textSize = 16.0F
                        newDate.setTextColor(Color.RED)
                        newDelete.setImageResource(R.drawable.red_x)
                    }
                }
                newRow.addView(newDelete)

//                val newEntry = TextView(view.context)
//                newEntry.text = "${entry.value}"
//                newEntry.textSize = 18.0F
//                newEntry.setPadding(0, 4, 0, 4)
//                newEntry.setOnClickListener {
//                    if (newEntry.currentTextColor == Color.RED) {
//                        Data.removeItem(entry.key)
//                        history.removeAllViews()
//                        updateEntries(view)
//                    } else {
//                        newEntry.text = newEntry.text as String + " Delete?"
//                        newEntry.setTextColor(Color.RED)
//                    }
//                }
                history.addView(newRow)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        view?.let {
            it.findViewById<LinearLayout>(R.id.linear_history).removeAllViews()
            updateEntries(it)
        }
    }
}
