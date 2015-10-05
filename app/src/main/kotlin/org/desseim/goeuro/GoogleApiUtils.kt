package org.desseim.goeuro

import android.content.Context
import android.os.Bundle
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.Api
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import rx.Observable
import rx.lang.kotlin.observable

fun Context.createGoogleApiClient(vararg apis: Api<out Api.ApiOptions.NotRequiredOptions>): GoogleApiClient =
    GoogleApiClient
            .Builder(this)
            .apply { apis.forEach { this.addApi(it) } }
            .build()

fun GoogleApiClient.connectRx(): Observable<GoogleApiClientConnectionEvent> =
    Observable.using<GoogleApiClientConnectionEvent, GoogleApiClient>(
            { this },
            { googleApiClient ->
                observable { subscriber ->
                    googleApiClient.registerConnectionCallbacks(object : GoogleApiClient.ConnectionCallbacks {
                        override fun onConnected(connectionHint: Bundle?) {
                            if (!subscriber.isUnsubscribed) {
                                subscriber.onNext(GoogleApiClientConnectionEvent.Connected(connectionHint))
                            }
                        }
                        override fun onConnectionSuspended(cause: Int) {
                            if (!subscriber.isUnsubscribed) {
                                subscriber.onNext(GoogleApiClientConnectionEvent.ConnectionSuspended(cause))
                            }
                        }
                    })

                    val connectionResult = googleApiClient.blockingConnect()

                    if (!connectionResult.isSuccess && !subscriber.isUnsubscribed) {
                        subscriber.onError(GoogleApiClientConnectionFailed(connectionResult))
                    }
                }
            },
            { it.disconnect() })

sealed class GoogleApiClientConnectionEvent() {
    class Connected(conectionHint: Bundle?) : GoogleApiClientConnectionEvent()
    class ConnectionSuspended(val cause: Int) : GoogleApiClientConnectionEvent()
}
class GoogleApiClientConnectionFailed(val result: ConnectionResult) : RuntimeException()


fun GoogleApiClient.lastLocation() = LocationServices.FusedLocationApi.getLastLocation(this)