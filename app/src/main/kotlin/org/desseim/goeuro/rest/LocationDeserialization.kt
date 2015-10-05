package org.desseim.goeuro.rest

import android.location.Location
import com.fasterxml.jackson.databind.util.StdConverter
import org.desseim.goeuro.model.createLocation

data class ApiGeoLocation(val latitude: Double, val longitude: Double)

class ApiGeoLocationToAndroidLocationConverter : StdConverter<ApiGeoLocation, Location>() {
    override fun convert(value: ApiGeoLocation?): Location? =
            if (value != null) createLocation(value.latitude, value.longitude) else null
}