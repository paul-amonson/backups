// Copyright (C) 2021 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//
package com.amonson.crypto;

import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Static class to handle plain or crypto file copies. Uses {@link EncryptedFileInputStream} and
 * {@link EncryptedFileOutputStream}. On a null key, a plain file copy is executed.
 */
public final class Copier {

    /**
     * Copy a source file to a destination file with encryption or decryption if key is not null.
     *
     * @param source The source file to copy from.
     * @param destination The destination file to copy to/over.
     * @param key The encryption/decryption key or null for a copy with no crypto.
     * @param direction Whether to encrypt or decrypt the file during copy iff key is not null.
     * @throws IOException For IOExceptions or crypto problems.
     */
    public static void copyFile(File source, File destination, KeyData key, Direction direction) throws IOException {
        if(key == null)
            plainCopy(source, destination);
        else if(direction == Direction.Encryption)
            encryptCopy(source, destination, key);
        else
            decryptCopy(source, destination, key);
    }

    private static void encryptCopy(File source, File destination, KeyData key) throws IOException {
        try (InputStream fileStream = new FileInputStream(source)) {
            try (OutputStream encryptStream = new EncryptedFileOutputStream(destination, key)) {
                try (OutputStream gzipStream = new GZIPOutputStream(encryptStream)) {
                    fileStream.transferTo(gzipStream);
                }
            }
        } catch(InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new IOException("Encryption error occurred!", e);
        }
    }

    private static void decryptCopy(File source, File destination, KeyData key) throws IOException {
        try (InputStream decryptStream = new EncryptedFileInputStream(source, key)) {
            try (OutputStream fileStream = new FileOutputStream(destination)) {
                try (InputStream gzipStream = new GZIPInputStream(decryptStream)) {
                    gzipStream.transferTo(fileStream);
                }
            }
        } catch(InvalidKeyException | InvalidAlgorithmParameterException | NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new IOException("Encryption error occurred!", e);
        }
    }

    private static void plainCopy(File source, File destination) throws IOException {
        Files.copy(source.toPath(), destination.toPath(), StandardCopyOption.COPY_ATTRIBUTES,
                StandardCopyOption.REPLACE_EXISTING);
    }

    public enum Direction {
        Encryption,
        Decryption
    }
}
