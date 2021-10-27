// Copyright (C) 2018-2019 Paul Amonson
//
// SPDX-License-Identifier: Apache-2.0
//

package com.amonson.crypto;

import javax.crypto.Cipher;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Class to open an encrypted file stream for write.
 */
public class EncryptedFileOutputStream extends OutputStream {
    /**
     * Open a file for write that is AES encrypted.
     * @param filename File to open for writing.
     * @param key Key object for crypto.
     * @throws FileNotFoundException When an input file is not found.
     * @throws InvalidAlgorithmParameterException When the AES/CBC is not supported.
     * @throws NoSuchAlgorithmException When the AES algorithm is not supported.
     * @throws InvalidKeyException When a bad key is used, usually the wrong bit length.
     * @throws NoSuchPaddingException When the AES/CBC/PKCS5PADDING is not supported.
     */
    public EncryptedFileOutputStream(String filename, KeyData key) throws FileNotFoundException, NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        super();
        key_ = key;
        init(new File(filename));
    }

    /**
     * Open a file for write that is AES encrypted.
     * @param file File to open for writing.
     * @param key Key object for crypto.
     * @throws FileNotFoundException When an input file is not found.
     * @throws InvalidAlgorithmParameterException When the AES/CBC is not supported.
     * @throws NoSuchAlgorithmException When the AES algorithm is not supported.
     * @throws InvalidKeyException When a bad key is used, usually the wrong bit length.
     * @throws NoSuchPaddingException When the AES/CBC/PKCS5PADDING is not supported.
     */
    public EncryptedFileOutputStream(File file, KeyData key) throws FileNotFoundException, NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeyException {
        super();
        key_ = key;
        init(file);
    }

    /**
     * Write a byte to the stream.
     * @param n The byte to write.
     * @throws IOException When a stream cannot be written.
     */
    @Override
    public void write(int n) throws IOException {
        out_.write(n);
    }

    /**
     * Write bytes to the stream.
     * @param bytes The bytes to write.
     * @throws IOException When a stream cannot be written.
     */
    @Override
    public void write(byte[] bytes) throws IOException {
        out_.write(bytes);
    }

    /**
     * Write bytes to the stream.
     * @param bytes The bytes to write.
     * @param off offset in byte buffer.
     * @param len length of bytes.
     * @throws IOException When a stream cannot be written.
     */
    @Override
    public void write(byte[] bytes, int off, int len) throws IOException {
        out_.write(bytes, off, len);
    }

    /**
     * Close the stream.
     * @throws IOException When a stream cannot be closed.
     */
    @Override
    public void close() throws IOException {
        out_.close();
        super.close();
    }

    /**
     * Flush bytes to disk.
     * @throws IOException When a stream cannot be written.
     */
    @Override
    public void flush() throws IOException {
        out_.flush();
        super.flush();
    }

    private void init(File file) throws NoSuchPaddingException, NoSuchAlgorithmException,
            InvalidAlgorithmParameterException, InvalidKeyException, FileNotFoundException {
        IvParameterSpec ivSpec = new IvParameterSpec(key_.IVAsBytes());
        SecretKeySpec keySpec = new SecretKeySpec(key_.keyAsBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
        out_ = new CipherOutputStream(new FileOutputStream(file), cipher);
    }

    private final KeyData key_;
    private CipherOutputStream out_;
}
