package org.desseim.goeuro.ui.search

import android.content.Context
import android.location.Location
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Filter
import com.google.android.gms.common.api.GoogleApiClient
import org.desseim.goeuro.lastLocation
import org.desseim.goeuro.rest.Place
import org.desseim.goeuro.rest.PlaceService
import java.util.*

public class DynamicLocationAdapter : ArrayAdapter<LocationInputElement> {
    val placeService: PlaceService
    var googleLocationApiClient: GoogleApiClient? = null
        internal set

    constructor(placeService: PlaceService, context: Context, resource: Int) : super(context, resource) { this.placeService = placeService }
    constructor(placeService: PlaceService, context: Context, resource: Int, textViewResourceId: Int) : super(context, resource, textViewResourceId) { this.placeService = placeService }
    constructor(placeService: PlaceService, context: Context, resource: Int, objects: Array<LocationInputElement>) : super(context, resource, objects) { this.placeService = placeService }
    constructor(placeService: PlaceService, context: Context, resource: Int, textViewResourceId: Int, objects: Array<LocationInputElement>) : super(context, resource, textViewResourceId, objects) { this.placeService = placeService }
    constructor(locationService: PlaceService, context: Context, resource: Int, objects: List<LocationInputElement>) : super(context, resource, objects) { this.placeService = locationService }
    constructor(placeService: PlaceService, context: Context, resource: Int, textViewResourceId: Int, objects: List<LocationInputElement>) : super(context, resource, textViewResourceId, objects) { this.placeService = placeService }

    val _filter: Filter by lazy {
        object : Filter() {
            val LOG_TAG = Filter::class.qualifiedName

            override fun publishResults(constraint: CharSequence?, results: Filter.FilterResults) {
                clear()
                addAll(results.values as List<LocationInputElement>)

                if (results.count > 0) notifyDataSetChanged()
                else notifyDataSetInvalidated()
            }

            override fun performFiltering(constraint: CharSequence?): Filter.FilterResults {
                val results = Filter.FilterResults()
                results.values =
                        if (constraint != null) {
                            try {
                                // this is called in a background thread already, so we call the API synchronously
                                placeService
                                        .getPlaces(constraint.toString(), contextLocaleLanguage())
                                        .sortedBy(distanceFrom(googleLocationApiClient?.lastLocation()))
                                        .map { LocationInputElement(it) }
                            } catch(e: Exception) {
                                listOf<Place>()
                            }
                        } else {
                            listOf<Place>()
                        }
                return results
            }

            private fun contextLocaleLanguage() = context?.resources?.configuration?.locale?.language ?: DEFAULT_LOCALE

            private fun distanceFrom(location: Location?, defaultValue: Float = 0f): (Place) -> Float {
                if (location == null) {
                    // temporary crude way of noticing when the connection to the location API client went well but no location is yet available
                    Log.d(LOG_TAG, "! location to check distance from is absent !")
                }
                return { place -> location?.distanceTo(place.location) ?: defaultValue }
            }
        }
    }
    override fun getFilter(): Filter? = _filter

    companion object {
        val DEFAULT_LOCALE = Locale.GERMANY.language
    }
}

private class LocationInputElement(val place: Place) {
    override fun toString(): String {
        return place.name
    }
}