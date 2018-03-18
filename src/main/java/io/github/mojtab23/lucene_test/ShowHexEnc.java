package io.github.mojtab23.lucene_test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;

public class ShowHexEnc {


    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static void main(String[] args) throws IOException {


        FileSystemProvider provider = new EncryptedFileSystemProvider();
        Map<String, byte[]> env = new HashMap<>();
        env.put(EncryptedFileSystemProvider.SECRET_KEY, "1234567890abcdef".getBytes()); // your 128 bit key

        final URI uri1 = new File("C:\\Users\\Mojtaba\\Desktop\\cryptofs").toURI();
        System.out.println(uri1);
        URI uri = URI.create("enc:C:/Users/Mojtaba/Desktop/cryptofs/");
        final FileSystem fs = provider.newFileSystem(uri, env);
        Path path = fs.getPath("C:\\Users\\Mojtaba\\Desktop\\cryptofs\\test\\text.enc");
        final InputStream inputStream = provider.newInputStream(path);
        final long size = Files.size(path);
        System.out.println("virtual file size: " + size);
        final byte[] bytes = new byte[8096];


        int read;

        while ((read = inputStream.read(bytes)) > 0) {
            System.out.println("read bytes: " + read);
            System.out.println(bytesToHex(bytes, read));
//            System.out.println(Arrays.toString(bytes));
        }

    }

    public static String bytesToHex(byte[] bytes, int len) {
        char[] hexChars = new char[len * 3];
        for (int j = 0; j < len; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = hexArray[v >>> 4];
            hexChars[j * 3 + 1] = hexArray[v & 0x0F];
            hexChars[j * 3 + 2] = ' ';
        }
        return new String(hexChars);
    }


}
