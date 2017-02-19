/*
 * Copyright (c) pakoito 2016
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.pacoworks.rxobservablediskcache;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.pacoworks.rxpaper2.RxPaperBook;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.observers.TestObserver;


@RunWith(AndroidJUnit4.class)
public class RxObservableDiskCacheTest {
    private static final String KEY = "test_key";

    @Rule
    public final ActivityTestRule<MainActivity> activity = new ActivityTestRule<>(
            MainActivity.class);

    private RxPaperBook testBook;

    @Before
    public void setUp() {
        RxPaperBook.init(activity.getActivity());
        testBook = RxPaperBook.with("test_book");
        testBook.destroy().subscribe();
    }

    public TestObserver initCache() {
        final List<Serializable> initialList = Arrays.<Serializable> asList(true, 1, "hello");
        final TestObserver<Cached<List<Serializable>, MyPolicy>> subscriber = TestObserver
                .create();
        RxObservableDiskCache.transform(Single.just(initialList), KEY, testBook,
                new Function<List<Serializable>, MyPolicy>() {
                    @Override
                    public MyPolicy apply(List<Serializable> serializables) {
                        return new MyPolicy();
                    }
                }, new Predicate<MyPolicy>() {
                    @Override
                    public boolean test(MyPolicy myPolicy) {
                        return true;
                    }
                }).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        return subscriber;
    }

    @Test
    public void emptyCache_cacheFail_getObservable() {
        final TestObserver subscriber = initCache();
        /* Assert */
        subscriber.assertNoErrors();
        subscriber.assertComplete();
        subscriber.assertValueCount(1);
    }

    @Test
    public void emptyCache_observableFails_getException() {
        final TestObserver<Cached<Integer, MyPolicy>> subscriber = TestObserver.create();
        /* Act */
        RxObservableDiskCache.transform(Single.<Integer> error(new IllegalStateException()), KEY,
                testBook, new Function<Integer, MyPolicy>() {
                    @Override
                    public MyPolicy apply(Integer serializables) {
                        return new MyPolicy();
                    }
                }, new Predicate<MyPolicy>() {
                    @Override
                    public boolean test(MyPolicy myPolicy) {
                        return true;
                    }
                }).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        /* Assert */
        subscriber.assertValueCount(0);
        subscriber.assertError(IllegalStateException.class);
    }

    @Test
    public void validCache_cacheHit_getCacheThenGetObservable() {
        initCache();
        final List<Serializable> list = Arrays.<Serializable> asList(true, 1, "hello");
        final TestObserver<Cached<List<Serializable>, MyPolicy>> subscriber = TestObserver
                .create();
        /* Act */
        RxObservableDiskCache.transform(Single.just(list), KEY, testBook,
                new Function<List<Serializable>, MyPolicy>() {
                    @Override
                    public MyPolicy apply(List<Serializable> serializables) {
                        return new MyPolicy();
                    }
                }, new Predicate<MyPolicy>() {
                    @Override
                    public boolean test(MyPolicy myPolicy) {
                        return true;
                    }
                }).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        /* Assert */
        subscriber.assertNoErrors();
        subscriber.assertComplete();
        subscriber.assertValueCount(2);
    }

    @Test
    public void validCache_cacheMiss_deleteCacheThenGetObservable() {
        initCache();
        final List<Serializable> list = Arrays.<Serializable> asList(true, 1, "hello");
        final TestObserver<Cached<List<Serializable>, MyPolicy>> subscriber = TestObserver
                .create();
        /* Act */
        RxObservableDiskCache.transform(Single.just(list), KEY, testBook,
                new Function<List<Serializable>, MyPolicy>() {
                    @Override
                    public MyPolicy apply(List<Serializable> serializables) {
                        return new MyPolicy();
                    }
                }, new Predicate<MyPolicy>() {
                    @Override
                    public boolean test(MyPolicy myPolicy) {
                        return false;
                    }
                }).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        /* Assert */
        subscriber.assertNoErrors();
        subscriber.assertComplete();
        subscriber.assertValueCount(1);
        Assert.assertTrue(testBook.exists(KEY).blockingGet());
    }

    @Test
    public void validCache_cacheHitAndObservableFails_getCacheThenGetException() {
        initCache();
        final TestObserver<Cached<Integer, MyPolicy>> subscriber = TestObserver.create();
        RxObservableDiskCache.transform(Single.<Integer> error(new IllegalStateException()), KEY,
                testBook, new Function<Integer, MyPolicy>() {
                    @Override
                    public MyPolicy apply(Integer serializables) {
                        return new MyPolicy();
                    }
                }, new Predicate<MyPolicy>() {
                    @Override
                    public boolean test(MyPolicy myPolicy) {
                        return true;
                    }
                }).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        /* Assert */
        subscriber.assertValueCount(1);
        subscriber.assertError(IllegalStateException.class);
    }

    @Test
    public void validCache_cacheMissAndObservableFails_deleteCacheThenGetException() {
        initCache();
        final TestObserver<Cached<Integer, MyPolicy>> subscriber = TestObserver.create();
        RxObservableDiskCache.transform(Single.<Integer> error(new IllegalStateException()), KEY,
                testBook, new Function<Integer, MyPolicy>() {
                    @Override
                    public MyPolicy apply(Integer serializables) {
                        return new MyPolicy();
                    }
                }, new Predicate<MyPolicy>() {
                    @Override
                    public boolean test(MyPolicy myPolicy) {
                        return false;
                    }
                }).subscribe(subscriber);
        subscriber.awaitTerminalEvent();
        /* Assert */
        subscriber.assertValueCount(0);
        subscriber.assertError(IllegalStateException.class);
        Assert.assertFalse(testBook.exists(KEY).blockingGet());
    }
}
