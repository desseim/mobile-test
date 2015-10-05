package org.desseim.goeuro.inject

import android.app.Activity
import android.app.Application
import android.app.Fragment
import android.app.FragmentManager
import android.content.res.Resources
import dagger.Module
import dagger.Provides

@Module
class ApplicationModule(val application: Application) {
    @Provides fun provideApplication(): Application {
        return application
    }

    @Provides fun provideApplicationResources(application: Application): Resources = application.getResources()
}

@Module
class ActivityModule(val activity: Activity) {
    @Provides @PerActivity fun provideFragmentManager(activity: Activity): FragmentManager = activity.getFragmentManager()

    @Provides @PerActivity fun provideActivity(): Activity = activity
}

@Module
class FragmentModule(val fragment: Fragment) {
    @Provides @PerFragment fun provideChildFragmentManager(fragment: Fragment) = fragment.getChildFragmentManager()

    @Provides @PerFragment fun provideFragment() = fragment
}