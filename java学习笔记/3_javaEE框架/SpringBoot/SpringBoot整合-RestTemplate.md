### SpringBoot整合RestTemplate

Spring框架提供的RestTemplate类可用于在应用中调用rest服务，它简化了与http服务的通信方式，统一了RESTful的标准，封装了http请求只需要传入url及返回值类型即可。相较于之前常用的HttpClient，RestTemplate是一种更优雅的调用RESTful服务的方式。

RestTemplate默认依赖JDK提供http连接的能力【HttpURLConnection】，可以通过setRequestFactory方法替换为Apache HttpClinet, OkHttp等其它http包。



#### 1. Apache HttpClient详解

```xml
<!-- https://mvnrepository.com/artifact/org.apache.httpcomponents/httpclient -->
<dependency>
    <groupId>org.apache.httpcomponents</groupId>
    <artifactId>httpclient</artifactId>
    <version>4.5.12</version>
</dependency>
```



##### 接口设计原理

```java
HttpClient接口
  接口核心方法：HttpResponse execute(HttpUriRequest request) throws xxx;
  --CloseableHttpClient抽象类
    --InternalHttpClient类


HttpClientBuilder类：构建者模式，用于创建复杂对象HttpClient属性
HttpClients工具类：用于创建HttpClient
  public static HttpClientBuilder custom() 
  public static CloseableHttpClient createDefault()


HttpMessage接口
  核心方法：add/remove/set/getHeader

  -- HttpRequest接口
    核心方法：RequestLine getRequestLine(); // 获取URL，请求方法类型，协议类型

    -- HttpUriRequest接口
      核心方法：String getMethod(); URI getURI();

      -- HttpRequestBase抽象类
        定义属性 URI uri; ProtocolVersion version; 实现基础uri, version相关接口

        -- HttpGet类【实现getMethod()方法】

        -- HttpEntityEnclosingRequestBase抽象类
          定义属性HttpEntity entity; 并实现获取方法
        
	        -- HttpPost, HttpDelete, HttpPut类【实现getMethod()方法】
	      

HttpResponse接口 
	StatusLine getStatusLine(); // 状态  HttpEntity getEntity(); //响应具体信息
  -- CloseableHttpResponse 空接口
    -- HttpResponseProxy代理类
      属性：BasicHttpResponse ==> 响应具体内容，ConnectionHolder ==> 关闭连接

  -- BasicHttpResponse类
    属性：StatusLine，HttpEntity等



HttpEntity接口【请求和响应体抽象】
  -- AbstractHttpEntity抽象类
    属性：定义了 Header contentType, Header contentEncoding
    
    -- StringEntity类
      属性：byte[] content  ==> 请求内容【通常是json】

	    -- UrlEncodedFormEntity
	      public UrlEncodedFormEntity (final List <? extends NameValuePair> parameters,final String charset) ==> 

    -- ResponseEntityProxy

工具类：EntityUtils.toString(entity, StandardCharsets.UTF_8); // 获取响应content


NameValuePair接口 ==> 【用于form请求参数构造】
  -- BasicNameValuePair类
    核心方法：public BasicNameValuePair(final String name, final String value)


URIBuilder类 ==> 用于构造URL
  URIBuilder uriBuilder = new URIBuilder("http://www.sogou.com/web");
  uriBuilder.addParameter("query", "hehe");

```





##### 工具类封装

```java
@Slf4j
public class HttpClientUtils {

    /**
     * ms毫秒,从池中获取链接超时时间
     */
    private static final int CONNECTION_REQUEST_TIMEOUT = 5000;

    /**
     * ms毫秒,建立链接超时时间
     */
    private static final int CONNECT_TIMEOUT = 5000;

    /**
     * ms毫秒,读取超时时间
     */
    private static final int SOCKET_TIMEOUT = 30000;

    /**
     * 最大总并发
     */
    private static final int MAX_TOTAL = 200;

    /**
     * 每路并发
     */
    private static final int MAX_PER_ROUTE = 100;


    private static CloseableHttpClient httpClient;


    static {
        try {
            //enable ssl
            TrustManager x509m = new X509TrustManager() {
                @Override
                public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                }

                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };

            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, new TrustManager[]{x509m}, null);
            SSLConnectionSocketFactory socketFactory = new SSLConnectionSocketFactory(context, NoopHostnameVerifier.INSTANCE);
            Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
                    .register("https", socketFactory)
                    .register("http", PlainConnectionSocketFactory.INSTANCE).build();


            PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
            // http连接池设置socket属性
            SocketConfig socketConfig = SocketConfig.custom().setTcpNoDelay(true).build();
            connManager.setDefaultSocketConfig(socketConfig);

            //http连接池设置connection属性
            MessageConstraints messageConstraints = MessageConstraints.custom()
                    .setMaxHeaderCount(200)
                    .setMaxLineLength(2000)
                    .build();
            ConnectionConfig connectionConfig = ConnectionConfig.custom().setMalformedInputAction(CodingErrorAction.IGNORE).setUnmappableInputAction(CodingErrorAction.IGNORE).setCharset(Consts.UTF_8).setMessageConstraints(messageConstraints).build();
            connManager.setDefaultConnectionConfig(connectionConfig);

            // http连接池设置并发属性
            connManager.setMaxTotal(MAX_TOTAL);
            connManager.setDefaultMaxPerRoute(MAX_PER_ROUTE);

            //默认请求设置
            RequestConfig requestConfig = RequestConfig.custom()
                    // 连接目标服务器超时时间：连接一个url的连接等待时间
                    .setConnectTimeout(CONNECT_TIMEOUT)
                    // 读取目标服务器数据超时时间：连接上一个url，获取response的返回等待时间
                    .setSocketTimeout(SOCKET_TIMEOUT)
                    // 从连接池获取连接的超时时间
                    .setConnectionRequestTimeout(CONNECTION_REQUEST_TIMEOUT)
                    .build();
            httpClient = HttpClients.custom()
                    .setConnectionManager(connManager)
                    .setDefaultRequestConfig(requestConfig)
                    .build();

        } catch (Exception e) {
            throw new IllegalStateException("HttpClient init error", e);
        }

    }

    private HttpClientUtils() {
        // blank
    }


    public static String getRequest(String url) {
        return getRequest(url, null);
    }


    public static String getRequest(String url, Map<String, Object> paramMap) {
        URI uri;
        try {
            URIBuilder builder = new URIBuilder(url);
            Optional.ofNullable(paramMap).ifPresent(map->{
                map.forEach((key, value) -> {
                    builder.addParameter(key, value.toString());
                });
            });
            uri = builder.build();
        } catch (URISyntaxException e) {
            throw new UnsupportedOperationException("Uri syntax error", e);
        }

        return executeRequest(new HttpGet(uri));
    }



    public static String postFormRequest(String url, Map<String, Object> paramMap) {
        HttpPost httpPost = new HttpPost(url);
        List<NameValuePair> paramList = Lists.newArrayList();
        if (!CollectionUtils.isEmpty(paramMap)) {
            paramMap.forEach((key, value) -> {
                paramList.add(new BasicNameValuePair(key, value.toString()));
            });
        }

        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(paramList, StandardCharsets.UTF_8);
        httpPost.setEntity(entity);
        return executeRequest(httpPost);
    }


    public static String postJsonRequest(String url, String json) {
        HttpPost httpPost = new HttpPost(url);
        StringEntity entity = new StringEntity(json, ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);
        return executeRequest(httpPost);
    }

    /**
     * 执行http请求
     *
     * @param request httpUri请求参数
     * @return 返回服务器响应内容
     */
    private static String executeRequest(HttpUriRequest request) {
        try (CloseableHttpResponse response = httpClient.execute(request);) {
            HttpEntity entity = response.getEntity();
            try {
                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    log.error("request server error, url==> {} ,status ==>{}, reason==> {}", request.getURI().toString(), statusCode, response.getStatusLine().getReasonPhrase());
                    throw new IllegalStateException("request server error");
                }
                return EntityUtils.toString(entity, StandardCharsets.UTF_8);
            } finally {
                if (entity != null) {
                    entity.getContent().close();
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("io error:", e);
        }
    }
}
```





#### 2. RestTemplate配置和使用

##### 自动配置原理

```java
// RestTemplateAutoConfiguration#restTemplateBuilder
// 自动配置会在spring上下文中获取messageConverters和RestTemplateCustomizer等配置
@Bean
@ConditionalOnMissingBean
public RestTemplateBuilder restTemplateBuilder(ObjectProvider<HttpMessageConverters> messageConverters,
		ObjectProvider<RestTemplateCustomizer> restTemplateCustomizers,
		ObjectProvider<RestTemplateRequestCustomizer<?>> restTemplateRequestCustomizers) {
	RestTemplateBuilder builder = new RestTemplateBuilder();
	HttpMessageConverters converters = messageConverters.getIfUnique();
	if (converters != null) {
		builder = builder.messageConverters(converters.getConverters());
	}
	builder = addCustomizers(builder, restTemplateCustomizers, RestTemplateBuilder::customizers);
	builder = addCustomizers(builder, restTemplateRequestCustomizers, RestTemplateBuilder::requestCustomizers);
	return builder;
}

```





##### 自动配置整合HttpClient

```java
@Configuration
public class RestTemplateConfig {

    @Bean
    public HttpComponentsClientHttpRequestFactory requestFactory() {
        PoolingHttpClientConnectionManager connectionManager =
                new PoolingHttpClientConnectionManager(30, TimeUnit.SECONDS);
        connectionManager.setMaxTotal(200);
        connectionManager.setDefaultMaxPerRoute(20);

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .evictIdleConnections(30, TimeUnit.SECONDS)
                .disableAutomaticRetries()
                // 有 Keep-Alive 认里面的值，没有的话永久有效
                //.setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy.INSTANCE)
                // 换成自定义的
                .setKeepAliveStrategy(new CustomConnectionKeepAliveStrategy())
                .build();

        return new HttpComponentsClientHttpRequestFactory(httpClient);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofMillis(1000))
                .setReadTimeout(Duration.ofMillis(5000))
                .requestFactory(this::requestFactory)
                .build();
    }

    static class CustomConnectionKeepAliveStrategy implements ConnectionKeepAliveStrategy {
        private final long DEFAULT_SECONDS = 30;

        @Override
        public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
            return Arrays.stream(response.getHeaders(HTTP.CONN_KEEP_ALIVE))
                    .filter(h -> StringUtils.equalsIgnoreCase(h.getName(), "timeout")
                            && StringUtils.isNumeric(h.getValue()))
                    .findFirst()
                    .map(h -> NumberUtils.toLong(h.getValue(), DEFAULT_SECONDS))
                    .orElse(DEFAULT_SECONDS) * 1000;
        }
    }
}


// public class MyResponseErrorHandler extends DefaultResponseErrorHandler 重写异常处理器
```





##### RestTemplate API

RestTemplate API大致可以分为三组：

- `getForObject`： optionsForAllow分为一组，是常规的Rest API（GET、POST、DELETE等）方法调用；
- `exchange`：接收`RequestEntity` 参数设置HTTP method, URL, headers和body。返回ResponseEntity。
- `execute`：通过callback 接口，可以对请求和返回做更加全面的自定义控制。一般不使用



```java
// 1. RestTemplate发送get请求
public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Map<String, ?> uriVariables)

// url中需要使用占位符{1,2...},从1开始。例如：http://127.0.0.1:8080/get?name={1}
public <T> ResponseEntity<T> getForEntity(String url, Class<T> responseType, Object... uriVariables)

// url中需要使用
public <T> T getForObject(String url, Class<T> responseType, Map<String, ?> uriVariables)
  

  // 2. RestTemplate发送post请求
public <T> ResponseEntity<T> postForEntity(String url, @Nullable Object request,
			Class<T> responseType, Map<String, ?> uriVariables)
  
public <T> T postForObject(String url, @Nullable Object request, Class<T> responseType,
			Map<String, ?> uriVariables)
  
 
// 3. 使用RestTemplate#exchange发送通用请求 
public <T> ResponseEntity<T> exchange(String url, HttpMethod method,
			@Nullable HttpEntity<?> requestEntity, Class<T> responseType, Map<String, ?> uriVariables)
  
 
// 4. 使用例子
// uri参数
Map<String,String> params=new HashMap<>();
params.put("param1","success");

// 请求头和请求体参数
HttpHeaders headers = new HttpHeaders();
headers.setContentType(MediaType.APPLICATION_JSON);
MultiValueMap<String, String> map= new LinkedMultiValueMap<>();
map.add("name", "wuji");
HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

// get请求
ResponseEntity<HttpController.CommonResult> response = restTemplate.getForEntity("http://127.0.0.1:8080/get?name={param1}", HttpController.CommonResult.class, params);
System.out.println(response.getStatusCode());

ResponseEntity<String> response2 = restTemplate.getForEntity("http://127.0.0.1:8080/get?name={1}", String.class, "success");
System.out.println(response2.getStatusCode());

String response3 = restTemplate.getForObject("http://baicu.com", String.class);
System.out.println(response3);

// post请求     
HttpController.CommonResult<List<HttpController.Student>> response4 = restTemplate.postForObject("http://127.0.0.1:8080/post?name={param1}",request, HttpController.CommonResult.class,params);


// 通用exchange处理所有请求
ResponseEntity<HttpController.CommonResult> response5 = restTemplate.exchange("http://127.0.0.1:8080/post?name={param1}", HttpMethod.POST, request, HttpController.CommonResult.class, params);

```



**getForEntity和getForObject的区别**：主要体现在ResponseExtractor结果提取器。getForObject使用的是HttpMessageConverterExtractor提取器直接提取接口返回内容。getForEntity使用的提取器是ResponseEntityResponseExtractor【包装HttpMessageConverterExtractor】，增加响应状态码和头信息。

