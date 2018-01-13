package io.github.mojtab23.lucene_test.xodus;

import jetbrains.exodus.vfs.ClusteringStrategy;
import jetbrains.exodus.vfs.File;
import jetbrains.exodus.vfs.VfsInputStream;
import org.apache.lucene.store.IndexInput;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

public class MyIndexInput extends IndexInput {


    @NotNull
    private final MyDirectory directory;
    @NotNull
    private final File file;
//    private boolean isClone = false;
    @NotNull
    private VfsInputStream input;
    private long currentPosition;
    private long offset = 0;
    private long end;


    //    todo need to use context
    public MyIndexInput(@NotNull final MyDirectory directory,
                        @NotNull final String name) {
        this(directory, name, 0L);
    }

    private MyIndexInput(@NotNull final MyDirectory directory,
                         @NotNull final String name,
                         final long currentPosition) {
        super("ExodusDirectory IndexInput for " + name);
        this.directory = directory;
        this.file = directory.openExistingFile(name, true);
        input = directory.getVfs().readFile(directory.getEnvironment().getAndCheckCurrentTransaction(), file, currentPosition);
        this.currentPosition = currentPosition;
    }

    public MyIndexInput(@NotNull String name, @NotNull MyDirectory directory, long off, long length) {

        this(directory, name);
        this.offset = off;
        this.end = off + length;

    }

    @Override
    public void close() throws IOException {
            input.close();
    }

    @Override
    public long getFilePointer() {
        return currentPosition;
    }

    @Override
    public void seek(long pos) throws IOException {
        if (pos != currentPosition) {
            if (pos > currentPosition) {
                final ClusteringStrategy clusteringStrategy = directory.getVfs().getConfig().getClusteringStrategy();
                final long bytesToSkip = pos - currentPosition;
                final int clusterSize = clusteringStrategy.getFirstClusterSize();
                if ((!clusteringStrategy.isLinear() ||
                        (currentPosition % clusterSize) + bytesToSkip < clusterSize) // or we are within single cluster
                        && input.skip(bytesToSkip) == bytesToSkip) {
                    currentPosition = pos;
                    return;
                }
            }
            input.close();
            input = directory.getVfs().readFile(directory.getEnvironment().getAndCheckCurrentTransaction(), file, pos);
            currentPosition = pos;
        }
    }

    @Override
    public long length() {
        return directory.getVfs().getFileLength(directory.getEnvironment().getAndCheckCurrentTransaction(), file);
    }

    @Override
    public IndexInput slice(String sliceDescription, long offset, long length) throws IOException {
        if (offset < 0 || length < 0 || offset + length > this.length()) {
            throw new IllegalArgumentException("slice() " + sliceDescription + " out of bounds: offset=" + offset + ",length=" + length + ",fileLength=" + this.length() + ": " + this);
        }


        return new MyIndexInput(getFullSliceDescription(sliceDescription),
                directory, this.offset + offset, length);
    }

    @Override
    public byte readByte() throws IOException {
        ++currentPosition;
        return (byte) input.read();
    }

    @Override
    public void readBytes(byte[] b, int offset, int len) throws IOException {
        if (len == 1) {
            b[offset] = readByte();
        } else {
            currentPosition += input.read(b, offset, len);
        }
    }

    protected String getFullSliceDescription(String sliceDescription) {
        if (sliceDescription == null) {
            // Clones pass null sliceDescription:
            return toString();
        } else {
            return toString() + " [slice=" + sliceDescription + "]";
        }
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public final IndexInput clone() {
        final MyIndexInput myIndexInput = new MyIndexInput(directory, file.getPath(), currentPosition);
        myIndexInput.offset = offset;
        myIndexInput.end = end;
        return myIndexInput;
    }

}
