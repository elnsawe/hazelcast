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

package com.hazelcast.client.impl;

import com.hazelcast.client.Call;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.Packet;
import com.hazelcast.client.ProxyHelper;
import com.hazelcast.core.ItemEventType;
import com.hazelcast.core.ItemListener;
import com.hazelcast.impl.ClusterOperation;
import com.hazelcast.impl.DataAwareItemEvent;
import com.hazelcast.nio.Data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.hazelcast.client.IOUtil.toObject;

public class QueueItemListenerManager {
    final private ConcurrentHashMap<String, List<ItemListener>> queueItemListeners = new ConcurrentHashMap<String, List<ItemListener>>();

    public QueueItemListenerManager() {
    }

    public Collection<? extends Call> calls(HazelcastClient client) {
        final List<Call> calls = new ArrayList<Call>();
        for (final String name : queueItemListeners.keySet()) {
            final ProxyHelper proxyHelper = new ProxyHelper(name, client);
            calls.add(createNewAddItemListenerCall(proxyHelper));
        }
        return calls;
    }

    public Call createNewAddItemListenerCall(ProxyHelper proxyHelper) {
        Packet request = proxyHelper.createRequestPacket(ClusterOperation.ADD_LISTENER, null, null);
        request.setLongValue(1);
        return proxyHelper.createCall(request);
    }

    public void notifyListeners(Packet packet) {
        List<ItemListener> list = queueItemListeners.get(packet.getName());
        if (list != null) {
            for (ItemListener<Object> listener : list) {
                Boolean added = (Boolean) toObject(packet.getValue());
                if (added) {
                    listener.itemAdded(new DataAwareItemEvent(packet.getName(), ItemEventType.ADDED, new Data(packet.getKey())));
                } else {
                    listener.itemRemoved(new DataAwareItemEvent(packet.getName(), ItemEventType.ADDED, new Data(packet.getKey())));
                }
            }
        }
    }

    public <E> void removeListener(String name, ItemListener<E> listener) {
        if (!queueItemListeners.containsKey(name)) {
            return;
        }
        queueItemListeners.get(name).remove(listener);
        if (queueItemListeners.get(name).isEmpty()) {
            queueItemListeners.remove(name);
        }
    }

    public <E> void registerListener(String name, ItemListener<E> listener) {
        List<ItemListener> newListenersList = new CopyOnWriteArrayList<ItemListener>();
        List<ItemListener> listeners = queueItemListeners.putIfAbsent(name, newListenersList);
        if (listeners == null) {
            listeners = newListenersList;
        }
        listeners.add(listener);
    }

    public boolean noListenerRegistered(String name) {
        if (!queueItemListeners.containsKey(name)) {
            return true;
        }
        return queueItemListeners.get(name).isEmpty();
    }
}
