package org.desseim.goeuro.ui

import android.app.Activity
import android.app.Dialog
import android.app.DialogFragment
import android.app.Fragment
import android.content.DialogInterface
import android.content.Intent
import android.content.IntentSender
import android.os.Bundle
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import org.desseim.goeuro.GoogleApiClientConnectionEvent
import org.desseim.goeuro.GoogleApiClientConnectionFailed
import org.desseim.goeuro.connectRx
import org.desseim.goeuro.createGoogleApiClient
import org.desseim.goeuro.inject.InjectableActivity
import org.desseim.goeuro.rx.observeOnMainThread
import rx.Subscription
import rx.lang.kotlin.BehaviourSubject
import rx.lang.kotlin.subscriber
import rx.schedulers.Schedulers

abstract class LocationAwareActivity : InjectableActivity() {
    abstract val locationConnectionErrorResolutionRequestCode: Int
    abstract val locationConnectionErrorDialogFragmentTag: String

    var isResolvingLocationError: Boolean = false
    var locationApiClientConnectionSubscription: Subscription? = null
    var connectionErrorDialogSubscription: Subscription? = null

    val locationApiClient: GoogleApiClient by lazy {
        createGoogleApiClient(LocationServices.API)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // restore state
        isResolvingLocationError = savedInstanceState?.getBoolean(STATE_KEY_IS_RESOLVING_LOCATION_ERROR) ?: isResolvingLocationError
    }

    override fun onStart() {
        super.onStart()

        // connect to the location API client
        connectLocationApiClient()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)

        connectionErrorDialogSubscription?.unsubscribe()
        outState?.putBoolean(STATE_KEY_IS_RESOLVING_LOCATION_ERROR, isResolvingLocationError)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            locationConnectionErrorResolutionRequestCode ->
                    if (resultCode == Activity.RESULT_OK) connectLocationApiClient()
                    // else we give up trying and connect
        }
    }

    override fun onAttachFragment(fragment: Fragment?) {
        super.onAttachFragment(fragment)

        when (fragment) {
            is LocationErrorDialogFragment -> {
                connectionErrorDialogSubscription?.unsubscribe()
                connectionErrorDialogSubscription = fragment
                        .dismissEvents
                        .observeOnMainThread()
                        .subscribe { isResolvingLocationError = false }
            }
        }
    }

    override fun onStop() {
        locationApiClientConnectionSubscription?.unsubscribe()

        super.onStop()
    }

    override fun onDestroy() {
        connectionErrorDialogSubscription?.unsubscribe()

        super.onDestroy()
    }

    private fun connectLocationApiClient() {
        if (!isResolvingLocationError) {
            locationApiClientConnectionSubscription?.unsubscribe()
            locationApiClientConnectionSubscription =
                    locationApiClient
                            .connectRx()
                            //TODO we could compose this observable so as to, on error, fallback to an observable retrieving the location through `android.location`
                            .subscribeOn(Schedulers.computation())
                            .observeOnMainThread()
                            .subscribe(subscriber<GoogleApiClientConnectionEvent>()
                                    .onError { error ->
                                        when (error) {
                                            is GoogleApiClientConnectionFailed -> tryResolveGoogleApiClientConnectionError(error.result)
                                            else -> Log.i(LOG_TAG, "Unexpected error trying to connect to the location API client", error)
                                        }
                                    }
                            )
        }
    }

    private fun tryResolveGoogleApiClientConnectionError(connectionResult: ConnectionResult) {
        check(!connectionResult.isSuccess)
        if (connectionResult.hasResolution()) {
            // try and start error resolution activity
            try {
                isResolvingLocationError = true
                connectionResult.startResolutionForResult(this, locationConnectionErrorResolutionRequestCode)
            } catch(e: IntentSender.SendIntentException) {
                Log.i(LOG_TAG, "Failed to start Google API connection error resolution activity", e)
            }
        } else {
            // show appropriate error dialog
            LocationErrorDialogFragment
                    .instantiate(connectionErrorCode = connectionResult.errorCode, errorResolutionRequestCode = locationConnectionErrorResolutionRequestCode)
                    .show(fragmentManager, locationConnectionErrorDialogFragmentTag)
            isResolvingLocationError = true
        }
    }

    companion object {
        val LOG_TAG = LocationAwareActivity::class.qualifiedName
        val STATE_KEY_IS_RESOLVING_LOCATION_ERROR = "location_aware_activity.state_is_resolving_location_error"
    }
}

private class LocationErrorDialogFragment : DialogFragment() {
    val dismissEvents = BehaviourSubject<Unit>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog? {
        val errorCode = arguments.getInt(ARG_KEY_CONNECTION_ERROR_CODE)
        val resolutionRequestCode = arguments.getInt(ARG_KEY_CONNECTION_ERROR_RESOLUTION_REQUEST_CODE)
        return GoogleApiAvailability.getInstance().getErrorDialog(activity, errorCode, resolutionRequestCode)
    }

    override fun onDismiss(dialog: DialogInterface?) {
        dismissEvents.onNext(Unit)
        super.onDismiss(dialog)
    }

    override fun onDestroy() {
        dismissEvents.onCompleted()
        super.onDestroy()
    }

    companion object {
        val ARG_KEY_CONNECTION_ERROR_CODE = "location_error_dialog_fragment.arg_connection_error_code"
        val ARG_KEY_CONNECTION_ERROR_RESOLUTION_REQUEST_CODE = "location_error_dialog_fragment.arg_connection_error_resolution_request_code"

        fun instantiate(connectionErrorCode: Int, errorResolutionRequestCode: Int): LocationErrorDialogFragment =
                Bundle()
                        .apply { this.putInt(ARG_KEY_CONNECTION_ERROR_CODE, connectionErrorCode) }
                        .apply { this.putInt(ARG_KEY_CONNECTION_ERROR_RESOLUTION_REQUEST_CODE, errorResolutionRequestCode) }
                        .let { bundle -> LocationErrorDialogFragment().apply { arguments = bundle } }
    }
}
