/*
 * Copyright (c) 2008-2012, Hazel Bilisim Ltd. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.web.tomcat;

public class LocalRequestId {

    private static final ThreadLocal<Long> requestId = new ThreadLocal<Long>();

    public static void set(long id) {
        requestId.set(id);
    }

    public static Long get() {
        return requestId.get();
    }

    public static void reset() {
        requestId.remove();
    }
}
