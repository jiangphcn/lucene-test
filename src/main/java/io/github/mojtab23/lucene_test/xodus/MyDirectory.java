package io.github.mojtab23.lucene_test.xodus;

import jetbrains.exodus.env.ContextualEnvironment;
import jetbrains.exodus.env.StoreConfig;
import jetbrains.exodus.env.Transaction;
import jetbrains.exodus.vfs.*;
import org.apache.lucene.index.IndexFileNames;
import org.apache.lucene.store.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

public class MyDirectory extends BaseDirectory {

    private static final int FIRST_CLUSTER_SIZE = 65536;
    private static final int MAX_CLUSTER_SIZE = 65536 * 16;
    /**
     * Used to generate temp file names in {@link #createTempOutput}.
     */
    private final AtomicLong nextTempFileCounter = new AtomicLong();

    private final ContextualEnvironment env;
     final VirtualFileSystem vfs;


    public MyDirectory(@NotNull final ContextualEnvironment env) {
        this(env, new SingleInstanceLockFactory());

    }

    public MyDirectory(@NotNull final ContextualEnvironment env,
                       @NotNull final LockFactory lockFactory) {
        this(env, StoreConfig.WITHOUT_DUPLICATES, lockFactory);

    }

    public MyDirectory(@NotNull final ContextualEnvironment env,
                       @NotNull final StoreConfig contentsStoreConfig,
                       @NotNull final LockFactory lockFactory) {
        this(env, createDefaultVfsConfig(), contentsStoreConfig, lockFactory);
    }

    public MyDirectory(@NotNull final ContextualEnvironment env,
                       @NotNull final VfsConfig vfsConfig,
                       @NotNull final StoreConfig contentsStoreConfig,
                       @NotNull final LockFactory lockFactory) {
        super(lockFactory);
        this.env = env;
        vfs = new VirtualFileSystem(env, vfsConfig, contentsStoreConfig);
    }

    private static VfsConfig createDefaultVfsConfig() {
        final VfsConfig result = new VfsConfig();
        final ClusteringStrategy clusteringStrategy = new ClusteringStrategy.QuadraticClusteringStrategy(FIRST_CLUSTER_SIZE);
        clusteringStrategy.setMaxClusterSize(MAX_CLUSTER_SIZE);
        result.setClusteringStrategy(clusteringStrategy);
        return result;
    }

    /**
     * done!
     *
     * @return
     * @throws IOException
     */
    @Override
    public String[] listAll() throws IOException {
        ensureOpen();
        final Transaction txn = env.getAndCheckCurrentTransaction();
        final ArrayList<String> allFiles = new ArrayList<>((int) vfs.getNumberOfFiles(txn));
        for (final File file : vfs.getFiles(txn)) {
            allFiles.add(file.getPath());
        }
        final String[] namesArray = allFiles.toArray(new String[allFiles.size()]);
        Arrays.sort(namesArray);
        return namesArray;
    }

    @Override
    public void deleteFile(String name) throws IOException {
        ensureOpen();
        vfs.deleteFile(env.getAndCheckCurrentTransaction(), name);
    }

    @Override
    public long fileLength(String name) throws IOException {
        ensureOpen();
        //todo use file discriptor for file length
        return vfs.getFileLength(env.getAndCheckCurrentTransaction(), openExistingFile(name, true));
    }

    @Override
    public IndexOutput createOutput(String name, IOContext context) throws IOException {
        return new MyIndexOutput(this, name);

    }

    @Override
    public IndexOutput createTempOutput(String prefix, String suffix, IOContext context) throws IOException {
        ensureOpen();

        while (true) {
            final String name = IndexFileNames.segmentFileName(prefix, suffix + "_" + Long.toString(nextTempFileCounter.getAndIncrement(), Character.MAX_RADIX), "tmp");
            if (vfs.openFile(env.getAndCheckCurrentTransaction(), name, false) == null) {
                return new MyIndexOutput(this, name);
            }
        }
    }

    @Override
    public void sync(Collection<String> names) throws IOException {
        //nothing
    }

    @Override
    public void rename(String source, String dest) throws IOException {
        final Transaction txn = env.getAndCheckCurrentTransaction();
        final File file = vfs.openFile(txn, source, false);
        if (file == null) {
            throw new java.io.FileNotFoundException(source);
        }
        final boolean b = vfs.renameFile(txn, file, dest);
        if (!b) {
            throw new FileAlreadyExistsException(dest);
        }
    }

    @Override
    public void syncMetaData() throws IOException {
        //nothing
    }

    @Override
    public IndexInput openInput(String name, IOContext context) throws IOException {
        try {
//            todo
            return new MyIndexInput(this, name);
        } catch (FileNotFoundException e) {
            // if index doesn't exist Lucene awaits an IOException
            throw new java.io.FileNotFoundException(name);
        }
    }

    @Override
    public void close() throws IOException {
        isOpen = false;
        vfs.shutdown();
    }

    public ContextualEnvironment getEnvironment() {
        return env;
    }

    public VirtualFileSystem getVfs() {
        return vfs;
    }

     File openExistingFile(@NotNull final String name, final boolean throwFileNotFound) {
        final File result = vfs.openFile(env.getAndCheckCurrentTransaction(), name, false);
        if (throwFileNotFound && result == null) {
            throw new FileNotFoundException(name);
        }
        return result;
    }
}
