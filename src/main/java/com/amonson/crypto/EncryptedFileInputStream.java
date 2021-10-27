// Copyright (C) 2018-2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.crypto;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Class to open an encrypted file stream for read.
 */
public class EncryptedFileInputStream extends InputStream {
    /**
     * Open a file for read.
     * @param filename File to open for read.
     * @param key Key object for crypto.
     * @throws FileNotFoundException When an input file is not found.
     * @throws InvalidAlgorithmParameterException When the AES/CBC is not supported.
     * @throws NoSuchAlgorithmException When the AES algorithm is not supported.
     * @throws InvalidKeyException When a bad key is used, usually the wrong bit length.
     * @throws NoSuchPaddingException When the AES/CBC/PKCS5PADDING is not supported.
     */
    public EncryptedFileInputStream(String filename, KeyData key) throws FileNotFoundException,
            InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        super();
        key_ = key;
        init(new File(filename));
    }

    /**
     * Open a file for read.
     * @param file File to open for read.
     * @param key Key object for crypto.
     * @throws FileNotFoundException When an input file is not found.
     * @throws InvalidAlgorithmParameterException When the AES/CBC is not supported.
     * @throws NoSuchAlgorithmException When the AES algorithm is not supported.
     * @throws InvalidKeyException When a bad key is used, usually the wrong bit length.
     * @throws NoSuchPaddingException When the AES/CBC/PKCS5PADDING is not supported.
     */
    public EncryptedFileInputStream(File file, KeyData key) throws FileNotFoundException,
            InvalidAlgorithmParameterException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException {
        super();
        key_ = key;
        init(file);
    }

    /**
     * Read a byte.
     * @return The byte read.
     * @throws IOException When a stream cannot be read.
     */
    @Override
    public int read() throws IOException {
        return in_.read();
    }

    /**
     * Read bytes.
     * @param bytes Buffer for bytes.
     * @return count of bytes read.
     * @throws IOException When a stream cannot be read.
     */
    @Override
    public int read(byte[] bytes) throws IOException {
        return in_.read(bytes);
    }

    /**
     * Read bytes.
     * @param bytes Buffer for bytes.
     * @param off offset in byte buffer.
     * @param len length of bytes.
     * @return count of bytes read.
     * @throws IOException When a stream cannot be read.
     */
    @Override
    public int read(byte[] bytes, int off, int len) throws IOException {
        return in_.read(bytes, off, len);
    }

    /**
     * Close the stream.
     * @throws IOException When a stream cannot be closed.
     */
    @Override
    public void close() throws IOException {
        super.close();
        in_.close();
    }

    private void init(File file) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException, FileNotFoundException {
        IvParameterSpec ivSpec = new IvParameterSpec(key_.IVAsBytes());
        SecretKeySpec keySpec = new SecretKeySpec(key_.keyAsBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
        in_ = new CipherInputStream(new FileInputStream(file), cipher);
    }

    private final KeyData key_;
    private CipherInputStream in_;
}
