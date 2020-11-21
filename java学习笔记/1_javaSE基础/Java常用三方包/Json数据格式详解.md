### Json数据格式解析

#### 1. Json语法格式

JSON 语法是 JavaScript 对象表示法语法的子集。

* 数据在名称/值对中  ==> "firstName" : "John"
* 数据由逗号分隔
* 数据中键一定要用""括起来
* {} 花括号保存对象
* [] 方括号保存数组



JSON 值可以是：

* 数字     ==> 整数或浮点数 如：1
* 字符串 ==> 在双引号中 如："hello"
* 逻辑值 ==> true 或 false 如：false
* 数组     ==> 在方括号中，如：[1,2,"hello"]
* 对象     ==> 在花括号中，如：{ "firstName":"John" , "lastName":"Doe" }
* null值  ==> null



#### 2. FastJson

```xml
<!-- https://mvnrepository.com/artifact/com.alibaba/fastjson -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>1.2.71</version>
</dependency>
```



**FastJson-API**

```
-- JSONObject类：继承JSON,实现Map<String, Object>
-- JSONArray类： 继承JSON,实现List<Object>

-- JSONReader类: 可以在本地读取或者网络读取数据并可以反序列化
-- JSONWriter类: 可以将对象序列化Json字符串，写入本地或者发送至服务器

-- TypeReference: 类型引用在反序列化化很常用
```

```java
// 1. JSON序列化
// 将JavaBean序列化为JSON文本  
public static final String toJSONString(Object object); 
public static final String toJSONString(Object object, boolean prettyFormat); 

//将JavaBean转换为JSONObject或者JSONArray
public static final Object toJSON(Object javaObject); 

// 1. JSON反序列化
// 把JSON文本parse为JSONObject或者JSONArray
public static final Object parse(String text); 
public static final JSONObject parseObject(String text);    
public static final JSONArray parseArray(String text); 

// 把JSON文本parse为JavaBean对象/JavaBean集合  
public static final  T parseObject(String text, Class clazz);  
public static final  List parseArray(String text, Class clazz); 

// 解析带泛型参数
Demo<SpringUtilTest.Person> person = JSON.parseObject(demoJson, new TypeReference<Demo<SpringUtilTest.Person>>() {});

```



#### 3. Jackson

```xml
<!-- https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-databind -->
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.11.0</version>
</dependency>

```



**ObjectMapper-API**

```java



```









#### 4. Gson

```xml
<!-- https://mvnrepository.com/artifact/com.google.code.gson/gson -->
<dependency>
    <groupId>com.google.code.gson</groupId>
    <artifactId>gson</artifactId>
    <version>2.8.6</version>
</dependency>

```



**Gson-API**


```java
-- JsonElement抽象类
  public boolean isXXX()  // 判断json类型 jsonnull/jsonobject/jsonarray/jsonprimitive 
  public JsonObject getAsJsonObject()
  public JsonArray getAsJsonArray()
  public xxx getAsXXX()  // 获取原生属性值会报错

  -- JsonNull类【null】
  -- JsonPrimitive类（json原生类型 ==> 重写获取所有原生json属性的值）
    public xxx getAsXXX()  
    public String getAsString()

  -- JsonObject类【json对象类型】
    private final LinkedTreeMap<String, JsonElement> members; //底层维护一个TreeMap
    public void add(String property, JsonElement value)
    public void addProperty(String property, String value)
    public void addProperty(String property, Number value)
    public void addProperty(String property, Boolean value)
    public void addProperty(String property, Character value)
    public JsonElement get(String memberName)

  -- JsonArray类【json数组】
    private final List<JsonElement> elements; // 底层维护一个集合
    public void add(xxx ) bool/char/Number/String/JsonElement
    public void addAll(JsonArray array)
    public boolean remove(JsonElement element)
    public JsonElement get(int i)
    public Iterator<JsonElement> iterator()

-- JsonParser类
public JsonElement parse(String json) throws JsonSyntaxException


-- Gson序列化和发序列化入口类      
// 序列化对象      
public String toJson(Object src)  
      
//反序列化对象      
public <T> T fromJson(String json, Class<T> classOfT) 
  
//反序列化集合 new TypeToken<List<String>>() {}.getType()
public <T> T fromJson(String json, Type typeOfT) throws JsonSyntaxException       

      
// 源码简单分析：gson创建过程会加载 TypeAdapterFactory ，每种工厂用于生成对应TypeAdapter【里面有read,write抽象方法，用于实现序列化和反序列化功能】，
      
```



**属性重命名**

```java
// json在序列化的时候，以name为属性名显示。在反序列化的时候同时支持name,alternate
@SerializedName(value="name",alternate= {"user_name","NAME"})
private String name; 

```



**字段过滤**

```java
// 1.基于@Expose注解
@Expose //
@Expose(deserialize = true,serialize = true) // 序列化和反序列化都都生效
@Expose(deserialize = true,serialize = false) // 反序列化时生效
@Expose(deserialize = false,serialize = true) // 序列化时生效
@Expose(deserialize = false,serialize = false) // 和不写一样
Gson gson = new GsonBuilder()
    .excludeFieldsWithoutExposeAnnotation()
    .create();

// 2.基于版本:Gson在对基于版本的字段导出提供了两个注解 @Since和@Until，注解都接收一个Double值。
// 使用方法：当前版本(GsonBuilder中设置的版本) 大于等于Since的值时该字段导出，小于Until的值时该字段导出。
Gson gson = new GsonBuilder().setVersion(version).create();

 
// 3.基于访问修饰符
Gson gson = new GsonBuilder()
  .excludeFieldsWithModifiers(Modifier.PRIVATE, Modifier.STATIC)
  .create();

// 4.transient修饰符：给需要排除的字段加上对应修饰符

```



**GsonBuilder个性配置**

```java
Gson gson = new GsonBuilder()
        //序列化null
        .serializeNulls()

        // 设置日期时间格式，另有2个重载方法【在序列化和反序化时均生效】
        .setDateFormat("yyyy-MM-dd")

        // 禁此序列化内部类
        .disableInnerClassSerialization()

        //生成不可执行的Json（多了 )]}' 这4个字符）
        .generateNonExecutableJson()

        //禁止转义html标签
        .disableHtmlEscaping()

        //格式化输出
        .setPrettyPrinting()
        .create();
```



**自定义序列化和反序列化**

```java
-- TypeAdapter泛型抽象类: 用于接管某种类型的序列化和反序列化过程，包含序列化和反序列化抽象方法，
  public abstract void write(JsonWriter var1, T var2) throws IOException;
  public abstract T read(JsonReader var1) throws IOException;

public class UserTypeAdapter extends TypeAdapter<User> {

    @Override
    public void write(JsonWriter jsonWriter, User user) throws IOException {
        //流式序列化成对象开始
        jsonWriter.beginObject();
      
        jsonWriter.name("Name").value(user.getName());
        jsonWriter.name("Age").value(user.getAge());
        jsonWriter.name("Sex").value(user.isSex());

        //流式序列化结束
        jsonWriter.endObject();
    }

    @Override
    public User read(JsonReader jsonReader) throws IOException {
        User user = new User();

        //流式反序列化开始
        jsonReader.beginObject();
        while (jsonReader.hasNext()) {
            switch (jsonReader.nextName()) {
                //首字母大小写均合法
                case "name":
                case "Name":
                    user.setName(jsonReader.nextString());
                    break;

                case "age":
                    user.setAge(jsonReader.nextInt());
                    break;
                case "sex":
                    user.setSex(jsonReader.nextBoolean());
                    break;
            }
        }

        //流式反序列化结束
        jsonReader.endObject();
        return user;
    }
}

Gson gson = new GsonBuilder()
  .registerTypeAdapter(User.class, new UserTypeAdapter())
  .create();

```

