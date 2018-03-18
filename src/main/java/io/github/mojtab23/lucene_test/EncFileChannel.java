package io.github.mojtab23.lucene_test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.*;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

public class EncFileChannel extends FileChannel {
    private static final Logger logger = LoggerFactory.getLogger(EncFileChannel.class);


    private static final int BLOCK_SIZE = 16;
    private static final int HEADER_SIZE = 8;
    private final SecretKeySpec secretKeySpec;
    //    private final Path fileSystemRoot;
    private final Path path;
    private Cipher cipher;
    private FileChannel fileChannel;
    private long position = HEADER_SIZE;
    private byte[] fileIV;

    public EncFileChannel(Path filePath, EncryptedFileSystem fileSystem,
                          Set<? extends OpenOption> options, FileAttribute<?>... attrs) throws IOException {

        this.secretKeySpec = fileSystem.secretKeySpec;
//        this.fileSystemRoot = fileSystem.rootPath;
        path = filePath.normalize();
        try {
            cipher = Cipher.getInstance(EncryptedFileSystemProvider.CIPHER_TRANSFORMATION);
            final EnumSet<StandardOpenOption> read = EnumSet.of(StandardOpenOption.READ);
            read.addAll((Collection<? extends StandardOpenOption>) options);
            fileChannel = FileChannel.open(path, read, attrs);
            checkHeader();
        } catch (NoSuchPaddingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }

    private void checkHeader() {
        try {
            final long size = fileChannel.size();
            if (size < HEADER_SIZE) {
                System.out.println("need new IV");
                fileIV = createFileIV();
                final int write = fileChannel.write(ByteBuffer.wrap(fileIV));
                assert write == HEADER_SIZE;
            } else {
                System.out.println("there is an IV");
                final ByteBuffer header = ByteBuffer.allocate(HEADER_SIZE);
                fileChannel.read(header);
                fileIV = header.array();
            }
        } catch (IOException | NonWritableChannelException e) {
//            should ignore IOExceptions.
        }
    }


    @Override
    public int read(ByteBuffer dst) throws IOException {

        final int blockStartOffset = (int) (position() % BLOCK_SIZE);
        final long startPos = position() - blockStartOffset;
        final long blockNumber = position() / BLOCK_SIZE;
        final long fileSize = this.size();
        final int requestedSize = dst.remaining();
        final long virtualEnd = position() + requestedSize;
        final long lastBlockStartOffset = virtualEnd % BLOCK_SIZE;
        final int availableSize;

        if (virtualEnd > fileSize) {
            final long overFlow = virtualEnd - fileSize;
            if (requestedSize == overFlow)
                availableSize = -1;
            else availableSize = (int) (requestedSize - overFlow);


        } else availableSize = requestedSize;
        if (availableSize == -1) {
            return -1;
        }
        final long endPos = Math.min(virtualEnd - lastBlockStartOffset + BLOCK_SIZE, fileSize);

        final int length = (int) (endPos - startPos);
        final ByteBuffer byteBuffer = ByteBuffer.allocate(length);
        final int read = fileChannel.read(byteBuffer, startPos + HEADER_SIZE);
        if (read != length) {
            throw new IOException("error miss match size! expected:" + length + " ,read:" + read);
        }

//        update position
//        position += availableSize;
        position(position() + availableSize);
        final byte[] decrypted = decrypt(byteBuffer, blockNumber);
        System.out.println("decrypted: " + ShowHexEnc.bytesToHex(decrypted, decrypted.length));

        dst.put(decrypted, blockStartOffset, availableSize);
        return availableSize;
    }

    @Override
    public long read(ByteBuffer[] dsts, int offset, int length) {
        throw new UnsupportedOperationException();
//        return 0;
    }

    private byte[] decrypt(ByteBuffer byteBuffer, long blockNumber) {
        try {
//            Cipher cipher = Cipher.getInstance(EncryptedFileSystemProvider.CIPHER_TRANSFORMATION);

            final byte[] iv = createIV(blockNumber);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

            final byte[] array = byteBuffer.array();
            System.out.println("encrypted: " + ShowHexEnc.bytesToHex(array, array.length));

            return cipher.doFinal(array);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException |
                BadPaddingException | IllegalBlockSizeException ignored) {
//            unreachable!
        }

        return new byte[0];
    }

    @Override
    public int write(ByteBuffer src) throws IOException {
//todo check write with gap
        final long gapPosition;
        final boolean hasGap;
        final int gapLength;

//        final long fileSize = fileChannel.size();
        final long fileSize = this.size();
        if (position() > fileSize) {
            hasGap = true;
            if (fileSize > 0) {
                gapPosition = fileSize - 1;
            } else {
                gapPosition = 0;
            }
            gapLength = (int) (position() - gapPosition);
        } else {
            hasGap = false;
            gapPosition = 0;
            gapLength = 0;
        }


        final int blockStartOffset;
        final long blockNumber;
        final int gapBlockStartOffset;
        final long gapBlockNumber;
        if (hasGap) {

            gapBlockNumber = gapPosition / BLOCK_SIZE;
            gapBlockStartOffset = (int) (gapPosition % BLOCK_SIZE);


//            blockStartOffset = (int) (gapPosition % BLOCK_SIZE);
//            blockNumber = gapPosition / BLOCK_SIZE;
            blockNumber = 0;
            blockStartOffset = 0;
        } else {
            gapBlockNumber = 0;
            gapBlockStartOffset = 0;
            blockStartOffset = (int) (position() % BLOCK_SIZE);
            blockNumber = position() / BLOCK_SIZE;
        }


        final int actualLength = src.remaining();

        final int length;
        final ByteBuffer byteBuffer;
        if (hasGap) {
            length = gapBlockStartOffset + gapLength + actualLength;
            byteBuffer = ByteBuffer.allocate(length);
            for (int i = 0; i < gapBlockStartOffset + gapLength; i++) {
                byteBuffer.put((byte) 0);
            }
        } else {
            length = blockStartOffset + actualLength;
            byteBuffer = ByteBuffer.allocate(length);
            for (int i = 0; i < blockStartOffset; i++) {
                byteBuffer.put((byte) 0);
            }
        }

//        final int length = blockStartOffset + actualLength;
//        final ByteBuffer byteBuffer = ByteBuffer.allocate(length);
//        for (int i = 0; i < blockStartOffset; i++) {
//            byteBuffer.put((byte) 0);
//        }
        byteBuffer.put(src);
        byteBuffer.flip();

        final byte[] encrypt = encrypt(byteBuffer, hasGap ? gapBlockNumber : blockNumber);
        System.out.println("encrypted: " + ShowHexEnc.bytesToHex(encrypt, encrypt.length));

        final ByteBuffer allocate;

        if (hasGap) {
            allocate = ByteBuffer.allocate(gapLength + actualLength);
            for (int i = gapBlockStartOffset; i < encrypt.length; i++) {
                allocate.put(encrypt[i]);
            }
        } else {
            allocate = ByteBuffer.allocate(actualLength);
            for (int i = blockStartOffset; i < encrypt.length; i++) {
                allocate.put(encrypt[i]);
            }
        }

//        for (int i = blockStartOffset; i < encrypt.length; i++) {
//            allocate.put(encrypt[i]);
//        }
        allocate.flip();

        final int write = fileChannel.write(allocate, hasGap ? gapPosition : position);

        position(position() + write - gapLength);
//        position += write - gapLength;
        return write - gapLength;

    }

    @Override
    public long write(ByteBuffer[] srcs, int offset, int length) {
        throw new UnsupportedOperationException();
//        return 0;
    }

    private byte[] encrypt(ByteBuffer byteBuffer, long blockNumber) {
        try {
//            this.cipher = Cipher.getInstance(EncryptedFileSystemProvider.CIPHER_TRANSFORMATION);
//            Cipher cipher = this.cipher;
            final int remaining = byteBuffer.remaining();

            final byte[] bytes = new byte[remaining];
            int i = 0;
            while (byteBuffer.hasRemaining()) {
                bytes[i] = byteBuffer.get();
                i++;
            }
            final byte[] iv = createIV(blockNumber);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
            System.out.println("source: " + ShowHexEnc.bytesToHex(bytes, bytes.length));

            return cipher.doFinal(bytes);
        } catch (InvalidKeyException | InvalidAlgorithmParameterException |
                BadPaddingException | IllegalBlockSizeException e) {
            logger.error("error", e);
        }

        return new byte[0];
    }


    @Override
    public long position() throws IOException {
        final long l = position - HEADER_SIZE;
        if (l > 0) return l;
        else return 0;
    }

    @Override
    public EncFileChannel position(long newPosition) throws IOException {
        if (newPosition < 0L) {
            throw new IllegalArgumentException();
        } else {
//            final long l = newPosition - HEADER_SIZE;
//            if (l > 0) position = l;
//            else position = 0;
            position = newPosition + HEADER_SIZE;
            return this;
        }
    }

    @Override
    public long size() throws IOException {
        final long size = fileChannel.size();
        final long l = size - HEADER_SIZE;
        if (l < 0) return 0;
        else return l;
    }

    @Override
    public EncFileChannel truncate(long size) throws IOException {
        fileChannel.truncate(size + HEADER_SIZE);
        if (size < position) position = size;
        return this;
    }

    @Override
    public void force(boolean metaData) throws IOException {
//        todo is correct?
        fileChannel.force(metaData);
    }

    @Override
    public long transferTo(long position, long count, WritableByteChannel target) throws IOException {
        throw new UnsupportedOperationException();
//        return 0;
    }

    @Override
    public long transferFrom(ReadableByteChannel src, long position, long count) throws IOException {
        throw new UnsupportedOperationException();
//        return 0;
    }

    @Override
    public int read(ByteBuffer dst, long position) throws IOException {
//        todo need rewrite
        final long temp = position();
        position(position);
        final int read = read(dst);
        position(temp);
        return read;
    }

    @Override
    public int write(ByteBuffer src, long position) throws IOException {
        //        todo need rewrite
        final long temp = position();
        position(position);
        final int write = write(src);
        position(temp);
        return write;

    }

    @Override
    public MappedByteBuffer map(MapMode mode, long position, long size) throws IOException {
        throw new UnsupportedOperationException();

//        return null;
    }

    @Override
    public FileLock lock(long position, long size, boolean shared) throws IOException {
        //todo position shit
        return fileChannel.lock(position, size, shared);
    }

    @Override
    public FileLock tryLock(long position, long size, boolean shared) throws IOException {
        //todo position shit
        return fileChannel.tryLock(position, size, shared);
    }

//    @Override
//    public boolean isOpen() {
//        return fileChannel.isOpen();
//    }

//    @Override
//    public void close() throws IOException {
//        fileChannel.close();
//    }

    @Override
    protected void implCloseChannel() throws IOException {
        fileChannel.close();
    }


    private byte[] createIV(long blockNumber) {


        ByteBuffer buffer = ByteBuffer.allocate(BLOCK_SIZE);
        buffer.put(fileIV);


        buffer.putLong(blockNumber);
        final byte[] array = buffer.array();
        System.out.println("block NO: " + blockNumber + ", blockIV:" + ShowHexEnc.bytesToHex(array, array.length));

//        final byte[] bytes = fileSystemRoot.relativize(path).toString().getBytes();
//        final MessageDigest md = MessageDigest.getInstance("MD5");
//        final byte[] digest = md.digest(bytes);
//        ByteBuffer buffer = ByteBuffer.wrap(digest);
//        buffer.position(8);
//        buffer.putLong(blockNumber);
        return array;
    }

    private byte[] createFileIV() {
        SecureRandom secureRandom = new SecureRandom();
        final byte[] rand = new byte[HEADER_SIZE];
        secureRandom.nextBytes(rand);

        System.out.println("fileIV:" + ShowHexEnc.bytesToHex(rand, rand.length));


        return rand;
    }

}
