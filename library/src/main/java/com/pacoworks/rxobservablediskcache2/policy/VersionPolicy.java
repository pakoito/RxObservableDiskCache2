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
 * Policy class using versioning for invalidation.
 * <p/>
 * It checks whether the version of the Value matches the expected one.
 * 
 * @author pakoito
 */
public class VersionPolicy {
    public final int version;

    VersionPolicy(int version) {
        this.version = version;
    }

    /**
     * Creation function to pass to {@link RxObservableDiskCache}
     * 
     * @param version version of the Value
     * @return creation function
     */
    public static <T> Function<T, VersionPolicy> create(final int version) {
        return new Function<T, VersionPolicy>() {
            @Override
            public VersionPolicy apply(T t) {
                return new VersionPolicy(version);
            }
        };
    }

    /**
     * Validation function to pass to {@link RxObservableDiskCache}
     *
     * @param expectedVersion expected version to pass validation
     * @return validation function
     */
    public static Predicate<VersionPolicy> validate(final int expectedVersion) {
        return new Predicate<VersionPolicy>() {
            @Override
            public boolean test(VersionPolicy myPolicy) {
                return myPolicy.version == expectedVersion;
            }
        };
    }
}
