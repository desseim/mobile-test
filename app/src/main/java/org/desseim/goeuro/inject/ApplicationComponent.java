package org.desseim.goeuro.inject;

import android.content.res.Resources;

import org.desseim.goeuro.GoEuroApplication;
import org.desseim.goeuro.rest.PlaceService;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(
        modules = {
                ApplicationModule.class,
                GoEuroApiSearchModule.class
        }
)
public interface ApplicationComponent {
    GoEuroApplication inject(GoEuroApplication application);

    Resources resources();

    PlaceService locationService();
}
