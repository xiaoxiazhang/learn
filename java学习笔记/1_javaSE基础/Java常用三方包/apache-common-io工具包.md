### apache-common-io工具类使用

```xml
<!-- https://mvnrepository.com/artifact/commons-io/commons-io -->
<dependency>
    <groupId>commons-io</groupId>
    <artifactId>commons-io</artifactId>
    <version>2.6</version>
</dependency>
```



#### 1. FilenameUtils

```java
// 举例：/root/hello/a.txt

// 获取文件名 ==> a.txt
public static String getName(final String filename)
  
// 文件路径去除目录和后缀后的文件 ==> a
public static String getBaseName(final String filename) 
 
// 获取文件的后缀 ==> .txt
public static String getExtension(final String filename)
  
// 获取文件的完整目录 ==> /root/hello/
public static String getFullPath(final String filename) 


// 移除文件的扩展名
public static String removeExtension(final String filename)
  
```







#### 2. FileUtils

```java
// 功能1：文件写入

// 写入字符到文件
public static void write(final File file, final CharSequence data, final String encoding, final boolean append) throws IOException
  
// 写入字符到文件
public static void writeStringToFile(final File file, final String data, final Charset encoding,final boolean append) throws IOException

// 写入字符集合到文件行 ==> 使用BuffedOutputStream 包装
public static void writeLines(final File file, final Collection<?> lines, final boolean append) throws IOException

// 写入字节数组到文件
public static void writeByteArrayToFile(final File file, final byte[] data, final boolean append) throws IOException
  

// 功能2：读取文件
  
// 读取文件到字节数组
public static byte[] readFileToByteArray(final File file) throws IOException

// 读取文件到字符，需要制定编码格式  
public static String readFileToString(final File file, final Charset encoding) throws IOException
  
// 按行读取文件，包装BufferedReader实现
public static List<String> readLines(final File file, final Charset encoding) throws IOException
  
  
// 功能3：拷贝文件
// 拷贝目录  
public static void copyDirectory(final File srcDir, final File destDir) throws IOException
// 拷贝文件
public static void copyFile(final File srcFile, final File destFile) throws IOException 
// 拷贝文件到输出流 
public static long copyFile(final File input, final OutputStream output) throws IOException
// 拷贝url内容到文件  
public static void copyURLToFile(final URL source, final File destination) throws IOException
  
// URL 内容转化为文件或者字节数组
URL url = new URL("http://www.baidu.com/img/baidu_logo.gif");
File file = new File("baidu1.gif");
FileUtils.copyURLToFile(url, file);
		
//下载方式2
InputStream in = new URL("http://www.baidu.com/img/baidu_logo.gif").openStream();
byte[] gif = IOUtils.toByteArray(in);
FileUtils.writeByteArrayToFile(new File("baidu2.gif"), gif);

```







#### 3. IOUtils

```java
// 功能1：将输入流转化为字节数组
public static byte[] toByteArray(final InputStream input) throws IOException
public static byte[] toByteArray(final Reader input, final Charset encoding) throws IOException
  
  
// 功能2：将输入流数组拷贝到输出流
  
// 字节流 ==> 字节流  
public static int copy(final InputStream input, final OutputStream output) throws IOException 
// 字节流 ==> 字符流 :将字节输入流转化为字符流，并指定编码
public static void copy(final InputStream input, final Writer output, final String inputEncoding) throws IOException  
  
// 字符流 ==> 字节流 :将字节输出流转为字符流，指定编码
public static void copy(final Reader input, final OutputStream output, final String outputEncoding) throws IOException
  
// 字符流 ==> 字符流  
public static int copy(final Reader input, final Writer output) throws IOException  
  
// 字节输入和输出流拷贝 
public static long copyLarge(final InputStream input, final OutputStream output)
throws IOException 
// 字符输入流和输出流拷贝   
public static long copyLarge(final Reader input, final Writer output) throws IOException 
  
  

```

