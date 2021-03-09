package com.concurrentperformance.throughput.apps;

import com.concurrentperformance.throughput.comms.lifecycle.ConnectionLifecycle;
import com.concurrentperformance.throughput.comms.lifecycle.impl.SkeletalConnectionLifecycle;
import com.concurrentperformance.throughput.identity.Identity;
import com.concurrentperformance.throughput.identity.StringIdentity;
import com.concurrentperformance.throughput.transport.Messages;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;
import com.google.protobuf.ByteString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.misc.Unsafe;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * TODO Comments
 *
 * @author Lake
 */
public class ConnectionManager extends SkeletalConnectionLifecycle implements ConnectionLifecycle, DataspaceGetter  {

    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private Socket socket;
    private ConcurrentMap<Long, SettableFuture<Dataspace>> dataspaceIdToFuture = new ConcurrentHashMap<>();
    private BlockingQueue<Messages.MSG> outgoingMessages = new ArrayBlockingQueue<Messages.MSG>(1024);

    protected ConnectionManager(Identity iAmA) {
        super(iAmA);
    }

    @Override
    public ListenableFuture<Dataspace> getDataspace(long dataspaceId, long size) throws InterruptedException {
        SettableFuture<Dataspace> future = SettableFuture.create();
        dataspaceIdToFuture.put(dataspaceId, future);

        Messages.MSG.Builder builder = Messages.MSG.newBuilder();
        builder.setType(Messages.MSG.Type.REQUEST);
        builder.setDataspaceId(dataspaceId);
        builder.setDataspaceSizeBytes(size);
        outgoingMessages.put(builder.build());

        return future;
    }



    @Override
    protected void startComponents(Socket socket, String baseThreadName) throws IOException {
        log.info("startComponents");
        this.socket = socket;
        InputStream inputStream = socket.getInputStream();
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(10_000_000);

        Thread reader = new Thread(() -> {
            try {
                while (true) {
                    Messages.MSG message = Messages.MSG.parseDelimitedFrom(inputStream);
                    //log.info("Receiving [{}]", message.getType());
                    switch (message.getType()) {
                        case IDENTITY: {
                            receiveHandshakeFromRemote(new StringIdentity(message.getIdentityName()));
                        }
                        break;
                        case REQUEST: {
                            Messages.MSG.Builder response = Messages.MSG.newBuilder();
                            response.setType(Messages.MSG.Type.RESPONSE);
                            response.setDataspaceId(message.getDataspaceId());
                            byteBuffer.position(0);
                            response.setValues(ByteString.copyFrom(byteBuffer, (int)message.getDataspaceSizeBytes()));
                            outgoingMessages.put(response.build());
                        }
                        break;
                        case RESPONSE: {
                            long dataspaceId = message.getDataspaceId();
                            SettableFuture<Dataspace> dataspaceSettableFuture = dataspaceIdToFuture.get(dataspaceId);
                            dataspaceSettableFuture.set(new Dataspace(dataspaceId));
                        }
                    }
                }
            } catch (IOException | InterruptedException e) {
            log.error("TODO", e);
        }

        });
        reader.setName("reader");
        reader.start();

        final OutputStream outputStream = socket.getOutputStream();
        Thread writer = new Thread(() -> {
            try {
                while (true) {
                    Messages.MSG message = outgoingMessages.take();
                    //log.info("Sending [{}]", message.getType());

                    message.writeDelimitedTo(outputStream);
                    outputStream.flush();
                }
            } catch (InterruptedException | IOException e) {
                log.error("TODO", e);
            }
        });
        writer.setName("writer");
        writer.start();
    }

    public static Unsafe getTheUnsafe() {
        try {
            Field theUnsafe = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafe.setAccessible(true);
            return (Unsafe) theUnsafe.get(null);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    @Override
    protected void sendHandshakeToRemote(Identity iAmA) throws IOException {
        log.info("sendHandshakeToRemote");

        Messages.MSG.Builder builder = Messages.MSG.newBuilder();

        builder.setType(Messages.MSG.Type.IDENTITY);
        builder.setIdentityName(iAmA.getName());
        Messages.MSG identity = builder.build();

        OutputStream outputStream = socket.getOutputStream();
        identity.writeDelimitedTo(outputStream);
        outputStream.flush();

    }

    @Override
    protected void signalImminentStopToRemoteSocket() {

        log.info("signalImminentStopToRemoteSocket");
    }

    @Override
    protected void stopComponents() {

        log.info("stopComponents");
    }

    @Override
    protected void doUpdateThreadName(String baseThreadName) {

        log.info("doUpdateThreadName");
    }

    protected void notifyStarted() {
       super.notifyStarted();


    }
}
