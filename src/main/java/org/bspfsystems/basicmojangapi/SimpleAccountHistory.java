/*
 * This file is part of the BasicMojangAPI Java library.
 *
 * Copyright 2021 BSPF Systems, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.bspfsystems.basicmojangapi;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;
import org.bspfsystems.simplejson.JSONArray;
import org.bspfsystems.simplejson.JSONObject;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a simple implementation of an {@link AccountHistory}. This uses a
 * {@link TreeMap} internally to maintain order of the name history.
 */
final class SimpleAccountHistory implements AccountHistory {
    
    private final UUID uniqueId;
    private final TreeMap<Long, String> nameHistory;
    
    /**
     * Constructs a new {@link AccountHistory} from the given {@link JSONArray}
     * data that was returned from the Mojang API.
     * 
     * @param uniqueId The {@link UUID} to assign to the {@link AccountHistory}.
     * @param data The {@link JSONArray} that contains the account history data
     *             from the Mojang API.
     * @throws IllegalArgumentException If the given {@link JSONArray} is
     *                                  missing the minimum amount of data (the
     *                                  original username).
     */
    SimpleAccountHistory(@NotNull final UUID uniqueId, @NotNull final JSONArray data) throws IllegalArgumentException {
        
        this.uniqueId = uniqueId;
        this.nameHistory = new TreeMap<Long, String>();
        
        if (data.size() == 0) {
            throw new IllegalArgumentException("No data returned from the Mojang API for UUID " + this.uniqueId.toString());
        }
        
        for (final Object dataItem : data) {
            if (!(dataItem instanceof JSONObject)) {
                throw new IllegalArgumentException("Invalid entry returned from the Mojang API for UUID " + this.uniqueId.toString());
            }
            
            final JSONObject jsonData = (JSONObject) dataItem;
            final String name = jsonData.getString("name", null);
            if (name == null) {
                throw new IllegalArgumentException("Missing name from API JSON data for UUID " + this.uniqueId.toString());
            } else if (name.length() < 1 || name.length() > 16) {
                throw new IllegalArgumentException("Name data is not a valid length (" + name.length() + " - " + name + ") for UUID " + this.uniqueId.toString());
            }
            
            final long time = jsonData.getLong("changedToAt", 0L);
            if (time < 0L) {
                throw new IllegalArgumentException("Invalid name change time given (" + time + ") for UUID " + this.uniqueId.toString());
            }
            
            if (this.nameHistory.containsKey(time)) {
                throw new IllegalArgumentException("Duplicate change time and name (" + time + " - " + name + ") for UUID " + this.uniqueId.toString());
            }
            
            this.nameHistory.put(time, name);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public UUID getUniqueId() {
        return this.uniqueId;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public String getOriginalName() {
        return this.nameHistory.firstEntry().getValue();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public String getCurrentName() {
        return this.nameHistory.lastEntry().getValue();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public String getNameAt(final long timestamp) {
        
        if (timestamp <= 0L) {
            return this.getOriginalName();
        }
        
        Map.Entry<Long, String> matchEntry = null;
        for (final Map.Entry<Long, String> entry : this.nameHistory.entrySet()) {
            if (entry.getKey() <= timestamp) {
                matchEntry = entry;
                continue;
            }
            break;
        }
        
        return matchEntry == null ? this.getOriginalName() : matchEntry.getValue();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    @NotNull
    public Iterator<Map.Entry<Long, String>> iterator() {
        return this.nameHistory.entrySet().iterator();
    }
}
