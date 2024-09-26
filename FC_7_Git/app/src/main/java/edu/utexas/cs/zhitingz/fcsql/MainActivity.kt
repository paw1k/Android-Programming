package edu.utexas.cs.zhitingz.fcsql

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.utexas.cs.zhitingz.fcsql.databinding.ActivityMainBinding
import java.io.IOException

class MainActivity : AppCompatActivity() {
    private lateinit var restaurantDb: SQLiteDatabase
    private lateinit var dbHelper: DatabaseHelper
    private lateinit var restaurantAdapter: RestaurantItemAdapter
    private lateinit var binding : ActivityMainBinding
    // For string arrays defined in XML resource files, get
    // them in arrays in memory lazily (because resources
    // remains null until after activity is initialized)
    private val cities: Array<String> by lazy {
        resources.getStringArray(R.array.city)
    }
    private val restaurantTypes: Array<String> by lazy {
        resources.getStringArray(R.array.restaurant_type)
    }
    private val order: Array<String> by lazy {
        resources.getStringArray(R.array.order)
    }
    private val limit: Array<String> by lazy {
        resources.getStringArray(R.array.limit)
    }

    // Useful snippet for creating spinners from arrays defined
    // in XML files in the res/values directory
    private fun createAdapterFromResource(arrayResource: Int):
            ArrayAdapter<CharSequence> {
        val adapter = ArrayAdapter.createFromResource(this,
            arrayResource,
            android.R.layout.simple_spinner_item)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        return adapter
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.restaurantTypeSpinner.adapter =
            createAdapterFromResource(R.array.restaurant_type)

        binding.citySpinner.adapter =
            createAdapterFromResource(R.array.city)

        binding.limitSpinner.adapter =
            createAdapterFromResource(R.array.limit)

        binding.chooseOrderSpinner.adapter =
            createAdapterFromResource(R.array.order)

        binding.queryButton.setOnClickListener(::doQuery)

        dbHelper = DatabaseHelper(this)
        try {
            dbHelper.createDatabase()
        } catch (e: IOException) {
            Log.e("DB", "Fail to create database", e)
        }
        restaurantDb = dbHelper.readableDatabase
        val defaultBadCursor = restaurantDb.query("businesses", null, null, null, null, null, null, "0")
        restaurantAdapter = RestaurantItemAdapter(this, defaultBadCursor, false)
        binding.restaurantList.adapter = restaurantAdapter
    }

    private fun doQuery(v: View) {
        // where contains the selection clause and args contains the corresponding arguments
        val where = ArrayList<String>()
        val args = ArrayList<String>()
        val table = handleRestaurant(where, args)

        // XXX you need to complete handleCity
        handleCity(where, args)

        // This code turns the where array into a single string
        // called selectionStr.  You will pass this to query.
        var selectionStr = ""
        if (where.size != 0) {
            selectionStr += where[0]
            for (i in 1 until where.size) {
                selectionStr += " AND " + where[i]
            }
        }
        // This code creates an Array<String> of arguments and logs them
        val argsStr = args.toTypedArray()
        // Debug output
        Log.d("whereStr ", selectionStr)
        for (s in argsStr) {
            Log.d("argsStr ", s)
        }

        // XXX WRITE ME: Handle ORDER BY and LIMIT request
        // Then use query (not rawQuery) on restaurantDb to get a cursor
        // Get the query to display in the list view by calling
        // the right function on restaurantAdapter

        // Determine the limit for the query
        val limitPos = binding.limitSpinner.selectedItemPosition
        val limitQ = if (limitPos > 0) limit[limitPos] else restaurantDb.maximumSize.toString()

        var orderBy = "name"
        if (binding.priceOrderCheckbox.isChecked) {
            orderBy = "price"
        }

        if (binding.chooseOrderSpinner.selectedItemPosition > 0) {
            orderBy += " ${order[binding.chooseOrderSpinner.selectedItemPosition]}"
        }

        val queryTo = restaurantDb.query(
            table,
            null,
            selectionStr,
            args.toTypedArray(),
            null,
            null,
            orderBy,
            limitQ
        )

        if (queryTo.count == 0) {
            Toast.makeText(this, "No matches found!", Toast.LENGTH_LONG).show()
            queryTo.close()
        } else {
            val lvItems: ListView = findViewById(R.id.restaurant_list)
            restaurantAdapter!!.changeCursor(queryTo)
        }

        // Look at the documentation for SQLiteDatabase.query
        // https://developer.android.com/reference/kotlin/android/database/sqlite/SQLiteDatabase.html#query
        // You can pass null to columns, groupBy and having
        // If the query result is empty, generate a toast.
    }

    // Helper method for generate selection clause for query city
    private fun handleCity(where: MutableList<String>, args: MutableList<String>) {
        // XXX Write me.
        val cityPos = binding.citySpinner.selectedItemPosition
        if (cityPos != 0) {
            var cityFilter = ""
            cityFilter = cities[cityPos]
            where.add("(businesses.city = ?)")
            args.add(cityFilter)
        }
    }

    // Helper method to generate the selection clause for the restaurant type.
    private fun handleRestaurant(where: MutableList<String>, args: MutableList<String>): String {
        var table = "businesses"
        val restaurantTypePos = binding.restaurantTypeSpinner.selectedItemPosition
        if (restaurantTypePos != 0) {
            var categoryFilter = ""
            when {
                restaurantTypePos == 1 -> categoryFilter = "newamerican"
                restaurantTypePos == 2 -> categoryFilter = "breakfast_brunch"
                // These are lower cased in the database, but cities are not
                restaurantTypePos > 0 -> categoryFilter = restaurantTypes[restaurantTypePos].lowercase()
            }
            table += ", categories"
            where.add("(businesses._id = categories._id) AND (categories.category_name = ?)")
            args.add(categoryFilter)
        }
        return table
    }
}
