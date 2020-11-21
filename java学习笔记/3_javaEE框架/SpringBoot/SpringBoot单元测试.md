### SpringBoot单元测试

#### (一). 单元测试原则

**单元测试基本目标：**语句覆盖率达到 60-70%；核心模块的语句覆盖率和分支覆盖率都要达到 100%。

**写单元测试时机：**在项目提测前完成单元测试。测试人员可以在验证功能代码的时候，间接验证单元测试。如果有bug，说明你的单元测试没有找到对应的问题。此时你不仅要修复你的功能代码，还需要完善你的单元测试。



**单元测试 (AIR) 原则：**

* 全自动性【Automatic】：测试中不准使用 System.out 来进行人肉验证，必须使用 assert 来验证。
* 独立性【Independent】：单元测试用例之间决不能互相调用，也不能依赖执行的先后次序。
* 可重复性【Repeatable】：不能受到外界环境的影响，可多次重复运行。



**单元测试应包含的Case：**

* 正常流程测试：正常参数输入，是否得到预期结果。

* 异常流程测试：非业务允许输入，非法数据，是否得到预期结果

* 边界值测试：循环边界、特殊取值等，是否得到预期结果。



**单元测试的好处：**第一，在添加新功能的时候，如果代码改动量超过10个类，可能会影响之前开发的功能，如果有单元测试，你只需要加入新增代码的单元测试。然后跑一下全量回归测试，基本上能验证是否对老业务是否有影响。其次，能够安全的进行重构，每次重构完一小块代码，执行一遍单元测试就能知道是否有问题。最后，对于一些小的修改和重构，每次都劳烦测试来帮忙验证肯定是不好的，单测是我们自测上线的信心来源。

说明：单测一定是测试验证过的，很重要，很重要，很重要。【你执行单测测试发现没有问题，但是测试人员测试你的功能代码有问题 ==> 说明你的单测就是有问题的，赶紧去修改。】





#### (二). JUnit的基本使用

##### 1. JUnit基本注解

@BeforeClass：在所有测试方法前执行一次，一般在其中写上整体初始化的代码

@AfterClass：在所有测试方法后执行一次，一般在其中写上销毁和释放资源的代码

@Before:：在每个测试方法前执行，一般用来初始化方法（比如我们会在@Before注解的方法中重置数据）

@After：在每个测试方法后执行，在方法执行完成后要做的事情

@Test(timeout = 1000)：测试方法执行超过1000毫秒后算超时，测试将失败

@Test(expected = Exception.class)：测试方法期望得到的异常类，如果方法执行没有抛出指定异常则测试失败

@Ignore(“not ready yet”)：执行测试时将忽略掉此方法，如果用于修饰类，则忽略整个类

@RunWith： 在JUnit中有很多种Runner，他们负责调用你的测试代码。

**注解的执行顺序：**@BeforeClass 修饰的方法 ==> @Before修饰的方法 ==> @Test修饰的方法 ==> @After修饰的方法 ==> @AfterClass修饰的方法

```java
// 测试方法必须使用@Test修饰
// 测试方法必须使用public void修饰，并且不带参数
// 新建test源代码目录来存放测试代码
// 测试类的包名应该和被测试类的报名保持一致
// 测试单元中的每个方法必须可以独立测试，测试方法之间不能有任何依赖
// 测试类一般使用Test作为后缀
// 测试方法一般使用test作为前缀
public class CalculatorTest {
    private static Calculator calculator;    
    @BeforeClass
    public static void BuildCalculator() {
        calculator = new Calculator();
    }   
    @Test
    public void testAdd() {
        Assert.assertEquals(8, calculator.add(3, 5));        
    }
}
```



##### 2. JUnit常用断言

```java
// Junit Assert类
assertEquals(? expected, ? actual)      // 断言两个对象或者基本对象是否相等
assertArrayEquals(? expected, ? actual) //断言两个数组是否包含相同的元素
assertTrue(boolean condition)   // 断言语句真
assertFalse(boolean condition)  // 断言语句为假
assertNull(obj)    // 断言对象为空
assertNotNull(obj) // 断言对象不为空
assertSame(? expected, ? actual)   // 断言两个对象引用指向同一实例
assertNotSame(? expected, ? actual)// 断言两个对象引用指向不同实例

// 可以等效替代前面的所有Assert,使用Matcher匹配器
assertThat(T actual, Matcher<? super T> matcher) //org.hamcrest.Matcher
```



```java
// Matchers常用匹配器【包含各种各样的匹配器】

// ===========通用匹配规则===================
// allOf：匹配符表明所有条件必须都成立，断言通过
assertThat(testedNumber, allOf(greaterThan(8), lessThan(16))); 

// anyOf：匹配符表明所有条件只要有一个成立，断言通过
assertThat(testedNumber, anyOf(greaterThan(16), lessThan(8)));

// anything：匹配符表明无论什么条件永远为true
assertThat(testedNumber, anything());

// is：匹配符表明如果前面等于后面的值，断言通过
assertThat(testedString, is("developerWorks"));

// not：匹配符和is匹配符正好相反，表明如果前面待测的object不等于后面给出的object，则测试通过
assertThat( testedString, not( "developerWorks"));


// ============字符串相关匹配符===========
// containsString：断言包含字符串
assertThat( testedString, containsString("developerWorks"));

// endsWith:断言字符结尾
assertThat( testedString, endsWith("developerWorks")); 

// startsWith：断言字符开头
assertThat( testedString, startsWith( "developerWorks"));

// equalTo：断言相等，equalTo可以测试数值之间，字符串之间和对象之间是否相等.
assertThat( testedValue, equalTo( expectedValue ) ); 

// equalToIgnoringCase：断言忽略大小写相等
assertThat( testedString, equalToIgnoringCase( "developerWorks" ) ); 

// equalToIgnoringWhiteSpace：断言字符trim后相等。
assertThat( testedString, equalToIgnoringWhiteSpace( "developerWorks" ) );


// ========== 数值相关匹配符 ============
// closeTo：断言浮点数字范围
assertThat( testedDouble, closeTo(20.0, 0.5 ) );

// greaterThan：断言数值>某个值
assertThat( testedNumber, greaterThan(16.0) );

// greaterThanOrEqualTo：断言数字 >= 某个值
assertThat( testedNumber, greaterThanOrEqualTo (16.0) );

// lessThan：断言数字< 某个值
assertThat( testedNumber, lessThan (16.0) );

// lessThanOrEqualTo：断言数字<= 某个值
assertThat( testedNumber, lessThanOrEqualTo (16.0));


//========collection相关匹配符==========
// hasEntry：断言map包含（key,value)。
assertThat( mapObject, hasEntry( "key", "value" ) );

// hasItem：断言集合中包含 item
assertThat( iterableObject, hasItem ("element"));

// hasKey：断言map中包含key
assertThat( mapObject, hasKey ( "key" ) );

// hasValue：断言map中包含value
assertThat( mapObject, hasValue ( "key" ) ); 
```



 ##### 3. JUnit扩展使用

**参数化测试：**

* 创建一个名为ParameterTest的测试类，并用RunWith注解来改变测试运行器。
* 声明变量来存放预期值和结果值。
* 声明一个返回值为Collection的公共静态方法，并使用 @Parameters进行修饰。
* 为测试类声明一个带有参数的公共构造方法，并在其中为之声明变量赋值。

```java
@RunWith(Parameterized.class)
public class ParameterTest {
    
    private static Calculator calculator;
    
    @BeforeClass
    public static void setUpBeforeClass() {
        calculator = new Calculator();
    }
    
    private int expected;
    private int input1;
    private int input2;
    
    @Parameters
    public static Collection<Object[]> setParameters() {
        Object[][] objs = {{8,3,5},{5,3,2}};
        
        return Arrays.asList(objs);
    }
    
    public ParameterTest(int expected, int input1, int input2) {
        this.expected = expected;
        this.input1 = input1;
        this.input2 = input2;
    }
    
    @ Test
    public void testParameters() {
        Assert.assertEquals(expected, calculator.add(input1, input2));
    }
}
```



**批量执行的测试类** 

```java
//使用@Suite.SuiteClasses来指定要批量执行的测试类（数组的形式）
@RunWith(Suite.class)
@Suite.SuiteClasses(StringTest.class)
public class SuiteTest {
}
```



**用规则来装饰测试：**

* 配置全局超时：@Test(timeout=XX)可以用于某一个测试方法。

* 预期异常：有时你想要的代码就只在某些情况抛出异常，比如，无效的输入，你希望抛出IllegalArgumentException。如果没有抛出异常或者抛出其他异常，则测试图不通过。

  ```java
  // 方式一：使用expected属性标识具体异常
  public void throwSomeException() throws IllegalArgumentException{
      throw new IllegalArgumentException("抛出异常了");
  				
  }
  
  @Test(expected=IllegalArgumentException.class)
  public void test3(){
      throwSomeException();
  }
  
  
  // 方式二：使用Rule
  @Rule
  public ExpectedException expectedException = ExpectedException.none();
  
  @Test
  public void shouldTestExceptionMessage() throws IndexOutOfBoundsException {
          List<Object> list = new ArrayList<Object>();
          thrown.expect(IndexOutOfBoundsException.class);
          thrown.expectMessage("Index: 0, Size: 0");
          list.get(0); // execution will never get past this line  
  }
  
  // 方式三：使用try…fail...catch…
  @Test
  public voidtestExceptionMessage() {
        try {
            new ArrayList<Object>().get(0);
            fail("Expected an IndexOutOfBoundsException to be thrown");
        } catch (IndexOutOfBoundsException anIndexOutOfBoundsException) {
            assertThat(anIndexOutOfBoundsException.getMessage(), 
                       is("Index: 0, Size: 0"));
        }  
  }
  
  // 注意：我更倾向于方式三。可以通过代码的方式
  ```

  

##### 4. JUnit源码分析

**Junit中类说明**

```java
FrameworkMethod类  ==> 封装了测试类的方法
FrameworkField类   ==> 封装测试类的属性

// 被测试类的封装
TestClass{
  //将注解信息封装到methodsForAnnotations，fieldsForAnnotatio
  private final Map<Class<?extends Annotation>,List<FrameworkMethod>> methodsForAnnotations;
  private final Map<Class<? extends Annotation>,List<FrameworkField>> fieldsForAnnotations;
}

Runner接口【继承结构】
  --ParentRunner抽象类    
    private volatile Collection<T> filteredChildren // 过滤后的测试方法集合
    private final TestClass testClass //测试类
    protected ParentRunner(Class<?> testClass) throws InitializationError { // 初始化设置测试类
      this.testClass = createTestClass(testClass);
      validate();
    }  
    --BlockJUnit4ClassRunner类(用来运行单个测试类)
      --SpringJUnit4ClassRunner类【spring单测】
        --SpringRunner
      --Suite(Suite用来一起运行多个测试类)

Statement抽象类【代表现实代码中的方法：如@Test,@before标识的方法或者如超时场景系统生成对应的Statement】
  evaluate(): 执行目标语句

  // 真实测试方法【简单反射调用】
  public class InvokeMethod extends Statement{
      // FrameworkMethod testMethod ;  Object target;
      @Override
      public void evaluate() throws Throwable {
          testMethod.invokeExplosively(target);
      }
  }

  // 前置执行语句【@Before,@ClassBefore对应方法】
  public class RunBefores extends Statement{
      // Statement next; Object target; List<FrameworkMethod> befores;
      public void evaluate() throws Throwable {
          for (FrameworkMethod before : befores) {
              before.invokeExplosively(target);
          }
          next.evaluate();
      }
  }

  // 后置执行语句【@After,@ClassAfter对应方法】
  public class RunAfters extends Statement {
      public void evaluate() throws Throwable {
        List<Throwable> errors = new ArrayList<Throwable>();
        try {
            next.evaluate();
        } catch (Throwable e) {
            errors.add(e);
        } finally {
            for (FrameworkMethod each : afters) {
                try {
                    each.invokeExplosively(target);
                } catch (Throwable e) {
                    errors.add(e);
                }
            }
        }
        MultipleFailureException.assertEmpty(errors);
    }
}

// 期待异常执行语句
public class ExpectException extends Statement {
    // Statement next ; Class<? extends Throwable> expected;
    public void evaluate() throws Exception {
        boolean complete = false;
        try {
            next.evaluate();
            complete = true;
        } catch (AssumptionViolatedException e) {
            throw e;
        } catch (Throwable e) {
            if (!expected.isAssignableFrom(e.getClass())) {
                String message = "Unexpected exception, expected<"
                        + expected.getName() + "> but was<"
                        + e.getClass().getName() + ">";
                throw new Exception(message, e);
            }
        }
        if (complete) {
            throw new AssertionError("Expected exception: "
                    + expected.getName());
        }
    }
}

// 超时对应的语句
public class FailOnTimeout extends Statement {
    ...
}

Description类【代表执行单测类或者单测方法的描述】
  Class<?> fTestClass        // 方法所在类信息
  String fDisplayName        // 描述名称
  Serializable fUniqueId     // 唯一ID
  Annotation[] fAnnotations  // 包含注解; 

Result类【结果是通过Listener的回调接口设置的】
  private final AtomicInteger count;                     // 测试数量
  private final AtomicInteger ignoreCount;               // 忽略数量
  private final CopyOnWriteArrayList<Failure> failures;  // 失败详情【包含方法描述和异常信息】
  private final AtomicLong runTime;                      // 测试开始时间
  private final AtomicLong startTime;                    // 测试开始时间
  // 创建监听器【用于监听单测执行过程，测试开始，结束，失败等】
  public RunListener createListener() {
        return new Listener();
  }

  @RunListener.ThreadSafe
  private class Listener extends RunListener {
      @Override
      public void testRunStarted(Description description) throws Exception {
          startTime.set(System.currentTimeMillis());
      }
      @Override
      public void testRunFinished(Result result) throws Exception {
          long endTime = System.currentTimeMillis();
          runTime.addAndGet(endTime - startTime.get());
      }
      @Override
      public void testFinished(Description description) throws Exception {
          count.getAndIncrement();
      }   
      @Override
      public void testFailure(Failure failure) throws Exception {
          failures.add(failure);
      }    
      @Override
      public void testIgnored(Description description) throws Exception {
          ignoreCount.getAndIncrement();
      }    
      @Override
      public void testAssumptionFailure(Failure failure) {
          // do nothing: same as passing (for 4.5; may change in 4.6)
      }
  }

RunNotifier类【代表的是执行通知类】
  // 通知的监听器
  private final List<RunListener> listeners = new CopyOnWriteArrayList<RunListener>();
  // 添加和删除监听器方法
  addListener(); removeListener();

  // 通知方法【调用所有listeners对应的通知回调方法】
  fireTestRunStarted(); fireTestRunFinished(); fireTestStarted()； 
  fireTestFailure(); fireTestIgnored(); fireTestFinished();

```



**junit单元测试执行流程**

```java
// 程序入口：JUnitCore#run
// RunnerBuilder是生产Runner的策略，如使用@RunWith(Suite.class)标注的类需要使用Suite。
// Idea启动的时候会根据注解信息创建对应的Runner.
public Result run(Runner runner) {
    Result result = new Result();
  
    //1. 创建RunListener单测运行监听器, 用于监听单测执行开始，结束，失败等事件，用于结果封装到Result对象
    RunListener listener = result.createListener();
    notifier.addFirstListener(listener);
    try {
        // 2. 执行前通知开始执行单测事件
        notifier.fireTestRunStarted(runner.getDescription());
      
        // 3. 开始真正执行单测的过程【实现见ParentRunner#run】
        runner.run(notifier);
      
        // 4. 执行后通知结束执行单测事件
        notifier.fireTestRunFinished(result);
    } finally {
        // 5. 移除监听器
        removeListener(listener);
    }
    return result;
}

// 核心流程1：执行单个类测试流程 ParentRunner#run
public void run(final RunNotifier notifier) {
    try {
        // 1. 封装类执行语句Statemen
        Statement statement = classBlock(notifier);
        // 2. 执行类测试语句Statement链
        // 执行链路：RunAfters#evaluate ==> RunBefores#evaluate  ==> 【类中所有子测试方法的执行Statement ==> 构造方法Statement链，并执行】
        statement.evaluate();
    } catch (AssumptionViolatedException e) {
        testNotifier.addFailedAssumption(e);
    } catch (StoppedByUserException e) {
        throw e;
    } catch (Throwable e) {
        testNotifier.addFailure(e);
    }
}


//核心流程2：构造类执行语句Statement过程【使用了责任链模式，封装Statement链】
protected Statement classBlock(final RunNotifier notifier) {
  
    // 封装类中所有测试方法的执行Statement
    Statement statement = childrenInvoker(notifier);
   
    if (!areAllChildrenIgnored()) {
        // 找到@BeforeClass注解对应方法，把上面的Statement包装到RunBefores的next中。
        statement = withBeforeClasses(statement);
        // 找到@AfterClass注解对应方法注解对应方法，把上面的Statement包装到RunAfters的next中。
        statement = withAfterClasses(statement);
        statement = withClassRules(statement);
    }
    return statement;
}    

// 【测试类中所有子测试方法的执行Statement】的封装 
protected Statement childrenInvoker(final RunNotifier notifier) {
    return new Statement() {
        @Override
        public void evaluate() {
            runChildren(notifier);
        }
    };
}

// 执行所有过滤后的测试方法
private void runChildren(final RunNotifier notifier) {
    final RunnerScheduler currentScheduler = scheduler;
    try {
        for (final T each : getFilteredChildren()) {
            currentScheduler.schedule(new Runnable() {
                public void run() {
                    // 抽象方法，执行单个测试的方法
                    ParentRunner.this.runChild(each, notifier);
                }
            });
        }
    } finally {
        currentScheduler.finished();
    }
}

    
// 核心流程3. 执行测试方法过程 BlockJUnit4ClassRunner#runChild
protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
    Description description = describeChild(method);
    if (isIgnored(method)) {
        notifier.fireTestIgnored(description);
    } else {
        // 构造单个执行方法语句链Statement，并执行
        runLeaf(methodBlock(method), description, notifier);
    }
}

    
    
// 构造单个测试方法执行语句链Statement
// 得到执行链路【RunAfters#evaluate ==> RunBefores#evaluate ==>FailOnTimeout#evaluate ==> ExpectException#evaluate ==>InvokeMethod#evaluate 】
protected Statement methodBlock(FrameworkMethod method) {
    // 反射创建test对象
    // Object test ...

    // 1. 目标测试方法先封装成InvokeMethod对象
    Statement statement = methodInvoker(method, test);

    // 2. 如果测试方法有except属性，把上面的Statement封装到ExpectException的next中 
    statement = possiblyExpectingExceptions(method, test, statement);

    // 3. 如果测试方法有timeout注属性，把上面的Statement封装到FailOnTimeout的next中
    statement = withPotentialTimeout(method, test, statement);

    // 4. 如果有@Before注解方法，把上面的Statement封装到RunBefores的next中
    statement = withBefores(method, test, statement);

    // 5. 如果有@After注解方法，把上面的Statement封装到RunAfters的next中
    statement = withAfters(method, test, statement);
    statement = withRules(method, test, statement);
    return statement;
}

// 执行方法单测语句链
protected final void runLeaf(Statement statement, Description description,
        RunNotifier notifier) {
    EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);
    eachNotifier.fireTestStarted();  // 通知开始执行单测方法事件
    try {
        statement.evaluate();  // 执行方法单测语句链
    } catch (AssumptionViolatedException e) {
        eachNotifier.addFailedAssumption(e);
    } catch (Throwable e) {
        eachNotifier.addFailure(e);    // 通知执行单测方法失败事件
    } finally {
        eachNotifier.fireTestFinished();  // 通知结束执行单测方法事件
    }
}

// 最后：Suite的原理：获取所有的子Runner集合，遍历执行对应的run类测试方法。
public class Suite extends ParentRunner<Runner> { 
   public class Suite extends ParentRunner<Runner> 
    ... 
    @Override
    protected void runChild(Runner runner, final RunNotifier notifier) {
        runner.run(notifier);
    }
}
```

**说明：**Junit主要用到的设计模式有监听者模式和责任链模式。监听者模式用于通知执行各个流程的事件，得到单测结果信息；责任链默认用于构造测试的运行语句流程。看到运行效果就是：@BeforeClass ==> @Before ==> @Test ==> @After  ==> @AfterClass。 上面把执行过程讲的比较完整，具体还需要自己去看下代码，理解会更深刻点哈。



#### (三). Mockito 模拟方法调用

Mockito 是一个流行 mock 框架，可以和JUnit结合起来使用。Mockito 允许你创建和配置 mock 对象。使用Mockito可以明显的简化对外部依赖的测试类的开发。

#####1. 添加maven依赖

```xml
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
    <version>3.2.4</version>
    <scope>test</scope>
</dependency>
```



##### 2. Mocktio API说明

```java
// ArgumentMatchers, AdditionalMatchers参数匹配器根据类
// ArgumentMatchers中常用参数匹配器：any,anyInt,anyXXX,isA,eq,startsWith,endsWith,matches
// AdditionalMatchers中常用参数匹配器 ==> eq geq leq gt lt find and or not
  
// Mocktio类
public static <T> T mock(Class<T> classToMock)  // 创建mock对象
public static <T> T spy(T object)   // 创建监控对象Spy
  
public static <T> OngoingStubbing<T> when(T methodCall)

// 生成方法打桩
public static Stubber doNothing()   // 什么都不干,方法只能返回null
public static Stubber doReturn(Object toBeReturned)    // 返回值
public static Stubber doThrow(Throwable... toBeThrown) // 生成异常
public static Stubber doCallRealMethod()   // 调用原始方法

// 校验打桩方法 【VerificationMode 校验模式类】
public static <T> T verify(T mock, VerificationMode mode) 
public static VerificationMode times(int wantedNumberOfInvocations) // 校验次数
public static VerificationMode atLeast(int minNumberOfInvocations)  // 最少次数
public static VerificationMode atMost(int maxNumberOfInvocations)   // 最多次数验证
  
  
// OngoingStubbing类 ==> 方法打桩类
OngoingStubbing<T> thenReturn(T value)
OngoingStubbing<T> thenThrow(Throwable... throwables)
OngoingStubbing<T> thenAnswer(Answer<?> answer)
  
// BDDMocktio类 (Given…When…Then…)
public static <T> BDDMyOngoingStubbing<T> given(T methodCall)
public static <T> OngoingStubbing<T> when(T methodCall)
public static BDDStubber willReturn(Object toBeReturned)
```



##### 3. Mockito使用举例

```java
@Test
public void mockTest() {
    List mock = Mockito.mock(List.class);
    
    // when-then使用姿势
    Mockito.when(mock.get(ArgumentMatchers.anyInt())).thenReturn("123");
    Assert.assertThat(mock.get(1),is("123"));

    // given-willXXX使用姿势
    BDDMockito.given(mock.get(ArgumentMatchers.anyInt())).willReturn("456");
    Assert.assertThat(mock.get(1),is("456"));
  
    Mockito.verify(mock,times(2)).get(1);
}

// spy-mock特点：如果mock对象的方法没有打桩会调用真实的方法。
@Test
public void spyMockTest() {
    List<String> strs = new ArrayList<>();
    List<String> spyList = Mockito.spy(strs);

    // when中调用spy对象，如果对象方法没有打桩会调用真实对象方法
    // Mockito.when(spyList.get(ArgumentMatchers.anyInt())).thenReturn("123");
    doReturn("123").when(spyList).get(ArgumentMatchers.eq(3));
    Assert.assertThat(spyList.get(3),is("123"));
    
    Mockito.when(spyList.size()).thenReturn(100);
    spyList.add("one");
    spyList.add("two");
    Assert.assertThat(spyList.get(0),is("one"));
    Assert.assertThat(spyList.get(1),is("two"));
}
```



##### 4. Mockito源码分析

```
ArgumentMatcher接口
  boolean matches(T argument); // 参数匹配接口
ArgumentMatcher具体实现：Any, Same, Null, NotNull, Matches, Equals, StartsWith, EndsWith, Contains,  Or, And, Not等


MockCreationSettings接口【代表的是Mock相关设置】
  -- CreationSettings类 【mockName，typeToMock，defaultAnswer,isStripAnnotations, spiedInstance==>指定spy监控的对象 等属性】
    -- MockSettingsImpl类【useConstructor, constructorArgs等属性】


MockHandler接口【mock方法调用处理核心逻辑】
  -- MockHandlerImpl类：处理mock方法的流程
  -- NullResultGuardian类：处于处理默认值Null值
  -- InvocationNotifierHandler类：用于方法正常调用和异常调用发布对应的事件，用于触发监听器逻辑。
// 使用包装器
MockHandler<T> handler = new MockHandlerImpl<T>(settings);
MockHandler<T> nullResultGuardian = new NullResultGuardian<T>(handler);
return new InvocationNotifierHandler<T>(nullResultGuardian, settings);


MockMethodInterceptor类
    MockHandler handler;   // Mock方法处理类
    MockCreationSettings mockCreationSettings;  // Mock Class信息


MockingProgress接口【表示模拟过程】
  void stubbingStarted(); void stubbingCompleted();
  void addListener(MockitoListener listener);  void removeListener(MockitoListener listener);     void clearListeners();
  -- MockingProgressImpl实现【主要负责mock执行生命周期通知所有监听器响应对应事件】
    private final ArgumentMatcherStorage argumentMatcherStorage = new ArgumentMatcherStorageImpl();
    private OngoingStubbing<?> ongoingStubbing;
    private Localized<VerificationMode> verificationMode;
    private Location stubbingInProgress = null;
    private VerificationStrategy verificationStrategy;
    private final Set<MockitoListener> listeners = new LinkedHashSet<MockitoListener>();



mock代码执行流程
1. 创建mock对象
  第一步：创建MockCreationSettings对象 ==> Mock对象相关配置信息（mockName, typeToMock, defaultAnswer, spiedInstance）
  第二步：根据mockType类型,使用字节码技术生成对应class类字节码，然后根据类信息创建对应的Mock对象。
  第三步：创建MockMethodInterceptor对象, 并设置给Mock对象【MockMethodInterceptor完成方法mock的处理流程】
  第四步：如果是Spy方式创建的mock bean, 需要把真实对象中的属性值，拷贝到mock对象中。

2. 执行mock方法流程
  第一步：调用mock方法的实现 ==>MockMethodInterceptor.DispatcherDefaultingToRealMethod#         
         interceptAbstract
  第二步：通过MockHandlerImpl#handle实现具体的mock处理逻辑：如果方法参数和名字匹配，
        响应对应的Answer值。否则使用DefaultAnswer响应。
```

```java
// 创建的字节码类
// sudo java -cp "/Library/Java/JavaVirtualMachines/jdk1.8.0_211.jdk/Contents/Home/lib/sa-jdi.jar" sun.jvm.hotspot.HSDB 
public class List$MockitoMock$719671761 implements List, MockAccess {
    // ...
    private MockMethodInterceptor mockitoInterceptor;

    // ... 实现所有的list方法【都是通过调用MockMethodInterceptor.DispatcherDefaultingToRealMethod#interceptAbstract来实现的】
    public void add(int paramInt, Object paramObject) { MockMethodInterceptor.DispatcherDefaultingToRealMethod
        .interceptAbstract(this, this.mockitoInterceptor, null,   cachedValue$eRmmeQdq$3us6oc3, new Object[] { Integer.valueOf(paramInt), paramObject }); 
    }
    public MockMethodInterceptor getMockitoInterceptor() { return this.mockitoInterceptor; }
}
```





#### (四). JsonPath 介绍和使用

Github对应文档地址：https://github.com/json-path/JsonPath

SpringBoot使用JsonPath方式进行json格式数据断言。JsonPath具体语法格式见官方文档



##### 1. JsonPath操作符

| 操作                    | 说明                                      |
| ----------------------- | ----------------------------------------- |
| $                       | 查询根元素。这将启动所有路径表达式。      |
| @                       | 当前节点由过滤谓词处理。                  |
| *                       | 通配符，必要时可用任何地方的名称或数字。  |
| ..                      | 深层扫描。 必要时在任何地方可以使用名称。 |
| .<name>                 | 点，表示子节点                            |
| ['<name>' (, '<name>')] | 括号表示子项                              |
| [<number> (, <number>)] | 数组索引或索引                            |
| [start:end]             | 数组切片操作                              |
| [?(<expression>)]       | 过滤表达式。 表达式必须求值为一个布尔值。 |



##### 2.   JsonPath支持函数

函数可以在路径的尾部调用，函数的输出是路径表达式的输出，该函数的输出是由函数本身所决定的。

| 函数       | 描述                     | 输出     |
| ---------- | ------------------------ | -------- |
| `min()`    | 提供数字数组的最小值     | `Double` |
| `max()`    | 提供数字数组的最大值     | `Double` |
| `avg()`    | 提供数字数组的平均值     | `Double` |
| `stddev()` | 提供数字数组的标准偏差值 | `Double` |
| `length()` | 提供数组的长度           | Integer  |



##### 3. JsonPath过滤器运算符

过滤器是用于**筛选数组的逻辑表达式**。一个典型的过滤器将是[?(@.age > 18)]，其中@表示正在处理的当前项目。 可以使用逻辑运算符&&和||创建更复杂的过滤器。 字符串文字必须用单引号或双引号括起来([?(@.color == 'blue')] 或者 [?(@.color == "blue")]).

| 操作符  | 描述                                     |
| ------- | ---------------------------------------- |
| `==`    | left等于right（注意1不等于'1'）          |
| `!=`    | 不等于                                   |
| `<`     | 小于                                     |
| `<=`    | 小于等于                                 |
| `>`     | 大于                                     |
| `>=`    | 大于等于                                 |
| `=~`    | 匹配正则表达式[?(@.name =~ /foo.*?/i)]   |
| `in`    | 左边存在于右边 [?(@.size in ['S', 'M'])] |
| `nin`   | 左边不存在于右边                         |
| `size`  | （数组或字符串）长度                     |
| `empty` | （数组或字符串）为空                     |



##### 4. JsonPath代码验证

```json
{
    "store": {
        "book": [
            {
                "category": "reference",
                "author": "Nigel Rees",
                "title": "Sayings of the Century",
                "price": 8.95
            },
            {
                "category": "fiction",
                "author": "Evelyn Waugh",
                "title": "Sword of Honour",
                "price": 12.99
            },
            {
                "category": "fiction",
                "author": "Herman Melville",
                "title": "Moby Dick",
                "isbn": "0-553-21311-3",
                "price": 8.99
            },
            {
                "category": "fiction",
                "author": "J. R. R. Tolkien",
                "title": "The Lord of the Rings",
                "isbn": "0-395-19395-8",
                "price": 22.99
            }
        ],
        "bicycle": {
            "color": "red",
            "price": 19.95
        }
    },
    "expensive": 10
}
```

```java
// 使用JsonPathExpectationsHelper验证JsonPath值
public static void main(String[] args) {
    System.out.println(new JsonPathExpectationsHelper("$.store.book[*].author").evaluateJsonPath(content));
    System.out.println(new JsonPathExpectationsHelper("$.store..price").evaluateJsonPath(content));
      System.out.println(new JsonPathExpectationsHelper("$.store.book[0].author").evaluateJsonPath(content));
    System.out.println(new JsonPathExpectationsHelper("$.store.book[0:3].author").evaluateJsonPath(content));

    System.out.println(new JsonPathExpectationsHelper("$.store.book[?(@.price==22.99)].author").evaluateJsonPath(content));
    System.out.println(new JsonPathExpectationsHelper("$..book[?(@.author =~ /.*REES/i)]").evaluateJsonPath(content));

    System.out.println(new JsonPathExpectationsHelper("$.store.book.length()").evaluateJsonPath(content));
}
```





#### (五). SpringBoot中集成测试

##### 1. SpringBoot测试集成

```xml
<!-- 依赖中包含junit,jsonPath,mockito等间接依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```



##### 2. SpringBoot注解和原理

**@Sql注解**：能够生成单元测试构造数据。@Sql可以注解在TYPE(类)，METHOD(方法上面)。

**@Transactional注解：**SpringBoot test对@Transactional的测试方法能够实现自动回滚。如果希望事务不会滚可以在方法上面使用@Rollback(false)。

**@MockBean**：使用@MockBean进行依赖bean的模拟；比如，服务依赖的rpc接口。

**@SpyBean**：模拟spring bean， 如果方法没有打桩，则调用真实接口。

```java\
SpringJUnit4ClassRunner执行流程
1. 构造SpringJUnit4ClassRunner对象【程序入口：SpringJUnit4ClassRunner(Class<?> clazz)】
  核心属性：TestContextManager测试上下文管理器
    -- testContext属性：测试类信息，方法信息，测试spring上下文
    -- testExecutionListeners：执行测试的监听器，默认从"META-INF/spring.factories"中加载
      目前有十二种默认类型：MockitoTestExecutionListener ,TransactionalTestExecutionListener，SqlScriptsTestExecutionListener

ServletTestExecutionListener：用户加载spring bean环境【prepareTestInstance方法 ==> 准备测试环境，如果已经启动和容器不会重新加载】
TransactionalTestExecutionListener：用于@Transactional事务控制，用于测试数据回滚。
SqlScriptsTestExecutionListener：用于测试执行前后，执行对应脚本 @Sql指定脚本。默认测试执行前执行脚本(ExecutionPhase.BEFORE_TEST_METHOD)/AFTER_TEST_METHOD）


2. 执行单测流程同Junit【程序入口：JUnitCore#run  ==> 核心流程同Junit测试】
  i).单个类测试流程【同Junit】
  ii). 构造类执行语句Statement过程【同Junit】
  iii). 执行单个测试方法流程【定制spring自己的实现】
  
 
3. @MockBean和@SpyBean注解原理
MockitoPostProcessor后置处理器，MockitoPostProcessor#register是创建mock bean和spy bean的过程
@MockBean：移除原始beanDenifition之前并复制给MockDefinition, 然后使用mockito api创建对应的mockBean
```



```java
@Override
protected Statement methodBlock(FrameworkMethod frameworkMethod) {
    Object testInstance;
    try {
        // 创建测试类对象，并执行所有TestExecutionListener#prepareTestInstance方法
        // 【包含启动spring容器】
        testInstance = new ReflectiveCallable() {
            @Override
            protected Object runReflectiveCall() throws Throwable {
                return createTest();
            }
        }.run();
    }
    catch (Throwable ex) {
        return new Fail(ex);
    }  
    // 构造调用链
    Statement statement = methodInvoker(frameworkMethod, testInstance);
    statement = withBeforeTestExecutionCallbacks(frameworkMethod, testInstance, statement);
    statement = withAfterTestExecutionCallbacks(frameworkMethod, testInstance, statement);
    statement = possiblyExpectingExceptions(frameworkMethod, testInstance, statement);
    statement = withBefores(frameworkMethod, testInstance, statement);
    statement = withAfters(frameworkMethod, testInstance, statement);
    statement = withRulesReflectively(frameworkMethod, testInstance, statement);
    statement = withPotentialRepeat(frameworkMethod, testInstance, statement);
    statement = withPotentialTimeout(frameworkMethod, testInstance, statement);
    return statement;
}

public class RunBeforeTestExecutionCallbacks extends Statement {
    // Statement next;   Object testInstance;  Method testMethod; TestContextManager testContextManager;
    @Override
    public void evaluate() throws Throwable {
        // 执行所有的TestExecutionListener#beforeTestMethod
        this.testContextManager.beforeTestExecution(this.testInstance, this.testMethod);
        this.next.evaluate();
    }
}
  
  
public class RunAfterTestMethodCallbacks extends Statement {
    // Statement next;   Object testInstance;  Method testMethod; TestContextManager testContextManager;
    @Override
    public void evaluate() throws Throwable {
        this.next.evaluate();
        
        try {
            // 执行所有的TestExecutionListener#afterTestMethod方法
            this.testContextManager.afterTestMethod(this.testInstance, this.testMethod, testException);
        }
        catch (Throwable ex) {
            errors.add(ex);
        }
    }
}
```





##### 3. 测试service接口

```java
@Slf4j
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = CoinCenterServiceApplication.class)
public class CoinAccountAPIImplTest {
    
    @MockBean
    private DictDataService dictDataService;

    @Test
    @Sql("classpath:dispatcher/batchSendRedPacket.sql")
    public void batchSendRedPacket() {

        // 模拟新库环境
        given(dictDataService.keepOldDB()).willReturn(false);

        // 参数校验哦
  	    checkRedPacketBatchSendParam(buildRedPacketBatchSendParam()
             .setExpireTime(DateUtils.addDays(new Date(),-1)));

        // 正常发放
        coinDistributeAPI.batchSendRedPacket(buildRedPacketBatchSendParam());

        CoinAccountDO coinAccountDO = coinAccountService.getByAccountId(1287405573L);
        Assert.assertThat(coinAccountDO.getCoinRedPackets(), is(1000));
}
```



##### 4. 测试controller接口

**常用注解说明：**

* @WebMvcTest：不会扫描controller下面的service,mapper等，需要@MockBean模拟service
* @AutoConfigureMockMvc注解：可以注入MockMvc 对象模拟请求【参考MockMvcAutoConfiguration】
* @SpringBootTest（webEnvironment = WebEnvironment.RANDOM_PORT）开启随机端口

```java
//  MockMvc#perform用于controller接口测试
public ResultActions perform(RequestBuilder requestBuilder)

RequestBuilder接口
    -- MockHttpServletRequestBuilder 实现类
   【包含method, url, contextPath, servletPath, characterEncoding, contentType, header等属性】
        -- MockMultipartHttpServletRequestBuilder实现类 【包含files, parts等属性】

// 常用设置方法
// 设置请求参数
MockHttpServletRequestBuilder param(String name, String... values)
MockHttpServletRequestBuilder params(MultiValueMap<String, String> params)
    
// 设置session和cookie
MockHttpServletRequestBuilder sessionAttr(String name, Object value)    
MockHttpServletRequestBuilder cookie(Cookie... cookies)    

// 设置http header
// MockHttpServletRequestBuilder
MockHttpServletRequestBuilder header(String name, Object... values)
MockHttpServletRequestBuilder accept(String... mediaTypes)
MockHttpServletRequestBuilder contentType(MediaType contentType) 

// 设置http body(json)
public MockHttpServletRequestBuilder content(byte[] content)


MockMvcRequestBuilders工具类
  // 生成get请求
  MockHttpServletRequestBuilder get(String urlTemplate, Object... uriVars)
  // 生成post请求(类似还有put请求, patch请求, delete请求, options请求, head请求)
  MockHttpServletRequestBuilder post(String urlTemplate, Object... uriVars)  
  // 生成Multipart请求
  MockMultipartHttpServletRequestBuilder multipart(String urlTemplate, Object... uriVars)


ResultActions接口
  ResultActions andExpect(ResultMatcher matcher)  // 用于返回结果断言匹配匹配
```




ResultMatcher接口【通过工具类产生】
```java
// MockMvcResultMatchers结果匹配（匹配forward，redirect）
public static ResultMatcher forwardedUrl(String expectedUrl)
public static ResultMatcher redirectedUrl(String expectedUrl)   
    
public static StatusResultMatchers status()    // 返回状态匹配器
public static ModelResultMatchers model()      // 返回model匹配器
public static ViewResultMatchers view()        // 返回view匹配器
public static ContentResultMatchers content()  // 返回内容匹配器
public static JsonPathResultMatchers jsonPath(String expression, Object... args) // 返回json匹配器   
 
// StatusResultMatchers状态匹配器
public ResultMatcher isOk() 
    
// ContentResultMatchers content匹配器
public ResultMatcher contentType(final MediaType contentType)
public ResultMatcher string(final String expectedContent)
public ResultMatcher json(final String jsonContent)     
 
// ModelResultMatchers model匹配器
public <T> ResultMatcher size(final int size)
public ResultMatcher attributeExists(final String... names)    
public ResultMatcher attribute(final String name, final Object value)
public <T> ResultMatcher attribute(final String name, final Matcher<T> matcher)
    
// ViewResultMatchers view匹配器
public ResultMatcher name(final String expectedViewName)
    
// JsonPathResultMatchers json匹配器
public <T> ResultMatcher value(Matcher<T> matcher)
```



Spring MVC接口举例：

```java
@RunWith(SpringRunner.class)
@AutoConfigureMockMvc  // 配置后，可以注入MockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CartControllerTest {
  
    @MockBean
    private UserRPC userRPC;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getUserInfo() throws Exception {

        // 模拟用户返回数据
        AccountWithUserTagDTO accountWithUserTagDTO = new AccountWithUserTagDTO();
        accountWithUserTagDTO.setAccountId(1);
        accountWithUserTagDTO.setIsBuy(1);
        accountWithUserTagDTO.setCpsLevel(2);
        accountWithUserTagDTO.setNickName("hehe");
        accountWithUserTagDTO.setCreateTime(new Date());
        given(userRPC.getAccountWithUserTag(anyInt())).willReturn(accountWithUserTagDTO);

        // 模拟请求和返回
        mockMvc.perform(MockMvcRequestBuilders.post("/user/getUserInfo"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.data.accountId").value(1))
        ;
    
        // 模拟 @RequestBody/ @ResponseBody
        mockMvc.perform(MockMvcRequestBuilders.post("/order/findOrders")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(JSONObject.toJSONString(reqData))
                .header("sign",sign))
                .andExpect(status().isOk());
}
```

ps：controller接口我们可以采用jsonpath的方式进行返回值校验。

