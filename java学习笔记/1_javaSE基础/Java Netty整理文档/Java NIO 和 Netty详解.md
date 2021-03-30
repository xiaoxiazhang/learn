### Java NIO 和 Netty详解



#### 基本概述

Netty 是由 JBOSS 提供的一个 Java 开源框架，现为 Github上的独立项目。Netty 是一个异步的、基于事件驱动的网络应用框架，用以快速开发高性能、高可靠性的网络 IO 程序。Netty主要针对在TCP协议下，面向Clients端的高并发应用，或者Peer-to-Peer场景下的大量数据持续传输的应用。Netty本质是一个NIO框架，适用于服务器通讯相关的多种应用场景。

java共支持3种网络编程模型/IO模式：BIO、NIO、AIO.

* Java BIO ： 同步并阻塞(**传统阻塞型**)，服务器实现模式为一个连接一个线程，即客户端有连接请求时服务器端就需要启动一个线程进行处理，如果这个连接不做任何事情会造成不必要的线程开销，并且对流进行读写操作是阻塞的。【SocketInputStream.read/ SocketOutputStream.write 】

* Java NIO ： **同步非阻塞**，服务器实现模式为一个线程处理多个请求(连接)，即客户端发送的连接请求都会注册到多路复用器上，多路复用器轮询到连接有I/O请求就进行处理。

* Java AIO(NIO.2) ： **异步非阻塞**，AIO 引入异步通道的概念，采用了 Proactor 模式，简化了程序编写，有效的请求才启动线程，它的特点是先由操作系统完成后才通知服务端程序启动线程去处理。

NIO 的类库和 API 繁杂，使用麻烦：需要熟练掌握 Selector、ServerSocketChannel、SocketChannel、ByteBuffer 等。需要具备其他的额外技能：要熟悉 Java 多线程编程，因为 NIO 编程涉及到 Reactor 模式，你必须对多线程和网络编程非常熟悉，才能编写出高质量的 NIO 程序。开发工作量和难度都非常大：例如客户端面临断连重连、网络闪断、半包读写、失败缓存、网络拥塞和异常流的处理等等。

JDK NIO 的 Bug：例如臭名昭著的 Epoll Bug，它会导致 Selector 空轮询，最终导致 CPU 100%。直到 JDK 1.7 版本该问题仍旧存在，没有被根本解决。





#### NIO三大核心组件

1)NIO 有三大核心部分：**Channel(****通道**)**，**Buffer(****缓冲区**)**, **Selector(****选择器**)**

2)NIO是 面向**缓****冲区 ，或者面向** **块** 编程的。数据读取到一个它稍后处理的缓冲区，需要时可在缓冲区中前后移动，这就增加了处理过程中的灵活性，使用它可以提供**非阻塞**式的高伸缩性网络

5)Java NIO的非阻塞模式，使一个线程从某通道发送请求或者读取数据，但是它仅能得到目前可用的数据，如果目前没有数据可用时，就什么都不会获取，而**不是保持线程阻塞**，所以直至数据变的可以读取之前，该线程可以继续做其他的事情。 非阻塞写也是如此，一个线程请求写入一些数据到某通道，但不需要等待它完全写入，这个线程同时可以去做别的事情。【**后面有案例说明**】





**Selector** **、** **Channel** **和** **Buffer** **的****关****系图****(****简单版****)**

**关****系****图的说明****:**

1)每个channel 都会对应一个Buffer

2)Selector 对应一个线程， 一个线程对应多个channel(连接)

3)该图反应了有三个channel 注册到 该selector //程序

4)程序切换到哪个channel 是有事件决定的, Event 就是一个重要的概念

5)Selector 会根据不同的事件，在各个通道上切换

6)Buffer 就是一个内存块 ， 底层是有一个数组

7)数据的读取写入是通过Buffer, 这个和BIO , BIO 中要么是输入流，或者是
 输出流, 不能双向，但是NIO的Buffer 是可以读也可以写, 需要 flip 方法切换

8)channel 是双向的, 可以返回底层操作系统的情况, 比如Linux ， 底层的操作系统
 通道就是双向的.



<img src="/Users/zhangxiaoxia/Library/Application Support/typora-user-images/image-20210305143432589.png" alt="image-20210305143432589" style="zoom:50%;" />







##### Buffer缓冲区

缓冲区（Buffer）：缓冲区本质上是一个可以读写数据的内存块，可以理解成是一个**容****器对象****(****含数组****)**，该对象提供了**一组方法**，可以更轻松地使用内存块，，缓冲区对象内置了一些机制，能够跟踪和记录缓冲区的状态变化情况。Channel 提供从文件、网络读取数据的渠道，但是读取或写入的数据都必须经由 Buffer，如图: 【后面举例说明】



**Buffer** **类及其子类**

**常用****Buffer****子类一览**



1)ByteBuffer，存储字节数据到缓冲区

2)ShortBuffer，存储字符串数据到缓冲区

3)CharBuffer，存储字符数据到缓冲区

4)IntBuffer，存储整数数据到缓冲区

5)LongBuffer，存储长整型数据到缓冲区

6)DoubleBuffer，存储小数到缓冲区

7)FloatBuffer，存储小数到缓冲区



**Buffer** **类及其子类**

2)Buffer类定义了所有的缓冲区都具有的四个属性来提供关于其所包含的数据元素的信息:



// Invariants: mark <= position <= limit <= capacity

  private int mark = -1;

  private int position = 0;

  private int limit;

  private int capacity;



| **属性** | **描述**                                                     |
| -------- | ------------------------------------------------------------ |
| Capacity | 容量，即可以容纳的最大数据量；在缓冲区创建时被设定并且不能改变 |
| Limit    | 表示缓冲区的当前终点，不能对缓冲区超过极限的位置进行读写操作。且极限是可以修改的 |
| Position | 位置，下一个要被读或写的元素的索引，每次读写缓冲区数据时都会改变改值，为下次读写作准备 |
| Mark     | 标记                                                         |

public abstract class **Buffer** {

  //JDK1.4时，引入的api

  public final int capacity( )//返回此缓冲区的容量

  public final int position( )//返回此缓冲区的位置

  public final Buffer position (int newPositio)//设置此缓冲区的位置

  public final int limit( )//返回此缓冲区的限制

  public final Buffer limit (int newLimit)//设置此缓冲区的限制

  public final Buffer mark( )//在此缓冲区的位置设置标记

  public final Buffer reset( )//将此缓冲区的位置重置为以前标记的位置

  public final Buffer clear( )//清除此缓冲区, 即将各个标记恢复到初始状态，但是数据并没有真正擦除, 后面操作会覆盖

  public final Buffer flip( )//反转此缓冲区

  public final Buffer rewind( )//重绕此缓冲区

  public final int remaining( )//返回当前位置与限制之间的元素数

  public final boolean hasRemaining( )//告知在当前位置和限制之间是否有元素

  public abstract boolean isReadOnly( );//告知此缓冲区是否为只读缓冲区

 

  //JDK1.6时引入的api

  public abstract boolean hasArray();//告知此缓冲区是否具有可访问的底层实现数组

  public abstract Object array();//返回此缓冲区的底层实现数组

  public abstract int arrayOffset();//返回此缓冲区的底层实现数组中第一个缓冲区元素的偏移量

  public abstract boolean isDirect();//告知此缓冲区是否为直接缓冲区

}





**ByteBuffer**

从前面可以看出对于 Java 中的基本数据类型(boolean除外)，都有一个 Buffer 类型与之相对应，**最常用**的自然是ByteBuffer 类（二进制数据），该类的主要方法如下：

public abstract class **ByteBuffer** {

  //缓冲区创建相关api

  public static ByteBuffer allocateDirect(int capacity)//创建直接缓冲区

  public static ByteBuffer allocate(int capacity)//设置缓冲区的初始容量

  public static ByteBuffer wrap(byte[] array)//把一个数组放到缓冲区中使用

  //构造初始化位置offset和上界length的缓冲区

  public static ByteBuffer wrap(byte[] array,int offset, int length)

   //缓存区存取相关API

  public abstract byte get( );//从当前位置position上get，get之后，position会自动+1

  public abstract byte get (int index);//从绝对位置get

  public abstract ByteBuffer put (byte b);//从当前位置上添加，put之后，position会自动+1

  public abstract ByteBuffer put (int index, byte b);//从绝对位置上put

 }



##### Channel通道

NIO的通道类似于流，但有些区别如下：通道可以同时进行读写，而流只能读或者只能写；通道可以实现异步读写数据通道可以从缓冲读数据，也可以写数据到缓冲:。

BIO 中的 stream 是单向的，例如 FileInputStream 对象只能进行读取数据的操作，而 NIO 中的通道(Channel)是双向的，可以读操作，也可以写操作。

Channel在NIO中是一个接口 **public interface Channel extends** **Closeable{}** 

常用的 Channel 类有：FileChannel、DatagramChannel、ServerSocketChannel 和 SocketChannel。

【ServerSocketChanne 类似 ServerSocket , SocketChannel 类似 Socket】

FileChannel 用于文件的数据读写，DatagramChannel 用于 UDP 的数据读写，ServerSocketChannel 和 SocketChannel 用于 TCP 的数据读写。





FileChannel主要用来对本地文件进行 IO 操作，常见的方法有

1)public int read(ByteBuffer dst) ：Channel中读入流中数据， 并写入到缓冲区中

2)public int write(ByteBuffer src) ：缓冲区数据写入到Channel中，然后写入到输出流

3)public long transferFrom(ReadableByteChannel src, long position, long count)，从目标通道中复制数据到当前通道

4)public long transferTo(long position, long count, WritableByteChannel target)，把数据从当前通道复制给目标通道



1)ByteBuffer 支持类型化的put 和 get, put 放入的是什么数据类型，get就应该使用相应的数据类型来取出，否则可能有 BufferUnderflowException 异常。[举例说明]

2)可以将一个普通Buffer 转成只读Buffer [举例说明] 

3)NIO 还提供了 MappedByteBuffer， 可以让文件直接在内存（堆外的内存）中进行修改， 而如何同步到文件由NIO 来完成. [举例说明]

前面我们讲的读写操作，都是通过一个Buffer 完成的，NIO 还支持 通过多个Buffer (即 Buffer 数组) 完成读写操作，即 Scattering 和 Gathering



##### Selector选择器

Java 的 NIO，用非阻塞的 IO 方式。可以用一个线程，处理多个的客户端连接，就会使用到**Selector**选择器

**Selector** 能够检测多个注册的通道上是否有事件发生(****注意****:****多个****Channel****以事件的方式可以注册到同一个****Selector)**，如果有事件发生，便获取事件然后针对每个事件进行相应的处理。这样就可以只用一个单线程去管理多个通道，也就是管理多个连接和请求。【示意图】

3)只有在 连接/通道 真正有读写事件发生时，才会进行读写，就大大地减少了系统开销，并且不必为每个连接都创建一个线程，不用去维护多个线程

4)避免了多线程之间的上下文切换导致的开销



**public abstract class Selector implements Closeable** **{** 

**public static Selector open****();//****得****到一个选择器对象**

**public** **int select(long timeout****);//****监****控所有注册的通道，当其中有** **IO** **操作可以进行时，将**

**对应的** **SelectionKey** **加入到内部集合中并返回，参数用来设置超时时间**

**public** **Set<****SelectionKey****>** **selectedKeys****();//****从****内部集合中得到所有的** **SelectionKey** 

**}**





1)NIO中的 ServerSocketChannel功能类似ServerSocket，SocketChannel功能类似Socket

2)selector 相关方法说明

selector.select()//阻塞

selector.select(1000);//阻塞1000毫秒，在1000毫秒后返回

selector.wakeup();//唤醒selector

selector.selectNow();//不阻塞，立马返还





SelectionKey，表示 Selector 和网络通道的注册关系, 共四种:

int OP_ACCEPT：有新的网络连接可以 accept，值为 16

int OP_CONNECT：代表连接已经建立，值为 8

int OP_READ：代表读操作，值为 1 

int OP_WRITE：代表写操作，值为 4

**public abstract class** SelectionKey {

   public abstract Selector selector();//得到与之关联的 Selector 对象

l public abstract SelectableChannel channel();//得到与之关联的通道

l public final Object attachment();//得到与之关联的共享数据

l public abstract SelectionKey interestOps(int ops);//设置或改变监听事件

l public final boolean isAcceptable();//是否可以 accept

l public final boolean isReadable();//是否可以读

l public final boolean isWritable();//是否可以写

}





**public abstract class** **ServerSocketChannel**
   **extends** AbstractSelectableChannel
   **implements** NetworkChannel{

public static ServerSocketChannel open()，得到一个 ServerSocketChannel 通道

public final ServerSocketChannel bind(SocketAddress local)，设置服务器端端口号

public final SelectableChannel configureBlocking(boolean block)，设置阻塞或非阻塞模式，取值 false 表示采用非阻塞模式

public SocketChannel accept()，接受一个连接，返回代表这个连接的通道对象

public final SelectionKey register(Selector sel, int ops)，注册一个选择器并设置监听事件

}



1)SocketChannel，网络 IO 通道，**具体负责进行读写操作**。NIO 把缓冲区的数据写入通道，或者把通道里的数据读到缓冲区。

**public** **abstract class** SocketChannel
   **extends** AbstractSelectableChannel
   **implements** ByteChannel, ScatteringByteChannel, GatheringByteChannel, NetworkChannel{

public static SocketChannel open();//得到一个 SocketChannel 通道

public final SelectableChannel configureBlocking(boolean block);//设置阻塞或非阻塞模式，取值 false 表示采用非阻塞模式

public boolean connect(SocketAddress remote);//连接服务器

public boolean finishConnect();//如果上面的方法连接失败，接下来就要通过该方法完成连接操作

public int write(ByteBuffer src);//往通道里写数据

public int read(ByteBuffer dst);//从通道里读数据

public final SelectionKey register(Selector sel, int ops, Object att);//注册一个选择器并设置监听事件，最后一个参数可以设置共享数据

public final void close();//关闭通道

}





##### Java零拷贝

在 Java 程序中，常用的零拷贝有 mmap(内存映射) 和 sendFile。通过内存映射，将文件映射到内核缓冲区，同时，用户空间可以共享内核空间的数据。这样，在进行网络传输时，就可以减少内核空间到用户控件的拷贝次数。

* 传统IO过程: 硬盘 ==> 内核buffer ==> user buffer ==> socket buffer  ==> 协议栈 
* mmap: 硬盘 ==> 内核buffer ==> socket buffer【使用cpu拷贝】  ==> 协议栈 
* Send file: 硬盘 ==> 内核buffer  ==> 协议栈 

<img src="../../images/nio_零拷贝.png" alt="img" style="zoom:70%;" />

mmap和send file对比：

* mmap 适合小数据量读写，sendFile 适合大文件传输。
* mmap 需要 4 次上下文切换，3 次数据拷贝；sendFile 需要 3 次上下文切换，最少 2 次数据拷贝。
* sendFile 可以利用 DMA 方式，减少 CPU 拷贝，mmap 则不能（必须从内核拷贝到 Socket 缓冲区）。







#### NIO执行流程



<img src="../../images/nio_执行流程.png" alt="image-20210305160808916" style="zoom:85%;" />



1.当客户端连接时，会通过ServerSocketChannel 得到 SocketChannel

2.Selector 进行监听 select 方法, 返回有事件发生的通道的个数.

3.将socketChannel注册到Selector上, register(Selector sel, **int** ops), 一个selector上可以注册多个SocketChannel

4.注册后返回一个 SelectionKey, 会和该Selector 关联(集合)

5.进一步得到各个 SelectionKey (有事件发生)

6.在通过 SelectionKey  反向获取 SocketChannel , 方法 channel()

7.可以通过 得到的 channel  , 完成业务处理

```java






```







#### Netty核心组件



##### Bytebuf缓冲区



##### Channel和ChannelFuture



##### EventLoop线程模型



##### Boostrap引导类



#### Netty通道处理器



##### ChannelHandler和ChannelPipeline



##### Netty编解码



1次编解码用于确定字符边界【byteToMessagexxx, 用于解决占包和半包问题】: bytebuf -> bytebuf ==>  2次编解码用于字符和对象的转换【message2message ==> bytebuf ==> java object】 :常用2次编码器，java , xml , json, messagepack, protobuf



keepalive配置 ==> tcp默认发送keealive 包需要2个小时才能判断下线。 idea检测 ==> 可以在应用层判断空闲状态触发业务逻辑。













#### Netty执行流程











##### 1)Netty抽象出两组线程池 BossGroup 专门负责接收客户端的连接, WorkerGroup 专门负责网络的读写

2)BossGroup 和 WorkerGroup 类型都是 NioEventLoopGroup

3)NioEventLoopGroup 相当于一个事件循环组, 这个组中含有多个事件循环 ，每一个事件循环是 NioEventLoop

4)NioEventLoop 表示一个不断循环的执行处理任务的线程， 每个NioEventLoop 都有一个selector , 用于监听绑定在其上的socket的网络通讯

5)NioEventLoopGroup 可以有多个线程, 即可以含有多个NioEventLoop

6)每个Boss NioEventLoop 循环执行的步骤有3步

1.轮询accept 事件

2.处理accept 事件 , 与client建立连接 , 生成NioScocketChannel , 并将其注册到某个worker NIOEventLoop 上的 selector

3.处理任务队列的任务 ， 即 runAllTasks

7) 每个 Worker NIOEventLoop 循环执行的步骤

1.轮询read, write 事件

2.处理i/o事件， 即read , write 事件，在对应NioScocketChannel 处理

3.处理任务队列的任务 ， 即 runAllTasks

8) 每个Worker NIOEventLoop  处理业务时，会使用pipeline(管道), pipeline 中包含了 channel , 即通过pipeline 可以获取到对应通道, 管道中维护了很多的 处理器



l**Bootstrap****、****ServerBootstrap**

1)Bootstrap 意思是引导，一个 Netty 应用通常由一个 Bootstrap 开始，主要作用是配置整个 Netty 程序，串联各个组件，Netty 中 Bootstrap 类是客户端程序的启动引导类，ServerBootstrap 是服务端启动引导类

●

2)**常****见的方法有**

•public ServerBootstrap group(EventLoopGroup parentGroup, EventLoopGroup childGroup)，该方法用于服务器端，用来设置两个 EventLoop

•public B group(EventLoopGroup group) ，该方法用于客户端，用来设置一个 EventLoop

•public B channel(Class<? extends C> channelClass)，该方法用来设置一个服务器端的通道实现

•public <T> B option(ChannelOption<T> option, T value)，用来给 ServerChannel 添加配置

•public <T> ServerBootstrap childOption(ChannelOption<T> childOption, T value)，用来给接收到的通道添加配置

•public ServerBootstrap childHandler(ChannelHandler childHandler)，该方法用来设置业务处理类（自定义的 handler）

•public ChannelFuture bind(int inetPort) ，该方法用于服务器端，用来设置占用的端口号

•public ChannelFuture connect(String inetHost, int inetPort) ，该方法用于客户端，用来连接服务器端







l**Future****、****ChannelFuture**

1) Netty 中所有的 IO 操作都是异步的，不能立刻得知消息是否被正确处理。但是可以过一会等它执行完成或者直接注册一个监听，具体的实现就是通过 Future 和 ChannelFutures，他们可以注册一个监听，当操作执行成功或失败时监听会自动触发注册的监听事件



2)**常见的方法有**

•Channel channel()，返回当前正在进行 IO 操作的通道

•ChannelFuture sync()，同步等待操作完毕





l**Channel**

1)Netty 网络通信的组件，能够用于执行网络 I/O 操作。

2)通过Channel 可获得当前网络连接的通道的状态

3)通过Channel 可获得 网络连接的配置参数 （例如接收缓冲区大小）

●

4)Channel 提供异步的网络 I/O 操作(如建立连接，读写，绑定端口)，异步调用意味着任何 I/O 调用都将立即返回，并且不保证在调用结束时所请求的 I/O 操作已完成

5)调用立即返回一个 ChannelFuture 实例，通过注册监听器到 ChannelFuture 上，可以 I/O 操作成功、失败或取消时回调通知调用方

6)支持关联 I/O 操作与对应的处理程序

7)不同协议、不同的阻塞类型的连接都有不同的 Channel 类型与之对应，常用的 Channel 类型:

•NioSocketChannel，异步的客户端 TCP Socket 连接。

•NioServerSocketChannel，异步的服务器端 TCP Socket 连接。

•NioDatagramChannel，异步的 UDP 连接。

•NioSctpChannel，异步的客户端 Sctp 连接。

•NioSctpServerChannel，异步的 Sctp 服务器端连接，这些通道涵盖了 UDP 和 TCP 网络 IO 以及文件 IO。



l**Selector**

1)Netty 基于 Selector 对象实现 I/O 多路复用，通过 Selector 一个线程可以监听多个连接的 Channel 事件。

2)当向一个 Selector 中注册 Channel 后，Selector 内部的机制就可以自动不断地查询(Select) 这些注册的 Channel 是否有已就绪的 I/O 事件（例如可读，可写，网络连接完成等），这样程序就可以很简单地使用一个线程高效地管理多个 Channel





l**ChannelHandler** **及其实现类**



1)ChannelHandler 是一个接口，处理 I/O 事件或拦截 I/O 操作，并将其转发到其 ChannelPipeline(业务处理链)中的下一个处理程序。

2)ChannelHandler 本身并没有提供很多方法，因为这个接口有许多的方法需要实现，方便使用期间，可以继承它的子类

3)**ChannelHandler** **及其实现类一览图****(****后****)**



•ChannelInboundHandler 用于处理入站 I/O 事件。

•ChannelOutboundHandler 用于处理出站 I/O 操作。

//适配器

•ChannelInboundHandlerAdapter 用于处理入站 I/O 事件。

•ChannelOutboundHandlerAdapter 用于处理出站 I/O 操作。

•ChannelDuplexHandler 用于处理入站和出站事件。

**public class** ChannelInboundHandlerAdapter **extends** ChannelHandlerAdapter **implements** ChannelInboundHandler {
   **public** ChannelInboundHandlerAdapter() { }
   **public void** channelRegistered(ChannelHandlerContext ctx) **throws** Exception {
     ctx.fireChannelRegistered();
   }
   **public void** channelUnregistered(ChannelHandlerContext ctx) **throws** Exception {
     ctx.fireChannelUnregistered();
   }
   **//** 通道就绪事件
   **public void** channelActive(ChannelHandlerContext ctx) **throws** Exception {
     ctx.fireChannelActive();
   }
   **public void** channelInactive(ChannelHandlerContext ctx) **throws** Exception {
     ctx.fireChannelInactive();
   }
   **//****通道读取数据事件**
   **public void** channelRead(ChannelHandlerContext ctx, Object msg) **throws** Exception {
     ctx.fireChannelRead(msg);
   }
   **//****数据读取完毕事件**
   **public void** channelReadComplete(ChannelHandlerContext ctx) **throws** Exception {
     ctx.fireChannelReadComplete();
   }
   **public void** userEventTriggered(ChannelHandlerContext ctx, Object evt) **throws** Exception {
     ctx.fireUserEventTriggered(evt);
   }
   **public void** channelWritabilityChanged(ChannelHandlerContext ctx) **throws** Exception {
     ctx.fireChannelWritabilityChanged();
   }
   **//****通道发生异****常事件**
   **public void** exceptionCaught(ChannelHandlerContext ctx, Throwable cause) **throws** Exception {
     ctx.fireExceptionCaught(cause);
   }
 }



l**Pipeline** **和** **ChannelPipeline**



1)ChannelPipeline 是一个 Handler 的集合，它负责处理和拦截 inbound 或者 outbound 的事件和操作，相当于一个贯穿 Netty 的链。(**也可以这样理解：****ChannelPipeline** **是 保****存** **ChannelHandler** **的** **List****，用于处理或拦截** **Channel** **的入站事件和出站操****作**)

2)ChannelPipeline 实现了一种高级形式的拦截过滤器模式，使用户可以完全控制事件的处理方式，以及 Channel 中各个的 ChannelHandler 如何相互交互

3)在 Netty 中每个 Channel 都有且仅有一个 ChannelPipeline 与之对应，它们的组成关系如下

![image-20210307183615556](/Users/zhangxiaoxia/Library/Application Support/typora-user-images/image-20210307183615556.png)



•一个 Channel 包含了一个 ChannelPipeline，而 ChannelPipeline 中又维护了一个由 ChannelHandlerContext 组成的双向链表，并且每个 ChannelHandlerContext 中又关联着一个 ChannelHandler

入站事件和出站事件在一个双向链表中，入站事件会从链表 head 往后传递到最后一个入站的 handler，出站事件会从链表 tail 往前传递到最前一个出站的 handler，两种类型的 handler 互不干扰

•ChannelPipeline addFirst(ChannelHandler... handlers)，把一个业务处理类（handler）添加到链中的第一个位置

•ChannelPipeline addLast(ChannelHandler... handlers)，把一个业务处理类（handler）添加到链中的最后一个位置





l**ChannelHandlerContext**



1)保存 Channel 相关的所有上下文信息，同时关联一个 ChannelHandler 对象

即ChannelHandlerContext 中 包 含 一 个 具 体 的 事 件 处 理 器 ChannelHandler ， 同 时ChannelHandlerContext 中也绑定了对应的 pipeline 和 Channel 的信息，方便对 ChannelHandler进行调用

•ChannelFuture close()，关闭通道

•ChannelOutboundInvoker flush()，刷新

•ChannelFuture writeAndFlush(Object msg) ， 将 数 据 写 到 ChannelPipeline 中 当 前

•ChannelHandler 的下一个 ChannelHandler 开始处理（出站）



l**ChannelOption**

**ChannelOption.SO_BACKLOG**

对应 TCP/IP 协议 listen 函数中的 backlog 参数，用来初始化服务器可连接队列大小。服

务端处理客户端连接请求是顺序处理的，所以同一时间只能处理一个客户端连接。多个客户

端来的时候，服务端将不能处理的客户端连接请求放在队列中等待处理，backlog 参数指定

了队列的大小。



**ChannelOption.SO_KEEPALIVE**

一直保持连接活动状态



l**EventLoopGroup** **和其实现类** **NioEventLoopGroup**



1)EventLoopGroup 是一组 EventLoop 的抽象，Netty 为了更好的利用多核 CPU 资源，一般会有多个 EventLoop 同时工作，每个 EventLoop 维护着一个 Selector 实例。

●

2)EventLoopGroup 提供 next 接口，可以从组里面按照一定规则获取其中一个 EventLoop来处理任务。在 Netty 服务器端编程中，我们一般都需要提供两个 EventLoopGroup，例如：BossEventLoopGroup 和 WorkerEventLoopGroup。

●3)通常一个服务端口即一个 ServerSocketChannel对应一个Selector 和一个EventLoop线程。BossEventLoop 负责接收客户端的连接并将 SocketChannel 交给 WorkerEventLoopGroup 来进行 IO 处理，如下图所示





•BossEventLoopGroup 通常是一个单线程的 EventLoop，EventLoop 维护着一个注册了ServerSocketChannel 的 Selector 实例BossEventLoop 不断轮询 Selector 将连接事件分离出来.通常是 OP_ACCEPT 事件，然后将接收到的 SocketChannel 交给 WorkerEventLoopGroup.

•WorkerEventLoopGroup 会由 next 选择其中一个 EventLoop来将这个 SocketChannel 注册到其维护的 Selector 并对其后续的 IO 事件进行处理

4)常用方法

•public NioEventLoopGroup()，构造方法

•public Future<?> shutdownGracefully()，断开连接，关闭线程





l**Unpooled** **类**

1)Netty 提供一个专门用来操作缓冲区(即Netty的数据容器)的工具类

2)常用方法如下所示

public static ByteBuf copiedBuffer(CharSequence string, Charset charset)

3)举例说明**Unpooled** **获****取** Netty的数据容器ByteBuf 的基本使用 【案例演示】

●

```
//创建一个ByteBuf
//说明
//1. 创建 对象，该对象包含一个数组arr , 是一个byte[10]
//2. 在netty 的buffer中，不需要使用flip 进行反转
//   底层维护了 readerindex 和 writerIndex
//3. 通过 readerindex 和  writerIndex 和  capacity， 将buffer分成三个区域
// 0---readerindex 已经读取的区域
// readerindex---writerIndex ， 可读的区域
// writerIndex -- capacity, 可写的区域
ByteBuf buffer = Unpooled.buffer(10);

```











```
IdleStateHandler心跳检测
```



```
 //加入一个netty 提供 IdleStateHandler
                   /*
                   说明
                   1. IdleStateHandler 是netty 提供的处理空闲状态的处理器
                   2. long readerIdleTime : 表示多长时间没有读, 就会发送一个心跳检测包检测是否连接
                   3. long writerIdleTime : 表示多长时间没有写, 就会发送一个心跳检测包检测是否连接
                   4. long allIdleTime : 表示多长时间没有读写, 就会发送一个心跳检测包检测是否连接

                   5. 文档说明
                   triggers an {@link IdleStateEvent} when a {@link Channel} has not performed
* read, write, or both operation for a while.
*                  6. 当 IdleStateEvent 触发后 , 就会传递给管道 的下一个handler去处理
*                  通过调用(触发)下一个handler 的 userEventTiggered , 在该方法中去处理 IdleStateEvent(读空闲，写空闲，读写空闲)
                    */
                   pipeline.addLast(new IdleStateHandler(7000,7000,10, TimeUnit.SECONDS));
```





l**编****码和解****码的基本****介绍**



1)编写网络应用程序时，因为数据在网络中传输的都是二进制字节码数据，在发送数据时就需要编码，接收数据时就需要解码 [示意图]

2)codec(编解码器) 的组成部分有两个：decoder(解码器)和 encoder(编码器)。encoder 负责把业务数据转换成字节码数据，decoder 负责把字节码数据转换成业务数据

![image-20210308134036859](/Users/zhangxiaoxia/Library/Application Support/typora-user-images/image-20210308134036859.png)

1)Netty 自身提供了一些 codec(编解码器)

2)Netty 提供的编码器

•StringEncoder，对字符串数据进行编码

•ObjectEncoder，对 Java 对象进行编码

3)Netty 提供的解码器

•StringDecoder, 对字符串数据进行解码

•ObjectDecoder，对 Java 对象进行解码

4)Netty 本身自带的 ObjectDecoder 和 ObjectEncoder 可以用来实现 POJO 对象或各种业务对象的编码和解码，底层使用的仍是 Java 序列化技术 , 而Java 序列化技术本身效率就不高，存在如下问题

•无法跨语言

•序列化后的体积太大，是二进制编码的 5 倍多。

•序列化性能太低

=> 引出 新的解决方案 [Google 的 Protobuf]



**Protobuf****基本介绍和使用示意图**

1)Protobuf 是 Google 发布的开源项目，全称 Google Protocol Buffers，是一种轻便高效的结构化数据存储格式，可以用于结构化数据串行化，或者说序列化。它很适合做数据存储或 **RPC[****远程过程调用** **remote procedure call ]** **数据交换格****式** 。
 目前很多公司 http+json è tcp+protobuf

2)参考文档 : [https://](https://developers.google.com/protocol-buffers/docs/proto)[developers.google.com/protocol-buffers/docs/proto](https://developers.google.com/protocol-buffers/docs/proto)  语言指南

3)Protobuf 是以 message 的方式来管理数据的.

4)支持跨平台、**跨****语言**，即[客户端和服务器端可以是不同的语言编写的] （**支持目前绝大多数语言**，例如 C++、C#、Java、python 等）

5)高性能，高可靠性

6)使用 protobuf 编译器能自动生成代码，Protobuf 是将类的定义使用.proto 文件进行描述。说明，在idea 中编写 .proto 文件时，会自动提示是否**下载** **.****ptotot** **编写插件**. 可以让**语法高****亮**。

7)然后通过 protoc.exe 编译器根据.proto 自动生成.java 文件

8)protobuf 使用示意图







1)ChannelHandler充当了处理入站和出站数据的应用程序逻辑的容器。例如，实现ChannelInboundHandler接口（或ChannelInboundHandlerAdapter），你就可以接收入站事件和数据，这些数据会被业务逻辑处理。当要给客户端发送响应时，也可以从ChannelInboundHandler冲刷数据。业务逻辑通常写在一个或者多个ChannelInboundHandler中。ChannelOutboundHandler原理一样，只不过它是用来处理出站数据的

ChannelPipeline提供了ChannelHandler链的容器。以客户端应用程序为例，如果事件的运动方向是从客户端到服务端的，那么我们称这些事件为出站的，即客户端发送给服务端的数据会通过pipeline中的一系列ChannelOutboundHandler，并被这些Handler处理，反之则称为入站的



l**编****码解码器**

1)当Netty发送或者接受一个消息的时候，就将会发生一次数据转换。入站消息会被解码：从字节转换为另一种格式（比如java对象）；如果是出站消息，它会被编码成字节。

2)Netty提供一系列实用的编解码器，他们都实现了ChannelInboundHadnler或者ChannelOutboundHandler接口。在这些类中，channelRead方法已经被重写了。以入站为例，对于每个从入站Channel读取的消息，这个方法会被调用。随后，它将调用由解码器所提供的decode()方法进行解码，并将已经解码的字节转发给ChannelPipeline中的下一个ChannelInboundHandler。



l**解码器****-****ByteToMessageDecoder**

1)由于不可能知道远程节点是否会一次性发送一个完整的信息，tcp有可能出现粘包拆包的问题，这个类会对入站数据进行缓冲，直到它准备好被处理.

public class ToIntegerDecoder extends ByteToMessageDecoder {

  @Override

  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {

​    if (in.readableBytes() >= 4) {

​      out.add(in.readInt());

​    }

  }

}



l**解码器****-****ReplayingDecoder**

1)public abstract class ReplayingDecoder<S> extends ByteToMessageDecoder

2)ReplayingDecoder扩展了ByteToMessageDecoder类，使用这个类，我们不必调用readableBytes()方法。参数S指定了用户状态管理的类型，其中Void代表不需要状态管理

3)**应用实例****：**使用ReplayingDecoder 编写解码器，对前面的案例进行简化 [案例演示]

4)ReplayingDecoder使用方便，但它也有一些局限性：

•并不是所有的 ByteBuf 操作都被支持，如果调用了一个不被支持的方法，将会抛出一个 UnsupportedOperationException。

•ReplayingDecoder 在某些情况下可能稍慢于 ByteToMessageDecoder，例如网络缓慢并且消息格式复杂时，消息会被拆成了多个碎片，速度变慢





**其它解码器**

1)LineBasedFrameDecoder：这个类在Netty内部也有使用，它使用行尾控制字符（\n或者\r\n）作为分隔符来解析数据。

2)DelimiterBasedFrameDecoder：使用自定义的特殊字符作为消息的分隔符。

3)HttpObjectDecoder：一个HTTP数据的解码器

4)LengthFieldBasedFrameDecoder：通过指定长度来标识整包消息，这样就可以自动的处理黏包和半包消息。





l**TCP** **粘包和拆包基本介绍**

1)TCP是面向连接的，面向流的，提供高可靠性服务。收发两端（客户端和服务器端）都要有一一成对的socket，因此，发送端为了将多个发给接收端的包，更有效的发给对方，使用了优化方法（Nagle算法），将多次间隔较小且数据量小的数据，合并成一个大的数据块，然后进行封包。这样做虽然提高了效率，但是接收端就难于分辨出完整的数据包了，因为**面****向流的通信是无消息保护边界**的

2)由于TCP无消息保护边界, 需要在接收端处理消息边界问题，也就是我们所说的粘包、拆包问题, 看一张图

![image-20210308170922436](/Users/zhangxiaoxia/Library/Application Support/typora-user-images/image-20210308170922436.png)







l**TCP** **粘包和拆包解决方案**

1)使用自定义协议 + 编解码器 来解决

2)关键就是要解决 **服****务器端每次读取数据长度的问题**, 这个问题解决，就不会出现服务器多读或少读数据的问题，从而避免的TCP 粘包、拆包 。



源码解析：

1-启动服务 2- 创建连接 3-接受数据 4-业务处理 5-发送数据 6-断开连接 7-关闭服务

1)Netty 提供了 IdleStateHandler ，ReadTimeoutHandler，WriteTimeoutHandler 三个**Handler** 检测连接的有效性，重点分析 **IdleStateHandler** .