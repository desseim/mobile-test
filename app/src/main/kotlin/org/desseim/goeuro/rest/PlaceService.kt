package org.desseim.goeuro.rest

import com.fasterxml.jackson.annotation.JsonProperty
import retrofit.http.GET
import retrofit.http.Path
import rx.Observable

public interface PlaceService {
    @GET("/v2/position/suggest/{locale}/{locationPrefix}")
    fun getPlaces(@Path("locationPrefix") locationPrefix: String, @Path("locale") locale: String): List<Place>
}

data class Place(
        @JsonProperty("name") val name: String,
        @JsonProperty("geo_position") val location: android.location.Location
)