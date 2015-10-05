package org.desseim.goeuro.inject

import java.lang.annotation.Retention

import javax.inject.Scope

import java.lang.annotation.RetentionPolicy.RUNTIME

/**
 * A scoping annotation to permit objects whose lifetime should
 * conform to the life of the activity to be memoized in the
 * correct component.
 * @see [Original source from dagger project](https://github.com/google/dagger/blob/master/examples/android-activity-graphs/src/main/java/com/example/dagger/activitygraphs/PerActivity.java)
 */
@Scope
@Retention(RUNTIME)
annotation class PerActivity
