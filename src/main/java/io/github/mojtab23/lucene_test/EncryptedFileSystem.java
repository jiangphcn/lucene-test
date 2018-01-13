package io.github.mojtab23.lucene_test;
/*
 * Encrypted Java FileSystem
 *
 * Copyright 2014 Agitos GmbH, Florian Sager, sager@agitos.de, http://www.agitos.de
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * A licence was granted to the ASF by Florian Sager on 31 December 2014
 */

import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.nio.file.spi.FileSystemProvider;
import java.util.Iterator;
import java.util.Set;

/*
 * FileSystem implementation that works like a layer above the base file system.
 * File system operations are delegated to the base file system except Path operations.
 */

class EncryptedFileSystem extends FileSystem {
    private final FileSystemProvider provider;
    private final FileSystem subFileSystem = FileSystems.getDefault();
     final SecretKeySpec secretKeySpec;
     final Path rootPath;

    EncryptedFileSystem(FileSystemProvider provider, Path rootPath, SecretKeySpec secretKeySpec) {
        this.provider = provider;
        this.rootPath = rootPath;
        this.secretKeySpec = secretKeySpec;
    }

    static Path dismantle(Path mantle) {
        if (mantle == null)
            throw new NullPointerException();
        if (!(mantle instanceof EncryptedFileSystemPath))
            throw new ProviderMismatchException();
        return ((EncryptedFileSystemPath) mantle).subFSPath;
    }

    @Override
    public FileSystemProvider provider() {
        return provider;
    }

    @Override
    public void close() throws IOException {
        subFileSystem.close();
    }

    @Override
    public boolean isOpen() {
        return subFileSystem.isOpen();
    }

    @Override
    public boolean isReadOnly() {
        return subFileSystem.isReadOnly();
    }

    @Override
    public String getSeparator() {
        return subFileSystem.getSeparator();
    }

    private EncryptedFileSystem getThis() {
        return this;
    }

    @Override
    public Iterable<Path> getRootDirectories() {
        final Iterable<Path> roots = subFileSystem.getRootDirectories();
        return () -> {
            final Iterator<Path> itr = roots.iterator();

            return new Iterator<Path>() {
                @Override
                public boolean hasNext() {
                    return itr.hasNext();
                }

                @Override
                public Path next() {
                    return new EncryptedFileSystemPath(itr.next(), getThis());
                }

                @Override
                public void remove() {
                    itr.remove();
                }
            };
        };
    }

    @Override
    public Iterable<FileStore> getFileStores() {
        return subFileSystem.getFileStores();
    }

    @Override
    public Set<String> supportedFileAttributeViews() {
        return subFileSystem.supportedFileAttributeViews();
    }

    @Override
    public Path getPath(String first, String... more) {
        return new EncryptedFileSystemPath(subFileSystem.getPath(first, more), this);
    }

    @Override
    public PathMatcher getPathMatcher(String syntaxAndPattern) {
        final PathMatcher matcher = subFileSystem
                .getPathMatcher(syntaxAndPattern);
        return path -> matcher.matches(dismantle(path));
    }

    @Override
    public UserPrincipalLookupService getUserPrincipalLookupService() {
        return subFileSystem.getUserPrincipalLookupService();
    }

    @Override
    public WatchService newWatchService() throws IOException {
        throw new UnsupportedOperationException();
    }

//    public FileSystem getSubFileSystem() {
//        return this.subFileSystem;
//    }

}