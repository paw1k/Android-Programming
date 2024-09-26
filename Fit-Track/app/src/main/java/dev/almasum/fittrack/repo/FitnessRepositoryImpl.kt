package dev.almasum.fittrack.repo

import android.content.Context
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.data.Field
import com.google.android.gms.fitness.request.DataReadRequest
import dev.almasum.fittrack.models.DailyFitnessModel
import dev.almasum.fittrack.models.WeeklyFitnessModel
import java.util.Calendar
import java.util.concurrent.TimeUnit

class FitnessRepositoryImpl {
    private val dailyFitnessLiveData = MutableLiveData<DailyFitnessModel>()

    val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_CALORIES_EXPENDED, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_DISTANCE_DELTA, FitnessOptions.ACCESS_READ)
        .build()

    fun getDailyFitnessDataLive(context: Context): MutableLiveData<DailyFitnessModel> {
        getDailyFitnessData(context)
        return dailyFitnessLiveData
    }

    fun getDailyFitnessData(context: Context) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val startTime = calendar.timeInMillis

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
            .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime, System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, getGoogleAccount(context))
            .readData(readRequest)
            .addOnSuccessListener { data ->
                val buckets = data.buckets
                val bucket = if (buckets.isNotEmpty()) buckets[0] else null
                var stepCount = 0
                var calories = 0
                var distance = 0.0f
                bucket?.dataSets?.forEach { dataSet ->
                    dataSet.dataPoints.forEach { dataPoint ->
                        when (dataPoint.dataType) {
                            DataType.TYPE_STEP_COUNT_DELTA -> {
                                stepCount = dataPoint.getValue(Field.FIELD_STEPS).asInt()
                            }

                            DataType.TYPE_CALORIES_EXPENDED -> {
                                calories = dataPoint.getValue(Field.FIELD_CALORIES).asFloat()
                                    .toInt() / 1000
                            }

                            DataType.TYPE_DISTANCE_DELTA -> {
                                distance = dataPoint.getValue(Field.FIELD_DISTANCE).asFloat() / 1000
                            }
                        }
                    }
                }
                val dailyFitness = DailyFitnessModel(stepCount, calories, distance)
                dailyFitnessLiveData.postValue(dailyFitness)
            }
            .addOnFailureListener {
                // Handle error
            }
    }

    fun getWeeklyFitnessData(context: Context): MutableLiveData<WeeklyFitnessModel> {
        val weeklyFitnessLiveData = MutableLiveData<WeeklyFitnessModel>()

        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.add(Calendar.DAY_OF_YEAR, -7)
        val startTime = calendar.timeInMillis

        // Build data read request for the last 7 days
        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
            .aggregate(DataType.TYPE_CALORIES_EXPENDED, DataType.AGGREGATE_CALORIES_EXPENDED)
            .aggregate(DataType.TYPE_DISTANCE_DELTA, DataType.AGGREGATE_DISTANCE_DELTA)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime, System.currentTimeMillis(), TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, getGoogleAccount(context))
            .readData(readRequest)
            .addOnSuccessListener { data ->
                val buckets = data.buckets
                val dailyFitnessList = mutableListOf<DailyFitnessModel>()

                // Process each bucket of fitness data
                buckets.forEach { bucket ->
                    var stepCount = 0
                    var calories = 0
                    var distance = 0.0f

                    bucket.dataSets.forEach { dataSet ->
                        dataSet.dataPoints.forEach { dataPoint ->
                            when (dataPoint.dataType) {
                                DataType.TYPE_STEP_COUNT_DELTA -> {
                                    stepCount = dataPoint.getValue(Field.FIELD_STEPS).asInt()
                                }

                                DataType.TYPE_CALORIES_EXPENDED -> {
                                    calories = dataPoint.getValue(Field.FIELD_CALORIES).asFloat()
                                        .toInt() / 1000
                                }

                                DataType.TYPE_DISTANCE_DELTA -> {
                                    distance =
                                        dataPoint.getValue(Field.FIELD_DISTANCE).asFloat() / 1000
                                }
                            }
                        }
                    }

                    val dailyFitness = DailyFitnessModel(stepCount, calories, distance)
                    dailyFitnessList.add(dailyFitness)
                }

                val weeklyFitness = WeeklyFitnessModel(dailyFitnessList)
                weeklyFitnessLiveData.postValue(weeklyFitness)
            }
            .addOnFailureListener { exception ->
                // Handle error
            }

        return weeklyFitnessLiveData
    }

    fun getGoogleAccount(context: Context): GoogleSignInAccount =
        GoogleSignIn.getAccountForExtension(context, fitnessOptions)

}