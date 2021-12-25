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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bspfsystems.simplejson.JSONArray;
import org.bspfsystems.simplejson.JSONObject;
import org.bspfsystems.simplejson.SimpleJSONArray;
import org.bspfsystems.simplejson.parser.JSONException;
import org.bspfsystems.simplejson.parser.JSONParser;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a utility class to interact with basic parts of the Mojang API
 * using Java.
 * <p>
 * The implemented API features include:
 * <li><a href="https://wiki.vg/Mojang_API#Username_to_UUID>Username to UUID</a></li>
 * <li><a href="https://wiki.vg/Mojang_API/Usernames_to_UUIDs>Usernames to UUIDs</a></li>
 * <li><a href="https://wiki.vg/Mojang_API/UUID_to_Name_History>UUID to Name History</a></li>
 * <p>
 * Other features of the Mojang API are not implemented here; this
 * implementation is designed to be a basic implementation to translate between
 * player's UUIDs and their names.
 */
public final class BasicMojangAPI {
    
    private static final String BASE_URL = "https://api.mojang.com/";
    
    private static final String USERNAME_TO_UUID = "users/profiles/minecraft/<username>?at=<timestamp>";
    private static final String USERNAMES_TO_UUIDS = "profiles/minecraft";
    private static final String UUID_TO_NAME_HISTORY = "user/profiles/<uuid>/names";
    
    private static final String POST_PROPERTY = "application/json";
    
    /**
     * Prevents the {@link BasicMojangAPI} from being instantiated.
     */
    private BasicMojangAPI() {
        
    }
    
    /**
     * Gets the {@link Account} data, containing the case-corrected username and
     * the associated {@link UUID}. This will use the current time as the
     * optional timestamp parameter.
     * <p>
     * PLEASE NOTE: Since November 2020, Mojang stopped supporting the timestamp
     * parameter. If a timestamp is provided, it is effectively ignored, and the
     * current time is used instead. This means that a username that has been
     * used on multiple accounts will return only the current {@link UUID} (if
     * any) that is associated with the given username. Please remind Mojang to
     * fix this issue here:
     * <a href="https://bugs.mojang.com/browse/WEB-3367">WEB-3367</a>
     * 
     * @param username The username of the account to retrieve.
     * @return The {@link Account} associated with the given username, including
     *         the case-corrected username and the {@link UUID}.
     * @throws IllegalArgumentException If there was an error retrieving the
     *                                  {@link Account} from the Mojang API.
     * @throws IOException If there was an error retrieving the {@link Account}
     *                     from the Mojang API.
     * @throws SecurityException If there was an error retrieving the
     *                           {@link Account} from the Mojang API.
     * @see BasicMojangAPI#usernameToUniqueId(String, long)
     */
    @NotNull
    public static Account usernameToUniqueId(@NotNull final String username) throws IllegalArgumentException, IOException, SecurityException {
        return BasicMojangAPI.usernameToUniqueId(username, System.currentTimeMillis());
    }
    
    /**
     * Gets the {@link Account} data, containing the case-corrected username and
     * the associated {@link UUID}. This will use the given time as the optional
     * timestamp parameter.
     * <p>
     * PLEASE NOTE: Since November 2020, Mojang stopped supporting the timestamp
     * parameter. If a timestamp is provided, it is effectively ignored, and the
     * current time is used instead. This means that a username that has been
     * used on multiple accounts will return only the current {@link UUID} (if
     * any) that is associated with the given username. Please remind Mojang to
     * fix this issue here:
     * <a href="https://bugs.mojang.com/browse/WEB-3367">WEB-3367</a>
     *
     * @param username The username of the account to retrieve.
     * @param timestamp The timestamp to use. The milliseconds portion of the
     *                  timestamp will be ignored, as the API uses the UNIX
     *                  timestamp without milliseconds.
     * @return The {@link Account} associated with the given username, including
     *         the case-corrected username and the {@link UUID}..
     * @throws IllegalArgumentException If there was an error retrieving the
     *                                  {@link Account} from the Mojang API.
     * @throws IOException If there was an error retrieving the {@link Account}
     *                     from the Mojang API.
     * @throws SecurityException If there was an error retrieving the
     *                           {@link Account} from the Mojang API.
     */
    @NotNull
    public static Account usernameToUniqueId(@NotNull final String username, final long timestamp) throws IllegalArgumentException, IOException, SecurityException {
        
        final URL url = new URL(BasicMojangAPI.BASE_URL + BasicMojangAPI.USERNAME_TO_UUID.replace("<username>", username).replace("<timestamp>", String.valueOf(timestamp / 1000L)));
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        final int responseCode = connection.getResponseCode();
        if (responseCode == 204) {
            throw new IOException("No content returned for username " + username + " at timestamp " + timestamp + ".");
        }
        
        final JSONObject responseData;
        try {
            responseData = JSONParser.deserializeObject(BasicMojangAPI.readData(connection.getInputStream()));
        } catch (JSONException e) {
            throw new IOException("Unable to parse data from the Mojang API for username " + username + " at timestamp " + timestamp + ".", e);
        }
        if (responseData == null) {
            throw new IOException("No deserialized data returned for username " + username + " at timestamp " + timestamp + ".");
        }
        
        if (responseCode == 400) {
            throw new IOException(responseData.getString("errorMessage", "An invalid parameter was given."));
        }
        
        return new SimpleAccount(responseData);
    }
    
    /**
     * Gets a {@link List} of {@link Account} data from the Mojang API, given a
     * {@link List} of usernames. The usernames in the respective
     * {@link Account}s will be case-corrected.
     * <p>
     * The supplied {@link List} may contain duplicates (Mojang usernames are
     * unique from a case-insensitive aspect, so {@code "example"} and
     * {@code "EXAMPLE"} cannot both be used at the same time). How Mojang
     * handles duplicates in the submitted data may change at any time.
     * <p>
     * Depending on how the Mojang API handles duplicates, there may or may not
     * be duplicate {@link Account}s in the returned {@link List}.
     * <p>
     * The supplied {@link List} may not contain more than 10 entries, and none
     * of the entries may be {@code null}. Otherwise, an
     * {@link IllegalArgumentException} will be thrown.
     * 
     * @param usernames The {@link List} of usernames to retrieve.
     * @return The {@link List} of {@link Account}s returned from the Mojang
     *         API.
     * @throws IllegalArgumentException If there was an error retrieving the
     *                                  {@link List} of {@link Account Accounts}
     *                                  from the Mojang API.
     * @throws IOException If there was an error retrieving the {@link List} of
     *                     {@link Account Accounts} from the Mojang API.
     * @throws SecurityException If there was an error retrieving the
     *                           {@link List} of {@link Account Accounts} from
     *                           the Mojang API.
     * @throws IllegalStateException If there was an error retrieving the
     *                               {@link List} of {@link Account Accounts}
     *                               from the Mojang API.
     * @throws NullPointerException If there was an error retrieving the
     *                              {@link List} of {@link Account Accounts}
     *                              from the Mojang API.
     */
    @NotNull
    public static List<Account> usernamesToUniqueIds(@NotNull final List<String> usernames) throws IllegalArgumentException, IOException, SecurityException, IllegalStateException, NullPointerException {
        
        if (usernames.size() > 10) {
            throw new IllegalArgumentException("You cannot request " + usernames.size() + " usernames (maximum 10).");
        }
        
        final JSONArray postData = new SimpleJSONArray();
        for (final String username : usernames) {
            if (username == null) {
                throw new IllegalArgumentException("You cannot request a null username.");
            }
            postData.addEntry(username);
        }
    
        final String postDataSerialized;
        try {
            postDataSerialized = JSONParser.serialize(postData);
        } catch (JSONException e) {
            throw new IOException("Unable to serialize the list of usernames.", e);
        }
        
        final byte[] postDataBytes = postDataSerialized.getBytes(StandardCharsets.UTF_8);
        
        final URL url = new URL(BasicMojangAPI.BASE_URL + BasicMojangAPI.USERNAMES_TO_UUIDS);
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", BasicMojangAPI.POST_PROPERTY + "; utf-8");
        connection.setRequestProperty("Accept", BasicMojangAPI.POST_PROPERTY);
        connection.setDoOutput(true);
        
        connection.getOutputStream().write(postDataBytes, 0, postDataBytes.length);
        
        final JSONArray responseData;
        try {
            responseData = JSONParser.deserializeArray(BasicMojangAPI.readData(connection.getInputStream()));
        } catch (JSONException e) {
            throw new IOException("Unable to parse data from the Mojang API.", e);
        }
        
        if (connection.getResponseCode() == 400) {
            throw new IOException("An invalid username was passed in as part of the request.");
        }
        
        final List<Account> accounts = new ArrayList<Account>();
        
        if (responseData == null || responseData.size() == 0) {
            return accounts;
        }
        
        for (final Object dataItem : responseData) {
            if (dataItem instanceof JSONObject) {
                accounts.add(new SimpleAccount((JSONObject) dataItem));
            } else {
                throw new IOException("Invalid account data returned.");
            }
        }
        
        return accounts;
    }
    
    /**
     * Gets the {@link AccountHistory} for the given {@link UUID} from the
     * Mojang API. The usernames in the {@link AccountHistory} will be
     * case-corrected.
     * 
     * @param uniqueId The {@link UUID} of the retrieved {@link AccountHistory}.
     * @return The retrieved {@link AccountHistory}.
     * @throws IllegalArgumentException If there was an error retrieving the
     *                                  {@link AccountHistory} from the Mojang
     *                                  API.
     * @throws IOException If there was an error retrieving the
     *                     {@link AccountHistory} from the Mojang API.
     * @throws SecurityException If there was an error retrieving the
     *                           {@link AccountHistory} from the Mojang API.
     */
    @NotNull
    public static AccountHistory uniqueIdToNameHistory(@NotNull final UUID uniqueId) throws IllegalArgumentException, IOException, SecurityException {
        
        final URL url = new URL(BasicMojangAPI.BASE_URL + BasicMojangAPI.UUID_TO_NAME_HISTORY.replace("<uuid>", uniqueId.toString()));
        final HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        
        final int responseCode = connection.getResponseCode();
        if (responseCode == 204) {
            throw new IOException("No content returned for UUID " + uniqueId.toString() + ".");
        }
        
        final JSONArray responseData;
        try {
            responseData = JSONParser.deserializeArray(BasicMojangAPI.readData(connection.getInputStream()));
        } catch (JSONException e) {
            throw new IOException("Unable to parse data from the Mojang API for UUID " + uniqueId.toString() + ".", e);
        }
        if (responseData == null) {
            throw new IOException("No deserialized data returned for UUID " + uniqueId.toString() + ".");
        }
        
        if (responseCode == 400) {
            throw new IOException("An invalid UUID (" + uniqueId.toString() + ") was passed in as part of the request.");
        }
        
        return new SimpleAccountHistory(uniqueId, responseData);
    }
    
    /**
     * Reads the data in from the given {@link InputStream} and returns the data
     * as a {@link String}.
     * 
     * @param input The {@link InputStream} to read the data from.
     * @return A {@link String} representing the data.
     * @throws IOException If an I/O error occurs while reading in the data.
     */
    @NotNull
    private static String readData(@NotNull final InputStream input) throws IOException {
        
        final BufferedReader reader = new BufferedReader(new InputStreamReader(input));
        final StringBuilder builder = new StringBuilder();
        
        String line;
        while ((line = reader.readLine()) != null) {
            builder.append(line);
        }
        
        return builder.toString();
    }
    
    /**
     * Translates a 32-character {@link UUID} from the Mojang API into a
     * {@link UUID} that will represent the 36-character version (with the "-"
     * characters in the {@link UUID}).
     * 
     * @param shortId The 32-character version of the {@link UUID} as a
     *                {@link String}.
     * @return The {@link UUID} translated from the given {@link String}.
     * @throws IllegalArgumentException See {@link UUID#fromString(String)}.
     */
    @NotNull
    static UUID translateUniqueId(@NotNull final String shortId) throws IllegalArgumentException {
        final StringBuilder builder = new StringBuilder();
        builder.append(shortId, 0, 8);
        builder.append("-");
        builder.append(shortId, 8, 12);
        builder.append("-");
        builder.append(shortId, 12, 16);
        builder.append("-");
        builder.append(shortId, 16, 20);
        builder.append("-");
        builder.append(shortId, 20, 32);
        return UUID.fromString(builder.toString());
    }
}
