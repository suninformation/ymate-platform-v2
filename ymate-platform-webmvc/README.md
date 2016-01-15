###ymate-platform-webmvc

WebMVC模块在YMP框架中是除了JDBC模块以外的另一个非常重要的模块，集成了YMP框架的诸多特性，在功能结构的设计和使用方法上依然保持一贯的简单风格，同时也继承了主流MVC框架的基因，对于了解和熟悉SSH等框架技术的开发人员来说，上手极其容易，毫无学习成本。

其主要功能特性如下：

- 标准MVC实现，结构清晰，完全基于注解方式配置简单；
- 支持约定模式，无需编写控制器代码，直接匹配并执行视图；
- 支持多种视图技术(JSP、Freemarker、Velocity、Text、HTML、JSON、Binary、Forward、Redirect、HttpStatus等)；
- 支持RESTful模式；
- 支持请求参数与控制器方法参数的自动绑定；
- 支持参数有效性验证；
- 支持控制器方法的拦截；
- 支持注解配置控制器请求路由映射；
- 支持自动扫描控制器类并注册；
- 支持事件和异常的自定义处理；
- 支持I18N资源国际化；
- 支持控制器方法和视图缓存；
- 支持控制器参数转义；
- 支持插件扩展；


####模块初始化：

在Web程序中监听器(Listener)是最先被容器初始化的，所以WebMVC模块是由监听器负责对YMP框架进行初始化：

> 监听器(Listener)：net.ymate.platform.webmvc.support.WebAppEventListener

处理浏览器请求并与模块中控制器匹配、路由的过程可分别由过滤器(Filter)和服务端程序(Servlet)完成：

> 过滤器(Filter)：net.ymate.platform.webmvc.support.DispatchFilter

> 服务端程序(Servlet)：net.ymate.platform.webmvc.support.DispatchServlet

首先看一下完整的web.xml配置文件：

	<?xml version="1.0" encoding="UTF-8"?>
	<web-app id="WebApp_ID" version="2.5" xmlns="http://java.sun.com/xml/ns/javaee"
	         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	         xsi:schemaLocation="http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd">
	
	    <listener>
	        <listener-class>net.ymate.platform.webmvc.support.WebAppEventListener</listener-class>
	    </listener>
	
	    <filter>
	        <filter-name>DispatchFilter</filter-name>
	        <filter-class>net.ymate.platform.webmvc.support.DispatchFilter</filter-class>
	    </filter>
	    <filter-mapping>
	        <filter-name>DispatchFilter</filter-name>
	        <url-pattern>/*</url-pattern>
	        <dispatcher>REQUEST</dispatcher>
	        <dispatcher>FORWARD</dispatcher>
	        <dispatcher>INCLUDE</dispatcher>
	    </filter-mapping>
	    
	    <!--
	    <servlet>
	        <servlet-name>DispatchServlet</servlet-name>
	        <servlet-class>net.ymate.platform.webmvc.support.DispatchServlet</servlet-class>
	    </servlet>
	    <servlet-mapping>
	        <servlet-name>DispatchServlet</servlet-name>
	        <url-pattern>/service/*</url-pattern>
	    </servlet-mapping>
	    -->
	
	    <welcome-file-list>
	        <welcome-file>index.html</welcome-file>
	        <welcome-file>index.jsp</welcome-file>
	    </welcome-file-list>
	</web-app>

####模块配置：

WebMVC模块的配置由以下四个部份组成：

> 基本初始化参数

	#-------------------------------------
	# 基本初始化参数
	#-------------------------------------
	
	# 控制器请求处理器，可选值为已知处理器名称或自定义处理器类名称，自定义类需实现net.ymate.platform.webmvc.IRequestProcessor接口，默认为default，目前支持已知处理器[default|json|xml|...]
	ymp.configs.webmvc.request_processor_class=
	
	# 异常错误处理器，可选参数，此类需实现net.ymate.platform.webmvc.IWebErrorProcessor接口
	ymp.configs.webmvc.error_processor_class=
	
	# 默认字符编码集设置，可选参数，默认值为UTF-8
	ymp.configs.webmvc.default_charset_encoding=
	
	# 请求忽略正则表达式，可选参数，默认值为^.+\.(jsp|jspx|png|gif|jpg|jpeg|js|css|swf|ico|htm|html|eot|woff|woff2|ttf|svg)$
	ymp.configs.webmvc.request_ignore_regex=
	
	# 请求方法参数名称，可选参数， 默认值为_method
	ymp.configs.webmvc.request_method_param=
	
	# 请求路径前缀，可选参数，默认值为空
	ymp.configs.webmvc.request_prefix=
	
	# 请求参数转义模式是否开启（开启状态时，控制器方法的所有参数将默认支持转义，可针对具体控制器主法或参数设置忽略转义操作），可选参数，默认值为false
	ymp.configs.webmvc.parameter_escape_mode=
	
	# 控制器视图文件基础路径（必须是以 '/' 开始和结尾，默认值为/WEB-INF/templates/）
	ymp.configs.webmvc.base_view_path=

**说明**：在服务端程序Servlet方式的请求处理中，请求忽略正则表达式参数无效；

> Cookie配置参数

	#-------------------------------------
	# Cookie配置参数
	#-------------------------------------
	
	# Cookie键前缀，可选参数，默认值为空
	ymp.configs.webmvc.cookie_prefix=
	
	# Cookie作用域，可选参数，默认值为空
	ymp.configs.webmvc.cookie_domain=
	
	# Cookie作用路径，可选参数，默认值为'/'
	ymp.configs.webmvc.cookie_path=
	
	# Cookie密钥，可选参数，默认值为空
	ymp.configs.webmvc.cookie_auth_key=

> 文件上传配置参数

	#-------------------------------------
	# 文件上传配置参数
	#-------------------------------------
	
	# 文件上传临时目录，为空则默认使用：System.getProperty("java.io.tmpdir")
	ymp.configs.webmvc.upload_temp_dir=
	
	# 上传文件大小最大值（字节），默认值：-1（注：10485760 = 10M）
	ymp.configs.webmvc.upload_file_size_max=

	# 上传文件总量大小最大值（字节）, 默认值：-1（注：10485760 = 10M）
	ymp.configs.webmvc.upload_total_size_max=

	# 内存缓冲区的大小，默认值： 10240字节（=10K），即如果文件大于10K，将使用临时文件缓存上传文件
	ymp.configs.webmvc.upload_size_threshold=

	# 文件上传状态监听器，可选参数，默认值为空
	ymp.configs.webmvc.upload_file_listener_class=

> 约定模式配置参数

	#-------------------------------------
	# 约定模式配置参数
	#-------------------------------------
	
	# 是否开启视图自动渲染（约定优于配置，无需编写控制器代码，直接匹配并执行视图）模式，可选参数，默认值为false
	ymp.configs.webmvc.convention_mode=

	# Convention模式开启时视图文件路径(基于base_view_path的相对路径，'-'号代表禁止访问，'+'或无符串代表允许访问)，可选参数，默认值为空(即不限制访问路径)，多个路径间用'|'分隔
	ymp.configs.webmvc.convention_view_paths=

	# Convention模式开启时是否采用URL伪静态(URL中通过分隔符'_'传递多个请求参数，通过_path[index]方式引用参数值)模式，可选参数，默认值为false
	ymp.configs.webmvc.convention_urlrewrite_mode=

	# Convention模式开启时是否采用拦截器规则设置，可选参数，默认值为false
	ymp.configs.webmvc.convention_interceptor_mode=


####控制器（Controller）：

控制器(Controller)是MVC体系中的核心，它负责处理浏览器发起的所有请求和决定响应内容的逻辑处理，控制器就是一个标准的Java类，不需要继承任何基类，通过类中的方法向外部暴露接口，该方法的返回结果将决定向浏览器响应的具体内容；

下面通过示例编写WebMVC模块中的控制器：

	@Controller
	public class DemoController {
		
		@RequestMapping("/sayhi")
		public IView sayHi() {
			return View.textView("Hi, YMPer!");
		}
	}

启动Tomcat服务并访问http://localhost:8080/sayhi，得到的输出结果将是：Hi, YMPer!

从以上代码中看到有两个注解，分别是：

- @Controller：声明一个类为控制器，框架在启动时将会自动扫描所有声明该注解的类并注册为控制器；

	> name：控制器名称，默认为“”（该参数暂时未被使用）；
	>
	> singleton：指定控制器是否为单例，默认为true；

- @RequestMapping：声明控制器请求路径映射，作用域范围：类或方法；

	> value：控制器请求路径映射，必选参数；
	>
	> method[]：允许的请求方式，默认为GET方式，取值范围：GET, HEAD, POST, PUT, DELETE, OPTIONS, TRACE；
	>
	> header[]：请求头中必须存在的头名称；
	>
	> param[]：请求中必须存在的参数名称；

示例一：

创建非单例控制器，其中的控制器方法规则如下：
> 1. 控制器方法仅支持POST和PUT方式访问；
> 2. 请求头参数中必须包含x-requested-with=XMLHttpRequest(即判断是否AJAX请求)；
> 3. 请求参数中必须存在name参数；

	@Controller(singleton = false)
	@RequestMapping("/demo")
	public class DemoController {
		
		@RequestMapping(value = "/sayhi",
            method = {Type.HttpMethod.POST, Type.HttpMethod.PUT},
            header = {"x-requested-with=XMLHttpRequest"},
            param = {"name=*"})
		public IView sayHi() {
			return View.textView("Hi, YMPer!");
		}
	}

示例说明：
> 本例主要展示了如何使用@Controller和@RequestMapping注解来对控制器和控制器方法对进配置；
> 
> 控制器方法必须使用public修饰，否则无效；
> 
> 由于控制器上也声明了@RequestMapping注解，所以控制器方法的请求路径映射将变成：/demo/sayhi；

示例二：

上例中展示了对请求的一些控制，下面展示如何对响应结果进行控制，规则如下：

> 1. 通过注解设置响应头参数：
> 	- from = "china"
> 	- age = 18
> 2. 通过注解设置控制器返回视图及内容："Hi, YMPer!"

	@Controller
	@RequestMapping("/demo")
	public class DemoController {
		
		@RequestMapping("/sayhi")
		@ResponseView(value = "Hi, YMPer!", type = Type.View.TEXT)
	    @ResponseHeader({
	            @Header(name = "from", value = "china"),
	            @Header(name = "age", value = "18", type = Type.HeaderType.INT)})
		public void sayHi() {
		}
	}
 
本例中用到了三个注解：

- @ResponseView：声明控制器方法默认返回视图对象, 仅在方法无返回值或返回值无效时使用

	> name：视图模板文件路径，默认为""；
	> 
	> type：视图文件类型，默认为Type.View.NULL；

- @ResponseHeader：设置控制器方法返回结果时增加响应头参数；

	> value[]：响应头@Header参数集合；

- @Header：声明一个请求响应Header键值对，仅用于参数传递；

	> name：响应头参数名称，必选参数；
	>
	> value：响应头参数值，默认为""；
	>
	> type：响应头参数类型，支持STRING, INI, DATE，默认为Type.HeaderType.STRING；


####控制器参数（Parameter）：

WebMVC模块不但让编写控制器变得非常简单，处理请求参数也变得更加容易！WebMVC会根据控制器方法参数或类成员的注解配置，自动转换与方法参数或类成员对应的数据类型，参数的绑定涉及以下注解：

#####基本参数注解：

- @RequestParam：绑定请求中的参数；

- @RequestHeader：绑定请求头中的参数变量；

- @CookieVariable：绑定Cookie中的参数变量；

上面三个注解拥有相同的参数：

> value：参数名称，若未指定则默认采用方法参数变量名；
>
> prefix：参数名称前缀，默认为""；
>
> defaultValue：指定参数的默认值，默认为""；

示例代码：
	
		@Controller
		@RequestMapping("/demo")
		public class DemoController {
			
			@RequestMapping("/param")
			public IView testParam(@RequestParam String name,
			                  @RequestParam(defaultValue = "18") Integer age,
			                  @RequestParam(value = "name", prefix = "user") String username,
			                  @RequestHeader(defaultValue = "BASIC") String authType,
			                  @CookieVariable(defaultValue = "false") Boolean isLogin) {

			    System.out.println("AuthType: " + authType);
			    System.out.println("IsLogin: " + isLogin);
				return View.textView("Hi, " + name + ", UserName: " + username + ", Age: " + age);
			}
		}
	
通过浏览器访问URL测试：
	
		http://localhost:8080/demo/param?name=webmvc&user.name=ymper
	
执行结果：

		控制台输出：
		AuthType: BASIC
		AuthType: false
		
		浏览器输出：
		Hi, webmvc, UserName: ymper, Age: 18

#####特别的参数注解：

- @PathVariable：绑定请求映射中的路径参数变量；
	> value：参数名称，若未指定则默认采用方法参数变量名；

	示例代码：
	
		@Controller
		@RequestMapping("/demo")
		public class DemoController {

			@RequestMapping("/path/{name}/{age}")
			public IView testPath(@PathVariable String name,
			                  @PathVariable(value = "age") Integer age,
			                  @RequestParam(prefix = "user") String sex) {

				return View.textView("Hi, " + name + ", Age: " + age + ", Sex: " + sex);
			}
		}
	
	通过浏览器访问URL测试：
	
		http://localhost:8080/demo/path/webmvc/20?user.sex=F
	
	执行结果：
	
		Hi, webmvc, Age: 20, Sex: F
	
	> **注意**：基于路径的参数变量必须是连续的，如：
	>
	> - 正确：/path/{name}/{age}
	>
	> - 错误：/path/{name}/age/{sex}

- @ModelBind：值对象参数绑定注解；
	> prefix：绑定的参数名称前缀，可选参数，默认为""；
	
	示例代码：
	
		public class DemoVO {
			
			@PathVariable
			private String name;
			
			@RequestParam
			private String sex;
			
			@RequestParam(prefix = "ext")
			private Integer age;
			
			// 省略Get和Set方法
		}
		
		@Controller
		@RequestMapping("/demo")
		public class DemoController {

			@RequestMapping("/bind/{demo.name}")
			public IView testBind(@ModelBind(prefix = "demo") DemoVO vo) {
				String _str = "Hi, " + vo.getName() + ", Age: " + vo.getAge() + ", Sex: " + vo.getSex();
				return View.textView(_str);
			}
		}

	通过浏览器访问URL测试：
	
		http://localhost:8080/demo/bind/webmvc?demo.sex=F&demo.ext.age=20
	
	执行结果：
	
		Hi, webmvc, Age: 20, Sex: F

- @ParameterEscape：控制器方法参数转义注解；
	> scope：字符串参数转义范围，默认为Type.EscapeScope.DEFAULT；
	>
	> - 取值范围包括：JAVA, JS, HTML, XML, SQL, CSV, DEFAULT；
	> - 默认值DEFAULT，它完成了对SQL和HTML两项转义；
	>
	> skiped：通知父级注解当前方法或参数的转义操作将被忽略，默认为false；
	>
	> processor：自定义字符串参数转义处理器；
	>
	> - 可以通过IParameterEscapeProcessor接口实现自定义的转义逻辑；
	> - 默认实现为DefaultParameterEscapeProcessor；

	示例代码一：
	
		@Controller
		@RequestMapping("/demo")
		@ParameterEscape
		public class DemoController {

			@RequestMapping("/escape")
		    public IView testEscape(@RequestParam String content,
		                            @ParameterEscape(skiped = true) @RequestParam String desc) {
		
		        System.out.println("Content: " + content);
		        System.out.println("Desc: " + desc);
		        return View.nullView();
		    }
		}
		
		// 或者：(两段代码执行结果相同)
		
		@Controller
		@RequestMapping("/demo")
		public class DemoController {

			@RequestMapping("/escape")
			@ParameterEscape
		    public IView testEscape(@RequestParam String content,
		                            @ParameterEscape(skiped = true) @RequestParam String desc) {
		
		        System.out.println("Content: " + content);
		        System.out.println("Desc: " + desc);
		        return View.nullView();
		    }
		}
	
	通过浏览器访问URL测试：
	
		http://localhost:8080/demo/escape?content=<p>content$<br><script>alert("hello");</script></p>&desc=<script>alert("hello");</script>
	
	执行结果：(控制台输出)
	
		Content: &lt;p&gt;content$&lt;br&gt;&lt;script&gt;alert(&quot;hello&quot;);&lt;/script&gt;&lt;/p&gt;
		Desc: <script>alert("hello");</script>

	> 示例一说明：
	>
	> - 由于控制器类被声明了@ParameterEscape注解，代表整个控制器类中所有的请求参数都需要被转义，因此参数content的内容被成功转义；
	> - 由于参数desc声明的@ParameterEscape注解中skiped值被设置为true，表示跳过上级设置，因此参数内容未被转义；

	示例代码二：
	
		@Controller
		@RequestMapping("/demo")
		@ParameterEscape
		public class DemoController {

		    @RequestMapping("/escape2")
		    @ParameterEscape(skiped = true)
		    public IView testEscape2(@RequestParam String content,
		                            @ParameterEscape @RequestParam String desc) {
		
		        System.out.println("Content: " + content);
		        System.out.println("Desc: " + desc);
		        return View.nullView();
		    }
		}
	
	通过浏览器访问URL测试：
	
		http://localhost:8080/demo/escape2?content=<p>content$<br><script>alert("hello");</script></p>&desc=<script>alert("hello");</script>
	
	执行结果：(控制台输出)
	
		Content: <p>content$<br><script>alert("hello");</script></p>
		Desc: &lt;script&gt;alert(&quot;hello&quot;);&lt;/script&gt;

	> 示例二说明：
	>
	> - 虽然控制器类被声明了@ParameterEscape注解，但控制器方法通过skiped设置跳过转义，这表示被声明的方法参数内容不进行转义操作，因此参数content的内容未被转义；
	> - 由于参数desc声明了@ParameterEscape注解，表示该参数需要转义，因此参数内容被成功转义；
	>
	> **注意**：当控制器类和方法都声明了@ParameterEscape注解时，则类上声明的注解将视为无效；

#####非单例控制器的特殊用法：

单例控制器与非单例控制器的区别：

- 单例控制器类在WebMVC模块初始化时就已经实例化；
- 非单例控制器类则是在每次接收到请求时都将创建实例对象，请求结束后该实例对象被释放；

基于以上描述，非单例控制器可以通过类成员来接收请求参数，示例代码如下：

	@Controller(singleton = false)
	@RequestMapping("/demo")
	public class DemoController {

		@RequestParam
		private String content;

	    @RequestMapping("/sayHi")
	    public IView sayHi(@RequestParam String name) {
	        return View.textView("Hi, " + name + ", Content: " + content);
	    }
	}

通过浏览器访问URL测试：

	http://localhost:8080/demo/sayHi?name=YMPer&content=Welcome!

此示例代码的执行结果：

	Hi, YMPer, Content: Welcome!

> **注意**：在单例模式下，WebMVC模块将忽略为控制器类成员赋值，同时也建议在单例模式下不要使用成员变量做为参数，在并发多线程环境下会发生意想不到的问题！！

####Web环境上下文对象（WebContext）：

为了上开发人员能够随时随地获取和使用Request、Response、Session等这样的Web容器对象，YMP框架在WebMVC模块中提供了一个叫WebContext的Web环境上下文封装类，简单又实用，先了解一下提供的方法：

直接获取Web容器对象：
> 
> - 获取ServletContext对象：
>
>			WebContext.getServletContext();
>
> - 获取HttpServletRequest对象：
>
>			WebContext.getRequest();
>
> - 获取HttpServletResponse对象：
>
>			WebContext.getResponse();
>
> - 获取PageContext对象：
>
>			WebContext.getPageContext();

获取WebMVC容器对象：

> - 获取IRequestContext对象：
> 
> 			WebContext.getRequestContext();
> 
> 	> WebMVC请求上下文接口，主要用于分析请求路径及存储相关参数；
>
> - 获取WebContext对象实例：
> 
> 			WebContext.getContext();
> 

WebContext将Application、Session、Request等Web容器对象的属性转换成Map映射存储，同时向Map的赋值也将自动同步至对象的Web容器对象中，起初的目的是为了能够方便代码移植并脱离Web环境依赖进行开发测试(功能参考Struts2)：

> - WebContext.getContext().getApplication();
>
> - WebContext.getContext().getSession();
> 
> - WebContext.getContext().getAttribute(Type.Context.REQUEST);
> 
>	> 原本可以通过WebContext.getContext().getRequest方法直接获取的，但由于设计上的失误，方法名已被WebContext.getRequest()占用，若变更方法名受影响的项目太多，所以只好委屈它了:D，后面会介绍更多的辅助方法来操作Request属性，所以可以忽略它的存在！
>
> - WebContext.getContext().getAttributes();
> 
> - WebContext.getContext().getLocale();
> 
> - WebContext.getContext().getOwner();
> 
> - WebContext.getContext().getParameters();
> 

WebContext操作Application的辅助方法：

> - boolean getApplicationAttributeToBoolean(String name);
> 
> - int getApplicationAttributeToInt(String name);
> 
> - long getApplicationAttributeToLong(String name);
> 
> - String getApplicationAttributeToString(String name);
> 
> - \<T> T getApplicationAttributeToObject(String name);
> 
> - WebContext addApplicationAttribute(String name, Object value)
> 

WebContext操作Session的辅助方法：

> - boolean getSessionAttributeToBoolean(String name);
> 
> - int getSessionAttributeToInt(String name);
> 
> - long getSessionAttributeToLong(String name);
> 
> - String getSessionAttributeToString(String name);
> 
> - \<T> T getSessionAttributeToObject(String name);
> 
> - WebContext addSessionAttribute(String name, Object value)
> 

WebContext操作Request的辅助方法：

> - boolean getRequestAttributeToBoolean(String name);
> 
> - int getRequestAttributeToInt(String name);
> 
> - long getRequestAttributeToLong(String name);
> 
> - String getRequestAttributeToString(String name);
> 
> - \<T> T getRequestAttributeToObject(String name);
> 
> - WebContext addRequestAttribute(String name, Object value)
> 

WebContext操作Parameter的辅助方法：

> - boolean getParameterToBoolean(String name);
> 
> - int getParameterToInt(String name)
> 
> - long getParameterToLong(String name);
> 
> - \<T> T getParameterToObject(String name);
> 
> - String getParameterToString(String name);
> 

WebContext操作Attribute的辅助方法：

> - \<T> T getAttribute(String name);
>
> - WebContext addAttribute(String name, Object value);
> 


WebContext获取IUploadFileWrapper上传文件包装器：

> - IUploadFileWrapper getUploadFile(String name);
> 
> - IUploadFileWrapper[] getUploadFiles(String name);
>

####文件上传（Upload）：

WebMVC模块针对文件的上传处理以及对上传的文件操作都非常的简单，通过注解就轻松搞定：

- @FileUpload：声明控制器方法需要处理上传的文件流；

	> 无参数，需要注意的是文件上传处理的表单类型为"multipart/form-data"；

- IUploadFileWrapper：上传文件包装器接口，提供对已上传文件操作的一系列方法；

示例代码：

	@Controller
	@RequestMapping("/demo)
	public class UploadController {
	
		// 处理单文件上传
		@RequestMapping(value = "/upload", method = Type.HttpMethod.POST)
		@FileUpload
		public IView doUpload(@RequestParam
					          IUploadFileWrapper file) throws Exception {
			// 获取文件名称
			file.getName();
			
			// 获取文件大小
			file.getSize();
			
			// 获取完整的文件名及路径
			file.getPath();
			
			// 获取文件Content-Type
			file.getContentType();
			
			// 转移文件
			file.transferTo(new File("/temp", file.getName()));
			
			// 保存文件
			file.writeTo(new File("/temp", file.getName());
			
			// 删除文件
			file.delete();
			
			// 获取文件输入流对象
			file.getInputStream();
			
			// 获取文件输出流对象
			file.getOutputStream();
			
			return View.nullView();
		}

		// 处理多文件上传
		@RequestMapping(value = "/uploads", method = Type.HttpMethod.POST)
		@FileUpload
		public IView doUpload(@RequestParam
					          IUploadFileWrapper[] files) throws Exception {

			// ......

			return View.nullView();
		}
	}


####视图（View）：

- JspView：
- FreemarkerView：
- VelocityView：
- TextView：
- HtmlView：
- JsonView：
- BinaryView：
- ForwardView：
- RedirectView：
- HttpStatusView：
- NullView：

####验证（Validation）：

####缓存（Cache）：

####拦截器（Intercept）：

####Cookies

####国际化（I18N）：

####约定模式（Convention Mode）：

- 访问权限规则配置

- 拦截器规则配置

####高级特性：

- 控制器请求处理器：@RequestProcessor

- 异常错误处理器

- 文件上传状态监听器

