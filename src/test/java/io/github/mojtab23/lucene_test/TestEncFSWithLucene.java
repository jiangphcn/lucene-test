package io.github.mojtab23.lucene_test;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.apache.lucene.store.SimpleFSLockFactory;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;

public class TestEncFSWithLucene {

    @Test
    public void testRun() throws IOException {


        FileSystemProvider provider = new EncryptedFileSystemProvider();
        Map<String, byte[]> env = new HashMap<>();
        env.put(EncryptedFileSystemProvider.SECRET_KEY, "1234567890abcdef".getBytes()); // your 128 bit key
        URI uri = URI.create("enc:C:/Users/Mojtaba/Desktop/cryptofs/");
        final FileSystem fs = provider.newFileSystem(uri, env);
        Path path = fs.getPath("C:\\Users\\Mojtaba\\Desktop\\cryptofs", "/lucene");


        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = new NIOFSDirectory(path, SimpleFSLockFactory.getDefault());
        IndexWriterConfig config = new
                IndexWriterConfig(analyzer);
        IndexWriter indexWriter = new IndexWriter(directory,
                config);
        Document doc = new Document();
        String text = "Lucene is an Information Retrieval library written in Java";
        doc.add(new TextField("fieldname", text, Field.Store.YES));
        indexWriter.addDocument(doc);
        indexWriter.close();
    }
}
