package org.desseim.goeuro.model

import android.location.Location

fun createLocation(latitude: Double, longitude: Double): Location {
    val location = Location("")
    location.latitude = latitude
    location.longitude = longitude
    return location
}
