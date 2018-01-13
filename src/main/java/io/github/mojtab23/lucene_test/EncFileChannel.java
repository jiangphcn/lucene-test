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
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;

public class EncFileChannel implements SeekableByteChannel {
    private static final Logger logger = LoggerFactory.getLogger(EncFileChannel.class);


    private static final long BLOCK_SIZE = 16;
    private final SecretKeySpec secretKeySpec;
    private final Path fileSystemRoot;
    private final Path path;
    private Cipher cipher;
    private FileChannel fileChannel;
    private long position = 0;

    public EncFileChannel(Path filePath, EncryptedFileSystem fileSystem,
                          Set<? extends OpenOption> options, FileAttribute<?>... attrs) {

        this.secretKeySpec = fileSystem.secretKeySpec;
        this.fileSystemRoot = fileSystem.rootPath;
        path = filePath.normalize();
        try {
            cipher = Cipher.getInstance(EncryptedFileSystemProvider.CIPHER_TRANSFORMATION);
            fileChannel = FileChannel.open(path, options, attrs);

        } catch (IOException | NoSuchPaddingException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

    }


    @Override
    public int read(ByteBuffer dst) throws IOException {

        final int blockStartOffset = (int) (position % BLOCK_SIZE);
        final long startPos = position - blockStartOffset;
        final long blockNumber = position / BLOCK_SIZE;
        final long fileSize = fileChannel.size();
        final int requestedSize = dst.remaining();
        final long virtualEnd = position + requestedSize;
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
        final int read = fileChannel.read(byteBuffer, startPos);
        if (read != length) {
            throw new IOException("error miss match size! expected:" + length + " ,read:" + read);
        }

//        update position
        position += availableSize;

        final byte[] decrypted = decrypt(byteBuffer, blockNumber);

        dst.put(decrypted, blockStartOffset, availableSize);
        return availableSize;
    }

    private byte[] decrypt(ByteBuffer byteBuffer, long blockNumber) {
        try {
//            Cipher cipher = Cipher.getInstance(EncryptedFileSystemProvider.CIPHER_TRANSFORMATION);

            final byte[] iv = createIV(blockNumber);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
            return cipher.doFinal(byteBuffer.array());
        } catch (NoSuchAlgorithmException | InvalidKeyException |
                InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException ignored) {
//            unreachable!
        }

        return new byte[0];
    }

    @Override
    public int write(ByteBuffer src) throws IOException {

        final long gapPosition;
        final boolean hasGap;
        final int gapLength;

        final long fileSize = fileChannel.size();
        if (position > fileSize) {
            hasGap = true;
            if (fileSize > 0) {
                gapPosition = fileSize - 1;
            } else {
                gapPosition = 0;
            }
            gapLength = (int) (position - gapPosition);
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
            blockStartOffset = (int) (position % BLOCK_SIZE);
            blockNumber = position / BLOCK_SIZE;
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


        position += write - gapLength;
        return write - gapLength;

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
            return cipher.doFinal(bytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException |
                InvalidAlgorithmParameterException | BadPaddingException | IllegalBlockSizeException e) {
            logger.error("error", e);
        }

        return new byte[0];
    }


    @Override
    public long position() throws IOException {
        return position;
    }

    @Override
    public SeekableByteChannel position(long newPosition) throws IOException {
        position = newPosition;
        return this;
    }

    @Override
    public long size() throws IOException {
        return fileChannel.size();
    }

    @Override
    public SeekableByteChannel truncate(long size) throws IOException {
        fileChannel.truncate(size);
        if (size < position) position = size;
        return this;
    }

    @Override
    public boolean isOpen() {
        return fileChannel.isOpen();
    }

    @Override
    public void close() throws IOException {
        fileChannel.close();
    }


    private byte[] createIV(long blockNumber) throws NoSuchAlgorithmException {
        final byte[] bytes = fileSystemRoot.relativize(path).toString().getBytes();
        final MessageDigest md = MessageDigest.getInstance("MD5");
        final byte[] digest = md.digest(bytes);
        ByteBuffer buffer = ByteBuffer.wrap(digest);
        buffer.position(8);
        buffer.putLong(blockNumber);
        return buffer.array();
    }

}
