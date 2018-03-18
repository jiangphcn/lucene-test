package io.github.mojtab23.lucene_test;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NIOFSDirectory;
import org.junit.Test;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.spi.FileSystemProvider;
import java.util.HashMap;
import java.util.Map;

public class TestEncFSWithLucene {

    @Test
    public void testRun() throws IOException, ParseException {


        FileSystemProvider provider = new EncryptedFileSystemProvider();
        Map<String, byte[]> env = new HashMap<>();
        env.put(EncryptedFileSystemProvider.SECRET_KEY, "1234567890abcdef".getBytes()); // your 128 bit key
        URI uri = URI.create("enc:C:/Users/Mojtaba/Desktop/cryptofs/");
        final FileSystem fs = provider.newFileSystem(uri, env);
        Path path = fs.getPath("C:\\Users\\Mojtaba\\Desktop\\cryptofs", "/lucene");

        final Path path1 = Paths.get("C:\\Users\\Mojtaba\\Desktop\\cryptofs", "/lucene");
//        StandardAnalyzer analyzer = new StandardAnalyzer();
        Directory directory = new NIOFSDirectory(path);
        IndexWriterConfig config = new IndexWriterConfig();
        IndexWriter indexWriter = new IndexWriter(directory, config);
        Document doc = new Document();
        String text = "Lucene is an Information Retrieval library written in Java";
        doc.add(new TextField("Content", text, Field.Store.YES));
        indexWriter.addDocument(doc);
        indexWriter.close();


        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new
                IndexSearcher(indexReader);
        QueryParser parser = new QueryParser("Content", new StandardAnalyzer());
        Query query = parser.parse("Lucene");
        int hitsPerPage = 10;
        TopDocs docs = indexSearcher.search(query, hitsPerPage);
        ScoreDoc[] hits = docs.scoreDocs;
        long end = Math.min(docs.totalHits, hitsPerPage);
        System.out.print("Total Hits: " + docs.totalHits);
        System.out.print("Results: ");
        for (int i = 0; i < end; i++) {
            Document d = indexSearcher.doc(hits[i].doc);
            System.out.println("Content: " + d.get("Content"));
        }

    }
}
