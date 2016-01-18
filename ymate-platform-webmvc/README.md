###ymate-platform-webmvc

WebMVC模块在YMP框架中是除了JDBC模块以外的另一个非常重要的模块，集成了YMP框架的诸多特性，在功能结构的设计和使用方法上依然保持一贯的简单风格，同时也继承了主流MVC框架的基因，对于了解和熟悉SSH等框架技术的开发人员来说，上手极其容易，毫无学习成本。

其主要功能特性如下：

- 标准MVC实现，结构清晰，完全基于注解方式配置简单；
- 支持约定模式，无需编写控制器代码，直接匹配并执行视图；
- 支持多种视图技术(JSP、Freemarker、Velocity、Text、HTML、JSON、Binary、Forward、Redirect、HttpStatus等)；
- 支持RESTful模式及URL风格；
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

WebMVC模块的基本初始化参数配置：

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

**说明**：在服务端程序Servlet方式的请求处理中，请求忽略正则表达式(request\_ignore\_regex)参数无效；

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

启动Tomcat服务并访问`http://localhost:8080/sayhi`，得到的输出结果将是：`Hi, YMPer!`

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

	> 无参数，需要注意的是文件上传处理的表单enctype属性：
	
	>		<form action="/demo/upload" method="POST" enctype="multipart/form-data">
	>		......
	>		</form>

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

文件上传相关配置参数：

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

文件上传状态监听器(upload\_file\_listener\_class)配置：

WebMVC模块的文件上传是基于Apache Commons FileUpload组件实现的，所以通过其自身提供的ProgressListener接口即可实现对文件上传状态的监听；

示例代码：实现上传文件的进度计算；

	public class UploadProgressListener implements ProgressListener {
	
	    public void update(long pBytesRead, long pContentLength, int pItems) {
	        if (pContentLength == 0) {
	            return;
	        }
	        // 计算上传进度百分比
	        double percent = (double) pBytesRead / (double) pContentLength;
	        // 将百分比存储在用户会话中
	        WebContext.getContext().getSession().put("upload_progress", percent);
	    }
	}

> - 将该接口实现类配置到 ymp.configs.webmvc.upload\_file\_listener\_class 参数中；
> 
> - 通过Ajax定时轮循的方式获取会话中的进度值，并展示在页面中；

####视图（View）：

WebMVC模块支持多种视图技术，包括JSP、Freemarker、Velocity、Text、HTML、JSON、Binary、Forward、Redirect、HttpStatus等，也可以通过IView接口扩展实现自定义视图；

#####控制器视图的表示方法：
> - 通过返回IView接口类型；
> - 通过字符串表达一种视图类型；
> - 无返回值或返回值为空，将使用当前RequestMapping路径对应的JspView视图；

#####视图文件路径配置：

> 控制器视图文件基础路径，必须是以 '/' 开始和结尾，默认值为/WEB-INF/templates/；
> 
>		ymp.configs.webmvc.base_view_path=/WEB-INF/templates/

#####视图对象操作示例：

> 视图文件可以省略扩展名称，通过IView接口可以直接设置请求参数和内容类型；
>
>		// 通过View对象创建视图对象
>		IView _view = View.jspView("/demo/test")
>	            .addAttribute("attr1", "value")
>	            .addAttribute("attr2", 2)
>	            .addHeader("head", "value")
>	            .setContentType(Type.ContentType.HTML.getContentType());
>
>		// 直接创建视图对象
>		_view = new JspView("/demo/test");
>
>		// 下面三种方式的结果是一样的，使用请求路径对应的视图文件返回
>		_view = View.jspView();
>		_view = JspView.bind();
>		_view = new JspView();

#####WebMVC模块提供的视图：

JspView：JSP视图；

>		View.jspView("/demo/test.jsp");
>		// = "jsp:/demo/test"

FreemarkerView：Freemarker视图；

> 		View.freemarkerView("/demo/test.ftl");
> 		// = "freemarker:/demo/test"

VelocityView：Velocity视图；

> 		View.velocityView("/demo/test.vm");
> 		// = "velocity:/demo/test"

TextView：文本视图；

> 		View.textView("Hi, YMPer!");
> 		// = "text:Hi, YMPer!"

HtmlView：HTML文件内容视图；

> 		View.htmlView("<p>Hi, YMPer!</p>");
> 		// = "html:<p>Hi, YMPer!</p>"

JsonView：JSON视图；

> 		// 直接传递对象
> 		User _user = new User();
> 		user.setId("...");
> 		...
> 		View.jsonView(_user);
> 
> 		// 传递JSON字符串
> 		View.jsonView("{id:\"...\", ...}");
> 		// = "json:{id:\"...\", ...}"

BinaryView：二进制数据流视图；

> 		// 下载文件，并重新指定文件名称
> 		View.binaryView(new File("/temp/demo.txt"))
> 				.useAttachment("测试文本.txt");
> 		// = "binary:/temp/demo.txt:测试文本.txt"
>
> > 若不指定文件名称，则回应头中将不包含 "attachment;filename=xxx"

ForwardView：请求转发视图；

> 		View.forwardView("/demo/test");
> 		// = "forward:/demo/test"

RedirectView：重定向视图；

> 		View.redirectView("/demo/test");
> 		// = "redirect:/demo/test"

HttpStatusView：HTTP状态视图

> 		View.httpStatusView(404);
> 		// = "http_status:404"
> 
> 		View.httpStatusView(500, "系统忙, 请稍后再试...");
> 		// = "http_status:500:系统忙, 请稍后再试..."

NullView：空视图；

>		View.nullView();

####验证（Validation）：

WebMVC模块已集成验证模块，控制器方法可以直接使用验证注解完成参数的有效性验证，详细内容请参阅 [验证(Validation)](http://git.oschina.net/suninformation/ymate-platform-v2/blob/master/ymate-platform-validation/README.md) 模块文档；

####缓存（Cache）：

WebMVC模块已集成缓存模块，通过@Cacheable注解即可轻松实现控制器方法的缓存，通过配置缓存模块的scope\_processor\_class参数可以支持APPLICATION和SESSION作用域；

	# 设置缓存作用域处理器
	ymp.configs.cache.scope_processor_class=net.ymate.platform.webmvc.support.WebCacheScopeProcessor

示例代码：将方法执行结果以会话(SESSION)级别缓存180秒；

		@Controller
		@RequestMapping("/demo")
		@Cacheable
		public class CacheController {
		
			@RequestMapping("/cache")
			@Cacheable(scope = ICaches.Scope.SESSION, timeout = 180)
			public IView doCacheable(@RequestParam String content) throws Exception {
				// ......
				return View.textView("Content: " + content);
			}
		}

> **注意**：基于@Cacheable的方法缓存只是缓存控制器方法返回的结果对象，并不能缓存IView视图的最终执行结果；

####拦截器（Intercept）：

WebMVC模块基于YMPv2.0的新特性，原生支持AOP方法拦截，通过以下注解进行配置：

> @Before：用于设置一个类或方法的前置拦截器，声明在类上的前置拦截器将被应用到该类所有方法上；

> @After：用于设置一个类或方的后置拦截器，声明在类上的后置拦截器将被应用到该类所有方法上；

> @Clean：用于清理类上全部或指定的拦截器，被清理的拦截器将不会被执行；

> @ContextParam：用于设置上下文参数，主要用于向拦截器传递参数配置；

示例代码：

		// 创建自定义拦截器
        public class UserSessionChecker implements IInterceptor {
            public Object intercept(InterceptContext context) throws Exception {
                // 判断当前拦截器执行方向
                if (context.getDirection() 
                		&& WebContext.getRequest().getSession(false) == null) {
                    return View.redirectView("/user/login");
                }
                return null;
            }
        }

		@Controller
		@RequestMapping("/user")
		@Before(UserSessionChecker.class)
		public class Controller {

			@RequestMapping("/center")
			public IView userCenter() throws Exception {
				// ......
				return View.jspView("/user/center");
			}

			@RequestMapping("/login")
			@Clean
			public IView userLogin() throws Exception {
				return View.jspView("/user/login");
			}
		}

####Cookies操作：

WebMVC模块针对Cookies这个小甜点提供了一个名为CookieHelper的小工具类，支持Cookie参数的设置、读取和移除操作，同时支持对编码和加密处理，并允许通过配置参数调整Cookie策略；

#####Cookie配置参数：

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

#####示例代码：演示Cookie操作方法；

	// 创建CookieHelper对象
	CookieHelper _helper = CookieHelper.bind(WebContext.getContext().getOwner());

	// 设置开启采用密钥加密(将默认开启Base64编码)
	_helper.allowUseAuthKey();

	// 设置开启采用Base64编码(默认支持UrlEncode编码)
	_helper.allowUseBase64();

	// 添加或重设Cookie，过期时间基于Session时效
	_helper.setCookie("current_username", "YMPer");

	// 添加或重设Cookie，并指定过期时间
	_helper.setCookie("current_username", "YMPer", 1800);

	// 获取Cookie值
	BlurObject _currUsername = _helper.getCookie("current_username");

	// 获取全部Cookie
	Map<String, BlurObject> _cookies = _helper.getCookies();

	// 移除Cookie
	_helper.removeCookie("current_username");

	// 清理所有的Cookie
	_helper.clearCookies();

####国际化（I18N）：

基于YMPv2.0框架I18N支持，整合WebMVC模块并提供了默认II18NEventHandler接口实现，配置方法：

	// 指定WebMVC模块的I18N资源管理事件监听处理器
	ymp.i18n_event_handler_class=net.ymate.platform.webmvc.support.I18NWebEventHandler
	
	// 语言设置的参数名称，可选参数，默认为空
	ymp.params._lang=

加载当前语言设置的步骤：

>  1. 加载请求作用域是中否存在`_lang`参数；
>  2. 尝试加载框架自定义配置`ymp.params._lang`是否存在；
>  3. 尝试从Cookies里加载名称为`_lang`的参数；
>  4. 使用系统默认语言设置；

I18N资源文件：

> - 所有I18N资源文件将默认被放置在`RuntimeUtils.getRootPath()`方法返回路径的`i18n`目录下；
> - `RuntimeUtils.getRootPath()`方法的执行结果受配置体系模块影响；


####约定模式（Convention Mode）：

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

- 访问权限规则配置

- 拦截器规则配置

####高级特性：

- 控制器请求处理器：@RequestProcessor

- 异常错误处理器


