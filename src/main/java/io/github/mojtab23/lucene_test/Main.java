package io.github.mojtab23.lucene_test;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;

public class Main {


    public static void main(String[] args) throws IOException {

        FileSystemProvider provider = new EncryptedFileSystemProvider();
        Map<String, Object> env = new HashMap<>();
        Path path1 = Paths.get("C:\\Users\\Mojtaba\\Desktop\\cryptofs").normalize();
        final URI uri1 = new File("C:\\Users\\Mojtaba\\Desktop\\cryptofs").toURI();
        System.out.println(uri1);
//        env.put(EncryptedFileSystemProvider.CIPHER_ALGORITHM, "AES");
//        env.put(EncryptedFileSystemProvider.CIPHER_ALGORITHM_MODE, "CTR");
//        env.put(EncryptedFileSystemProvider.CIPHER_ALGORITHM_PADDING, "NoPadding");
        env.put(EncryptedFileSystemProvider.SECRET_KEY, "1234567890abcdef".getBytes()); // your 128 bit key
        env.put(EncryptedFileSystemProvider.NEED_MAC, Boolean.TRUE); // your 128 bit key
//        env.put(EncryptedFileSystemProvider.REVERSE_MODE, "true"); // "false" or remove for default mode
//        env.put(EncryptedFileSystemProvider.FILESYSTEM_ROOT_URI, "file:C:\\Users\\Mojtaba\\Desktop\\cryptofs"); // base directory for file system operations
//        env.put(EncryptedFileSystemProvider.FILESYSTEM_ROOT_URI, uri1.toString()); // base directory for file system operations


//        URI uri = URI.create("enc:///");
        URI uri = URI.create("enc:C:/Users/Mojtaba/Desktop/cryptofs/");
        final FileSystem fs = provider.newFileSystem(uri, env);
        Path path = fs.getPath("C:\\Users\\Mojtaba\\Desktop\\cryptofs", "/test/text.enc");
        OutputStream outStream = provider.newOutputStream(path);
        outStream.write("test2".getBytes());
        outStream.close();
        final InputStream inputStream1 = Files.newInputStream(Paths.get("C:\\Users\\Mojtaba\\Desktop\\cryptofs\\test\\test.txt"));
        final InputStream inputStream = provider.newInputStream(path);
//        final byte[] bytes = new byte[100];
//        StringBuilder builder = new StringBuilder();
//        while (inputStream.read(bytes) > 0) {
//            builder.append(new String(bytes));
//        }
//        System.out.println(builder.toString());

        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);


        final BufferedReader bufferedReader = new BufferedReader(
                inputStreamReader);

        final StringBuilder builder = new StringBuilder();
        for (; ; ) {
            final int read = bufferedReader.read();
            if (read < 0) {
                break;
            }
            builder.append((char) read);
        }

        System.out.println(builder.toString());

//        final String s = bufferedReader.readLine();
//        System.out.println(s);
//        final String s1 = bufferedReader.readLine();
//        System.out.println(s1);
//        final String s2 = bufferedReader.readLine();
//        System.out.println(s2);

//        List<String> doc = bufferedReader
//                .lines()
//                .collect(Collectors.toList());
////                Files.readAllLines(path);
//
//        System.out.println(doc);
    }


}
