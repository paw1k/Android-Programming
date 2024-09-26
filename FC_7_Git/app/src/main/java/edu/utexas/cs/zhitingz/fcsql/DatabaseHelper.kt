package edu.utexas.cs.zhitingz.fcsql

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by zhitingz on 9/9/16, updated since
 */
class DatabaseHelper(private val context: Context) :
        SQLiteOpenHelper(context, DB_NAME, null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    @Throws(IOException::class)
    private fun copyDatabase() {
        val input = context.assets.open(DB_NAME)
        val outFile = context.getDatabasePath(DB_NAME)
        val output = FileOutputStream(outFile)
        Log.d(javaClass.simpleName, "Copying DB from assets in: ${input} out: ${outFile.absolutePath}")
        input.copyTo(output)
    }
    // Called by app, so it can print meaningful error
    @Throws(IOException::class)
    fun createDatabase() {
        val inFile = context.getDatabasePath(DB_NAME)
        Log.d(javaClass.simpleName, "DBHelper input: ${inFile.absolutePath} exists: ${inFile.exists()}")
        if(!inFile.exists()) {
            copyDatabase()
        }
    }


    override fun onOpen(db: SQLiteDatabase) {
        super.onOpen(db)
        db.execSQL("PRAGMA foreign_keys=ON")
    }

    companion object {
        private const val DB_NAME = "restaurants.db"
    }
}
