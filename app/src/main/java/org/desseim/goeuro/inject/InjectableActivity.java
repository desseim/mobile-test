package org.desseim.goeuro.inject;

import android.app.Application;
import android.support.v7.app.AppCompatActivity;

import org.desseim.goeuro.GoEuroApplication;

import java.util.Objects;

public class InjectableActivity extends AppCompatActivity {
    private ActivityComponent component;

    protected ActivityComponent component() {
        if (component == null) {
            component = createActivityComponent();
        }
        return component;
    }

    private ActivityComponent createActivityComponent() {
        final Application application = getApplication();
        Objects.requireNonNull(application);
        if (!(application instanceof GoEuroApplication)) {
            throw new RuntimeException("Unexpected application type: " + application.getClass().getCanonicalName());
        }

        return DaggerActivityComponent.builder()
                .applicationComponent(((GoEuroApplication)application).component())
                .activityModule(new ActivityModule(this))
                .build();
    }
}
