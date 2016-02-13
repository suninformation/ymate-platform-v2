###框架核心（Core）

YMP框架主要是由核心(Core)和若干模块(Modules)组成，核心主要负责框架的初始化和模块的生命周期管理。

####主要核心功能

- Beans：类对象管理器（微型的Spring容器），提供包类的自动扫描（AutoScan）以及Bean生命周期管理、依赖注入（IoC）和方法拦截（AOP）等特性。

- Event：事件服务，通过事件注册和广播的方式触发和监听事件动作，并支持同步和异步两种模式执行事件队列。

- Module：模块，是YMP框架所有功能特性封装的基础形式，负责模块的生命周期管理，模块将在框架初始化时自动加载并初始化，在框架销毁时自动销毁。

- I18N：国际化资源管理器，提供统一的资源文件加载、销毁和内容读取，支持自定义资源加载和语言变化的事件监听。

- Lang：提供了一组自定义的数据结构，它们在部分模块中起到了重要的作用，包括：
    + BlurObject：用于解决常用数据类型间转换的模糊对象。
    + PairObject：用于将两个独立的对象捆绑在一起的结对对象。
    + TreeObject：使用级联方式存储各种数据类型，不限层级深度的树型对象。

- Util：提供框架中需要的各种工具类。

####框架初始化

YMP框架的初始化是从加载ymp-conf.properties文件开始的，该文件必须被放置在classpath的根路径下；

- 根据程序运行环境的不同，YMP框架初始化时将根据当前操作系统优先级加载配置：

    + Unix/Linux环境下，优先加载 ymp-conf_UNIX.properties；
    + Windows环境下，优先加载 ymp-conf_WIN.properties；
    + 若以上配置文件未找到，则加载默认配置 ymp-conf.properties；

- 框架初始化基本配置参数：

		#-------------------------------------
		# 框架基本配置参数
		#-------------------------------------
		
		# 是否为开发模式，默认为false
		ymp.dev_mode=
		
		# 框架自动扫描的包路径集合，多个包名之间用'|'分隔，默认已包含net.ymate.platform包，其子包也将被扫描
		ymp.autoscan_packages=
		
		# 模块排除列表，多个模块名称或类名之间用'|'分隔，被包含的模块在加载过程中将被忽略
		ymp.excluded_modules=
		
		# 国际化资源默认语言设置，可选参数，默认采用系统环境语言
		ymp.i18n_default_locale=zh_CN
		
		# 国际化资源管理器事件监听处理器，可选参数，默认为空
		ymp.i18n_event_handler_class=
		
		# 框架全局自定义参数，xxx表示自定义参数名称，vvv表示参数值
		ymp.params.xxx=vvv
		
		# 本文测试使用的自定义参数
		ymp.params.helloworld=Hello, YMP!


- 测试代码，完成框架的启动和销毁：

        public static void main(String[] args) throws Exception {
            YMP.get().init();
            try {
                // 输出自定义参数值：Hello, YMP!
                System.out.println(YMP.get().getConfig().getParam("helloworld"));
            } finally {
                YMP.get().destroy();
            }
        }

####Beans

#####包类的自动扫描（AutoScan）

YMP框架初始化时将自动扫描由autoscan_packages参数配置的包路径下所有声明了@Bean注解的类文件，首先分析被加载的类所有已实现接口并注册到Bean容器中，然后执行类成员的依赖注入和方法拦截代理的绑定；

**注**：相同接口的多个实现类被同时注册到Bean容器时，通过接口获取的实现类将是最后被注册到容器的那个，此时只能通过实例对象类型才能正确获取；

- 示例一：

        // 业务接口
        public interface IDemo {
            String sayHi();
        }

        // 业务接口实现类，单例模式
        @Bean
        public class DemoBean implements IDemo {
            public String sayHi() {
                return "Hello, YMP!";
            }
        }

- 示例二：

        // 示例一中的业务接口实现类，非单例模式
        @Bean(singleton = false)
        public class DemoBean implements IDemo {
            public String sayHi() {
                return "Hello, YMP!";
            }
        }

- 测试代码：

        public static void main(String[] args) throws Exception {
            YMP.get().init();
            try {
                // 1. 通过接口获取实例对象
                IDemo _demo = YMP.get().getBean(IDemo.class);
                System.out.println(_demo.sayHi());

                // 2. 直接获取实例对象
                _demo = YMP.get().getBean(DemoBean.class);
                System.out.println(_demo.sayHi());
            } finally {
                YMP.get().destroy();
            }
        }

#####依赖注入（IoC）

通过在类成员属性上声明`@Inject`和`@By`注解来完成依赖注入的设置，且只有被Bean容器管理的类对象才支持依赖注入，下面举例说明：

- 示例：

        // 业务接口
        public interface IDemo {
            String sayHi();
        }

        // 业务接口实现类1
        @Bean
        public class DemoOne implements IDemo {
            public String sayHi() {
                return "Hello, YMP! I'm DemoOne.";
            }
        }

        // 业务接口实现类2
        @Bean
        public class DemoTwo implements IDemo {
            public String sayHi() {
                return "Hello, YMP! I'm DemoTwo.";
            }
        }

- 测试代码：

        @Bean
        public class TestDemo {

            @Inject
            private IDemo __demo1;

            @Inject
            @By(DemoOne.class)
            private IDemo __demo2;

            public void sayHi() {
                // _demo1注入的将是最后被注册到容器的IDemo接口实现类
                System.out.println(__demo1.sayHi());
                // _demo2注入的是由@By注解指定的DemoOne类
                System.out.println(__demo2.sayHi());
            }

            public static void main(String[] args) throws Exception {
                YMP.get().init();
                try {
                    TestDemo _demo = YMP.get().getBean(TestDemo.class);
                    _demo.sayHi();
                } finally {
                    YMP.get().destroy();
                }
            }
        }

#####方法拦截（AOP）

YMP框架的AOP是基于CGLIB的MethodInterceptor实现的拦截，通过以下注解进行配置：

- @Before：用于设置一个类或方法的前置拦截器，声明在类上的前置拦截器将被应用到该类所有方法上；

- @After：用于设置一个类或方的后置拦截器，声明在类上的后置拦截器将被应用到该类所有方法上；

- @Clean：用于清理类上全部或指定的拦截器，被清理的拦截器将不会被执行；

- @ContextParam：用于设置上下文参数，主要用于向拦截器传递参数配置；

示例一：

        // 创建自定义拦截器
        public class DemoInterceptor implements IInterceptor {
            public Object intercept(InterceptContext context) throws Exception {
                // 判断当前拦截器执行方向
                switch (context.getDirection()) {
                    // 前置
                    case BEFORE:
                        System.out.println("before intercept...");
                        // 获取拦截器参数
                        String _param = context.getContextParams("param");
                        if (StringUtils.isNotBlank(_param)) {
                            System.out.println(_param);
                        }
                        break;
                    // 后置
                    case AFTER:
                        System.out.println("after intercept...");
                }
            }
        }

        @Bean
        public class TestDemo {

            @Before(DemoInterceptor.class)
            public String beforeTest() {
                return "前置拦截测试";
            }

            @After(DemoInterceptor.class)
            public String afterTest() {
                return "后置拦截测试";
            }

            @Before(DemoInterceptor.class)
            @After(DemoInterceptor.class)
            @ContextParam({
                    @ParamItem(key = "param", value = "helloworld")
                })
            public String allTest() {
                return "拦截器参数传递";
            }

            public static void main(String[] args) throws Exception {
                YMP.get().init();
                try {
                    TestDemo _demo = YMP.get().getBean(TestDemo.class);
                    _demo.beforeTest();
                    _demo.afterTest();
                    _demo.allTest();
                } finally {
                    YMP.get().destroy();
                }
            }
        }

示例二：

        @Bean
        @Before(DemoInterceptor.class)
        @ContextParam({
                @ParamItem(key = "param", value = "helloworld")
            })
        public class TestDemo {

            public String beforeTest() {
                return "默认前置拦截测试";
            }

            @After(DemoInterceptor.class)
            public String afterTest() {
                return "后置拦截测试";
            }

            @Clean
            public String cleanTest() {
                return "清理拦截器测试";
            }

            public static void main(String[] args) throws Exception {
                YMP.get().init();
                try {
                    TestDemo _demo = YMP.get().getBean(TestDemo.class);
                    _demo.beforeTest();
                    _demo.afterTest();
                    _demo.cleanTest();
                } finally {
                    YMP.get().destroy();
                }
            }
        }

####Event

事件服务，通过事件的注册、订阅和广播完成事件消息的处理，目的是为了减少代码侵入，降低模块之间的业务耦合度，事件消息采用队列存储，采用多线程接口回调实现消息及消息上下文对象的传输，支持同步和异步两种处理模式；

#####框架事件初始化配置参数

    #-------------------------------------
    # 框架事件初始化参数
    #-------------------------------------

    # 默认事件触发模式(不区分大小写)，取值范围：NORMAL-同步执行，ASYNC-异步执行，默认为ASYNC
    ymp.event.default_mode=

    # 事件管理提供者接口实现，默认为net.ymate.platform.core.event.impl.DefaultEventProvider
    ymp.event.provider_class=

    # 事件线程池初始化大小，默认为Runtime.getRuntime().availableProcessors()
    ymp.event.thread_pool_size=

    # 事件配置扩展参数，xxx表示自定义参数名称，vvv表示参数值
    ymp.event.params.xxx=vvv

#####YMP核心事件对象

- ApplicationEvent：框架事件

        APPLICATION_INITED - 框架初始化
        APPLICATION_DESTROYED - 框架销毁

- ModuleEvent：模块事件

        MODULE_INITED - 模块初始化
        MODULE_DESTROYED - 模块销毁

**注**：以上只是YMP框架核心中包含的事件对象，其它模块中包含的事件对象将在其相应的文档描述中阐述；

#####事件的订阅

- 方式一：通过代码手动完成事件的订阅

        public static void main(String[] args) throws Exception {
            YMP.get().init();
            try {
                // 订阅模块事件
                YMP.get().getEvents().registerListener(ModuleEvent.class, new IEventListener<ModuleEvent>() {
                    @Override
                    public boolean handle(ModuleEvent context) {
                        switch (context.getEventName()) {
                            case MODULE_INITED:
                                // 注意：这段代码是不会被执行的，因为在我们进行事件订阅时，模块的初始化动作已经完成
                                System.out.println("Inited :" + context.getSource().getName());
                                break;
                            case MODULE_DESTROYED:
                                System.out.println("Destroyed :" + context.getSource().getName());
                                break;
                        }
                        return false;
                    }
                });
            } finally {
                YMP.get().destroy();
            }
        }

- 方式二：通过`@EventRegister`注解和IEventRegister接口实现事件的订阅

        // 首先创建事件注册类，通过实现IEventRegister接口完成事件的订阅
        // 通过@EventRegister注解，该类将在YMP框架初始化时被自动加载
        @EventRegister
        public class DemoEventRegister implements IEventRegister {
            public void register(Events events) throws Exception {
                // 订阅模块事件
                events.registerListener(new IEventListener<ModuleEvent>() {
                    @Override
                    public boolean handle(ModuleEvent context) {
                        switch (context.getEventName()) {
                            case MODULE_INITED:
                                System.out.println("Inited :" + context.getSource().getName());
                                break;
                            case MODULE_DESTROYED:
                                System.out.println("Destroyed :" + context.getSource().getName());
                                break;
                        }
                        return false;
                    }
                });
                //
                // ... 还可以添加更多的事件订阅代码
            }
        }

        // 框架启动测试
        public static void main(String[] args) throws Exception {
            YMP.get().init();
            try {
                // Do Nothing...
            } finally {
                YMP.get().destroy();
            }
        }

#####自定义事件

YMP的事件对象必须实现IEvent接口的同时需要继承EventContext对象，下面的代码就是一个自定义事件对象：

- 创建自定义事件对象

        public class DemoEvent extends EventContext<Object, DemoEvent.EVENT> implements IEvent {

            public enum EVENT {
                CUSTOM_EVENT_ONE, CUSTOM_EVENT_TWO
            }

            public DemoEvent(Object owner, Class<? extends IEvent> eventClass, EVENT eventName) {
                super(owner, eventClass, eventName);
            }
        }

    说明：EventContext的注解中的第一个参数代表事件源对象类型，第二个参数是指定用于事件监听事件名称的枚举类型；

- 注册自定义事件

        YMP.get().getEvents().registerEvent(DemoEvent.class);

- 订阅自定义事件

    事件订阅（或监听）需实现IEventListener接口，该接口的handle方法返回值在异步触发模式下将影响事件监听队列是否终止执行，同步触发模式下请忽略此返回值；

        YMP.get().getEvents().registerListener(DemoEvent.class, new IEventListener<DemoEvent>() {

            public boolean handle(DemoEvent context) {
                switch (context.getEventName()) {
                    case CUSTOM_EVENT_ONE:
                        System.out.println("CUSTOM_EVENT_ONE");
                        break;
                    case CUSTOM_EVENT_TWO:
                        System.out.println("CUSTOM_EVENT_TWO");
                        break;
                }
                return false;
            }
        });

    当然，也可以通过`@EventRegister`注解和IEventRegister接口实现自定义事件的订阅；

    **注**：当某个事件被触发后，订阅（或监听）该事件的接口被回调执行的顺序是不能被保证的；

- 触发自定义事件

        // 采用默认模式触发事件
        YMP.get().getEvents().fireEvent(new DemoEvent(YMP.get(), DemoEvent.class, DemoEvent.EVENT.CUSTOM_EVENT_ONE));

        // 采用异步模式触发事件
        YMP.get().getEvents().fireEvent(Events.MODE.ASYNC, new DemoEvent(YMP.get(), DemoEvent.class, DemoEvent.EVENT.CUSTOM_EVENT_TWO));

- 示例测试代码：

        public static void main(String[] args) throws Exception {
            YMP.get().init();
            try {
                // 注册自定义事件对象
                YMP.get().getEvents().registerEvent(DemoEvent.class);
                // 注册自定义事件监听
                YMP.get().getEvents().registerListener(DemoEvent.class, new IEventListener<DemoEvent>() {

                    public boolean handle(DemoEvent context) {
                        switch (context.getEventName()) {
                            case CUSTOM_EVENT_ONE:
                                System.out.println("CUSTOM_EVENT_ONE");
                                break;
                            case CUSTOM_EVENT_TWO:
                                System.out.println("CUSTOM_EVENT_TWO");
                                break;
                        }
                        return false;
                    }
                });
                // 采用默认模式触发事件
                YMP.get().getEvents().fireEvent(new DemoEvent(YMP.get(), DemoEvent.class, DemoEvent.EVENT.CUSTOM_EVENT_ONE));
                // 采用异步模式触发事件
                YMP.get().getEvents().fireEvent(Events.MODE.ASYNC, new DemoEvent(YMP.get(), DemoEvent.class, DemoEvent.EVENT.CUSTOM_EVENT_TWO));
            } finally {
                YMP.get().destroy();
            }
        }


####Module

#####创建自定义模块

- 步骤一：根据业务需求创建需要对外暴露的业务接口

        public interface IDemoModule {

            // 为方便引用，定义模块名称常量
            String MODULE_NAME = "demomodule";

            // 返回自定义模块的参数配置接口对象
            IDemoModuleCfg getModuleCfg();

            // 对外暴露的业务方法
            String sayHi();
        }

- 步骤二：处理自定义模块的配置参数，下列代码假定测试模块有两个自定义参数

        // 定义模块配置接口
        public interface IDemoModuleCfg {

            String getModuleParamOne();

            String getModuleParamTwo();
        }

        // 实现模块配置接口
        public class DemoModuleCfg implements IDemoModuleCfg {

            private String __moduleParamOne;

            private String __moduleParamTwo;

            public DemoModuleCfg(YMP owner) {
                // 从YMP框架中获取模块配置映射
                Map<String, String> _moduleCfgs = owner.getConfig().getModuleConfigs(IDemoModule.MODULE_NAME);
                //
                __moduleParamOne = _moduleCfgs.get("module_param_one");
                __moduleParamTwo = _moduleCfgs.get("module_param_two");
            }

            public String getModuleParamOne() {
                return __moduleParamOne;
            }

            public String getModuleParamTwo() {
                return __moduleParamTwo;
            }
        }

- 步骤三：实现模块及业务接口

    **注**：一定不要忘记在模块实现类上声明`@Module`注解，这样才能被YMP框架自动扫描、加载并初始化；

        @Module
        public class DemoModule implements IModule, IDemoModule {

            private YMP __owner;

            private IDemoModuleCfg __moduleCfg;

            private boolean __inited;

            public String getName() {
                return IDemoModule.MODULE_NAME;
            }

            public void init(YMP owner) throws Exception {
                if (!__inited) {
                    __owner = owner;
                    __moduleCfg = new DemoModuleCfg(owner);
                    //
                    __inited = true;
                }
            }

            public boolean isInited() {
                return __inited;
            }

            public YMP getOwner() {
                return __owner;
            }

            public IDemoModuleCfg getModuleCfg() {
                return __moduleCfg;
            }

            public void destroy() throws Exception {
                if (__inited) {
                    __inited = false;
                    //
                    __moduleCfg = null;
                    __owner = null;
                }
            }

            public String sayHi() {
                return "Hi, YMP!";
            }
        }

- 步骤四：在YMP的配置文件ymp-conf.properties中添加模块的配置内容

    格式： ymp.configs.<模块名称>.<参数名称>=[参数值]

        ymp.configs.demomodule.module_param_one=module_param_one_value
        ymp.configs.demomodule.module_param_two=module_param_two_value


#####调用自定义模块

    public static void main(String[] args) throws Exception {
        YMP.get().init();
        try {
            // 获取自定义模块实例对象
            IDemoModule _demoModule = YMP.get().getModule(IDemoModule.class);
            // 调用模块业务接口方法
            System.out.println(_demoModule.sayHi());
            // 调用模块配置信息
            System.out.println(_demoModule.getModuleCfg().getModuleParamOne());
        } finally {
            YMP.get().destroy();
        }
    }

**注**：自定义模块不支持IoC、AOP等特性；

####I18N

I18N服务是在YMP框架启动时初始化，其根据ymp.i18n_default_locale进行语言配置，默认采用系统运行环境的语言设置；


- 国际化资源管理器提供的主要方法：

    + 获取当前语言设置

            I18N.current();

    + 设置当前语言

            // 变更当前语言设置且不触发事件
            I18N.current(Locale.ENGLISH);

            或者

            // 将触发监听处理器onChanged事件
            I18N.change(Locale.ENGLISH);

    + 根据当前语言设置，加载指定名称资源文件内指定的属性值

            I18N.load("resources", "home_title");

            或者

            I18N.load("resources", "home_title", "首页");

    + 格式化消息字符串并绑定参数

            // 加载指定名称资源文件内指定的属性并使用格式化参数绑定
            I18N.formatMessage("resources", "site_title", "Welcome {0}, {1}"，"YMP"，“GoodLuck！”);

            // 使用格式化参数绑定
            I18N.formatMessage("Hello, {0}, {1}", "YMP"，“GoodLuck！”);

- 国际化资源管理器事件监听处理器，通过实现II18NEventHandler接口，在YMP配置文件中的`i18n_event_handler_class`参数进行设置，该监听器可以完成如下操作：

    + 自定义资源文件加载过程

    + 自定义获取当前语言设置

    + 语言设置变更的事件处理过程

####Lang

#####BlurObject：模糊对象

    BlurObject.bind("1234").toLongValue();

#####PairObject：结对对象

    List<String> _key = new ArrayList<String>();
    Map<String, String> _value = new HashMap<String, String>();
    ...
    PairObject _pObj = new PairObject(_key, _value);

    //
    _pObj.getKey();
    //
    _pObj.getValue();

#####TreeObject：树型对象

    Object _id = UUIDUtils.UUID();
    TreeObject _target = new TreeObject()
            .put("id", _id)
            .put("category", new Byte[]{1, 2, 3, 4})
            .put("create_time", new Date().getTime(), true)
            .put("is_locked", true)
            .put("detail", new TreeObject()
                    .put("real_name", "汉字将被混淆", true)
                    .put("age", 32));

    // 这样赋值是List
    TreeObject _list = new TreeObject();
    _list.add("list item 1");
    _list.add("list item 2");

    // 这样赋值代表Map
    TreeObject _map = new TreeObject();
    _map.put("key1", "keyvalue1");
    _map.put("key2", "keyvalue2");

    TreeObject idsT = new TreeObject();
    idsT.put("ids", _list);
    idsT.put("maps", _map);

    // List操作
    System.out.println(idsT.get("ids").isList());
    System.out.println(idsT.get("ids").getList());

    // Map操作
    System.out.println(idsT.get("maps").isMap());
    System.out.println(idsT.get("maps").getMap());

    //
    _target.put("map", _map);
    _target.put("list", _list);

    //
    System.out.println(_target.get("detail").getMixString("real_name"));

    // TreeObject对象转换为JSON字符串输出
    String _jsonStr = _target.toJson().toJSONString();
    System.out.println(_jsonStr);

    // 通过JSON字符串转换为TreeObject对象-->再转为JSON字符串输出
    String _jsonStrTmp = (_target = TreeObject.fromJson(_target.toJson())).toJson().toJSONString();
    System.out.println(_jsonStrTmp);
    System.out.println(_jsonStr.equals(_jsonStrTmp));

####Util

关于YMP框架常用的工具类，这里着重介绍以下几个：

- ClassUtils提供的BeanWrapper工具，它是一个类对象包裹器，赋予对象简单的属性操作能力；

        public static void main(String[] args) throws Exception {
            // 包裹一个Bean对象
            ClassUtils.BeanWrapper<DemoBean> _w = ClassUtils.wrapper(new DemoBean());
            // 输出该对象的成员属性名称
            for (String _fieldName : _w.getFieldNames()) {
                System.out.println(_fieldName);
            }
            // 为成员属性设置值
            _w.setValue("name", "YMP");
            // 获取成员属性值
            _w.getValue("name");
            // 拷贝Bean对象属性到目标对象(不局限相同对象)
            DemoBean _bean = _w.duplicate(new DemoBean());
            // 将对象属性转为Map存储
            Map<String, Object> _maps = _w.toMap();
            // 通过Map对象构建Bean对象并获取Bean实例
            DemoBean _target = ClassUtils.wrapper(DemoBean.class).fromMap(_maps).getTargetObject();
        }

- RuntimeUtils运行时工具类，获取运行时相关信息；

    + 获取当前环境变量：

            RuntimeUtils.getSystemEnvs();

            RuntimeUtils.getSystemEnv("JAVA_HOME");

    + 判断当前运行环境操作系统：

            RuntimeUtils.isUnixOrLinux();

            RuntimeUtils.isWindows();

    + 获取应用根路径：若WEB工程则基于.../WEB-INF/返回，若普通工程则返回类所在路径

            RuntimeUtils.getRootPath();

            RuntimeUtils.getRootPath(false);

    + 替换环境变量：支持${root}、${user.dir}和${user.home}环境变量占位符替换

            RuntimeUtils.replaceEnvVariable("${root}/home");
