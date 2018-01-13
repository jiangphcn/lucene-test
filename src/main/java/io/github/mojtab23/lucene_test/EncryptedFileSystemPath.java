package io.github.mojtab23.lucene_test;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.*;
import java.util.Iterator;

class EncryptedFileSystemPath implements Path {
    final Path subFSPath;
    final EncryptedFileSystem encFs;
    private final FileSystem subFS = FileSystems.getDefault();

    EncryptedFileSystemPath(Path subFSPath, EncryptedFileSystem encFs) {
//            this.subFS = subFS;
        this.subFSPath = subFSPath;
        this.encFs = encFs;

    }

    static EncryptedFileSystemPath checkPath(Path path) {
        if (path == null) {
            throw new NullPointerException();
        } else if (!(path instanceof EncryptedFileSystemPath)) {
            throw new ProviderMismatchException();
        } else {
            return (EncryptedFileSystemPath) path;
        }
    }

    @Override
    public FileSystem getFileSystem() {
        return encFs;
    }

    @Override
    public Path getParent() {
        return mantle(subFSPath.getParent());
    }

    @Override
    public Path getRoot() {
        return mantle(subFSPath.getRoot());
    }

    @Override
    public Path getFileName() {
        return mantle(subFSPath.getFileName());
    }

    @Override
    public Path getName(int index) {
        return mantle(subFSPath.getName(index));
    }

    @Override
    public int getNameCount() {
        return subFSPath.getNameCount();
    }

    @Override
    public Path subpath(int beginIndex, int endIndex) {
        return mantle(subFSPath.subpath(beginIndex, endIndex));
    }

    @Override
    public boolean isAbsolute() {
        return subFSPath.isAbsolute();
    }

    @Override
    public boolean startsWith(Path other) {
        return subFSPath.startsWith(EncryptedFileSystem.dismantle(other));
    }

    @Override
    public boolean startsWith(String other) {
        return subFSPath.startsWith(other);
    }

    @Override
    public boolean endsWith(Path other) {
        return subFSPath.endsWith(EncryptedFileSystem.dismantle(other));
    }

    @Override
    public boolean endsWith(String other) {
        return subFSPath.endsWith(other);
    }

    @Override
    public Path normalize() {
        return mantle(subFSPath.normalize());
    }

    @Override
    public Path resolve(Path other) {
        return mantle(subFSPath.resolve(EncryptedFileSystem.dismantle(other)));
    }

    @Override
    public Path resolve(String other) {
        return mantle(subFSPath.resolve(other));
    }

    @Override
    public Path resolveSibling(Path other) {
        return mantle(subFSPath.resolveSibling(EncryptedFileSystem.dismantle(other)));
    }

    @Override
    public Path resolveSibling(String other) {
        return mantle(subFSPath.resolveSibling(other));
    }

    @Override
    public Path relativize(Path other) {
        return mantle(subFSPath.relativize(EncryptedFileSystem.dismantle(other)));
    }

    @Override
    public int compareTo(Path other) {
        final EncryptedFileSystemPath checkPath = checkPath(other);


        return subFSPath.compareTo(EncryptedFileSystem.dismantle(checkPath));
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof EncryptedFileSystemPath && subFSPath.equals(EncryptedFileSystem.dismantle((EncryptedFileSystemPath) other));
    }

    @Override
    public int hashCode() {
        return subFSPath.hashCode();
    }

    @Override
    public Path toAbsolutePath() {
        return mantle(subFSPath.toAbsolutePath());
    }

    @Override
    public Path toRealPath(LinkOption... options) throws IOException {
        return mantle(subFSPath.toRealPath(options));
    }

    @Override
    public File toFile() {
        return subFSPath.toFile();
    }

    @Override
    public URI toUri() {
        String ssp = subFSPath.toUri().getSchemeSpecificPart();
        return URI.create(subFS.provider().getScheme() + ":" + ssp);
    }

    @Override
    public Iterator<Path> iterator() {
        final Iterator<Path> itr = subFSPath.iterator();
        return new Iterator<Path>() {
            @Override
            public boolean hasNext() {
                return itr.hasNext();
            }

            @Override
            public Path next() {
                return mantle(itr.next());
            }

            @Override
            public void remove() {
                itr.remove();
            }
        };
    }

    @Override
    public WatchKey register(WatchService watcher,
                             WatchEvent.Kind<?>[] events, WatchEvent.Modifier... modifiers) {
        throw new UnsupportedOperationException("not implemented");
    }

    @Override
    public WatchKey register(WatchService watcher,
                             WatchEvent.Kind<?>... events) {
        throw new UnsupportedOperationException("not implemented");
    }

    private Path mantle(Path path) {
        return (path != null) ? new EncryptedFileSystemPath(path, encFs)
                : null;
    }

    @Override
    public String toString() {
        return subFSPath.toString();
    }
}
