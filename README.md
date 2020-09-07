# Blizzard

## Design

[Event Loop](#Event-Loop)

[Events](#Events)    

[HTTP](#HTTP)    

[Server](#Server)

### Event Loop

A **Listener** object runs on a worker thread, listening for new [SocketChannel](https://docs.oracle.com/javase/7/docs/api/java/nio/channels/SocketChannel.html) connections and adding new connections to a queue of connections waiting to be accepted.

The **EventLoop** runs on the main thread and has four separate components. They are run in an infinite loop, performing their jobs one after another. 

The **Acceptor** accepts new connections from the queue and pairs them with a [Selector](https://docs.oracle.com/javase/7/docs/api/java/nio/channels/Selector.html) that determines which channels can be read from and a Selector that determines which channels can be written to. The **Acceptor** also pairs each connection with a [BlizzardMessage](#HTTP) object that handles parsing of the bytes from the buffers that are read from the connection's SocketChannel.

### Events

### HTTP

### Server

## Testing 

All of the unit tests were written using JUnit. In order to run the tests run BlizzardTestRunner.
