package org.desseim.goeuro.ui.search

import android.app.Fragment
import android.os.Bundle
import org.desseim.goeuro.R
import org.desseim.goeuro.ui.LocationAwareActivity

public class TravelSearchActivity : LocationAwareActivity() {
    override val locationConnectionErrorResolutionRequestCode: Int = 101
    override val locationConnectionErrorDialogFragmentTag: String = "LocationAwareActivity.ConnectionErrorDialog"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_travel_search)
    }

    override fun onAttachFragment(fragment: Fragment?) {
        super.onAttachFragment(fragment)

        when(fragment) {
            is TravelSearchFragment ->
                    fragment.googleLocationApiClient = this.locationApiClient
        }
    }
}
