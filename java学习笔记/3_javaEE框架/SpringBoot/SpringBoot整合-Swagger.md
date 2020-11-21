### SpringBoot整合Swagger

#### 1. 添加依赖和自动配置

Maven中引入Jar包

```xml
<dependency>
 <groupId>io.springfox</groupId>
 <artifactId>springfox-swagger2</artifactId>
 <version>2.9.2</version>
</dependency>

<!==maven仓库查询最新版本==>
<dependency>
  <groupId>com.github.xiaoymin</groupId>
  <artifactId>swagger-bootstrap-ui</artifactId>
  <version>${lastVersion}</version>
</dependency>
```



SpringBoot配置swagger

```java
@Configuration
@EnableSwagger2
@Profile({"dev","test","pre"})
public class SwaggerConfiguration {

    @Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
  //设置controller包路径              .apis(RequestHandlerSelectors.basePackage("com.ggj.platform.promotion.controller"))
                .paths(PathSelectors.any())
                .build();
    }
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("促销前端接口")
                .description("了解更多关于项目信息")
                .termsOfServiceUrl("http://www.baidu.com")
                .version("1.0")
                .build();
    }
}
//swagger-bootstrap-ui默认访问地址是：http://${host}:${port}/doc.html
```

 

**注意事项**

Springfox-swagger默认提供了两个Swagger接口,需要开发者放开权限,如果使用SwaggerBootstrapUi的增强功能,还需放开增强接口地址,所以，放开的权限接口包括3个，分别是：

- **/swagger-resources**：Swagger的分组接口
- **/v2/api-docs?group=groupName**：具体分组实例接口,返回该分组下所有接口相关的Swagger信息
- **/v2/api-docs-ext?group=groupName**：该接口是SwaggerBootstrapUi提供的增强接口地址,如不使用UI增强,则可以忽略该接口

需要开放出：/swagger-resources,/v2/api-docs, /v2/api-docs-ext,/doc.html,/webjars/**

> Swagger-Bootstrap-UI官方说明文档地址：https://github.com/xiaoymin/Swagger-Bootstrap-UI/blob/master/README_zh.md



#### 2. swagger常用注解使用

##### @Api注解
 **说明：**用于标识Controller
 **常用注解属性:**  value==>默认说明，tags==> tags说明

> 举例:
>  ```java
> @RestController
> @RequestMapping("coupon")
> @Api(value = "店铺优惠券Controller",tags = {"店铺优惠券接口"})
> public class ShopCouponController 
>  ```
>
> ![1546768080432](..\..\..\images\swagger_1.png)
>




##### @ApiOperation注解
 **说明：**用于标识说明controller中具体的某个方法

 **常用注解属性**：value==->具体描述，notes\images==>提示内容 tags==>

>举例：
>
>```java
>@PostMapping("listShopCoupon")
>    @ApiOperation(value="查询店铺优惠券列表")
>    public CommonResp<PageVO<ShopCouponListVO>> listShopCoupon(@RequestBody CouponSearch couponSearch){
>        return CommonResp.success(shopCouponManager.listShopCoupon(couponSearch));
>    }
>```
>
>![1546768277408](..\..\..\images\swagger_2.png)
>
>



##### @ApiParam注解
 **说明：** 用于标识controller方法参数

 **常用注解属性：**name–->参数名称  value–->参数说明  required–->是否必填

>举例: @ApiParam(name="testParam",value="测试参数",required = true)



##### @ApiImplicitParams, @ApiImplicitParam注解
 **说明：**controller方法*非对象参数*描述

 **常用注解属性：**

* paramType  ==>查询参数类型

  > path：位置参数 
  >
  > body：@RequestBody  
  >
  > query：@RequestParam 
  >
  > header：http头信息 
  >
  > form：APPLICATION_FORM_URLENCODED_VALUE)

*    dataType==>参数的数据类型 只作为标志说明，并没有实际验证(Long,String)

*    name==>接收参数名称

*    value==>接收参数名称描述

*    required==>参数是否必填(true,false)

* defaultValue==->默认值

> **注意：**ApiImplicitParams只能由ApiImplicitParam组成
>
> ```java
>  @ApiOperation(value = "获取优惠券信息")
>    @ApiImplicitParams({
>             @ApiImplicitParam(name = "couponId", value = "优惠券id", dataType = "Long", required = true),
>             @ApiImplicitParam(name = "shopId", value = "店铺id-(平台优惠券店铺id为0)", dataType = "Long", required = true)
>     })
>     @GetMapping("/getCouponById")
>     public CommonResp<CouponVO> getCouponById(
>             @RequestParam(defaultValue = "0") Long couponId,
>             @RequestParam(defaultValue = "0") Long shopId) {
>         return CommonResp.success(couponService.getCoupon(couponId, shopId));
>     }
> ```
>
> ![1546768793999](..\..\..\images\swagger_3.png)



##### @ApiModel注解
**说明：**用于标识controller*对象参数*或者*返回对象*-==>(用于类)
**常用注解属性：**value==>对象名  description==>描述 

> 举例：
>
> ```java
> @ApiModel(value = "ShopCouponUpdateParam", description = "商家优惠券修改参数")
> public class ShopCouponUpdateParam {
> ```
>
> ```java
> @ApiModel(value = "CommonResp", description = "返回结果集")
> public class CommonResp<T> implements Serializable {
> ```
>
> 注意：需要和@ApiModelProperty一起使用



##### @ApiModelProperty
**说明：** 用于对model属性的说明
**常用注解属性：**  

* name==>属性名称
* value==>属性名称说明 
* dataType–->属性类型
* required==>是否必填 
* example–->举例说明  
* hidden–->隐藏

> 参数model举例：
>
> ```java
> @ApiModel(value = "ShopCouponUpdateParam", description = "商家优惠券修改参数")
> public class ShopCouponUpdateParam {
> 
>     @ApiModelProperty(value = "优惠券id", required = true)
>     private Long id;
> 
>     @ApiModelProperty(value = "发放数量", required = true)
>     private Integer totalNum;
> 
>     @ApiModelProperty(value = "推广方式", required = true)
>     private Integer extendType;
> }
> ```
>
> ![1546769440297](..\..\..\images\swagger_4.png)
>
> 返回model描述
>
> ```java
> @ApiModel(value = "CommonResp", description = "返回结果集")
> public class CommonResp<T> implements Serializable {
> 
>     private static final long serialVersionUID = 5759929866327167996L;
> 
>     private static final int SUCCESS_STATUS = 1; //成功状态
> 
>     private static final int ERROR_STATUS = 0; //失败状态
> 
>     private static final long SUCCESS_CODE = 1; //成功code
> 
> 
>     @ApiModelProperty(value = "状态码: 0-失败，1-成功")
>     private Integer status;
> 
>     @ApiModelProperty(value = "业务码")
>     private Long code;
> 
>     @ApiModelProperty(value = "提示信息")
>     private String message;
> 
>     @ApiModelProperty(value = "返回数据集")
>     private T data;
> ```
>
> ![1546769537299](..\..\..\images\swagger_5.png)



##### @ApiIgnore注解
**说明：**方法或者类被忽略(用于类，方法，方法参数 )

>举例：
>
>```java
>@ApiIgnore
>@GetMapping( "/check")
>public String check() {
>        return "200";
>}
>```



#### 3. 实战使用

controller类：@Api(value = "商家优惠券Controller",**tags** = {"店铺优惠券接口"})

controller方法：@ApiOperation(value="查询商家优惠券列表",notes\images="注意参数必须是json格式")

请求参数：

* 单个简单参数：@ApiImplicitParam

* 多个简单参数：@ApiImplicitParams

* @RequestBody复杂参数：

  > @ApiModel(description="类描述")进行描述类
  >
  > @ApiModelProperty(value="属性描述",required...)

返回结果：@ApiModel(description="类描述")/@ApiModelProperty(value="属性描述",) 

**访问ui界面：**http://${host}:${port}/doc.html