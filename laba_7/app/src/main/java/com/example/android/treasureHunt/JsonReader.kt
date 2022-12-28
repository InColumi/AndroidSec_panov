package com.example.android.treasureHunt

import android.content.Context
import com.google.gson.Gson

import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type


object JsonReader{
    class Geofencing(
        val geofencing: List<LandmarkDataObject>
    )

    fun readJson(context:Context): List<LandmarkDataObject>{
        val jsonAsset = context.assets.open("geofencing_lab_landmarks.json")
        val text = jsonAsset.reader().readText()

        val listType: Type = object : TypeToken<List<LandmarkDataObject>>() {}.type

        return Gson().fromJson<Geofencing>(text, Geofencing::class.java).geofencing
    }
}