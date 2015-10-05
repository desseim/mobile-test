package org.desseim.goeuro.rx

import rx.Observable
import rx.android.schedulers.AndroidSchedulers

fun <T> Observable<T>.observeOnMainThread() = this.observeOn(AndroidSchedulers.mainThread())