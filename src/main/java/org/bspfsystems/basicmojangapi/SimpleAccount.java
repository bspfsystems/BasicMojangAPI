/* 
 * This file is part of the BasicMojangAPI Java library.
 * 
 * Copyright 2021-2023 BSPF Systems, LLC
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

import java.util.UUID;
import org.bspfsystems.simplejson.JSONObject;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a simple implementation of an {@link Account}.
 */
final class SimpleAccount implements Account {
    
    private final UUID uniqueId;
    private final String name;
    
    /**
     * Constructs a new {@link Account} from the given {@link JSONObject} data
     * that was returned from the Mojang API.
     * 
     * @param data The {@link JSONObject} that contains the account information
     *             from the Mojang API.
     * @throws IllegalArgumentException If the given {@link JSONObject} is
     *                                  missing the minimum amount of data (the
     *                                  username and {@link UUID}).
     */
    SimpleAccount(@NotNull final JSONObject data) throws IllegalArgumentException {
        
        final String uniqueId = data.getString("id", null);
        if (uniqueId == null) {
            throw new IllegalArgumentException("Missing UUID from API JSON data.");
        } else if (uniqueId.length() != 32) {
            throw new IllegalArgumentException("UUID data is not 32 characters long: " + uniqueId);
        }
        final StringBuilder builder = new StringBuilder();
        builder.append(uniqueId, 0, 8);
        builder.append("-");
        builder.append(uniqueId, 8, 12);
        builder.append("-");
        builder.append(uniqueId, 12, 16);
        builder.append("-");
        builder.append(uniqueId, 16, 20);
        builder.append("-");
        builder.append(uniqueId, 20, 32);
        
        this.uniqueId = UUID.fromString(builder.toString());
        
        final String name = data.getString("name", null);
        if (name == null) {
            throw new IllegalArgumentException("Missing name from API JSON data for UUID: " + this.uniqueId.toString());
        } else if (name.length() < 1 || name.length() > 16) {
            throw new IllegalArgumentException("Name data is not a valid length(" + name.length() + " - " + name + ") for UUID " + this.uniqueId.toString());
        }
        this.name = name;
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
    public String getName() {
        return this.name;
    }
}
