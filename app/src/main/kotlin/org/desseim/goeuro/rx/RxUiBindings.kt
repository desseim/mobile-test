package org.desseim.goeuro.rx

import android.widget.CalendarView
import com.jakewharton.rxbinding.internal.MainThreadSubscription
import com.jakewharton.rxbinding.internal.Preconditions
import rx.lang.kotlin.observable
import java.util.*

fun CalendarView.dateSelectionEvents() =
        observable<DateSelectionEvent> { subscriber ->
            Preconditions.checkUiThread()

            this.setOnDateChangeListener { calendarView, year, month, day ->
                if (!subscriber.isUnsubscribed) {
                    subscriber.onNext(DateSelectionEvent(this, GregorianCalendar(year, month, day)))
                }
            }
            subscriber.add(object : MainThreadSubscription() {
                override fun onUnsubscribe() {
                    this@dateSelectionEvents.setOnDateChangeListener(null)
                }
            })
            subscriber.onNext(DateSelectionEvent(this, Calendar.getInstance().apply { timeInMillis = date }))
        }

data class DateSelectionEvent(val view: CalendarView, val selectedDate: Calendar)