package cn.xlibs.lib4j.core.utility;

import cn.xlibs.lib4j.core.exception.OperationException;

import java.io.*;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.apache.commons.codec.binary.Hex;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;
import java.util.Objects;

import static cn.xlibs.lib4j.core.support.Constants.PKCS5Padding;

/**
 * Crypto Utils
 *
 * @author Fury
 * @since 2020-11-21 01:08
 * <p>
 * All rights Reserved.
 */
public final class CryptoUtils {
    private CryptoUtils(){}

    /**
     * Symmetric Crypto
     */
    public enum Symmetric {
        /** AES/CBC/PKCS5Padding */
        AES_CBC_PKCS5Padding("AES", "CBC", PKCS5Padding, true),
        /** AES/ECB/PKCS5Padding */
        AES_ECB_PKCS5Padding("AES", "ECB", PKCS5Padding, false),
        /** TripleDES/CBC/PKCS5Padding */
        TripleDES_CBC_PKCS5Padding("TripleDES", "CBC", PKCS5Padding, true),
        /** TripleDES/ECB/PKCS5Padding */
        TripleDES_ECB_PKCS5Padding("TripleDES", "ECB", PKCS5Padding, false),
        ;

        /** AES, TripleDES e.g. */
        private final String algorithm;
        /** 'TripleDES/ECB/PKCS5Padding' e.g. */
        private final String transform;
        /** if iv needed */
        private final boolean needIv;

        /**
         * get key spec from key bytes
         * @param key key bytes
         * @return SecretKey
         */
        private SecretKey getKeySpec(byte[] key) {
            return Objects.isNull(key) ? null : new SecretKeySpec(key, algorithm);
        }

        /**
         * get iv spec from iv bytes
         * @param iv iv bytes
         * @return AlgorithmParameterSpec
         */
        private AlgorithmParameterSpec getIvSpec(byte[] iv){
            return Objects.isNull(iv) ? null : new IvParameterSpec(iv);
        }

        /**
         * Constructor
         * @param algorithm AES, TripleDES e.g.
         * @param mode Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
         * @param transform 'AES/CBC/PKCS5Padding' e.g.
         * @param needIv if iv needed
         */
        Symmetric(String algorithm, String mode, String transform, boolean needIv) {
            this.needIv = needIv;
            this.algorithm = algorithm;
            this.transform = String.join("/", algorithm, mode, transform);
        }

        /**
         * build SymmCryptoImpl with key
         * @param key key string
         * @return SymmCryptoImpl
         */
        public SymmCryptoImpl with(String key) {
            if (needIv) {
                throw new OperationException("IV need");
            }
            return with(key.getBytes(StandardCharsets.UTF_8), null);
        }

        /**
         * build SymmCryptoImpl with key and iv
         * @param key key string
         * @param iv iv string
         * @return SymmCryptoImpl
         */
        public SymmCryptoImpl with(String key, String iv) {
            return with(key.getBytes(StandardCharsets.UTF_8), iv.getBytes(StandardCharsets.UTF_8));
        }

        /**
         * build SymmCryptoImpl with key and iv
         * @param key key bytes
         * @param iv iv bytes
         * @return SymmCryptoImpl
         */
        public SymmCryptoImpl with(byte[] key, byte[] iv) {
            return new SymmCryptoImpl(
                    this.transform,
                    this.getKeySpec(key),
                    this.needIv,
                    this.getIvSpec(iv)
            );
        }

        /** SymmCrypto implementation */
        @AllArgsConstructor(access = AccessLevel.PRIVATE)
        public static class SymmCryptoImpl {
            private final String transform;
            private final SecretKey secretKey;
            private final boolean needIv;
            private final AlgorithmParameterSpec ivSpec;

            /**
             * encrypt string to hex encoding
             * @param data raw data
             * @return hex encoding encrypted data
             */
            public String encryptHex(String data) {
                return Hex.encodeHexString(cipherImpl(getUtf8Bytes(data)
                        , Cipher.ENCRYPT_MODE, this));
            }

            /**
             * decrypt hex encoding crypto string
             * @param hex hex crypto string
             * @return raw string
             */
            public String decryptHex(String hex) {
                try {
                    return new String(cipherImpl(Hex.decodeHex(hex), Cipher.DECRYPT_MODE
                            , this), StandardCharsets.UTF_8);
                } catch (Exception e) {
                    throw new OperationException(e);
                }
            }

            /**
             * encrypt string to base64 encoding
             * @param data string for encrypt
             * @return encrypted string in base64 encoding
             */
            public String encryptBase64(String data) {
                return Base64.getEncoder().encodeToString(
                        cipherImpl(getUtf8Bytes(data), Cipher.ENCRYPT_MODE, this)
                );
            }

            /**
             * decrypt crypto string with base64 encoding
             * @param b64Data base64 encoded string
             * @return decrypted string
             */
            public String decryptBase64(String b64Data) {
                return new String(cipherImpl(Base64.getDecoder()
                        .decode(getUtf8Bytes(b64Data)), Cipher.DECRYPT_MODE, this));
            }

            /**
             * encrypt file
             * @param inputFile file for encrypt
             * @param outputFile encrypted file
             */
            public void encryptFile(File inputFile, File outputFile) {
                cipherFileImpl(inputFile, outputFile, Cipher.ENCRYPT_MODE, this);
            }

            /**
             * decrypt file
             * @param inputFile file for decrypt
             * @param outputFile decrypted file
             */
            public void decryptFile(File inputFile, File outputFile) {
                cipherFileImpl(inputFile, outputFile, Cipher.DECRYPT_MODE, this);
            }

            /**
             * encrypt java object
             * @param object object for encrypt
             * @return SealedObject
             * @param <T> serializable object for encrypt
             */
            public <T extends Serializable> SealedObject encryptObject(T object) {
                try {
                    return new SealedObject(object, getCipher(Cipher.ENCRYPT_MODE, this));
                } catch (Exception e) {
                    throw new OperationException(e);
                }
            }

            /**
             * decrypt java object
             * @param sealedObject sealed Object
             * @return decrypted Object
             * @param <T> Serializable Object
             */
            @SuppressWarnings("unchecked")
            public <T extends Serializable> T decryptObject(SealedObject sealedObject) {
                try {
                    return (T)sealedObject.getObject(getCipher(Cipher.DECRYPT_MODE, this));
                } catch (Exception e) {
                    throw new OperationException(e);
                }
            }

            /**
             * decrypt/encrypt bytes
             * @param utf8Bytes bytes for encrypt/decrypt
             * @param mode Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
             * @param sci SymmCryptoImpl
             * @return decrypt/encrypt bytes
             */
            private static byte[] cipherImpl(byte[] utf8Bytes, int mode, SymmCryptoImpl sci){
                try {
                    Cipher cipher = getCipher(mode, sci);
                    return cipher.doFinal(utf8Bytes);
                } catch (Exception e) {
                    throw new OperationException(e);
                }
            }

            /**
             * decrypt/encrypt file implementation
             * @param inputFile input file for encrypt/decrypt
             * @param outputFile decrypted/encrypted output file
             * @param mode Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
             * @param sci SymmCryptoImpl
             */
            private static void cipherFileImpl(File inputFile, File outputFile, int mode, SymmCryptoImpl sci) {
                try {
                    byte[] buffer = new byte[1024];
                    if(!Files.exists(outputFile.toPath()) && !outputFile.createNewFile()) {
                        throw new OperationException("Create file failure: " + outputFile.getAbsolutePath());
                    }
                    try(FileInputStream inputStream = new FileInputStream(inputFile)) {
                        try(FileOutputStream outputStream = new FileOutputStream(outputFile)) {
                            int bytesRead;
                            Cipher cipher = getCipher(mode, sci);
                            while ((bytesRead = inputStream.read(buffer)) != -1) {
                                byte[] output = cipher.update(buffer, 0, bytesRead);
                                if (output != null) {
                                    outputStream.write(output);
                                }
                            }
                            byte[] outputBytes = cipher.doFinal();
                            if (outputBytes != null) {
                                outputStream.write(outputBytes);
                            }
                        }
                    }
                } catch (Exception e) {
                    throw new OperationException(e);
                }
            }

            /**
             * get cipher
             * @param mode Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE
             * @param sci SymmCryptoImpl
             * @return Cipher
             */
            private static Cipher getCipher(int mode, SymmCryptoImpl sci){
                try {
                    Cipher cipher = Cipher.getInstance(sci.transform);
                    if (sci.needIv) {
                        cipher.init(mode, sci.secretKey, sci.ivSpec);
                    } else {
                        cipher.init(mode, sci.secretKey);
                    }
                    return cipher;
                } catch (Exception e) {
                    throw new OperationException(e);
                }
            }
        }
    }

    /**
     * get utf-8 encoding bytes
     * @param data data in string
     * @return bytes
     */
    private static byte[] getUtf8Bytes(String data) {
        return data.getBytes(StandardCharsets.UTF_8);
    }
}
