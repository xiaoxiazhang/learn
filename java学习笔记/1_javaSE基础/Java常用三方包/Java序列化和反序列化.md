### Java序列化和反序列化

**对象的序列化：**把对象转换为字节序列的过程。

**对象的反序列化：**把字节序列恢复为对象的过程。

**对象的序列化主要用途：**

* 对象持久化（persistence）：把对象的字节序列永久地保存到硬盘上，通常存放在一个文件中；
* 对象复制：将对象保存在内存中，可以再通过此数据得到多个对象的副本。
* 对象传输：在网络上传送对象的字节序列。

**常用序列化和反序列化方式：**jdk, hessian, fastjson, gson, jackson, Protostuff, kryo



#### 1. JDK序列化

##### 原理讲解

JDK序列化反序列化(**对象图实现**)：对象图是通过ObjectInputStream#readObject实现的。(相当于构造器)，使用这种序列化方式性能低，然后被攻击，并且不具有跨语言特性。优先考虑考虑json和protobuf序列化方式。

JDK序列化：

* 实现Serializable接口, 需要设置序列化版本号serialVersionUID（不存在会通过类结构生成）

  > ```java
  > // 序列化版本号
  > private static final long serialVersionUID = -3665804199014368530L;
  > ```

* transient修饰符：瞬时的，表示实例域将从一个类的默认序列化形式中省略掉。

* static修饰符：静态的变量不会被默认序列化。

* 可序列化的类：对象的物理表示法等同于它的逻辑内容。(Boolean, Number,String, entity类等 )，对于一些复杂对象需要序列化，可以通过readObject,writeObject自定义序列化出对象逻辑结构.。例如ArrayList

  > ```java
  > // ArrayList自定义序列化只需要序列化对象个数和对象数据
  > // elementData中还存在null元素
  > transient Object[] elementData; 
  > private int size;
  > 
  > private void writeObject(java.io.ObjectOutputStream s)
  >     throws java.io.IOException{
  >     // Write out element count, and any hidden stuff
  >     int expectedModCount = modCount;
  >     s.defaultWriteObject(); // 不管怎样都要调用
  > 
  >     // Write out size as capacity for behavioural compatibility with clone()
  >     s.writeInt(size);
  > 
  >     // Write out all elements in the proper order.
  >     // 只序列化集合中真实存在的元素
  >     for (int i=0; i<size; i++) {
  >         s.writeObject(elementData[i]);
  >     }
  > 
  >     if (modCount != expectedModCount) {
  >         throw new ConcurrentModificationException();
  >     }
  > }
  > 
  > 
  > private void readObject(java.io.ObjectInputStream s)
  >     throws java.io.IOException, ClassNotFoundException {
  >     elementData = EMPTY_ELEMENTDATA;
  > 
  >     // Read in size, and any hidden stuff
  >     s.defaultReadObject(); // 不管怎样都要调用
  > 
  >     // Read in capacity
  >     s.readInt(); // ignored
  > 
  >     if (size > 0) {
  >         // be like clone(), allocate array based upon size not capacity
  >         int capacity = calculateCapacity(elementData, size);
  >         SharedSecrets.getJavaOISAccess().checkArray(s, Object[].class, capacity);
  >         ensureCapacityInternal(size);
  > 
  >         Object[] a = elementData;
  >         // Read in all elements in the proper order.
  >         for (int i=0; i<size; i++) {
  >             a[i] = s.readObject();
  >         }
  >     }
  > }
  > 
  > ```
  
* 谨慎序列化的类：为继承设计的类(除了entity类)，内部类（会使得内部类变成导出api一部分）



**jdk序列化注意问题：**

* 序列化后的api的类中私有实例域也会导出成api一部分

* 序列化后的字节数组可以被修改，反序列化(`可以理解为隐藏的构造器`) 需要编写保护性的readObject

  > ```java
  > private void readObject(ObjectInputStream s)
  >             throws IOException, ClassNotFoundException {
  >     s.defaultReadObject();
  >     // 保护性考虑拷贝可变对象
  >     start = new Date(start.getTime());
  >     end = new Date(end.getTime());
  >     // 校验属性的可靠性
  >     if (start.compareTo(end) > 0)
  >     throw new InvalidObjectException(start +" after "+ end);
  > }
  > 
  > ```

* 可序列化类修改需要保证之前的序列化的字节能正常反序列化成功。



##### 序列化工具类

```java
public abstract class JDKSerializer {

    public static <T> byte[] serialize(T obj) {
        try(ByteArrayOutputStream baos =new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(baos); ) {
            oos.writeObject(obj);
            return baos.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("序列化失败", ex);
        }
    }

    public static <T> T deserialize(byte[] bytes, Class<T> clazz) {

        try(ByteArrayInputStream bais =  new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);){
            return (T) ois.readObject();
        }catch (IOException | ClassNotFoundException ex){
            throw new IllegalStateException("反序列化失败", ex);
        }
    }

}


```



#### 2. Hessian序列化

```xml
<dependency>
    <groupId>com.caucho</groupId>
    <artifactId>hessian</artifactId>
    <version>4.0.60</version>
</dependency>

```

```java
public abstract class HessianUtils {

    public static byte[] serialize(Object object) {

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();) {
            HessianOutput hessianOutput = null;
            try {
                hessianOutput = new HessianOutput(byteArrayOutputStream);
                // Hessian的序列化输出
                hessianOutput.writeObject(object);
                return byteArrayOutputStream.toByteArray();
            } finally {
                if (hessianOutput != null) {
                    hessianOutput.close();
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("序列化失败", ex);
        }
    }

    public static <T> T deserialize(byte[] bytes, Class<T> clazz) {
        try (ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);) {
            HessianInput hessianInput = null;
            try {
                // Hessian的反序列化读取对象
                hessianInput = new HessianInput(byteArrayInputStream);
                return (T) hessianInput.readObject();
            } finally {
                if (hessianInput != null) {
                    hessianInput.close();
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("反序列化失败", ex);
        }
    }

}
```





#### 3. Json序列化

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>1.2.56</version>
</dependency>

```

```java
// 序列化成json
String couponJson = JSON.toJSONString(coupon);

// 反序列化成对象
CouponEntity couponEntity = JSON.parseObject(couponJson, CouponEntity.class);
```

**说明：**Fastjson序列化小对象速度很快。



#### 4. Protostuff序列化

##### 原理说明

protostuff序列化对象严格依赖字段定义的顺序。

**第一步：创建Schema对象**：（单例的）

```java
// RumtimeSchema(维护了一个静态的IdStrategy)
public static final IdStrategy ID_STRATEGY;
static{
   ID_STRATEGY = new DefaultIdStrategy(); 
}

public static <T> Schema<T> getSchema(Class<T> typeClass,IdStrategy strategy){
    return strategy.getSchemaWrapper(typeClass, true).getSchema();
}

// 创建RuntimeSchema
public static <T> RuntimeSchema<T> createFrom(Class<T> typeClass,
        IdStrategy strategy){
    return createFrom(typeClass, NO_EXCLUSIONS, strategy);
}


// DefaultIdStrategy（存放的是类和HasSchema==> lasy类型的）
pojoMapping = new ConcurrentHashMap<String, HasSchema<?>>();

public <T> HasSchema<T> getSchemaWrapper(Class<T> typeClass, boolean create){
    HasSchema<T> hs = (HasSchema<T>) pojoMapping.get(typeClass.getName());
    // 如果Schema没有，会创建一个Lazy（HasSchema类型），
    // Lazy委托对象有typeClass，DefaultIdStrategy，Schema
    if (hs == null && create){
        hs = new Lazy<T>(typeClass, this);
        final HasSchema<T> last = (HasSchema<T>) pojoMapping.putIfAbsent(
                typeClass.getName(), hs);
        if (last != null)
            hs = last;
    }

    return hs;
}

// Lazy#getSchema
// double-check的方式获取schema并设置给Lazy
public Schema<T> getSchema(){
    Schema<T> schema = this.schema;
    if (schema == null){
        synchronized (this){
            if ((schema = this.schema) == null) {
                if (Message.class.isAssignableFrom(typeClass)) {                   
                    Message<T> m = (Message<T>) createMessageInstance(typeClass);
                    this.schema = schema = m.cachedSchema();
                }else{
                    // 创建schema对象
                    this.schema = schema = strategy.newSchema(typeClass);
                }
            }
        }
    }

    return schema;
}

```



**第二步：序列化过程：**

```java
// RuntimeSchema#writeTo
// 循环读取RuntimeSchema对象中封装序列化对象的filed(是按定义顺序存储的)
// 将对象的属性f的值写到output中
@Override
public final void writeTo(Output output, T message) throws IOException{
    for (Field<T> f : getFields())
        f.writeTo(output, message);
}


// RuntimeUnsafeFieldFactory#
// Unsafe#objectFieldOffset()方法用于获取某个字段相对Java对象的“起始地址”的偏移量，使用前面获取的偏移量来访问某个Java对象的某个字段。
public void writeTo(Output output, T message)throws IOException{
    if (primitive)
        output.writeInt64(number, us.getLong(message, offset),
                false);
    else
    {
        // 获取对象字段位置的值并写到outpu中
        Long value = (Long) us.getObject(message, offset);
        if (value != null)
            output.writeInt64(number, value.longValue(), false);
    }
}
```



**第三步：反序列化过程：**

```java
// IOUtil#mergeFrom
static <T> void mergeFrom(byte[] data, int offset, int length, T message, Schema<T> schema, boolean decodeNestedMessageAsGroup) {
    try {
        ByteArrayInput input = new ByteArrayInput(data, offset, length,
             decodeNestedMessageAsGroup);
        // input流中的数据merge到对象中
        schema.mergeFrom(input, message);
        input.checkLastTagWas(0);
        
    } 
}

// RuntimeSchema#mergeFrom
public final void mergeFrom(Input input, T message) throws IOException{
    for (int n = input.readFieldNumber(this); n != 0; n = input.readFieldNumber(this)){
        // 通过input流中字节，按字段定义顺序获取字段
        final Field<T> field = getFieldByNumber(n);
        if (field == null) {
            input.handleUnknownField(n, this);
        }else{
            field.mergeFrom(input, message);
        }
    }
}

// RuntimeFieldFactory#mergeFrom
public void mergeFrom(Input input, T message) throws IOException{
    if (primitive)
        us.putLong(message, offset, input.readInt64());
    else
        // 把值设置到对应对象的位置上
        us.putObject(message, offset,
                Long.valueOf(input.readInt64()));
}
```



##### 工具类使用

```xml
<dependency>
    <groupId>io.protostuff</groupId>
    <artifactId>protostuff-core</artifactId>
    <version>1.6.0</version>
</dependency>

<dependency>
    <groupId>io.protostuff</groupId>
    <artifactId>protostuff-runtime</artifactId>
    <version>1.6.0</version>
</dependency>
```

```java
// Protostuff序列化时是按可序列化字段顺序只把value保存到字节码中。
// 说明: Protostuff不能修改字段顺序，
public abstract class ProtostuffUtils {

    // 序列化方法，把指定对象序列化成字节数组
    @SuppressWarnings("unchecked")
    public static <T> byte[] serialize(T obj) {
        Class<T> clazz = (Class<T>) obj.getClass();
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        return ProtostuffIOUtil.toByteArray(obj, schema, LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE));
    }

    // 反序列化方法，将字节数组反序列化成指定Class类型
    public static <T> T deserialize(byte[] data, Class<T> clazz) {
        Schema<T> schema = RuntimeSchema.getSchema(clazz);;
        T obj = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(data, obj, schema);
        return obj;
    }
}


```



#### 5. kryo序列化方式

```xml
 <dependency>
    <groupId>com.esotericsoftware</groupId>
    <artifactId>kryo</artifactId>
    <version>5.0.0-RC2</version>
</dependency>
```

```java
// 说明：如果添加，删除，修改字段名字和位置都不会引起反序列化异常。
// 注意：添加的新字段序列化字段不是默认的值。
public abstract  class KryoUtils {
    private static Pool<Kryo> kryoPool = new Pool<Kryo>(true, false, 8) {
        protected Kryo create() {
            Kryo kryo = new Kryo();
            kryo.setRegistrationRequired(false);
            kryo.setReferences(false);
            // Configure the Kryo instance.
            return kryo;
        }
    };
    private static Pool<Output> outputPool = new Pool<Output>(true, false, 16) {
        protected Output create() {
            return new Output(1024, -1);
        }
    };
    private static Pool<Input> inputPool = new Pool<Input>(true, false, 16) {
        protected Input create() {
            return new Input(1024);
        }
    };

    public static byte[] serialize(Object object) {
        Kryo kryo = kryoPool.obtain();
        Output output = outputPool.obtain();
        try {
            output.reset();
            kryo.writeObject(output, object);
            return output.getBuffer();
        } finally {
            kryoPool.free(kryo);
            outputPool.free(output);
        }
    }
    public static <T> T deserialize(byte[] bytes,Class<T> clazz) {
        Kryo kryo = kryoPool.obtain();
        Input input= inputPool.obtain();
        try {
            input.setBuffer(bytes);
            return kryo.readObject(input, clazz);
        } finally {
            kryoPool.free(kryo);
            inputPool.free(input);
        }
    }
}
```



#### 6. 各种序列化反序列化对比

```java
// 序列化对象
coupon.setId(10000L)
coupon.setActivityId(10000L)
coupon.setAppCode(1)
coupon.setChannel(1)
coupon.setCouponType(1)
coupon.setCreateTime(new Date())
coupon.setCreator("wuji")
coupon.setDays(12)
coupon.setDescription("wuji的牛逼券")
coupon.setEndTime(new Date())
coupon.setExtendType(1)
coupon.setIsMember(1)
coupon.setIsNewMember(1)
coupon.setIsNewPerson(1)
coupon.setLimitNum(10)
coupon.setLimitType(1)
coupon.setName("wujiwujizhenwuji")
coupon.setPreferentialContent("满100减10元")
coupon.setPreferentialDetail(100)
coupon.setPreferentialType(1)
coupon.setSendEndTime(new Date())
coupon.setSendStartTime(new Date())
coupon.setShopId(0L)
coupon.setSource(1)
coupon.setStartTime(new Date())
coupon.setStatus(1)
coupon.setTerminalType(1)
coupon.setThreshold(10000)
coupon.setUpdateTime(new Date())
coupon.setUseMode(1)
coupon.setValidityDaysType(1);

// 序列化和反序列化1000000次的时间
jdk: 7386ms / 28097ms
hessian：10488ms / 13398ms
json: 1981ms / 3651ms
kryo: 1239ms / 1298ms
protostuff: 1110ms / 1003ms

```



|                  | Protostuff | Kryo    | Fastjson | Hession  | JDK      |
| ---------------- | ---------- | ------- | -------- | -------- | -------- |
| 序列化(单位us)   | 1.11us     | 1.239us | 1.981us  | 10.488us | 7.386us  |
| 反序列化(单位us) | 1.003us    | 1.298us | 3.651us  | 13.398us | 28.097us |
| bytes            | 173        | 256     | 592      | 661      | 1110     |



**序列化：**

* 时间复杂度：jdk > hession > fastjson, jackson, gson > protobuf, protostuff, kryo
* 空间复杂度：jdk > hession ≈ fastjson, jackson, gson > protobuf, protostuff, kryo



**反序列化：**

* 时间复杂度：jdk > hession ≈ fastjson, jackson, gson > protobuf, protostuff, kryo



**说明：**json序列化反序列化小对象的速度几乎和kryo一样。经过一序列对比，我觉得使用Protostuff和Fastjson最好，Protostuff速度最快，空间最小，跨平台。Fastjson可读性最好且跨平台。