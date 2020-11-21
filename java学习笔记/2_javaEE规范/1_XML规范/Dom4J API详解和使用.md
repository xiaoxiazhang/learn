###Dom4J API详解和使用

####  一. DOM4J API说明

```
// DOM接口设计
-- Node接口 ==> 节点抽象：属性节点，文本节点，元素节点, CDATA, DOCUMENT, NAMESPACE
  核心方法：getParent, setParent, getDocument, getText, selectNodes,

  -- Attribute接口 ==> 属性节点
    核心方法：getQName, getNamespace, getData

  -- Branch接口  ==> 分支：元素节点，文档节点
    核心接口：node, indexOf, nodeIterator, add, remove,   
    -- Document接口 ==> 文档根节点
      核心接口：getRootElement, getDocType, getXMLEncoding
    -- Element接口 ==> 节点属性
      核心接口：getQName, getNamespace, attribute, attributeValue

  -- CharacterData接口 ==> 字符数据：CDATA, 文本节点，注释
    -- CDATA接口   ==> 标识接口
    -- Comment接口 ==> 标识接口
    -- Text接口    ==> 标识接口

  -- DocumentType接口 ==> 定义XML DOCTYPE 声明
    核心接口：getElementName, getPublicID
  -- Entity接口
  -- ProcessingInstruction接口 ==> 定义XML处理指令
```



##### 1.  SAXReader类

```java
// 解析XML文件或者流
public Document read(File file)
public Document read(InputStream in)
public Document read(Reader reader)
  
```



##### 2. DocumentHelper类

```java
// 解析xml文本
public static Document parseText(String text)
  
```



#### 二. DOM4j API使用

##### 1.  Document对象相关 

```java
// #1.读取XML文件,获得document对象.      
SAXReader reader = new SAXReader();  
Document   document = reader.read(new File("input.xml"));  
    
// #2.解析XML形式的文本,得到document对象.      
String text = "<members></members>";      
Document document = DocumentHelper.parseText(text);      

// #3.主动创建document对象.      
Document document = DocumentHelper.createDocument();      
Element root = document.addElement("members");// 创建根节点

```

 

##### 2.  Element节点相关 

```java
// #1.获取文档的根节点.      
Element  rootElm = document.getRootElement();      

// #2.取得某节点的单个子节点.      
Element  memberElm=root.element("member"); 
     
// #3.取得节点的内容   
String text=memberElm.getText();     
String text=root.elementText("name");       

// #4.取得某节点下指定名称的所有节点并进行遍历.      
List nodes = rootElm.elements("member");      
for (Iterator it = nodes.iterator(); it.hasNext();) {      
    Element elm = (Element) it.next();      
   // do something      
}     
 
// #5.对某节点下的所有子节点进行遍历.      
  for(Iterator it=root.elementIterator();it.hasNext();){      
       Element element = (Element) it.next();      
      // do something      
  }  
    
// #6.在某节点下添加子节点.      
Element ageElm = newMemberElm.addElement("age");    
  
// #7.设置文本节点.      
ageElm.setText("29");   
   
// #8.删除某节点.      
parentElm.remove(childElm);    

// #9.添加一个CDATA节点.      
Element contentElm = infoElm.addElement("content");      
contentElm.addCDATA(diary.getContent());   

```



##### 3.  Attribute 属性相关.

```java
// #1.取得节点的指定的属性      
Element root = document.getRootElement();          
Attribute  attribute = root.attribute("size");    // 属性名name   
   
// #2.取得属性的文本      
String text=attribute.getText();    
String text2=root.element("name").attributeValue("firstname");

   
// #3.遍历某节点的所有属性      
Element root=document.getRootElement();
for(Iterator it=root.attributeIterator();it.hasNext();){
    Attribute attribute = (Attribute) it.next();
    String text=attribute.getText();
    System.out.println(text);
}

// #4.设置某节点的属性和文字.      
newMemberElm.addAttribute("name", "sitinspring");    

// #5.设置属性的文字      
Attribute attribute=root.attribute("name");      
attribute.setText("sitinspring");   
   
// #6.删除某属性      
Attribute attribute=root.attribute("size");      
root.remove(attribute);  

```



##### 4.  将文档写入XML文件. 

```java
// #1.文档中全为英文,不设置编码,直接写入.      
XMLWriter writer = new XMLWriter(new FileWriter("output.xml"));      
writer.write(document);      
writer.close();      

// #2.文档中含有中文,设置编码格式再写入.      
OutputFormat format = OutputFormat.createPrettyPrint();
format.setEncoding("GBK");    // 指定XML编码
XMLWriter writer = new XMLWriter(new FileWriter("output.xml"),format);
writer.write(document);
writer.close();

```



##### 5.  字符串与XML的转换   

```java
// #1.将字符串转化为XML      
String text = "<members> <member>sitinspring</member> </members>";      
Document document = DocumentHelper.parseText(text);    
  
// #2.将文档或节点的XML转化为字符串.      
SAXReader reader = new SAXReader();
Document document = reader.read(new File("input.xml"));
Element root=document.getRootElement();
String docXmlText = document.asXML();
String rootXmlText = root.asXML();
Element memberElm = root.element("member");
String memberXmlText = memberElm.asXML();

```

 

#### 三. XML文档操作详解

##### 1.  创建xml文件

```java

@Test
public void createXML() throws Exception {
    List<Book> books = Arrays.asList(
            new Book("1", "java性能优化", "zxx", "2016-10-08", "20", "zh-cn"),
            new Book("2", "python性能优化", "zqq", "2016-11-30", "20.1", "zh-cn")
    );

    Document doc = DocumentHelper.createDocument();
    doc.setDocType(new DOMDocumentType("1", "2", "3"));

    Element bookstore = doc.addElement("bookstore");
    for (Book book : books) {
        Element bookEle = bookstore.addElement("book");
        bookEle.addAttribute("id", book.getId());

        BeanInfo beanInfo = Introspector.getBeanInfo(Book.class);
        PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            if("class".equals(propertyDescriptor.getName())){
                continue;
            }
            Element PropertiesElement = bookEle.addElement(propertyDescriptor.getName());
            Method readMethod = propertyDescriptor.getReadMethod();
            readMethod.setAccessible(true);
            PropertiesElement.setText((String) readMethod.invoke(book));
        }
    }

    OutputFormat format = OutputFormat.createPrettyPrint();
    format.setEncoding("UTF-8");
    XMLWriter writer = new XMLWriter(new FileOutputStream("books.xml"), format);
    writer.setEscapeText(false);
    writer.write(doc);
    writer.close();
}
```



##### 2.  读取解析XML文件  

```java
@Test
public void parserXML() throws Throwable {
    List<Book> books = new ArrayList<Book>();
    Document doc = new SAXReader().read(this.getClass().getClassLoader().getResourceAsStream("books.xml"));

    Element bookStoreElement = doc.getRootElement();
    BeanInfo beanInfo = Introspector.getBeanInfo(Book.class);
    PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();

    Iterator bookStoreIterator = bookStoreElement.elementIterator();
    while (bookStoreIterator.hasNext()){
        Element bookElement = (Element)bookStoreIterator.next();
        Book book = new Book();
        for (PropertyDescriptor propertyDescriptor : propertyDescriptors) {
            Element propertiesElement = bookElement.element(propertyDescriptor.getName());
            if (propertiesElement != null) {
                Method writeMethod = propertyDescriptor.getWriteMethod();
                writeMethod.setAccessible(true);
                try {
                    writeMethod.invoke(book, propertiesElement.getText());
                } catch (Exception e) {
                    log.error("invoke method error", e);
                }
            }
        }
        books.add(book);
    }
    System.out.println(books);
}
```



 

 