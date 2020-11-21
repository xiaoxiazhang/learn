### Hibernate-Validation校验器

#### JSR303规范

web开发有一句名言：永远不要相信用户输入，在任何时候，当你要处理一个应用程序的业务逻辑，数据校验是你必须要考虑和面对的事情。JSR 303 – Bean Validation 是一个数据验证的规范，2009 年 11 月确定最终方案。2009 年 12 月 Java EE 6 发布。Hibernate Validator在实现jsr303的基础上，还进行了自己一些扩展。
JSR303一些制定的一些注解：

```
@Null	        被注释的元素必须为 null
@NotNull	    被注释的元素必须不为 null
@AssertTrue	    被注释的元素必须为 true
@AssertFalse    被注释的元素必须为 false
@Min(value)	    被注释的元素必须是一个数字，其值必须大于等于指定的最小值
@Max(value)	    被注释的元素必须是一个数字，其值必须小于等于指定的最大值
@DecimalMin(value)	 被注释的元素必须是一个数字，其值必须大于等于指定的最小值
@DecimalMax(value)	 被注释的元素必须是一个数字，其值必须小于等于指定的最大值
@Size(max, min)	     被注释的元素的大小必须在指定的范围内
@Digits (integer, fraction)	被注释的元素必须是一个数字，其值必须在可接受的范围内
@Past  	        被注释的元素必须是一个过去的日期
@Future	        被注释的元素必须是一个将来的日期
@Pattern(value)	被注释的元素必须符合指定的正则表达式

Hibernate Validator 附加的 constraint
@Email	    被注释的元素必须是电子邮箱地址
@Length	    被注释的字符串的大小必须在指定的范围内
@NotEmpty	被注释的集合,字符串非空
@Range	    被注释的元素必须在合适的范围内


级联校验(使用@Valid)--->类里面的其它类也需要校验，该字段需要使用@Valid
分组验证:
```



#### Validation使用

**1. 引入依赖**

```xml
<!-- api工程引入，可以使用JSR303和hibernate validate注解-->
<dependency>
    <groupId>org.hibernate</groupId>
    <artifactId>hibernate-validator</artifactId>
    <version>6.0.13.Final</version>
</dependency>

<!-- service工程使用 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>
```



**2.自定义校验**

**方式一：**组合现有的constraint来生成一个更复杂的constraint

```java
// @Max 和 @Min 都是内置的 constraint 
 @Max(10000) 
 @Min(8000) 
 @Constraint(validatedBy = {}) 
 @Documented 
 @Target( { ElementType.ANNOTATION_TYPE, ElementType.METHOD, ElementType.FIELD }) 
 @Retention(RetentionPolicy.RUNTIME) 
 public @interface Price { 
     String message() default "错误的价格"; 
     Class<?>[] groups() default {}; 
     Class<? extends Payload>[] payload() default {}; 
 }

```



**方式二：**实现ConstraintValidator接口

```java
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {EnumValidAnnotation.EnumValidtor.class})
@Documented
public @interface EnumValidAnnotation {

    String message() default "{custom.value.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    Class<? extends Enum<?>> target();


    class EnumValidtor implements ConstraintValidator<EnumValidAnnotation, Integer> {

        private Logger logger = LoggerFactory.getLogger(EnumValidtor.class);

        private Class<? extends Enum<?>> clazz; //枚举类

        @Override
        public void initialize(EnumValidAnnotation constraintAnnotation) {
            clazz = constraintAnnotation.target();
        }

        @Override
        public boolean isValid(Integer code, ConstraintValidatorContext context) {
            if (null == code) {
                return Boolean.TRUE;
            }
            try {
                Method method = clazz.getMethod("getByCode",Integer.class);
                if (null == method) return Boolean.FALSE;
                //枚举类验证
                Object result = method.invoke(null, code);
                return result != null;
            }catch (Exception e){
                logger.error("something wrong occur during verification!");
            }
            return false;
        }
    }
}
```





**3. 工具类使用** 

```java
@Slf4j
public class ValidationUtil {

    private static Validator validator = Validation
            .byProvider(HibernateValidator.class)
            .configure()
            .failFast(true)
            .buildValidatorFactory()
            .getValidator();

    private ValidationUtil() {
    }


    public static <T> void validate(T obj, Class<?>... groups) {
        Set<ConstraintViolation<T>> constraintViolations = validator.validate(obj, groups);
        // 抛出检验异常
        if (!constraintViolations.isEmpty()) {
            ConstraintViolation constraintViolation = constraintViolations.iterator().next();
            String param = constraintViolation.getPropertyPath().toString();
            Object invalidValue = constraintViolation.getInvalidValue();
            String errorMsg = constraintViolation.getMessage();
            log.error("param is invalid,param==>{},value==>{},msg==>{}",param, JSON.toJSONString(invalidValue),errorMsg);
            throw new IllegalArgumentException(errorMsg);
        }
    }
}

```



**4. 代码中使用**

```java
//1.注解需要校验的对象
@Data
public class RedPacketCreateRequest implements Serializable {
    private static final long serialVersionUID = -6907792001618076873L;

    /**
     * 创建红包账户--必填
     */
    @NotNull(message = "账户id不能为空")
    @Range(min = 1, message = "账户id必须大于0")
    private Long accountId;

    /**
     * 红包总额--必填（分）
     */
    @NotNull(message = "红包总额不能为空")
    @Range(min = 1, message = "红包总额必须大于0")
    private Integer totalAmount;

    /**
     * 红包总个数--必填
     */
    @NotNull(message = "红包总个数不能为空")
    @Range(min = 1, max = 200, message = "红包总个数范围【1-200】")
    private Integer totalNum;

    /**
     * 红包标题
     */
    @NotEmpty(message = "红包标题不能为空")
    private String title;

    /**
     * 红包类型--必填  (0,"普通红包") (1,"拼手气红包")
     *
     * @see com.ggj.center.coin.constants.RedPacketTypeEnum
     */
    @NotNull(message = "红包类型不能为空")
    @Range(min = 0, max = 1, message = "红包类型不存在")
    private Integer packetType;

    /**
     * 领取人类型--必填 (0,"所有人可领"),(1,"仅粉丝可领取")
     */
    @NotNull(message = "领取人类型不能为空")
    @Range(min = 0, max = 1, message = "领取人类型不存在")
    private Integer receiveType;

    /**
     * 来源
     * @see com.ggj.center.coin.constants.SourceEnum
     */
    @NotNull(message = "来源不能为空")
    @Range(min = 60, max = 60, message = "来源不存在")
    private Integer source;

}

//2.校验变量
方式一：springMVC中可以使用@Validated注解验证对象，在需要验证的对象后面加上 
一个Errors或BindingResult对象来保存验证结果。

方式二：ValidationUtil.validate(request);

```



 

#### Validation源码分析

**第一步：**创建单例Validator

SpringMVC是通过JavaSPI创建HibernateValidator实现：

![1545983541832](..\..\..\images\hibernate_validation_1.png)



Utils手动创建单例validator：

```	java
 private static Validator validator = Validation
            .byProvider(HibernateValidator.class)
            .configure()  
            .failFast(true)
            .buildValidatorFactory()
            .getValidator();
```

核心属性：beanMetaDataManager管理beanMetaData(反射得到的类信息)

```java
//校验失败结果信息
private final Set<ConstraintViolation<T>> failingConstraintViolations;
//管理beanmetadata信息
private final BeanMetaDataManager beanMetaDataManager;


/**
 * Used to cache the constraint meta data for validated entities
 * 缓存Meta数据，减少反射代码的执行（软引用）
*/
private final ConcurrentReferenceHashMap<Class<?>, BeanMetaData<?>> beanMetaDataCache;
```



**第二步：**Validator每次执行validate,都会创建一个校验上下文validationContext

```java
ValidationContext<T> validationContext = getValidationContextBuilder()
    .forValidate( object );
//1. 生成ValidationContextBuilder创建器（绑定beanMetaDataManager）
private ValidationContextBuilder getValidationContextBuilder() {
		return ValidationContext.getValidationContextBuilder(
				beanMetaDataManager,
				constraintValidatorManager,
				constraintValidatorFactory,
				validatorScopedContext,
				TraversableResolvers.wrapWithCachingForSingleValidation( traversableResolver, validatorScopedContext.isTraversableResolverResultCacheEnabled() ),
				constraintValidatorInitializationContext

		);
	}

//2.校验上下文信息(rootBean,rootBeanClass,BeanMetaData)
Class<T> rootBeanClass = (Class<T>) rootBean.getClass(); //rootBean对应校验对象
return new ValidationContext<>(
					ValidationOperation.BEAN_VALIDATION,
					constraintValidatorManager,
					constraintValidatorFactory,
					validatorScopedContext,
					traversableResolver,
					constraintValidatorInitializationContext,
					rootBean,
					rootBeanClass,
					beanMetaDataManager.getBeanMetaData( rootBeanClass ),
					null, //executable
					null, //executable parameters
					null, //executable return value
					null //executable metadata
			);


public <T> BeanMetaData<T> getBeanMetaData(Class<T> beanClass) {
		Contracts.assertNotNull( beanClass, MESSAGES.beanTypeCannotBeNull() );

        //beanMetaData没获取到就创建,-->(包含字段约束和字段信息),并放入缓存,缓存key为对应class
		BeanMetaData<T> beanMetaData = 
            (BeanMetaData<T>) beanMetaDataCache.computeIfAbsent( beanClass,
				bc -> createBeanMetaData( bc ) );
		return beanMetaData;
	}
```



**第三步：**Validation上下文中执行校验（通过注解的信息找到对应的Validator,并执行对应的isValid方法）

```java
//validationOrder:封装group信息
//ValueContext:值上下文
validateInContext( validationContext, valueContext, validationOrder );


Iterator<Group> groupIterator = validationOrder.getGroupIterator();
while ( groupIterator.hasNext() ) {
	Group group = groupIterator.next();
	valueContext.setCurrentGroup( group.getDefiningClass() );
    //遍历所有group,然后根据value上下文校验
	validateConstraintsForCurrentGroup( validationContext, valueContext );
    //如果设置failfast为true,返回校验失败项
	if ( shouldFailFast( validationContext ) ) {
		return validationContext.getFailingConstraints();
	}
}
```










