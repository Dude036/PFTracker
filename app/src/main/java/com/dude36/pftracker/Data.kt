package com.dude36.pftracker

import android.content.Context
import java.io.File
import java.util.*
import org.json.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import kotlin.Comparator


/* Consider Using Firebase for remote database*/
/**An Entry that can be made from data, or a raw JSON string
 * measure          The list of data for each entry
 * date             String of the time in the following format
 *                      YYYY-MM-DD HH:MM
 * treat            True if pre treatment, False if post treatment
 * rawJson          When parsing raw JSON, use this argument
 */
class Entry(measure: List<Short> = listOf(), date : String = "",  treat : Boolean = false, rawJson: String = "") {
    var time : String
    var entry : List<Short>
    var treatment : Boolean

    init {
        if (rawJson == "") {
            time = date
            entry = measure
            treatment = treat
        } else {
            val tempJs = JSONObject(rawJson)
            time = tempJs.getString("date")
            treatment = tempJs.getBoolean("treatment")
            val tempEntry = tempJs.getJSONArray("measure")

            var muteEntry = mutableListOf<Short>()
            for (i in 0 until tempEntry.length()) {
                val x = tempEntry[i] as Int
                muteEntry.add(i, x.toShort())
            }
            entry = muteEntry.toList()
        }
    }

    fun getDate() : Calendar {
        val now = Calendar.getInstance()
        now.set(Calendar.YEAR, time.slice(0..3).toInt())
        now.set(Calendar.MONTH, time.slice(5..6).toInt()-1)
        now.set(Calendar.DAY_OF_MONTH, time.slice(8..9).toInt())
        now.set(Calendar.HOUR_OF_DAY, time.slice(11..12).toInt())
        now.set(Calendar.MINUTE, time.slice(14..15).toInt())
        return now
    }

    fun average() : Short {
        return entry.average().toInt().toShort()
    }

    fun treat() : String {
        return if (treatment) "Post" else "Pre"
    }


    override fun toString(): String {
        return "$time = $entry"
    }
}



object Data {
    private const val persistentFileName : String = "data.log"
    private var lastId : Int = 0
    var allData : MutableMap<Int, Entry> = mutableMapOf()
    private lateinit var activity : MainActivity

    /**Constructor
     * Creates a new data object, and creates data. If no file exists, then make one.
     */
    fun start(mainActivity: MainActivity) {
        activity = mainActivity
        // Get list of all files
        val filesList = activity.fileList()

        // If the files list is empty, store an empty file
        if (filesList.isEmpty()) {
            // Create Sample data
            println("[LOG     ] First time install")

            // Get current date
            val date = SimpleDateFormat("YYYY-MM-dd").format(Date())
            val time = SimpleDateFormat("HH:MM").format(System.currentTimeMillis())
            addData(listOf(600, 650, 700), "$date $time", false)

            // Translate to file
            updateToStorage()
        } else {
            // If there is a file, interpret it
            for (f in filesList) {
                println("[LOG     ] File: $f")
            }
            updateFromStorage()
        }

        println("[LOG     ] Data class initialized")
    }

    /**Add to persistent storage device
     * @param mainActivity : MainActivity      Capability of where to store the data
     * @param dataFromUser : List<Short>       Data to store
     */
    fun addData(entryData : List<Short>, dateData : String, treatmentData : Boolean) {
        // Write temp and permanent
        allData[lastId] = Entry(measure = entryData, date = dateData, treat = treatmentData)
        lastId += 1
        updateToStorage()
    }

    /**Update the database with all the necessary information
     * @param mainActivity: MainActivity       Context to get the file from persistent storage
     */
    private fun updateFromStorage() {
        // Read the file
        val file = File(activity.filesDir, persistentFileName)
        if (file.canRead()) {
            println("[LOG     ] This file can be read")
            val rawDataImport = activity.openFileInput(persistentFileName).bufferedReader().readLine()

            // Interpret raw data
            println("[LOG     ] $rawDataImport")
            val jsObj = JSONObject(JSONTokener(rawDataImport))
            lastId = 0
            var allEntries = mutableListOf<Entry>()

            // Get all Data
            for (key in jsObj.keys()) {
                val rawValue = jsObj.getString(key)
                allEntries.add(Entry(rawJson = rawValue))
//                allData[key.toInt()] =
//                lastId += 1
            }

            // Sort Entries
            allEntries.sortWith(Comparator { entry1, entry2 ->
                (entry1.getDate().timeInMillis - entry2.getDate().timeInMillis).toInt()
            })

            // Add all entries to all Data
            for (newE in allEntries.reversed()) {
                allData[lastId] = newE
                lastId += 1
            }

            println("[LOG     ] Parsing has been completed")
            updateToStorage()
            println("[LOG     ] Saved Sorted")
        } else {
            println("[ERROR   ] This file cannot be read!!")
//            throw
        }
    }

    /**Update the Database on persistent storage
     * @param mainActivity : MainActivity      Context for where to save the data on the device
     */
    private fun updateToStorage() {
        println("[LOG     ] Writing to Storage")
        activity.openFileOutput(persistentFileName, Context.MODE_PRIVATE).use {
            it.write("${toJSON()}".toByteArray())
        }
    }

    /**Convert the Data Object into a JSON object so it can be saved or transported
     * @return JSONObject           Json Object of all the data
     */
    fun toJSON() : JSONObject {
        val jsObj = JSONObject()

        for (dataPoint in allData.iterator()) {
            // Temp object
            val temp = JSONObject()

            // Array of data
            val jsArr = JSONArray()
            for (v in dataPoint.value.entry) { jsArr.put(v) }

            // Add to temp
            temp.put("date", dataPoint.value.time)
            temp.put("treatment", dataPoint.value.treatment)
            temp.put("measure", jsArr)

            // Commit to final product
            jsObj.put(dataPoint.key.toString(), temp)
        }
        return jsObj
    }

    /**Overwrite local storage
     * @param rawData : String      Raw data to be overwritten with
     */
    fun fromJSON(rawData : String) : Boolean {
        // Verify it's usable
        // TODO(@Josh) This needs to be done. Not sure how yet
        var testData : MutableMap<Int, Entry> = mutableMapOf()
        var num = 0
        try {
            // Can it be turned into a JSON object?
            val jsObj = JSONObject(JSONTokener(rawData))

            // Iterate through keys and see if they're valid
            for (k in jsObj.keys()) {
                testData.put(num, Entry(rawJson = jsObj.getString(k)))
                num++
            }
        } catch (e : Exception) {
            println(e)
            return false
        }

        // Write data and update local
        allData = testData
        updateToStorage()
        return true
    }

    /**
     * @param userId : Int          Remove an item from the database based on user's request
     */
    fun removeItem(userId : Int) {
        allData.remove(userId)
        updateToStorage()
    }

    fun averageData(days : Int = -1) : Any? {
        // Figure out Days
        val totalDays =  if (days == -1 || days > allData.size) { allData.size } else days

        // Subtract day in milliseconds by the total days backwards
        val lastDayAvailable = Calendar.getInstance().timeInMillis - (86400000 * totalDays).toLong()
        var average = 0
        var avgCount = 0
        for (i in allData.entries.indices) {
            if (allData[i]!!.getDate().timeInMillis > lastDayAvailable) {
                average += allData[i]!!.average()
                avgCount += 1
            } else {
                break
            }
        }
        return if (avgCount > 0) average / avgCount else "N/A"
    }

    /** Function designed to get a specific days info. If there is no data, send back an empty list
     * @param days          Number of days to go back from present
     * @return The days in question or null if no data exists
     */
    fun specificDate(days: Int) : List<Entry>? {
        var daysEntries = mutableListOf<Entry>()
        // Set correct date info
        val start = Calendar.getInstance()
        start.set(Calendar.HOUR_OF_DAY, 0)
        start.set(Calendar.MINUTE, 0)
        start.timeInMillis = start.timeInMillis - (86400000 * days)

        val startMilli = start.timeInMillis
        val endMilli = start.timeInMillis + 86400000
//        println("~~~ Day Range ~~~")
//        println("${start.get(Calendar.MONTH)+1}/${start.get(Calendar.DAY_OF_MONTH)}/${start.get(Calendar.YEAR)} ${start.get(Calendar.HOUR_OF_DAY)}:${start.get(Calendar.MINUTE)}")
//        start.timeInMillis = start.timeInMillis + 86400000
//        println("${start.get(Calendar.MONTH)+1}/${start.get(Calendar.DAY_OF_MONTH)}/${start.get(Calendar.YEAR)} ${start.get(Calendar.HOUR_OF_DAY)}:${start.get(Calendar.MINUTE)}")

        println("Start: $startMilli - End: $endMilli")
        for (entry in allData) {
            val entryMilli = entry.value.getDate().timeInMillis
            println("Entry: $entryMilli")
            if (entryMilli in (startMilli + 1) until endMilli) {
                daysEntries.add(entry.value)
            } else if (entryMilli < endMilli) {
                break
            }
        }
        // Send back null if there is no data, otherwise the day's entry(s)
        return if (daysEntries.isEmpty()) null else daysEntries
    }
}

