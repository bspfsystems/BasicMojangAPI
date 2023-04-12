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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import javax.net.ssl.HttpsURLConnection;
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
 * <ul>
 * <li>Username to UUID
 * <li>Usernames to UUIDs
 * <li>UUID to Username
 * </ul>
 * <p>
 * Other features of the Mojang API are not implemented here; this
 * implementation is designed to be a basic implementation to translate between
 * player's UUIDs and their names.
 */
public final class BasicMojangAPI {
    
    private static final String BASE_URL = "https://api.mojang.com/";
    
    private static final String USERNAME_TO_UUID = "users/profiles/minecraft/<username>";
    private static final String USERNAMES_TO_UUIDS = "profiles/minecraft";
    private static final String UUID_TO_USERNAME = "user/profile/<uuid>";
    
    private static final String POST_PROPERTY = "application/json";
    
    /**
     * Prevents the {@link BasicMojangAPI} from being instantiated.
     */
    private BasicMojangAPI() {
        // Do nothing besides preventing instances of BasicMojangAPI.
    }
    
    /**
     * Gets the {@link Account} data for the given username, containing the
     * case-corrected username and the associated {@link UUID}. This will use a
     * connection timeout of 30 seconds and a read timeout of 30 seconds.
     * 
     * @param username The username of the {@link Account} to retrieve.
     * @return The {@link Account} containing the case-corrected username and
     *         {@link UUID}.
     * @throws IllegalArgumentException If there was an error retrieving the
     *                                  {@link Account} from the Mojang API.
     * @throws IOException If there was an error retrieving the {@link Account}
     *                     from the Mojang API.
     * @throws SecurityException If there was an error retrieving the
     *                           {@link Account} from the Mojang API.
     * @see BasicMojangAPI#usernameToAccount(String, int, int)
     */
    @NotNull
    public static Account usernameToAccount(@NotNull final String username) throws IllegalArgumentException, IOException, SecurityException {
        return BasicMojangAPI.usernameToAccount(username, 30000, 30000);
    }
    
    /**
     * Gets the {@link Account} data for the given username, containing the
     * case-corrected username and the associated {@link UUID}. This will use
     * the given connection and read timeouts for their respective purposes.
     * 
     * @param username The username of the {@link Account} to retrieve.
     * @param connectTimeout The connection timeout to use, in milliseconds.
     * @param readTimeout The read timeout to use, in milliseconds.
     * @return The {@link Account} containing the case-corrected username and
     *         {@link UUID}.
     * @throws IllegalArgumentException If there was an error retrieving the
     *                                  {@link Account} from the Mojang API.
     * @throws IOException If there was an error retrieving the {@link Account}
     *                     from the Mojang API.
     * @throws SecurityException If there was an error retrieving the
     *                           {@link Account} from the Mojang API.
     */
    @NotNull
    public static Account usernameToAccount(@NotNull final String username, final int connectTimeout, final int readTimeout) throws IllegalArgumentException, IOException, SecurityException {
        
        final URL url = new URL(BasicMojangAPI.BASE_URL + BasicMojangAPI.USERNAME_TO_UUID.replace("<username>", username));
        final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);
        connection.setRequestMethod("GET");
        
        final int responseCode;
        try {
            responseCode = connection.getResponseCode();
            if (responseCode == 204) {
                throw new IOException("No content returned for username " + username + ".");
            }
        } catch (SocketTimeoutException e) {
            throw new IOException("No response code retrieved for username " + username + ". Connection timeout was " + connectTimeout + ", read timeout was " + readTimeout + ".", e);
        }
        
        final JSONObject responseData;
        try {
            responseData = JSONParser.deserializeObject(BasicMojangAPI.readData(connection.getInputStream()));
        } catch (JSONException e) {
            throw new IOException("Unable to parse data from the Mojang API for username " + username + ".", e);
        } catch (SocketTimeoutException e) {
            throw new IOException("No account data retrieved for username " + username + ". Connection timeout was " + connectTimeout + ", read timeout was " + readTimeout + ".", e);
        }
        if (responseData == null) {
            throw new IOException("No deserialized data returned for username " + username + ".");
        }
        
        if (responseCode == 400) {
            throw new IOException(responseData.getString("errorMessage", "An invalid parameter was given."));
        }
        
        return new SimpleAccount(responseData);
    }
    
    /**
     * Gets a {@link List} of {@link Account} data from the Mojang API, given a
     * {@link List} of usernames. The usernames in the respective
     * {@link Account Accounts} will be case-corrected. This will use a
     * connection timeout of 30 seconds and a read timeout of 30 seconds.
     * <p>
     * The supplied {@link List} may contain duplicates (Mojang usernames are
     * unique from a case-insensitive aspect, so {@code "example"} and
     * {@code "EXAMPLE"} cannot both be used at the same time). How Mojang
     * handles duplicates in the submitted data may change at any time.
     * <p>
     * Depending on how the Mojang API handles duplicates, there may or may not
     * be duplicate {@link Account Accounts} in the returned {@link List}.
     * <p>
     * The supplied {@link List} may not contain more than 10 entries, and none
     * of the entries may be {@code null}. Otherwise, an
     * {@link IllegalArgumentException} will be thrown.
     * 
     * @param usernames The {@link List} of usernames to retrieve.
     * @return The {@link List} of {@link Account Accounts} returned from the
     *         Mojang API.
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
     * @see BasicMojangAPI#usernamesToAccounts(List, int, int)
     */
    @NotNull
    public static List<Account> usernamesToAccounts(@NotNull final List<String> usernames) throws IllegalArgumentException, IOException, SecurityException, IllegalStateException, NullPointerException {
        return BasicMojangAPI.usernamesToAccounts(usernames, 30000, 30000);
    }
    
    /**
     * Gets a {@link List} of {@link Account} data from the Mojang API, given a
     * {@link List} of usernames. The usernames in the respective
     * {@link Account Accounts} will be case-corrected. This will use the given
     * connection and read timeouts for their respective purposes.
     * <p>
     * The supplied {@link List} may contain duplicates (Mojang usernames are
     * unique from a case-insensitive aspect, so {@code "example"} and
     * {@code "EXAMPLE"} cannot both be used at the same time). How Mojang
     * handles duplicates in the submitted data may change at any time.
     * <p>
     * Depending on how the Mojang API handles duplicates, there may or may not
     * be duplicate {@link Account Accounts} in the returned {@link List}.
     * <p>
     * The supplied {@link List} may not contain more than 10 entries, and none
     * of the entries may be {@code null}. Otherwise, an
     * {@link IllegalArgumentException} will be thrown.
     * 
     * @param usernames The {@link List} of usernames to retrieve.
     * @param connectTimeout The connection timeout to use, in milliseconds.
     * @param readTimeout The read timeout to use, in milliseconds.
     * @return The {@link List} of {@link Account Accounts} returned from the
     *         Mojang API.
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
    public static List<Account> usernamesToAccounts(@NotNull final List<String> usernames, final int connectTimeout, final int readTimeout) throws IllegalArgumentException, IOException, SecurityException, IllegalStateException, NullPointerException {
        
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
        final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);
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
        } catch (SocketTimeoutException e) {
            throw new IOException("No account data retrieved for multiple usernames. Connection timeout was " + connectTimeout + ", read timeout was " + readTimeout + ".", e);
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
     * Gets the {@link Account} data for the given {@link UUID}, containing the
     * {@link UUID} and the associated case-corrected username. This will use a
     * connection timeout of 30 seconds and a read timeout of 30 seconds.
     * 
     * @param uniqueId The {@link UUID} of the {@link Account} to retrieve.
     * @return The associated {@link Account} containing the {@link UUID} and
     *         case-corrected username.
     * @throws IllegalArgumentException If there was an error retrieving the
     *                                  {@link Account} from the Mojang API.
     * @throws IOException If there was an error retrieving the {@link Account}
     *                     from the Mojang API.
     * @throws SecurityException If there was an error retrieving the
     *                           {@link Account} from the Mojang API.
     * @see BasicMojangAPI#uniqueIdToAccount(UUID, int, int)
     */
    @NotNull
    public static Account uniqueIdToAccount(@NotNull final UUID uniqueId) throws IllegalArgumentException, IOException, SecurityException {
        return BasicMojangAPI.uniqueIdToAccount(uniqueId, 30000, 30000);
    }
    
    
    
    
    /**
     * Gets the {@link Account} data for the given {@link UUID}, containing the
     * {@link UUID} and the associated case-corrected username. This will use a
     * connection timeout of 30 seconds and a read timeout of 30 seconds.
     * 
     * @param uniqueId The {@link UUID} of the {@link Account} to retrieve.
     * @param connectTimeout The connection timeout to use, in milliseconds.
     * @param readTimeout The read timeout to use, in milliseconds.
     * @return The associated {@link Account} containing the {@link UUID} and
     *         case-corrected username.
     * @throws IllegalArgumentException If there was an error retrieving the
     *                                  {@link Account} from the Mojang API.
     * @throws IOException If there was an error retrieving the {@link Account}
     *                     from the Mojang API.
     * @throws SecurityException If there was an error retrieving the
     *                           {@link Account} from the Mojang API.
     */
    @NotNull
    public static Account uniqueIdToAccount(@NotNull final UUID uniqueId, final int connectTimeout, final int readTimeout) throws IllegalArgumentException, IOException, SecurityException {
        
        final URL url = new URL(BasicMojangAPI.BASE_URL + BasicMojangAPI.UUID_TO_USERNAME.replace("<uuid>", uniqueId.toString()));
        final HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
        connection.setConnectTimeout(connectTimeout);
        connection.setReadTimeout(readTimeout);
        connection.setRequestMethod("GET");
        
        final int responseCode;
        try {
            responseCode = connection.getResponseCode();
            if (responseCode == 204) {
                throw new IOException("No content returned for UUID " + uniqueId.toString() + ".");
            }
        } catch (SocketTimeoutException e) {
            throw new IOException("No response code retrieved for UUID " + uniqueId.toString() + ". Connection timeout was " + connectTimeout + ", read timeout was " + readTimeout + ".", e);
        }
        
        final JSONObject responseData;
        try {
            responseData = JSONParser.deserializeObject(BasicMojangAPI.readData(connection.getInputStream()));
        } catch (JSONException e) {
            throw new IOException("Unable to parse data from the Mojang API for UUID " + uniqueId.toString() + ".", e);
        } catch (SocketTimeoutException e) {
            throw new IOException("No account data retrieved for UUID " + uniqueId.toString() + ". Connection timeout was " + connectTimeout + ", read timeout was " + readTimeout + ".", e);
        }
        if (responseData == null) {
            throw new IOException("No deserialized data returned for UUID " + uniqueId.toString() + ".");
        }
        
        if (responseCode == 400) {
            throw new IOException(responseData.getString("errorMessage", "An invalid parameter was given."));
        }
        
        return new SimpleAccount(responseData);
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
}
