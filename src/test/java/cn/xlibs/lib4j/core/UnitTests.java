package cn.xlibs.lib4j.core;

import cn.xlibs.lib4j.core.utility.CryptoUtils;
import cn.xlibs.lib4j.core.utility.RandomUtils;
import com.google.common.io.Files;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static cn.xlibs.lib4j.core.utility.IPv4Utils.*;

/**
 * Testing
 *
 * @author Fury
 * @since 2020-04-17
 * <p>
 * All rights Reserved.
 */
public class UnitTests {

    @BeforeAll
    public static void beforeAll() throws Exception {
        // NO Sonar
    }

    @BeforeEach
    public void beforeEach() {
        // NO Sonar
    }

    @AfterEach
    public void afterEach() {
        // NO Sonar
    }

    @Test
    void testRandomUtils() {
        Assertions.assertEquals(5, RandomUtils.randAZaz09String(5).length());
    }

    @Test
    void testIpV4Utils() {
        Assertions.assertTrue(match("127.0.0.1", "127.0.0.1"));
        Assertions.assertTrue(match("127.0.0.1/24", "127.0.0.1"));
        Assertions.assertTrue(match("127.0.0.1/24", "127.0.0.1/24"));
        Assertions.assertTrue(match("127.0.0.1", "127.0.0.1/24"));

        Assertions.assertFalse(match("", ""));
        Assertions.assertFalse(match("127.0.0", "127.0.0.1"));
        Assertions.assertFalse(match("127.0.0.1/16", "127.0.0.1/24"));

        Assertions.assertTrue(contains("114.80.56.0/24", "114.80.56.9"));
        Assertions.assertTrue(contains("114.80.56.0/24", "114.80.56.23"));
        Assertions.assertFalse(contains("211.95.54.0/24", "47.106.253.120"));
    }

    @Test
    void testCryptoUtils() throws IOException {
        // 01.Prepare
        String aesKey = "c4d8e62d6b23c5e4";
        String desKey = "9mng65v8jf4lxn93nabf981m";
        String aesIv = "Xef2ydYUiCbM8Nx7";
        String desIv = "Xef2ydYU";
        String data = "data for testing";


        String fk = RandomUtils.randAZaz09String(6);
        String tmpDir = System.getProperty("java.io.tmpdir");
        File inputFile = Paths.get(tmpDir + "input_" + fk).toFile();
        File encryptFile = Paths.get(tmpDir + "encrypt_" + fk).toFile();
        File outputFile = Paths.get(tmpDir + "output_" + fk).toFile();

        // 02. When
        ForTest testObj = new ForTest("data");
        System.out.println(inputFile.getAbsolutePath());
        Assertions.assertTrue(inputFile.createNewFile());
        Files.write(data.getBytes(StandardCharsets.UTF_8), inputFile);
        CryptoUtils.Symmetric.SymmCryptoImpl aesCbc = CryptoUtils.Symmetric
                .AES_CBC_PKCS5Padding.with(aesKey, aesIv);
        CryptoUtils.Symmetric.SymmCryptoImpl aesEcb = CryptoUtils.Symmetric
                .AES_ECB_PKCS5Padding.with(aesKey);
        CryptoUtils.Symmetric.SymmCryptoImpl tripleDesCbc = CryptoUtils.Symmetric
                .TripleDES_CBC_PKCS5Padding.with(desKey, desIv);
        CryptoUtils.Symmetric.SymmCryptoImpl tripleDesEcb = CryptoUtils.Symmetric
                .TripleDES_ECB_PKCS5Padding.with(desKey);

        // 03. Verify
        Assertions.assertEquals(data, aesCbc.decryptHex(aesCbc.encryptHex(data)));
        Assertions.assertEquals(data, aesCbc.decryptBase64(aesCbc.encryptBase64(data)));
        Assertions.assertEquals(testObj.getValue(), ((ForTest)aesCbc.decryptObject(aesCbc.encryptObject(testObj))).getValue());
        aesCbc.encryptFile(inputFile, encryptFile);
        aesCbc.decryptFile(encryptFile, outputFile);
        Assertions.assertEquals(new String(Files.toByteArray(inputFile))
                , new String(Files.toByteArray(outputFile)));

        Assertions.assertEquals(data, aesEcb.decryptHex(aesEcb.encryptHex(data)));
        Assertions.assertEquals(data, aesEcb.decryptBase64(aesEcb.encryptBase64(data)));
        Assertions.assertEquals(testObj.getValue(), ((ForTest)aesEcb.decryptObject(aesEcb.encryptObject(testObj))).getValue());
        aesEcb.encryptFile(inputFile, encryptFile);
        aesEcb.decryptFile(encryptFile, outputFile);
        Assertions.assertEquals(new String(Files.toByteArray(inputFile))
                , new String(Files.toByteArray(outputFile)));

        Assertions.assertEquals(data, tripleDesCbc.decryptHex(tripleDesCbc.encryptHex(data)));
        Assertions.assertEquals(data, tripleDesCbc.decryptBase64(tripleDesCbc.encryptBase64(data)));
        Assertions.assertEquals(testObj.getValue(), ((ForTest)tripleDesCbc.decryptObject(tripleDesCbc.encryptObject(testObj))).getValue());
        tripleDesCbc.encryptFile(inputFile, encryptFile);
        tripleDesCbc.decryptFile(encryptFile, outputFile);
        Assertions.assertEquals(new String(Files.toByteArray(inputFile))
                , new String(Files.toByteArray(outputFile)));

        Assertions.assertEquals(data, tripleDesEcb.decryptHex(tripleDesEcb.encryptHex(data)));
        Assertions.assertEquals(data, tripleDesEcb.decryptBase64(tripleDesEcb.encryptBase64(data)));
        Assertions.assertEquals(testObj.getValue(), ((ForTest)tripleDesEcb.decryptObject(tripleDesEcb.encryptObject(testObj))).getValue());
        tripleDesEcb.encryptFile(inputFile, encryptFile);
        tripleDesEcb.decryptFile(encryptFile, outputFile);
        Assertions.assertEquals(new String(Files.toByteArray(inputFile))
                , new String(Files.toByteArray(outputFile)));
    }

    @Data
    @AllArgsConstructor
    public static class ForTest implements Serializable {
        private String value;
    }
}
