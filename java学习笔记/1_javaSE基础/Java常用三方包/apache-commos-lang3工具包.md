### apache-commos-lang3工具类

```xml
<dependency>
  <groupId>org.apache.commons</groupId>
  <artifactId>commons-lang3</artifactId>
  <version>3.9</version>
</dependency>
```



#### 1. StringUtils

```java
// 判断空字符串：null和空字符串为真
public static boolean isEmpty(CharSequence cs)
public static boolean isNotEmpty(CharSequence cs)

// 判断空字符串+空白字符串：null和空字符串和空白字符为真
public static boolean isBlank(CharSequence cs)
public static boolean isNotBlank(CharSequence cs)
public static boolean isWhitespace(CharSequence cs)

// 判断数字字符串
public static boolean isNumeric(CharSequence cs) 
public static boolean isNumericSpace(CharSequence cs)

// 判断字符串是否字母
public static boolean isAlpha(CharSequence cs) 
public static boolean isAlphaSpace(CharSequence cs)

// 判断字符串是否都是大写或者小写
public static boolean isAllUpperCase(CharSequence cs)
public static boolean isAllLowerCase(CharSequence cs)

//分割字符（splitPreserveAllTokens会包含空格）
public static String[] split(String str)
public static String[] split(String str, String separatorChars) {
public static String[] split(String str, String separatorChars, int max)
public static String[] splitPreserveAllTokens(final String str)

//字符串连接 包括各种基本类型数组
public static String join(final Object[] array, final String separator)
```



#### 2. RandomStringUtils

```java
// 产生count位(a-z, A-Z)的随机字符
public static String randomAlphabetic(final int count)

// 产生count位(a-z, A-Z) and the digits 0-9)的随机字符
public static String randomAlphanumeric(final int count) 

// 产生count位(ASCII value is between {@code 32} and {@code 126} )的随机字符
public static String randomAscii(final int count) 

// 产生count位(0-9)的随机字符
public static String randomNumeric(final int count)

// 产生count位(chars变量中)的随机字符
public static String random(final int count, final String chars) 
```





#### 3. ArrayUtils

```java
//追加元素到数组尾部 ，返回新的数组 
public static int[] add(final int[] array, final int element)

//删除指定位置数组，返回新的数组 
public static int[] remove(final int[] array, final int index)

//截取数组，不包括后面的值，返回新的数组 
public static int[] subarray(final int[] array, int startIndexInclusive, int endIndexExclusive)

//获取数组第一次（从前面开始或者从后面开始）匹配索引
public static int indexOf(final int[] array, final int valueToFind)
public static int lastIndexOf(final int[] array, final int valueToFind)

//判断数组是否包含某元素（indexOf方法来实现）
public static boolean contains(final int[] array, final int valueToFind) 

//判断数组是否为空(null后者长度为0)
public static boolean isEmpty(final int[] array)	
```





#### 4. DateUtils

```java
// 设置日期时间，返回新的日期时间（Calendar实现）
public static Date setYears(final Date date, final int amount)
public static Date setSeconds(final Date date, final int amount)
private static Date set(final Date date, final int calendarField, final int amount)

// 计算日期时间（Calendar实现）
public static Date addYears(final Date date, final int amount)
public static Date addSeconds(final Date date, final int amount) 
private static Date add(final Date date, final int calendarField, final int amount)

// 比较日期时间(Date类)
public long getTime() 

// 解析日期时间(按多种格式解析)
public static Date parseDate(final String str, final String... parsePatterns) throws ParseException 

// 格式化日期时间(DateFormatUtils)
public static String format(final Date date, final String pattern) 
```





#### 5. StopWatch

```java
// 启动开始时间属性
private long startTime;
private long startTimeMillis;

// 结束时间属性
private long stopTime;

// 
start();         //开始计时
split();         //设置split点
getSplitTime();  //获取从start 到 最后一次split的时间
reset();         //重置计时
suspend();       //暂停计时, 直到调用resume()后才恢复计时
resume();        //恢复计时
stop();          //停止计时
getTime();       //统计从start到现在的计时

```

