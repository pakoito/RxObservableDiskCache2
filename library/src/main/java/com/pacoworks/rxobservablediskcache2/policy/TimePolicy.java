/*
 * Copyright (c) pakoito 2017
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

package com.pacoworks.rxobservablediskcache2.policy;

import com.pacoworks.rxobservablediskcache2.RxObservableDiskCache;

import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;


/**
 * Policy class using timestamping for invalidation.
 * <p/>
 * It checks whether the Value has been stored less than a maximum caching time.
 *
 * @author pakoito
 */
public class TimePolicy {
    public final long timestamp;

    TimePolicy() {
        timestamp = System.currentTimeMillis();
    }

    TimePolicy(long timestampMillis) {
        this.timestamp = timestampMillis;
    }

    /**
     * Creation function to pass to {@link RxObservableDiskCache}
     * <p/>
     * It uses {@link System#currentTimeMillis()} internally.
     *
     * @return creation function
     */
    public static <T> Function<T, TimePolicy> create() {
        return new Function<T, TimePolicy>() {
            @Override
            public TimePolicy apply(T t) {
                return new TimePolicy();
            }
        };
    }

    /**
     * Creation function to pass to {@link RxObservableDiskCache}
     * 
     * @param timestampMillis timestamp in milliseconds
     * @return creation function
     */
    public static <T> Function<T, TimePolicy> create(final long timestampMillis) {
        return new Function<T, TimePolicy>() {
            @Override
            public TimePolicy apply(T t) {
                return new TimePolicy(timestampMillis);
            }
        };
    }

    /**
     * Validation function to pass to {@link RxObservableDiskCache}
     *
     * @param maxCacheDurationMillis maximum caching time allowed
     * @return validation function
     */
    public static Predicate<TimePolicy> validate(final long maxCacheDurationMillis) {
        return new Predicate<TimePolicy>() {
            @Override
            public boolean test(TimePolicy myPolicy) {
                return System.currentTimeMillis() - myPolicy.timestamp < maxCacheDurationMillis;
            }
        };
    }
}
