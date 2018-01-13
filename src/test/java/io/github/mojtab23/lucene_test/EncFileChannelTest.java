package io.github.mojtab23.lucene_test;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.EnumSet;

public class EncFileChannelTest {
    private static final Logger logger = LoggerFactory.getLogger(EncFileChannelTest.class);
    private final Path filePath = Paths.get("C:\\Users\\Mojtaba\\Desktop\\cryptofs\\test\\enc.enc");
    private final Path fileSystemRoot = Paths.get("C:\\Users\\Mojtaba\\Desktop\\cryptofs\\");

//    @Test
//    public void write() throws Exception {
//
//        final byte[] secret = "1234567891234056".getBytes();
//        SecretKeySpec secretKeySpec = new SecretKeySpec(secret, "AES");
//
//
//        final EncFileChannel encFileChannel = new EncFileChannel(
//                filePath,
//                secretKeySpec, fileSystemRoot,
//                EnumSet.of(StandardOpenOption.WRITE)
//        );
//        final ByteBuffer allocate = ByteBuffer.allocate(20);
//        allocate.put((byte) '1');
//        allocate.put((byte) '2');
//        allocate.put((byte) '3');
//        allocate.put((byte) '4');
//        allocate.put((byte) '5');
//
//        allocate.flip();
//        encFileChannel.position(30);
//        int write = encFileChannel.write(allocate);
//        logger.debug("write: {}", write);
//        logger.debug("Position: {}", encFileChannel.position());
//        for (int i = 0; i < allocate.position(); i++)
////            System.out.print(allocate.get(i) + " ");
//            System.out.print((char) allocate.get(i));
//        System.out.println();
//
//
//    }

//    @Test
//    public void read() throws Exception {
//
//        final byte[] secret = "1234567891234056".getBytes();
//        SecretKeySpec secretKeySpec = new SecretKeySpec(secret, "AES");
//
//
//        final EncFileChannel encFileChannel = new EncFileChannel(
//                filePath,
//                secretKeySpec, fileSystemRoot,
//                Collections.emptySet()
//        );
//        final ByteBuffer allocate = ByteBuffer.allocate(50);
////        encFileChannel.position(27);
//        int read = encFileChannel.read(allocate);
//        logger.debug("read: {}", read);
//        logger.debug("Position: {}", encFileChannel.position());
//        for (int i = 0; i < allocate.position(); i++)
////            System.out.print(allocate.get(i) + " ");
//            System.out.print((char) allocate.get(i));
//        System.out.println();
//
////        allocate.clear();
////        read = encFileChannel.read(allocate);
////        logger.debug("read: {}", read);
////        logger.debug("Position: {}", encFileChannel.position());
////        for (int i = 0; i < allocate.position(); i++)
////            System.out.print(allocate.get(i) + " ");
////        System.out.println();
//
////        allocate.clear();
////        read = encFileChannel.read(allocate);
////        logger.debug("read: {}", read);
////        logger.debug("Position: {}", encFileChannel.position());
////        for (int i = 0; i < allocate.position(); i++)
////            System.out.print(allocate.get(i) + " ");
////        System.out.println();
////[0, 0, 0, 0, 0, 0, 0, 0, 33, -5, 52, 9, 3, 53, 32, -16]
//    }

    private byte[] createIV(long blockNumber, Path filePath, Path fileSystemRoot) throws NoSuchAlgorithmException {
        final byte[] bytes = fileSystemRoot.relativize(filePath).toString().getBytes();
        final MessageDigest md = MessageDigest.getInstance("MD5");
        final byte[] digest = md.digest(bytes);
        ByteBuffer buffer = ByteBuffer.wrap(digest);
        buffer.position(8);
        buffer.putLong(blockNumber);
        logger.debug("IV:{}", buffer.array());
        return buffer.array();
    }


    @Test
    public void testCTR() throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CTR/NoPadding");

        // calculate a file specific IV based on the unique relative filename
        byte[] iv = createIV(0, filePath, fileSystemRoot);

        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
        final byte[] secret = "1234567891234056".getBytes();
        final byte[] s = "abc_def_ghi_jkl_mno_pqrs_tuv_wxyz".getBytes(StandardCharsets.US_ASCII);
        for (int i = 0; i < s.length; i++)
//            System.out.print(allocate.get(i) + " ");
            System.out.print(s[i] + " ");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secret, "AES");

        // load the mode, symmetric key and IV into the Cipher
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        final byte[] enc = cipher.doFinal(s);
        Files.write(filePath, enc);
    }

    @Test
    public void printl1() throws Exception {
        final FileChannel open = FileChannel.open(filePath, EnumSet.of(StandardOpenOption.WRITE));
        open.position(17);
        final ByteBuffer allocate = ByteBuffer.allocate(5);
        allocate.put((byte) '1');
        allocate.put((byte) '2');
        allocate.put((byte) '3');
        allocate.put((byte) '4');
        allocate.put((byte) '5');
        allocate.flip();

        int write = open.write(allocate);

    }

    @Test
    public void printl0() throws Exception {

        final byte[] s = "abc_def_ghi_jkl_mno_pqrs_tuv_wxyz".getBytes(StandardCharsets.US_ASCII);
        Files.write(filePath, s);

    }


    @Test
    public void buffer() throws Exception {
        ByteBuffer buffer = ByteBuffer.allocate(7);
        System.out.println("Capacity = " + buffer.capacity());
        System.out.println("Limit = " + buffer.limit());
        System.out.println("Position = " + buffer.position());
        System.out.println("Remaining = " + buffer.remaining());
        buffer.put((byte) 10).put((byte) 20).put((byte) 30);
        System.out.println("Capacity = " + buffer.capacity());
        System.out.println("Limit = " + buffer.limit());
        System.out.println("Position = " + buffer.position());
        System.out.println("Remaining = " + buffer.remaining());
        for (int i = 0; i < buffer.position(); i++)
            System.out.println(buffer.get(i));
    }
}