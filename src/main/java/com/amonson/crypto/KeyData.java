// Copyright (C) 2018-2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.crypto;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import java.io.IOException;
import com.google.gson.stream.*;
import com.google.gson.*;

/**
 * Class to create/store initialization vectors and keys for AES encryption/decryption.
 */
public class KeyData {
    private KeyData() {}

    /**
     * Constructor that takes a IV and Key Strings for AES encryption encoded with Base64.
     * @param iv The Base64 encoded initialization vector.
     * @param key The base64 encoded key.
     */
    public KeyData(String iv, String key) {
        iv_ = iv;
        key_ = key;
    }

    /**
     * Generates new strong IV and Key's for AES encryption/decryption.
     * @return The new KeyData instance.
     * @throws NoSuchAlgorithmException Thrown when the JRE platform does not support strong algorithms for
     * IV/Key generation.
     */
    public static KeyData newKeyData() throws NoSuchAlgorithmException {
        SecureRandom randomSecureRandom = SecureRandom.getInstance(getAlgorithm());
        byte[] iv = new byte[16];
        randomSecureRandom.nextBytes(iv);
        String sIv = Base64.getEncoder().encodeToString(iv);
        KeyGenerator kgen = KeyGenerator.getInstance("HmacSHA256");
        kgen.init(BITS);
        SecretKey aesKey = kgen.generateKey();
        String sKey = Base64.getEncoder().encodeToString(aesKey.getEncoded());
        return new KeyData(sIv, sKey);
    }

    public static TypeAdapter<KeyData> getGSonAdapter() {
        return new TypeAdapter<>() {
            @Override public KeyData read(JsonReader reader) throws IOException {
                KeyData data = new KeyData();
                reader.beginObject();
                while(reader.hasNext()) {
                    JsonToken token = reader.peek();
                    String fieldName = null;
                    if (token.equals(JsonToken.NAME)) {
                        //get the current token
                        fieldName = reader.nextName();
                    }

                    if ("iv".equals(fieldName) || "A".equals(fieldName)) {
                        //move to next token
                        token = reader.peek();
                        data.iv_ = reader.nextString();
                    }

                    if("key".equals(fieldName) || "B".equals(fieldName)) {
                        //move to next token
                        token = reader.peek();
                        data.key_ = reader.nextString();
                    }
                }
                reader.endObject();
                return data;
            }
            @Override public void write(JsonWriter writer, KeyData data) throws IOException {
                writer.beginObject(); 
                writer.name("A"); 
                writer.value(data.IV()); 
                writer.name("B"); 
                writer.value(data.key()); 
                writer.endObject(); 
            }
        };
    }

    /**
     * IV Accessor method.
     * @return The base64 encoded initialization vector.
     */
    public String IV() {
        return iv_;
    }

    /**
     * Key accessor method.
     * @return The base64 encoded key.
     */
    public String key() {
        return key_;
    }

    /**
     * Convert object to human-readable representation.
     * @return A human-readable string representing the object.
     */
    @Override
    public String toString() {
        return String.format("IV='%s'; Key='%s'", iv_, key_);
    }

    /**
     * @param o The KeyData to compare with this instance.
     * @return true if the 2 instances contain the same data; false otherwise.
     */
    @Override
    public boolean equals(Object o) {
        if(o instanceof KeyData keyData) {
            return keyData.iv_.equals(iv_) && keyData.key_.equals(key_);
        }
        return super.equals(o);
    }

    /**
     * Standard hashCode implementation.
     * @return super.hashCode()
     */
    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Gets the IV as bytes.
     * @return The IV as bytes.
     */
    public byte[] IVAsBytes() {
        return Base64.getDecoder().decode(iv_);
    }

    /**
     * Gets the Key as bytes.
     * @return The Key as bytes.
     */
    public byte[] keyAsBytes() {
        return Base64.getDecoder().decode(key_);
    }

    String iv_;
    String key_;

    // Strong but may block on some OSes. Try "NativePRNGNonBlocking" if you have problems.
    private static String getAlgorithm() {
        if(System.getProperty("os.name").matches(".*[wW]indows.*"))
            return "Windows-PRNG";
        else
            return "NativePRNGBlocking";
    }

    static int BITS  = 256;
}
