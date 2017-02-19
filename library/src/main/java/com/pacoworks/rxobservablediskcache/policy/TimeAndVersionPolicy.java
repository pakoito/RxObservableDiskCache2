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

package com.pacoworks.rxobservablediskcache.policy;

import com.pacoworks.rxobservablediskcache.RxObservableDiskCache;

import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;


/**
 * Policy class using timestamping and versioning for invalidation.
 * <p/>
 * It checks whether the Value has been stored less than a maximum caching time, and that the
 * version of the Value matches the expected one.
 *
 * @author pakoito
 */
public class TimeAndVersionPolicy {
    public final long timestamp;

    public final int version;

    TimeAndVersionPolicy(final long timestampMillis, final int version) {
        this.timestamp = timestampMillis;
        this.version = version;
    }

    /**
     * Creation function to pass to {@link RxObservableDiskCache}
     * <p/>
     * It uses {@link System#currentTimeMillis()} internally.
     * 
     * @param version version of the Value
     * @return creation function
     */
    public static <T> Function<T, TimeAndVersionPolicy> create(final int version) {
        return new Function<T, TimeAndVersionPolicy>() {
            @Override
            public TimeAndVersionPolicy apply(T t) {
                return new TimeAndVersionPolicy(System.currentTimeMillis(), version);
            }
        };
    }

    /**
     * Creation function to pass to {@link RxObservableDiskCache}
     *
     * @param timestampMillis timestamp in milliseconds
     * @param version version of the Value @return creation function
     */
    public static <T> Function<T, TimeAndVersionPolicy> create(final long timestampMillis,
            final int version) {
        return new Function<T, TimeAndVersionPolicy>() {
            @Override
            public TimeAndVersionPolicy apply(T t) {
                return new TimeAndVersionPolicy(timestampMillis, version);
            }
        };
    }

    /**
     * Validation function to pass to {@link RxObservableDiskCache}
     *
     * @param maxCacheDurationMillis maximum caching time allowed
     * @param expectedVersion expected version to pass validation
     * @return validation function
     */
    public static Predicate<TimeAndVersionPolicy> validate(final long maxCacheDurationMillis,
            final int expectedVersion) {
        return new Predicate<TimeAndVersionPolicy>() {
            @Override
            public boolean test(TimeAndVersionPolicy myPolicy) {
                final boolean isTimeCorrect = System.currentTimeMillis() - myPolicy.timestamp < maxCacheDurationMillis;
                final boolean isVersionCorrect = myPolicy.version == expectedVersion;
                return isTimeCorrect && isVersionCorrect;
            }
        };
    }
}
