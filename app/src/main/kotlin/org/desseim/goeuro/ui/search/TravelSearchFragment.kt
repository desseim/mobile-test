package org.desseim.goeuro.ui.search

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.CalendarView
import android.widget.TextView
import com.google.android.gms.common.api.GoogleApiClient
import com.jakewharton.rxbinding.view.RxView
import com.jakewharton.rxbinding.widget.RxTextView
import org.desseim.goeuro.R
import org.desseim.goeuro.inject.InjectableFragment
import org.desseim.goeuro.rest.PlaceService
import org.desseim.goeuro.rx.dateSelectionEvents
import org.desseim.goeuro.rx.observeOnMainThread
import rx.Observable
import rx.lang.kotlin.emptyObservable
import rx.subscriptions.CompositeSubscription
import java.text.DateFormat
import java.util.*
import javax.inject.Inject

public class TravelSearchFragment : InjectableFragment() {
    @Inject lateinit var placeService: PlaceService
    var googleLocationApiClient: GoogleApiClient? = null
        internal set(value) {
            field = value
            departureViewLocationAdapter?.googleLocationApiClient = value
            arrivalViewLocationAdapter?.googleLocationApiClient = value
        }

    val subscriptions = CompositeSubscription()

    private val departureAutoCompleteTextView: AutoCompleteTextView?
        get() = view?.findViewById(R.id.departure_input_view) as? AutoCompleteTextView
    private val departureViewLocationAdapter: DynamicLocationAdapter?
        get() = departureAutoCompleteTextView?.adapter as? DynamicLocationAdapter
    private val arrivalAutoCompleteTextView: AutoCompleteTextView?
        get() = view?.findViewById(R.id.arrival_input_view) as? AutoCompleteTextView
    private val arrivalViewLocationAdapter: DynamicLocationAdapter?
        get() = arrivalAutoCompleteTextView?.adapter as? DynamicLocationAdapter
    private val travelDateCalendarView: CalendarView?
        get() = view?.findViewById(R.id.travel_date_calendar) as? CalendarView
    private val travelDateLabelView: TextView?
        get() = view?.findViewById(R.id.travel_date_label) as? TextView
    private val searchActionView: FloatingActionButton?
        get() = view?.findViewById(R.id.search_action_button) as? FloatingActionButton

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater!!.inflate(R.layout.fragment_travel_search, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        component().inject(this)

        departureAutoCompleteTextView!!.setupAutoCompleteTextViews()
        arrivalAutoCompleteTextView!!.setupAutoCompleteTextViews()

        travelDateCalendarView!!.minDate = Calendar.getInstance().timeInMillis
    }

    override fun onResume() {
        super.onResume()

        val dateInputChanges = travelDateCalendarView!!
                .dateSelectionEvents()
                .observeOnMainThread()
                .doOnNext { travelDateLabelView?.text = it.selectedDate.formatForDisplay() }
        val departureInputChanges = RxTextView.textChangeEvents(departureAutoCompleteTextView!!)
        val arrivalInputChanges = RxTextView.textChangeEvents(arrivalAutoCompleteTextView!!)

        val inputChanges = Observable.combineLatest(
                departureInputChanges.map { it.text() },
                arrivalInputChanges.map { it.text() },
                dateInputChanges.map { it.selectedDate },
                ::AggregateUserInput)

        inputChanges
                .observeOnMainThread()
                .doOnNext { searchActionView?.visibility = if (it.isValid()) View.VISIBLE else View.GONE }
                .filter { it.isValid() }
                .switchMap { validInput -> searchActionView?.let { RxView.clicks(it).map { validInput } } ?: emptyObservable<AggregateUserInput>() }
                .subscribe { searchAction -> Snackbar.make(view, "TODO :( Search for a trip from: ${searchAction.departure} to: ${searchAction.arrival} on the: ${searchAction.date.formatForDisplay()}", Snackbar.LENGTH_LONG).show() }
                .let { subscriptions.add(it) }
    }

    override fun onPause() {
        subscriptions.clear()

        super.onPause()
    }

    private fun AutoCompleteTextView.setupAutoCompleteTextViews() {
        val dynamicLocationAdapter = DynamicLocationAdapter(placeService, activity, android.R.layout.simple_list_item_1)
                .apply { googleLocationApiClient = this@TravelSearchFragment.googleLocationApiClient }
        setAdapter(dynamicLocationAdapter)
    }

    private fun Calendar.formatForDisplay() =
            DateFormat.getDateInstance().format(this.time)

    private data class AggregateUserInput(
            val departure: CharSequence,
            val arrival: CharSequence,
            val date: Calendar)
    private fun AggregateUserInput.isValid() =
            departure.toString().isNotBlank() &&
                    arrival.toString().isNotBlank()
}
