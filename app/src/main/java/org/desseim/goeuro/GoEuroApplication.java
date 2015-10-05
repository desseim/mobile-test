package org.desseim.goeuro;

import android.app.Application;

import org.desseim.goeuro.inject.ApplicationComponent;
import org.desseim.goeuro.inject.ApplicationModule;
import org.desseim.goeuro.inject.DaggerApplicationComponent;

public class GoEuroApplication extends Application {
    private ApplicationComponent component;

    @Override
    public void onCreate() {
        super.onCreate();

        component().inject(this);
    }

    private ApplicationComponent createApplicationComponent() {
        return DaggerApplicationComponent
                .builder()
                .applicationModule(new ApplicationModule(this))
                .build();
    }

    public ApplicationComponent component() {
        if (component == null) {
            component = createApplicationComponent();
        }
        return component;
    }
}