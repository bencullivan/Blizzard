# Blizzard

[Design](#Design)

[Tutorial](#Tutorial)

## Design

**Blizzard** consists of two threads that listen for connections and execute the event loop, respectively. The listener and event loop are non-blocking. The event loop adds all events (which may require a db call or an expensive parsing operation) to an event queue. These events are executed by a processor pool of worker threads in order to ensure that the event loop never blocks or is slowed down.

The url routes (parametrized and non-parametrized) are stored in a custom Trie data structure with their respective callbacks. This allows for efficient lookup time to find the callback of a particular route that has been hit by an HTTP request.

[Event Loop](#Event-Loop)

[Events](#Events)    

[HTTP](#HTTP)    

[Server](#Server)

### Event Loop

A **BlizzardListener** object runs on a worker thread, listening for new [SocketChannel](https://docs.oracle.com/javase/7/docs/api/java/nio/channels/SocketChannel.html) connections and adding new connections to an [ArrayBlockingQueue](https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ArrayBlockingQueue.html) of connections waiting to be accepted.

The **EventLoop** runs on the main thread and has four separate components. They are run in an infinite loop, performing their jobs one after another. 

Here is what happens during each iteration of the **EventLoop**.

The **BlizzardAcceptor** accepts new SocketChannel connections from the queue and pairs them with a [Selector](https://docs.oracle.com/javase/7/docs/api/java/nio/channels/Selector.html) that determines which SocketChannels can be read from and a Selector that determines which SocketChannels can be written to. The **BlizzardAcceptor** also pairs each connection with a **BlizzardMessage** object that handles parsing the bytes from the buffers that are read from the SocketChannel.

The **BlizzardReader** uses the read Selector to determine which SocketChannels can be read from. It then reads bytes from the available SocketChannels (if there are any) into the [ByteBuffers](https://docs.oracle.com/javase/7/docs/api/java/nio/ByteBuffer.html) of the SocketChannels' **BlizzardMessage** objects. For each read operation a **ProcessMessageEvent** is added to the event queue.

The **BlizzardProcessor** polls the request queue to see if there is an available **BlizzardRequest** to be processed. If there is, it creates a **ProcessRequestEvent** which it adds to the event queue. (It sends a maximum of eight requests for processing in each loop iteration.)

The **BlizzardWriter** uses the write Selector to determine which SocketChannels can be written to. For each SocketChannel that is available to be written to, the **BlizzardWriter** writes the bytes from the SocketChannel's **BlizzardOutgoingMessage**.

### Events

The biggest challenge when creating **Blizzard** was dealing with the reception of partial HTTP messages. When using non-blocking IO in Java it is possible for messages to be read only partially from their SocketChannels. This made figuring out how to parse and store messages as well as when a full message has actually been received very difficult. 

To solve this problem I created several custom parsing algorithms that are run by the **BlizzardMessage** objects during **ProcessMessageEvents**. The algorithms solve all of the problems I just mentioned and run in linear time with respect to the length of the message (in bytes). I will not explain them in depth here but if you are interested in seeing how they work check out the code in **BlizzardMessage.java**.

A **ProcessMessageEvent** handles the parsing of bytes from a buffer that was just read into. 

### HTTP

### Server

## Testing 

All of the unit tests were written using JUnit. In order to run the tests run BlizzardTestRunner.
