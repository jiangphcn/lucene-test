/*
package io.github.mojtab23.lucene_test.xodus;

*/
/**
 * Copyright 2010 - 2017 JetBrains s.r.o.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *//*


import jetbrains.exodus.vfs.File;
import jetbrains.exodus.vfs.VirtualFileSystem;
import org.apache.lucene.store.BufferedChecksum;
import org.apache.lucene.store.IndexOutput;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public class ExodusIndexOutput extends IndexOutput {

    @NotNull
    private final Checksum crc;
    @NotNull
    private OutputStream output;
    private long currentPosition;


    public ExodusIndexOutput(@NotNull final MyDirectory directory,
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
            if (length == 1) {
                writeByte(b[offset]);
            } else {
                output.write(b, offset, length);
                currentPosition += length;
            }
        }
    }
}*/
