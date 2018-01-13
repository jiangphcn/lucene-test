package io.github.mojtab23.lucene_test.xodus;

import jetbrains.exodus.env.ContextualEnvironment;
import jetbrains.exodus.env.StoreConfig;
import org.apache.lucene.store.*;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

public class DebugMyDirectory extends Directory {

    private final IOContext ioContext = new IOContext();
    private final MyDirectory directory;
    private final RAMDirectory debugDirectory;

    public DebugMyDirectory(@NotNull final ContextualEnvironment env) throws IOException {
        this(env, new SingleInstanceLockFactory());
    }

    public DebugMyDirectory(@NotNull final ContextualEnvironment env,
                            @NotNull final LockFactory lockFactory) throws IOException {
        this(env, StoreConfig.WITH_DUPLICATES, lockFactory);
    }

    public DebugMyDirectory(@NotNull final ContextualEnvironment env,
                            @NotNull final StoreConfig contentsStoreConfig,
                            @NotNull final LockFactory lockFactory) throws IOException {
        directory = new MyDirectory(env, contentsStoreConfig, NoLockFactory.INSTANCE);
        debugDirectory = new RAMDirectory(lockFactory);
        final String[] listAll = directory.listAll();
        for (String file : listAll) {
            debugDirectory.copyFrom(directory, file, file, ioContext);
        }
    }

//    public DebugMyDirectory(@NotNull final ContextualEnvironment env,
//                            @NotNull final VfsConfig vfsConfig,
//                            @NotNull final StoreConfig contentsStoreConfig,
//                            @NotNull final LockFactory lockFactory) throws IOException {
//        directory = new MyDirectory(env, vfsConfig, contentsStoreConfig, NoLockFactory.INSTANCE);
//        debugDirectory = new RAMDirectory(lockFactory);
//        final String[] listAll = directory.listAll();
//        for (String file : listAll) {
//            debugDirectory.copyFrom(directory, file, file, ioContext);
//        }
//    }

    private static void throwDebugMismatch() {
        throw new RuntimeException("Debug directory mismatch");
    }

    @Override
    public String[] listAll() throws IOException {
        final String[] result = directory.listAll();
        final String[] debugResult = debugDirectory.listAll();
        if (result.length != debugResult.length) {
            throwDebugMismatch();
        }
        Arrays.sort(result);
        Arrays.sort(debugResult);
        int i = 0;
        for (String file : debugResult) {
            if (!file.equals(result[i++])) {
                throwDebugMismatch();
            }
        }
        return result;
    }

    @Override
    public void deleteFile(String name) throws IOException {
        directory.deleteFile(name);
        debugDirectory.deleteFile(name);
    }

    @Override
    public long fileLength(String name) throws IOException {
        final long result = directory.fileLength(name);
        if (result != debugDirectory.fileLength(name)) {
            throwDebugMismatch();
        }
        return result;
    }

    @Override
    public IndexOutput createOutput(String name, IOContext ioContext) throws IOException {
        return new DebugIndexOutput(name);
    }

    @Override
    public IndexOutput createTempOutput(String prefix, String suffix, IOContext context) throws IOException {
        final IndexOutput tempOutput = directory.createTempOutput(prefix, suffix, context);
        final IndexOutput tempOutput1 = debugDirectory.createTempOutput(prefix, suffix, context);
        return new DebugIndexOutput(tempOutput, tempOutput1);
    }

    @Override
    public void sync(Collection<String> names) throws IOException {
        directory.sync(names);
        debugDirectory.sync(names);
    }

    @Override
    public void rename(String source, String dest) throws IOException {
        directory.rename(source, dest);
        debugDirectory.rename(source, dest);
    }

    @Override
    public void syncMetaData() throws IOException {
        directory.syncMetaData();
        debugDirectory.syncMetaData();
    }

    @Override
    public IndexInput openInput(String name, IOContext ioContext) throws IOException {
        return new DebugIndexInput(name);
    }

    @Override
    public Lock obtainLock(String name) throws IOException {
        directory.obtainLock(name);
        return debugDirectory.obtainLock(name);
    }

    @Override
    public void close() throws IOException {
        directory.close();
        debugDirectory.close();
    }

    private class DebugIndexOutput extends IndexOutput {

        private final IndexOutput output;
        private final IndexOutput debugOutput;

        private DebugIndexOutput(String name) throws IOException {
            super("DebugIndexOutput(name=\"" + name + "\")", name);
            output = directory.createOutput(name, ioContext);
            debugOutput = debugDirectory.createOutput(name, ioContext);
        }

        private DebugIndexOutput(IndexOutput output, IndexOutput debugOutput) {
            super("DebugIndexOutput(name=\"" + output.getName() + "\")", output.getName());
            this.output = output;
            this.debugOutput = debugOutput;
        }


//        @Override
//        public void flush() throws IOException {
//            output.flush();
//            debugOutput.flush();
//        }

        @Override
        public void close() throws IOException {
            output.close();
            debugOutput.close();
        }

        @Override
        public long getFilePointer() {
            final long result = output.getFilePointer();
            if (result != debugOutput.getFilePointer()) {
                throwDebugMismatch();
            }
            return result;
        }

        @Override
        public long getChecksum() throws IOException {
            final long checksum = output.getChecksum();
            final long checksum1 = debugOutput.getChecksum();
            if (checksum != checksum1) {
                throwDebugMismatch();

            }
            return checksum;
        }

//        @Override
//        public void seek(long pos) throws IOException {
//            output.seek(pos);
//            debugOutput.seek(pos);
//        }

//        @Override
//        public long length() throws IOException {
//            final long result = output.length();
//            if (result != debugOutput.length()) {
//                throwDebugMismatch();
//            }
//            return result;
//        }

        @Override
        public void writeByte(byte b) throws IOException {
            output.writeByte(b);
            debugOutput.writeByte(b);
        }

        @Override
        public void writeBytes(byte[] b, int offset, int length) throws IOException {
            if (getName().equals("_0.fdx") && length == 1) {
                boolean bo = true;
            }
            output.writeBytes(b, offset, length);
            debugOutput.writeBytes(b, offset, length);
        }
    }

    @SuppressWarnings("CloneableClassInSecureContext")
    private class DebugIndexInput extends IndexInput {
        private String name;
        private IndexInput input;
        private IndexInput debugInput;

        private DebugIndexInput(String name) throws IOException {
            super("DebugIndexInput for " + name);
            this.name = name;
            input = directory.openInput(name, ioContext);
            debugInput = debugDirectory.openInput(name, ioContext);
        }

        private DebugIndexInput(DebugIndexInput input, IndexInput debugInput) throws IOException {
            super("DebugIndexInput for " + input.name);
            this.input = input;
            this.debugInput = debugInput;
        }


        @Override
        public void close() throws IOException {
            input.close();
            debugInput.close();
        }

        @Override
        public long getFilePointer() {
            final long result = input.getFilePointer();
            if (result != debugInput.getFilePointer()) {
                throwDebugMismatch();
            }
            return result;
        }

        @Override
        public void seek(long pos) throws IOException {
            input.seek(pos);
            debugInput.seek(pos);
        }

        @Override
        public long length() {
            final long result = input.length();
            if (result != debugInput.length()) {
                throwDebugMismatch();
            }
            return result;
        }

        @Override
        public byte readByte() throws IOException {
            final byte result = input.readByte();
            if (result != debugInput.readByte()) {
                throwDebugMismatch();
            }
            return result;
        }

        @Override
        public void readBytes(byte[] b, int offset, int len) throws IOException {
            final long before = getFilePointer();
            input.readBytes(b, offset, len);
            final byte[] bytes = new byte[len];
            debugInput.readBytes(bytes, 0, len);
            final long after = getFilePointer();
            for (int i = 0; i < (int) (after - before); ++i) {
                if (bytes[i] != b[offset + i]) {
                    throwDebugMismatch();
                }
            }
        }

        @Override
        public final IndexInput clone() {
            final DebugIndexInput result = (DebugIndexInput) super.clone();
            result.input = input.clone();
            result.debugInput = debugInput.clone();
            return result;
        }

        @Override
        public IndexInput slice(String sliceDescription, long offset, long length) throws IOException {

            final IndexInput slice = input.slice(sliceDescription, offset, length);
            final IndexInput slice1 = debugInput.slice(sliceDescription, offset, length);
            return new DebugIndexInput((DebugIndexInput) slice, slice1);
        }
    }

}
