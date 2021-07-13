## SpringBoot整合Thrift

###(一). Thrift基础知识

Thrift是一个跨语言的服务部署框架，最初由Facebook于2007年开发，2008年进入Apache开源项目。Thrift通过一个中间语言(IDL, 接口定义语言)来定义RPC的接口和数据类型，然后通过一个编译器生成不同语言的代码（目前支持C++,Java, Python, PHP, Ruby, Erlang, Perl, Haskell, C#, Cocoa, Smalltalk和OCaml）,**并由生成的代码负责RPC协议层和传输层的实现**。

官方文档地址：https://thrift.apache.org/tutorial/




#### Thrift接口定义

Thrift接口定义【Document ::=  Header* Definition*】 ==> 主要包含多个头信息和多个语法定义信息。其中，头信息可以是Include[引入其他接口定义], Namespace[命名空间 - 类比于Java中的包]；定义信息包含有：Const[常量], Typedef[类型定义], Enum[枚举] , Struct[结构体], Union[], Exception[异常], Service[服务]。定义信息由字段信息 + 函数信息 + 类型信息组成。

接口定义参考地址：https://thrift.apache.org/docs/idl 【编译原理中 - 语法表达式】

##### Header头信息

```idl
// header语法格式
Header   ::=  Include | CppInclude | Namespace
 
include "user.thrift"
namespace java com.moce.demo.thrift.service
 
```



#####Definition定义信息

```idl
// 定义信息语法格式
Definition  ::=  Const | Typedef | Enum | Senum | Struct | Union | Exception | Service

// 常量
const i32 INT_CONST = 1234;    // a
const map<string,string> MAP_CONST = {"hello": "world", "goodnight": "moon"}

// Typedef自定义类型信息
typedef i32 MyInteger
 
struct  User {  
  1:MyInteger userId,
  2:string name
}

// 枚举
enum Gender {
	MALE,
	FEMALE,
	UNKONWN
}

// Struct ==> java bean
struct  User {  
  1:i32 userId,
  2:string name
}

// Exception ==> 异常 bean
exception CustomException {
	1: string code;
	2: string message;
}

// Service ==> 接口定义
service  UserService {
  string sayHello(1:string name);
}

```



##### Field字段信息

```idl
// 字段信息语法格式
Field    ::=  FieldID? FieldReq? FieldType Identifier ('=' ConstValue)? XsdFieldOptions ListSeparator?

FieldID ==> 表示字段序号
FieldReq ==> 表示字段是否必选 【'required' | 'optional'】
FieldType ==> 表示字段类型信息
```



##### Function函数信息

```idl
Function        ::=  'oneway'? FunctionType Identifier '(' Field* ')' Throws? ListSeparator?

FunctionType    ::=  FieldType | 'void'

Throws          ::=  'throws' '(' Field* ')'
```



##### Type类型信息

```idl
FieldType       ::=  Identifier | BaseType | ContainerType

DefinitionType  ::=  BaseType | ContainerType

BaseType        ::=  'bool' | 'byte' | 'i8' | 'i16' | 'i32' | 'i64' | 'double' | 'string' | 'binary' | 'slist'

ContainerType   ::=  MapType | SetType | ListType

MapType         ::=  'map' CppType? '<' FieldType ',' FieldType '>'

SetType         ::=  'set' CppType? '<' FieldType '>'

ListType        ::=  'list' '<' FieldType '>' CppType?
```





#### Thrift数据类型

Thrift类型系统包括预定义的基本类型（如bool , byte, double, string）、特殊类型(如binary)、用户自定义结构体（看上去像C 语言的结构体）、容器类型（如list，set，map）以及异常和服务定义。【TType ==> 包含所有类型常量】

**类型说明参考地址**：https://thrift.apache.org/docs/types



##### 基本类型

| Thrift类型 | Java类型 |                说明                 |
| :--------: | :------: | :---------------------------------: |
|    bool    | boolean  | 布尔类型(true or value)，占一个字节 |
|  I8/byte   |   byte   |             有符号字节              |
|    i16     |  short   |           16位有符号整型            |
|    I32     |   int    |           32位有符号整型            |
|    I64     |   long   |           64位有符号整型            |
|   double   |  double  |             64位浮点数              |
|   string   |  string  |     未知编码或者二进制的字符串      |

**说明：**thrift不支持无符号整型，因为很多目标语言不存在无符号整型



##### 特殊类型

| Thrift类型 |  Java类型  |        说明        |
| :--------: | :--------: | :----------------: |
|   binary   | ByteBuffer | 未经过编码的字节流 |



##### 容器类型

| Thrift类型 |      Java类型       |                         说明                         |
| :--------: | :-----------------: | :--------------------------------------------------: |
|    list    | java.util.ArrayList |     一系列t1类型的元素组成的有序表，元素可以重复     |
|    set     |  java.util.HashSet  |       一系列t1类型的元素组成的无序表，元素唯一       |
|    map     |  java.util.HashMap  | key/value对（key的类型是t1且key唯一，value类型是t2） |



##### 自定义结构体

Thrift结构体在概念上同C语言结构体类型 ==> 将相关属性聚集（封装）在一起。在面向对象语言中，thrift结构体被转换成类。在Java语言中等价于JavaBean的概念。

```c
struct  User {  
  1:i32 userId,
  2:string name
}
```





##### 异常类型

异常在语法和功能上类似于结构体，只不过异常使用关键字exception而不是struct关键字声明。但它在语义上不同于结构体—当定义一个RPC服务时，开发者可能需要声明一个远程方法抛出一个异常。

```C
exception CustomException {
	1: string code;
	2: string message;
}
```



##### 服务定义

一个服务包含一系列命名函数，每个函数包含一系列的参数以及一个返回类型。**在语法上，服务等价于定义一个接口或者纯虚抽象类~**

```java 
// 格式
service <name> {
  <returntype> <name> (<arguments>)
  [throws (<exceptions>)]
}


// 实例
service  UserService {
  string sayHello(1:string name);
}

```





####Thrift生成目标接口代码

##### thrift客户端下载

下载地址：https://thrift.apache.org/download

MAC thrift客户端安装：` brew install thrift`  ==> 【可执行文件：/usr/local/bin/thrift】



##### .thrift文件编写

语法规则 & 数据结构

```idl

include "user.thrift"
include "exception.thrift"

namespace java com.mooc.demo.thrift.service  

/**
 * 用户服务
 */
service  UserService {   

  /**保存用户*/ 
  bool save(1:user.User user),
  
  /**根据name获取用户列表*/ 
  list<user.User> findUsersByName(1:string name),
  
  /**删除用户*/ 
  void deleteByUserId(1:i32 userId) throws (1: exception.UserNotFoundException e)
}  
```





#####thrift目标代码生成

```shell
# 使用脚本创建目标语言代码
thrift -r --gen <language> <Thrift filename>

# 生成Java代码
thrift -r --gen java xxx.thrift
```

PS：thrift生成代码的脚本版本和pom中依赖的版本要一一对应，否则会有问题哦。





### (二). Thrift实现原理分析

#### 系统架构

![Apache Thrift Layered Architecture](https://github.com/apache/thrift/raw/master/doc/images/thrift-layers.png)

* 基础语言层【Language】：根据thrift idl文件生成各个语言代码，位于compiler目录内。
* 低级传输层【Low-Level Transport】：靠近网络层、作为rpc框架接收报文的入口，提供各种底层实现如socket创建、读写、接收连接等。
* 传输层包装器【Transport Wrapper】：基于低级传输层，实现各种复写传输层包括http、framed、buffered、压缩传输层等，用户也可以通过重写低级传输层和复写传输层实现自己的传输层。
* 协议层【Protocol】：协议层主要负责解析请求、应答报文为具体的结构体、类实例，供处理层直接使用，目前的协议包括Binary(最为常用)、json、多路混合协议等。protocol层则主要负责序列化和反序列化.
* 服务层：IO模型 + 线程模型 实现客户端和服务器端服务。





#### 源码分析

<img src="/Users/zhangxiaoxia/git_dir/learn/images/thrift模型.png" alt="image-20210713000631488" style="zoom:33%;" />

**TProtocol（协议层）**：定义数据传输格式

- TBinaryProtocol：二进制格式；
- TCompactProtocol：压缩格式；
- TJSONProtocol：JSON格式；
- TSimpleJSONProtocol：提供JSON只写协议, 生成的文件很容易通过脚本语言解析；



**TTransport（传输层）**：定义数据传输方式，可以为TCP/IP传输

- TSocket：阻塞式socker；
- TFramedTransport：以frame为单位进行传输，非阻塞式服务中使用；
- TFileTransport：以文件形式进行传输；
- TMemoryTransport：将内存用于I/O，java实现时内部实际使用了简单的ByteArrayOutputStream；
- TZlibTransport：使用zlib进行压缩， 与其他传输方式联合使用，当前无java实现；

 

**Thrift支持的服务模型**

- TSimpleServer：简单的单线程服务模型，常用于测试；
- TThreadPoolServer：多线程服务模型，使用标准的阻塞式IO；
- TNonblockingServer：多线程服务模型，使用非阻塞式IO（需使用TFramedTransport数据传输方式）；



```java
-- TServerTransport ==> Server端传输通道【ServerSocket】
  核心接口：listen(), accept(), close()

  -- TServerSocket ==> BIO ServerSocket
  -- TNonblockingServerTransport 
    -- TNonblockingServerSocket  ==> NIO ServerSocket



-- TServer  ==> 服务端接口
  核心属性：
    protected TServerTransport serverTransport_;  // ServerSocket
    TProcessorFactory processorFactory; // 处理器工厂
    protected TProtocolFactory inputProtocolFactory_, outputProtocolFactory_; // input,output协议
    protected TTransportFactory inputTransportFactory_, outputTransportFactory_; // input, output传输

  核心接口：
    abstract void serve();

  -- TSimpleServer 	==> BIO
  -- TThreadPoolServer ==> BIO + 多线程

  -- AbstractNonblockingServer
    核心方法：
      FrameBuffer#read/write/
      FrameBuffer#invoke() ==> 处理响应
      

    -- TNonblockingServer
      核心方法：
        startThreads()  ==> 启动一个线程，阻塞在select()方法【等待读写事件】
        select() ==> 等待连接，读写事件

       -- THsHaServer ==> NIO + 多线程
         核心属性：
           private final ExecutorService invoker;
         核心方法：
           protected boolean requestInvoke(FrameBuffer frameBuffer) ==> 使用工作线程处理读写请求



-- TProcessor ==> 处理器[thrift自动生成]
  	-- TBaseProcessor 
  	  核心属性：
  	    private final I iface;  // service实现类
        private final Map<String,ProcessFunction<I, ? extends TBase>> processMap;
      核心方法：
        public void process(TProtocol in, TProtocol out) ==> 调用iface实现类响应服务器请求

  	-- TBaseAsyncProcessor
  	  


-- TProcessorFactory ==> Processor工厂
  核心方法：TProcessor getProcessor(TTransport trans)




-- TServiceClient ==> 客户端抽象类
  核心属性：
    TProtocol iprot_, oprot_ ==> input协议=读，output协议=写
  核心方法：
    sendBase(String methodName, TBase<?,?> args, byte type)
    void receiveBase(TBase<?,?> result, String methodName) 

-- TMessage ==> 消息类


-- TProtocol ==> 传输协议，主要对字符进行编解码
  核心属性: TTransport trans_
  核心接口: writeXXX(), readXXX()

  -- TBinaryProtocol  ==> 使用二进制编解码字符
  -- TJSONProtocol ==> 使用json进行编解码
  -- TCompactProtocol ==> 使用紧凑数据类型


-- TProtocolFactory ==> 使用工厂创建协议
  核心方法：TProtocol getProtocol(TTransport trans);

  -- TBinaryProtocol#Factory
  -- TJSONProtocol#Factory
  -- TCompactProtocol#Factory


-- TTransport ==> 抽象类，相当于TCP连接
  核心接口：open(), read(), write(), close()

  -- TIOStreamTransport ==> 阻塞同步IO
      -- TSocket ==> 获取socket输入输出流，然后执行网络读写

  -- TNonblockingTrasnsort, TNonblockingSocket ==> 非阻塞IO
  -- TMemoryInputTransport ==> 封装了一个字节数组byte[]来做输入流的封装
  -- TFramedTransport ==> 封装了TMemoryInputTransport做输入流，封装了TByteArryOutPutStream做输出流.
    核心属性：private int maxLength_; private TTransport transport_ = null;



-- TTransportFactory ==> 传输工厂
  核心接口： TTransport getTransport(TTransport base)

  -- TFramedTransport#Factory

```





###(三) 与SpringBoot整合过程

