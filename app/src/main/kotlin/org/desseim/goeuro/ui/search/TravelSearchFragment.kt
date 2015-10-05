package org.desseim.goeuro.ui.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import com.google.android.gms.common.api.GoogleApiClient
import org.desseim.goeuro.R
import org.desseim.goeuro.inject.InjectableFragment
import org.desseim.goeuro.rest.PlaceService
import javax.inject.Inject

public class TravelSearchFragment : InjectableFragment() {
    @Inject lateinit var placeService: PlaceService
    var googleLocationApiClient: GoogleApiClient? = null
        internal set(value) {
            field = value
            updateAutoCompleteViewsApiClient(value)
        }

    private val departureAutoCompleteTextView: AutoCompleteTextView?
        get() = view?.findViewById(R.id.departure_input_view) as? AutoCompleteTextView
    private val departureViewLocationAdapter: DynamicLocationAdapter?
        get() = departureAutoCompleteTextView?.adapter as? DynamicLocationAdapter

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_travel_search, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        component().inject(this)

        val departureAutoCompleteTextView = getView().findViewById(R.id.departure_input_view) as AutoCompleteTextView
        setupAutoCompleteTextViews(departureAutoCompleteTextView)
    }

    private fun setupAutoCompleteTextViews(departureAutoCompleteTextView: AutoCompleteTextView) {
        val dynamicLocationAdapter = DynamicLocationAdapter(placeService, activity, android.R.layout.simple_list_item_1)
        departureAutoCompleteTextView.setAdapter(dynamicLocationAdapter)

        updateAutoCompleteViewsApiClient(googleLocationApiClient)
    }

    private fun updateAutoCompleteViewsApiClient(apiClient: GoogleApiClient?) {
        departureViewLocationAdapter?.googleLocationApiClient = apiClient
    }
}
