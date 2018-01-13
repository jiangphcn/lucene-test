package io.github.mojtab23.lucene_test.xodus;

import jetbrains.exodus.vfs.File;
import jetbrains.exodus.vfs.VirtualFileSystem;
import org.apache.lucene.store.BufferedChecksum;
import org.apache.lucene.store.IndexOutput;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class MyIndexOutput extends IndexOutput {


    @NotNull
    private final Checksum crc;
    @NotNull
    private OutputStream output;
    private long currentPosition;


    public MyIndexOutput(@NotNull final MyDirectory directory,
                             @NotNull final String name) {
        super("ExodusIndexOutput(name=\"" + name + "\")", name);

        crc = new BufferedChecksum(new CRC32());
        final VirtualFileSystem vfs = directory.getVfs();
        final File file = vfs.openFile(directory.getEnvironment().getAndCheckCurrentTransaction(), name, true);
        if (file == null) {
            throw new NullPointerException("Can't be");
        }
        output = vfs.writeFile(directory.getEnvironment().getAndCheckCurrentTransaction(), file);
        currentPosition = 0;
    }


    @Override
    public void close() throws IOException {
        output.close();
    }

    @Override
    public long getFilePointer() {
        return currentPosition;
    }

    @Override
    public long getChecksum() throws IOException {
        return crc.getValue();
    }

    @Override
    public void writeByte(byte b) throws IOException {
        output.write(b);
        crc.update(b);
        ++currentPosition;
    }

    @Override
    public void writeBytes(byte[] b, int offset, int length) throws IOException {

        crc.update(b, offset, length);
        if (length > 0) {
//            if (length == 1) {
//                writeByte(b[offset]);
//            } else {
                output.write(b, offset, length);
                currentPosition += length;
//            }
        }
    }
}
