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
import java.util.UUID;
import org.jetbrains.annotations.NotNull;

/**
 * Represents the history of a Mojang account, including the account's
 * {@link UUID}, all of the names, and the times that the respective names were
 * changed.
 */
public interface AccountHistory extends Iterable<Map.Entry<Long, String>> {
    
    /**
     * Gets the {@link UUID} for this {@link AccountHistory}.
     * 
     * @return The {@link UUID} for this {@link AccountHistory}.
     */
    @NotNull
    UUID getUniqueId();
    
    /**
     * Gets the original name stored in this {@link AccountHistory}.
     * 
     * @return The original (first) name stored in this {@link AccountHistory}.
     */
    @NotNull
    String getOriginalName();
    
    /**
     * Gets the current name stored in this {@link AccountHistory}.
     * 
     * @return The current (latest) name stored in this {@link AccountHistory}.
     */
    @NotNull
    String getCurrentName();
    
    /**
     * Gets the name at the given timestamp.
     * <p>
     * If the given timestamp is <= 0, then the original name will be returned.
     * If the given timestamp is > the current time, the current name will be
     * returned.
     * 
     * @param timestamp The timestamp of the name to retrieve.
     * @return The name at the given timestamp.
     */
    @NotNull
    String getNameAt(final long timestamp);
    
    /**
     * Gets an {@link Iterator} over the names and change times stored in this
     * {@link AccountHistory}, ordered by the timestamp in increasing order
     * (oldest to newest).
     * 
     * @return An {@link Iterator} over the names and change times stored in
     *         this {@link AccountHistory}.
     */
    @Override
    @NotNull
    Iterator<Map.Entry<Long, String>> iterator();
}
