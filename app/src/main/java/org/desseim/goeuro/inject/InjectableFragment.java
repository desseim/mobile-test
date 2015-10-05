package org.desseim.goeuro.inject;

import android.app.Activity;
import android.app.Fragment;

import java.util.Objects;

public class InjectableFragment extends Fragment {
    private FragmentComponent component;

    protected FragmentComponent component() {
        if (component == null) {
            component = createFragmentComponent();
        }
        return component;
    }

    private FragmentComponent createFragmentComponent() {
        final Activity attachedActivity = getActivity();
        Objects.requireNonNull(attachedActivity, "Fragment not yet attached to an activity. Its component can only be accessed when it is attached to an activity.");
        if (!(attachedActivity instanceof InjectableActivity)) {
            throw new RuntimeException("Fragment needs to be attached to an injectable activity to be itself injected");
        }

        return DaggerFragmentComponent.builder()
                                      .activityComponent(((InjectableActivity)attachedActivity).component())
                                      .fragmentModule(new FragmentModule(this))
                                      .build();
    }
}
