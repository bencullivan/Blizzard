# Blizzard

## Design

**Blizzard** consists of two threads that listen for connections and execute the event loop, respectively. The listener and event loop are non-blocking. The event loop adds all events (which may require a db call or an expensive parsing operation) to an event queue. These events are executed by a processor pool of worker threads in order to ensure that the event loop never blocks or is slowed down.

[Event Loop](#Event-Loop)

[Events](#Events)    

[HTTP](#HTTP)    

[Server](#Server)

### Event Loop

A **BlizzardListener** object runs on a worker thread, listening for new [SocketChannel](https://docs.oracle.com/javase/7/docs/api/java/nio/channels/SocketChannel.html) connections and adding new connections to an [ArrayBlockingQueue](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ArrayBlockingQueue.html) of connections waiting to be accepted.

The **EventLoop** runs on the main thread and has four separate components. They are run in an infinite loop, performing their jobs one after another. 

Here is what happens during each iteration of the **EventLoop**.

The **BlizzardAcceptor** accepts new SocketChannel connections from the queue and pairs them with a [Selector](https://docs.oracle.com/javase/7/docs/api/java/nio/channels/Selector.html) that determines which SocketChannels can be read from and a Selector that determines which SocketChannels can be written to. The **BlizzardAcceptor** also pairs each connection with a **BlizzardMessage** object that handles parsing of the bytes from the buffers that are read from the SocketChannel.

The **BlizzardReader** has the read Selector determine which SocketChannels can be read from. It then reads bytes from the available SocketChannels (if there are any) into the [ByteBuffers](https://docs.oracle.com/javase/7/docs/api/java/nio/ByteBuffer.html) of the SocketChannels' **BlizzardMessage** objects. For each read operation a **ProcessMessageEvent** is added to the event queue.

The **BlizzardProcessor** polls the request queue to see if there are any complete http message request objects. 

### Events

### HTTP

### Server

## Testing 

All of the unit tests were written using JUnit. In order to run the tests run BlizzardTestRunner.
