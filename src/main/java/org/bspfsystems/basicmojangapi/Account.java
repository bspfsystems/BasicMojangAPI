/* 
 * This file is part of the BasicMojangAPI Java library.
 * 
 * Copyright 2021-2022 BSPF Systems, LLC
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
import org.jetbrains.annotations.NotNull;

/**
 * Represents the basic data about a Minecraft/Mojang account, including a
 * {@link UUID} and case-corrected username.
 */
public interface Account {
    
    /**
     * Gets the {@link UUID} for this {@link Account}.
     * 
     * @return The {@link UUID} for this {@link Account}.
     */
    @NotNull
    UUID getUniqueId();
    
    /**
     * Gets the username for this {@link Account}.
     * 
     * @return The username for this {@link Account}.
     */
    @NotNull
    String getName();
    
    /**
     * Gets if this {@link Account} is a legacy account (has not been migrated
     * to a Mojang account).
     * 
     * @return {@code true} if this {@link Account} is a legacy account,
     *         {@code false} otherwise.
     */
    boolean isLegacy();
    
    /**
     * Gets if this {@link Account} is a demo account (has not been paid yet).
     * 
     * @return {@code true} if this {@link Account} is a demo account,
     *         {@code false} otherwise.
     */
    boolean isDemo();
}
