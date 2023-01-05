package com.example.laba_8_1

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.laba_8_1.databinding.FragmentFirstBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.*
import com.google.android.gms.fitness.request.DataDeleteRequest
import com.google.android.gms.fitness.request.DataReadRequest
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private  val TAG = "FirstFragment"
    val fitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE, FitnessOptions.ACCESS_WRITE)
        .addDataType(DataType.TYPE_STEP_COUNT_CUMULATIVE, FitnessOptions.ACCESS_READ)
        .build()

    private val GOOGLE_FIT_PERMISSIONS_REQUEST_CODE = 1
    private val MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (!GoogleSignIn.hasPermissions(GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions), fitnessOptions)) {
            GoogleSignIn.requestPermissions(
                this, // your activity
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE, // e.g. 1
                GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions),
                fitnessOptions)
        } else {
            accessGoogleFit()
            binding.countBtn.setOnClickListener {
                addCountSteps()
                binding.enterStepCount.setText("")
            }
            binding.deleteBtn.setOnClickListener {
                deleteCountSteps()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        binding.countBtn.setOnClickListener {
            addCountSteps()
            binding.enterStepCount.setText("")
        }
        binding.deleteBtn.setOnClickListener {
            deleteCountSteps()
        }

        when (resultCode) {
            Activity.RESULT_OK -> when (requestCode) {
                GOOGLE_FIT_PERMISSIONS_REQUEST_CODE -> accessGoogleFit()
                MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION -> accessGoogleFit()
                else -> {
                    // Result wasn't from Google Fit
                }
            }
            else -> {
                // Permission not granted
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun accessGoogleFit() {
        // Read the data that's been collected throughout the past week.
        val end = LocalDateTime.now()
        val start = end.minusYears(1)
        val endSeconds = end.atZone(ZoneId.systemDefault()).toEpochSecond()
        val startSeconds = start.atZone(ZoneId.systemDefault()).toEpochSecond()

        val readRequest = DataReadRequest.Builder()
            .aggregate(DataType.AGGREGATE_STEP_COUNT_DELTA)
            .setTimeRange(startSeconds, endSeconds, TimeUnit.SECONDS)
            .bucketByTime(1, TimeUnit.DAYS)
            .build()

        Fitness.getHistoryClient(requireActivity(), GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions))
            .readData(readRequest)
            .addOnSuccessListener { response ->
                Fitness.getHistoryClient(requireActivity(), GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions))
                    .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                    .addOnSuccessListener { result ->
                        val totalSteps =
                            result.dataPoints.firstOrNull()?.getValue(Field.FIELD_STEPS)?.asInt() ?: 0
                        binding.countStep.text = "$totalSteps"
                    }
                    .addOnFailureListener { e ->
                        Log.i(TAG, "There was a problem getting steps.", e)
                    }

                Log.i(TAG, "OnSuccess()")
            }
            .addOnFailureListener { e ->
                Log.w(TAG,"There was an error reading data from Google Fit", e)
            }
    }

    private fun addCountSteps() {
        // Declare that the data being inserted was collected during the past hour.
        val endTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
        val startTime = endTime.minusSeconds(5)

// Create a data source
        val dataSource = DataSource.Builder()
            .setAppPackageName(requireContext())
            .setDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .setStreamName("$TAG - step count")
            .setType(DataSource.TYPE_RAW)
            .build()

// For each data point, specify a start time, end time, and the
// data value -- in this case, 950 new steps.
        val stepCountDelta = binding.enterStepCount.text.toString()
        if (stepCountDelta.isBlank()) {
            Toast.makeText(requireContext(), "Введите количество шагов!", Toast.LENGTH_LONG).show()
            return
        }
        val dataPoint =
            DataPoint.builder(dataSource)
                .setField(Field.FIELD_STEPS, stepCountDelta.toInt())
                .setTimeInterval(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
                .build()

        val dataSet = DataSet.builder(dataSource)
            .add(dataPoint)
            .build()

        Fitness.getHistoryClient(requireActivity(), GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions))
            .insertData(dataSet)
            .addOnSuccessListener {
                accessGoogleFit()
                Log.i(TAG, "DataSet added successfully!")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was an error adding the DataSet", e)
            }
    }

    private fun deleteCountSteps() {
        // Declare that this code deletes step count information that was collected
// throughout the past day.
        val endTime = LocalDateTime.now().atZone(ZoneId.systemDefault())
        val startTime = endTime.minusSeconds(30)

// Create a delete request object, providing a data type and a time interval
        val request = DataDeleteRequest.Builder()
            .setTimeInterval(startTime.toEpochSecond(), endTime.toEpochSecond(), TimeUnit.SECONDS)
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA)
            .build()

// Invoke the History API with the HistoryClient object and delete request, and
// then specify a callback that will check the result.
        Fitness.getHistoryClient(requireActivity(), GoogleSignIn.getAccountForExtension(requireContext(), fitnessOptions))
            .deleteData(request)
            .addOnSuccessListener {
                accessGoogleFit()
                Log.i(TAG, "Data deleted successfully!")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "There was an error with the deletion request", e)
            }
    }
}