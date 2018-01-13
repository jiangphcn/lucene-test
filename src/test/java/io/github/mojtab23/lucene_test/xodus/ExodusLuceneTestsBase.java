/*
package io.github.mojtab23.lucene_test.xodus;

import jetbrains.exodus.bindings.StringBinding;
import jetbrains.exodus.core.dataStructures.hash.HashSet;
import jetbrains.exodus.env.*;
import jetbrains.exodus.env.EnvironmentTestsBase;
import jetbrains.exodus.log.LogConfig;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.StopwordAnalyzerBase;
//import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.SerialMergeScheduler;
import org.apache.lucene.search.IndexSearcher;
//import org.apache.lucene.search.similar.MoreLikeThis;
//import org.apache.lucene.search.similarities.MoreLikeThis;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.NoLockFactory;
import org.apache.lucene.util.Version;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;

import java.io.IOException;

public abstract class ExodusLuceneTestsBase {

    public static final Version LUCENE_VERSION = Version.LUCENE_7_1_0;

    protected static final String SUMMARY = "Summary";
    protected static final String DESCRIPTION = "Description";
    protected static final String SUMMARY_TEXT =
            "Sukhoi Superjet 100";
    protected static final String DESCRIPTION_TEXT =
            "Development of the Sukhoi Superjet 100 began in 2000.[4] On 19 December 2002, Sukhoi Civil Aircraft " +
                    "and the American company Boeing signed a Long-term Cooperation Agreement to work together on the plane. " +
                    "Boeing consultants had already been advising Sukhoi on marketing, design, certification, manufacturing, " +
                    "program management and aftersales support for a year.[5] On 10 October 2003, the technical board of the " +
                    "project selected the suppliers of major subsystems.[6] The project officially passed its third stage of " +
                    "development on 12 March 2004, meaning that Sukhoi could now start selling the plane to customers.[7] On " +
                    "13 November 2004, the Superjet 100 passed the fourth stage of development, implying that the plane was " +
                    "now ready for commencing of prototype production.[8] In August 2005, a contract between the Russian " +
                    "government and Sukhoi was signed. Under the agreement, the Superjet 100 project would receive 7.9 " +
                    "billion rubles of research and development financing under the Federal Program titled Development of " +
                    "Civil Aviation in Russia in 2005-2009." +
                    "settings java.lang.NullPointerException " +
                    "as a player.";

    protected Directory directory;
    protected Analyzer analyzer;
    protected IndexWriterConfig indexConfig;
    protected IndexWriter indexWriter;
    protected ContextualEnvironment env;
    protected IndexReader indexReader;
    protected IndexSearcher indexSearcher;
    protected MoreLikeThis moreLikeThis;
    protected Transaction txn;

    @Before
    public void setUp() throws Exception {
//        super.setUp();
        createEnvironment();
        beginTransaction();
        //directory = new ExodusDirectory(env);
        //directory = new RAMDirectory();
        directory = new DebugExodusDirectory(env, getContentsConfig(), NoLockFactory.INSTANCE);
        createAnalyzer();
        createIndexWriter();
    }

    protected void createEnvironment() {
        final  ContextualEnvironment env = Environments.newContextualInstance("testdb/.myAppData");
//        final Environment env = Environments.newInstance("testdb/.myAppData");
//        env.executeInTransaction(txn -> {
//            final Store store = env.openStore("Messages", StoreConfig.WITHOUT_DUPLICATES, txn);
//            store.put(txn, StringBinding.stringToEntry("Hello"), StringBinding.stringToEntry("World!"));
//        });
//        env.close();
//

        this.env = env;
    }

    protected abstract StoreConfig getContentsConfig();

    @After
    public void tearDown() throws Exception {
        closeIndexSearcher();
        closeIndexWriter();
        commitTransaction();
        directory.close();
//        super.tearDown();
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

    protected void createAnalyzer() {
        analyzer = new StandardAnalyzer();
    }

    protected void removeStopWord(final String stopWord) {
        CharArraySet arraySet = new CharArraySet();
        final HashSet<Object> stopSet = new HashSet<>();
        for (Object word : ((StopwordAnalyzerBase) analyzer).getStopwordSet()) {
            if (!stopWord.equals(new String((char[]) word))) {
                stopSet.add(word);
            }
        }
        analyzer = new StandardAnalyzer( stopSet);
    }

    protected void createIndexWriterConfig() {
        indexConfig = new IndexWriterConfig(LUCENE_VERSION, analyzer);
        indexConfig.setMergeScheduler(new SerialMergeScheduler());
        indexConfig.setMaxThreadStates(1);
    }

    protected void createIndexWriter() throws IOException {
        closeIndexWriter();
        createIndexWriterConfig();
        indexWriter = new IndexWriter(directory, indexConfig);
    }

    protected void createIndexReader() throws IOException {
        closeIndexReader();
        indexReader = IndexReader.open(directory);
    }

    protected void createIndexSearcher() throws IOException {
        closeIndexSearcher();
        createIndexReader();
        indexSearcher = new IndexSearcher(indexReader);
    }

    protected void createMoreLikeThis() throws IOException {
        closeMoreLikeThis();
        createIndexSearcher();
        moreLikeThis = new MoreLikeThis(indexReader);
        moreLikeThis.setAnalyzer(analyzer);
        moreLikeThis.setMinTermFreq(1);
        moreLikeThis.setMinDocFreq(1);
    }

    protected void closeIndexWriter() throws IOException {
        if (indexWriter != null) {
            indexWriter.close();
            indexWriter = null;
        }
    }

    protected void closeIndexReader() throws IOException {
        if (indexReader != null) {
            indexReader.close();
            indexReader = null;
        }
    }

    protected void closeIndexSearcher() throws IOException {
        if (indexSearcher != null) {
            indexSearcher.close();
            indexSearcher = null;
        }
    }

    protected void closeMoreLikeThis() {
        moreLikeThis = null;
    }
}*/
