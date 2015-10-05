package org.desseim.goeuro.inject;

import android.content.res.Resources;

import org.desseim.goeuro.rest.PlaceService;

import dagger.Component;

@PerActivity
@Component(
        dependencies = ApplicationComponent.class,
        modules = ActivityModule.class
)
public interface ActivityComponent {
    Resources resources();

    PlaceService locationService();
}
