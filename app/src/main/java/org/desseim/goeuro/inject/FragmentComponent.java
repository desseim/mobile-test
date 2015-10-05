package org.desseim.goeuro.inject;

import org.desseim.goeuro.ui.search.TravelSearchFragment;

import dagger.Component;

@PerFragment
@Component(
        dependencies = ActivityComponent.class,
        modules = FragmentModule.class
)
public interface FragmentComponent {
    TravelSearchFragment inject(TravelSearchFragment travelSearchFragment);
}
