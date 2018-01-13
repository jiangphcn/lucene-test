package io.github.mojtab23.lucene_test.xodus;

import jetbrains.exodus.env.ContextualEnvironment;
import jetbrains.exodus.env.Environments;
import jetbrains.exodus.env.Transaction;
import org.apache.lucene.analysis.Analyzer;
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
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class DebugMyDirectoryTest {

    protected Transaction txn;
    private IndexWriter indexWriter;
    private ContextualEnvironment env;

    @Before
    public void setUp() throws Exception {

    }


    @Test
    public void testLucene() throws IOException, ParseException {
        env = Environments.newContextualInstance("test/.myAppData");
        beginTransaction();
        Analyzer analyzer = new StandardAnalyzer();
        Directory directory = new DebugMyDirectory(env);
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        indexWriter = new IndexWriter(directory, config);
        Document doc = new Document();
        String text = "Lucene is an Information Retrieval library written in Java";
        doc.add(new TextField("fieldname", text, Field.Store.YES));
        indexWriter.addDocument(doc);
        indexWriter.close();
        commitTransaction();


        beginTransaction();


        IndexReader indexReader = DirectoryReader.open(directory);
        IndexSearcher indexSearcher = new
                IndexSearcher(indexReader);
        QueryParser parser = new QueryParser( "Content",
                analyzer);
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

        commitTransaction();

    }


    @After
    public void tearDown() throws Exception {

    }


    protected void beginTransaction() {
        if (txn != null) {
            throw new IllegalStateException("Not committed transaction");
        }
        txn = env.beginTransaction();
    }

    protected void commitTransaction() {
        if (txn == null) {
            throw new IllegalStateException("Not started transaction");
        }
        txn.commit();
        txn = null;
    }

}