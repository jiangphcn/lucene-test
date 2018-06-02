import org.junit.Test;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;

public class TestCTR {


    @Test
    public void testCTR() throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

        // calculate a file specific IV based on the unique relative filename
        byte[] iv = new byte[cipher.getBlockSize()];
//        MessageDigest md = MessageDigest.getInstance("MD5");
//        final byte[] bytes = fileSystemRoot.relativize(path).toString().getBytes();
//        md.update(bytes);
//        md.digest(iv, 0, cipher.getBlockSize());

        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        final byte[] secret = "1234567891234056".getBytes();
        final byte[] s = "abc_def_ghi_jkl_mno_pqrs_tuv_wxyz".getBytes();
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret, "AES");

        // load the mode, symmetric key and IV into the Cipher
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        final byte[] enc = cipher.doFinal(s);
        Files.write(Paths.get("C:\\Users\\Mojtaba\\Desktop\\cryptofs\\test\\enc.enc"), enc);
    }

    @Test
    public void testDecCTR() throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

        // calculate a file specific IV based on the unique relative filename
        byte[] iv = new byte[cipher.getBlockSize()];

        final byte[] bytes = Files.readAllBytes(Paths.get("C:\\Users\\Mojtaba\\Desktop\\cryptofs\\test\\enc.enc"));
        iv[15] = 2;
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        final byte[] secret = "1234567891234056".getBytes();
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret, "AES");

        // load the mode, symmetric key and IV into the Cipher
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);


        final byte[] dec = cipher.doFinal(bytes, 32, 1);
        System.out.println(new String(dec));
    }

    @Test
    public void testLongToArray() throws Exception {
        final MessageDigest md = MessageDigest.getInstance("MD5");
        final byte[] bytes = "cryptofs\\test\\enc.enc".getBytes();

        final byte[] digest = md.digest(bytes);
        ByteBuffer buffer = ByteBuffer.wrap(digest);
        for (byte b : buffer.array()) {
            System.out.print(b + ",");
        }
        System.out.println();
        long l = 1;
        buffer.putLong(l);
        for (byte b : buffer.array()) {
            System.out.print(b + ",");
        }
    }

    @Test
    public void testMD5() throws Exception {

        MessageDigest md = MessageDigest.getInstance("MD5");
        final byte[] bytes = "cryptofs\\test\\enc.enc".getBytes();

        final byte[] digest = md.digest(bytes);
        for (byte b : digest) {

            System.out.println(b);
        }
    }

    @Test
    public void dummy() {



    }
}
