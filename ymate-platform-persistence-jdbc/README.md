###ymate-platform-persistence-jdbc

JDBC持久化模块针对关系型数据库(RDBMS)数据存取的一套简单解决方案，主要关注数据存取的效率、易用性和透明，其具备以下功能特征：

- 基于JDBC框架API进行轻量封装，结构简单、便于开发、调试和维护；
- 优化批量数据更新、标准化结果集、预编译SQL语句处理；
- 支持单实体ORM操作，无需编写SQL语句；
- 支持结果集与值对象的自动装配，支持自定义装配规则；
- 支持多数据源、连接池配置，可动态配置，支持JNDI，支持数据源扩展；
- 支持多种数据库(如:Oracle、MySQL 、SQLServer等)；
- 支持面向对象的数据库查询封装，有助于减少或降低程序编译期错误；
- 支持数据库事务嵌套；
- 支持数据库存储过程*；


####模块初始化配置：

    #-------------------------------------
    # JDBC持久化模块初始化参数
    #-------------------------------------

    # 默认数据源名称，默认值为default
    ymp.configs.persistence.jdbc.ds_default_name=

    # 数据源列表，多个数据源名称间用'|'分隔，默认为default
    ymp.configs.persistence.jdbc.ds_name_list=

    # 是否显示执行的SQL语句，默认为false
    ymp.configs.persistence.jdbc.ds.default.show_sql=

    # 数据库表前缀名称，默认为空
    ymp.configs.persistence.jdbc.ds.default.table_prefix=

    # 数据源适配器，可选值为已知适配器名称或自定义适配置类名称，默认为default，目前支持已知适配器[default|dbcp|c3p0|jndi|...]
    ymp.configs.persistence.jdbc.ds.default.adapter_class=

    # 数据库类型，可选参数，默认值将通过连接字符串分析获得，目前支持[mysql|oracle|sqlserver|db2|sqlite|postgresql|hsqldb|h2]
    ymp.configs.persistence.jdbc.ds.default.type=

    # 数据库方言，可选参数，自定义方言将覆盖默认配置
    ymp.configs.persistence.jdbc.ds.default.dialect_class=

    # 数据库连接驱动，可选参数，框架默认将根据数据库类型进行自动匹配
    ymp.configs.persistence.jdbc.ds.default.driver_class=

    # 数据库连接字符串，必填参数
    ymp.configs.persistence.jdbc.ds.default.connection_url=

    # 数据库访问用户名称，必填参数
    ymp.configs.persistence.jdbc.ds.default.username=

    # 数据库访问密码，可选参数，经过默认密码处理器加密后的admin字符串为TutKfblER6k
    ymp.configs.persistence.jdbc.ds.default.password=

    # 数据库访问密码是否已加密，默认为false
    ymp.configs.persistence.jdbc.ds.default.password_encrypted=

    # 数据库密码处理器，可选参数，用于对已加密数据库访问密码进行解密，默认为空
    ymp.configs.persistence.jdbc.ds.default.password_class=

配置参数补充说明：
> 数据源的数据库连接字符串和用户名是必填项，其它均为可选参数，最简配置如下：
>
>   >ymp.configs.persistence.jdbc.ds.default.connection_url=jdbc:mysql://localhost:3306/mydb
>   >
>   >ymp.configs.persistence.jdbc.ds.default.username=root
>
> 为了避免明文密码出现在配置文件中，YMP框架提供了默认的数据库密码处理器，或者通过IPasswordProcessor接口自行实现；
>
>   >net.ymate.platform.core.support.impl.DefaultPasswordProcessor

####数据源（DataSource）：

- 多数据源连接：

    JDBC持久化模块默认支持多数据源配置，下面通过简单的配置来展示如何连接多个数据库：

		# 定义两个数据源分别用于连接MySQL和Oracle数据库，同时指定默认数据源为default(即MySQL数据库)
        ymp.configs.persistence.jdbc.ds_default_name=default
        ymp.configs.persistence.jdbc.ds_name_list=default|oracledb
        
        # 连接到MySQL数据库的数据源配置
        ymp.configs.persistence.jdbc.ds.default.connection_url=jdbc:mysql://localhost:3306/mydb
        ymp.configs.persistence.jdbc.ds.default.username=root
        ymp.configs.persistence.jdbc.ds.default.password=123456

		# 连接到Oracle数据库的数据源配置
        ymp.configs.persistence.jdbc.ds.oracledb.connection_url=jdbc:oracle:thin:@localhost:1521:ORCL
        ymp.configs.persistence.jdbc.ds.oracledb.username=ORCL
        ymp.configs.persistence.jdbc.ds.oracledb.password=123456

    从上述配置中可以看出，配置不同的数据源时只需要定义数据源名称列表，再根据列表逐一配置即可；

- 连接池配置：

    JDBC持久化模块提供的数据源类型如下：

    + default：默认数据源适配器，通过DriverManager直接连接数据库，建议仅用于测试；
    + c3p0：基于C3P0连接池的数据源适配器；
    + dbcp：基于DBCP连接池的数据源适配器；
    + jndi：基于JNDI的数据源适配器；

    只需根据实际情况调整对应数据源名称的配置，如：

        ymp.configs.persistence.jdbc.ds.default.adapter_class=dbcp

    针对于dbcp和c3p0连接池的配置文件及内容，请将对应的dbcp.properties或c3p0.properties文件放置在工程的classpath根路径下，配置内容请参看JDBC持久化模块开源工程中的示例文件；

    当然，也可以通过IDataSourceAdapter接口自行实现，框架针对IDataSourceAdapter接口提供了一个抽象封装AbstractDataSourceAdapter类，直接继承即可；

- 数据库连接持有者（IConnectionHolder）：

	用于记录真正的数据库连接对象（Connection）原始的状态及与数据源对应关系；

####数据实体（Entity）：

- 数据实体注解：

    + @Entity：声明一个类为数据实体对象；

        > value：实体名称(数据库表名称)，默认采用当前类名称；

            @Entity("tb_demo")
            public class Demo {
                //...
            }

    + @Id：声明一个类成员为主键；

        > 无参数，配合@Property注解使用；

            @Entity("tb_demo")
            public class Demo {

                @Id
                @Property
                private String id;

                public String getId() {
                    return id;
                }

                public void setId(String id) {
                    this.id = id;
                }
            }

    + @Property：声明一个类成员为数据实体属性；

        > name：实现属性名称，默认采用当前成员名称；
        >
        > autoincrement：是否为自动增长，默认为false；
        >
        > sequenceName：序列名称，适用于类似Oracle等数据库，配合autoincrement参数一同使用；
        > 
        > nullable：允许为空，默认为true；
        > 
        > unsigned：是否为无符号，默认为false；
        > 
        > length：数据长度，默认0为不限制；
        > 
        > decimals：小数位数，默认0为无小数；
        > 
        > type：数据类型，默认为Type.FIELD.VARCHAR；

            @Entity("tb_user")
            public class User {

                @Id
                @Property
                private String id;

                @Property(name = "user_name",
                          nullable = false,
                          length = 32)
                private String username;

                @Property(name = "age",
                          unsigned = true,
                          type = Type.FIELD.INT)
                private Integer age;

                // 省略Get/Set方法...
            }

    + @PK：声明一个类为某数据实体的复合主键对象；

        > 无参数；
        
            @PK
            public class UserExtPK {

                @Property
                private String uid;

                @Property(name = "wx_id")
                private String wxId;

                // 省略Get/Set方法...
            }
        
            @Entity("tb_user_ext")
            public class UserExt {

                @Id
                private UserExtPK id;

                @Property(name = "open_id",
                          nullable = false,
                          length = 32)
                private String openId;

                // 省略Get/Set方法...
            }

    + @Readonly：声明一个成员为只读属性，数据实体更新时其将被忽略；

        > 无参数，配合@Property注解使用；

            @Entity("tb_demo")
            public class Demo {

                @Id
                @Property
                private String id;

                @Property(name = "create_time")
                @Readonly
                private Date createTime;

                // 省略Get/Set方法...
            }

    + @Indexes：声明一组数据实体的索引；
 
    + @Index：声明一个数据实体的索引；
    
    + @Comment：注释内容；

    + @Default：为一个成员属性或方法参数指定默认值；
    
    看着这么多的注解，是不是觉得编写实体很麻烦呢，不要急，框架提供了自动生成实体的方法，往下看:)
    
    **注**：上面注解或注解参数中有一些是用于未来能通过实体对象直接创建数据库表结构（以及SQL脚本文件）的，可以暂时忽略；

- 自动生成实体类：

    YMP框架自v1.0开始就支持通过数据库表结构自动生成实体类代码，所以v2.0版本不但重构了实体代码生成器，而且更简单好用！

        #-------------------------------------
        # JDBC数据实体代码生成器配置参数
        #-------------------------------------

        # 是否生成新的BaseEntity类，默认为false(即表示使用框架提供的BaseEntity类)
        ymp.params.jdbc.use_base_entity=

        # 是否使用类名后缀，不使用和使用的区别如: User-->UserModel，默认为false
        ymp.params.jdbc.use_class_suffix=

        # 数据库名称(仅针对特定的数据库使用，如Oracle)，默认为空
        ymp.params.jdbc.db_name=

        # 数据库用户名称(仅针对特定的数据库使用，如Oracle)，默认为空
        ymp.params.jdbc.db_username=

        # 数据库表名称前缀，多个用'|'分隔，默认为空
        ymp.params.jdbc.table_prefix=

        # 否剔除生成的实体映射表名前缀，默认为false
        ymp.params.jdbc.remove_table_prefix=

        # 预生成实体的数据表名称列表，多个用'|'分隔，默认为空表示全部生成
        ymp.params.jdbc.table_list=

        # 排除的数据表名称列表，在此列表内的数据表将不被生成实体，多个用'|'分隔，默认为空
        ymp.params.jdbc.table_exclude_list=

        # 生成的代码文件输出路径，默认为${root}
        ymp.params.jdbc.output_path=

        # 生成的代码所属包名称，默认为: packages
        ymp.params.jdbc.package_name=

    实际上你可以什么都不用配置（请参看以上配置项说明，根据实际情况进行配置），但使用过程中需要注意以下几点：

    > - 代码生成器依赖JDBC持久化模块才能完成与数据库连接等操作；
    > 
    > - 在多数据源模式下，代码生成器使用的是默认数据源；
    > 
    > - 代码生成器依赖freemarker模板引擎，所以请检查依赖关系是否正确；
    >
    > - 在WEB工程中运行代码生成器时请确认servlet-api和jsp-api包依赖关系是否正确；
    >
    > - 如果你的工程中引用了很多的模块，在运行代码生成器时可以暂时通过ymp.excluded_modules参数排除掉；

    了解了以上的配置后，直接运行代码生成器：

        net.ymate.platform.persistence.jdbc.scaffold.EntityGenerator

    找到并运行它，如果是Maven项目，可以通过以下命令执执行：
    
    	mvn compile exec:java -Dexec.mainClass="net.ymate.platform.persistence.jdbc.scaffold.EntityGenerator"

	OK！就这么简单，一切都结束了！

####事务（Transaction）：

基于YMPv2.0的新特性，JDBC模块对数据库事务的处理更加灵活，任何被类对象管理器管理的对象都可以通过@Transaction注解支持事务；

- @Transaction注解：
	+ 参数说明：

		> value：事务类型（参考JDBC事务类型），默认为JDBC.TRANSACTION.READ_COMMITTED；
	
	+ 使用方式：
	
		> 首先，需要数据库事务支持的类对象必须声明@Transaction注解；
		>
		> 然后，在具体需要开启事务处理的类方法上添加@Transaction注解；

- 事务示例代码：

		public interface IUserService {
		
			User doGetUser(String username, String pwd);
			
			boolean doLogin(String username, String pwd);
		}
		
		@Bean
		@Transaction
		public class UserService implements IUserService {
		
			public User doGetUser(final String username, final String pwd) {
				return JDBC.get().openSession(new ISessionExecutor<User>() {
	                public User execute(ISession session) throws Exception {
	                    Cond _cond = Cond.create().eq("username").param(username).eq("pwd").param(pwd);
	                    return session.findFirst(EntitySQL.create(User.class), Where.create(_cond));
	                }
	            });
			}
		
			@Transaction
			public boolean doLogin(String username, String pwd) {
				User _user = doGetUser(username, pwd);
				if (_user != null) {
					_user.setLastLoginTime(System.currentTimeMillis());
					_user.update();
					//
					return true;
				}
				return false;
			}
		}
		
		@Bean
		public class TransDemo {
			
			@Inject
			private IUserService __userService;
			
			public boolean testTrans() {
				return __userService.doLogin("suninformation", "123456");
			}
			
			public static void main(String[] args) throws Exception {
				YMP.get().init();
				try {
					TransDemo _demo = YMP.get().getBean(TransDemo.class);
					_demo.testTrans();
				} finally {
					YMP.get().destroy();
				}
			}
		}

####会话（Session）：

会话是对应用中具体业务操作触发的一系列与数据库之间的交互过程的封装，通过建立一个临时通道，负责与数据库之间连接资源的创建及回收，同时提供更为高级的抽象指令接口调用，基于会话的优点：

> 开发人员不需要担心连接资源是否正确释放；
> 
> 严格的编码规范更利于维护和理解；
> 
> 更好的业务封装性；

- 会话对象参数：

	+ 数据库连接持有者（IConnectionHolder）：
	
		指定本次会话使用的数据源连接；

	+ 会话执行器（ISessionExecutor）：
	
		以内部类的形式定义本次会话返回结果对象并提供Session实例对象的引用；
	

- 开启会话示例代码：

		// 使用默认数据源开启会话
		JDBC.get().openSession(new ISessionExecutor<List<User>>() {
			public User execute(ISession session) throws Exception {
				// TODO 此处填写业务逻辑代码
				return null;
			}
		});
		
		// 使用指定的数据源开启会话
		IConnectionHolder _conn = JDBC.get().getConnectionHolder("oracledb");
		// 不需要关心_conn对象的资源释放
		JDBC.get().openSession(_conn, new ISessionExecutor<List<User>>() {
			public User execute(ISession session) throws Exception {
				// TODO 此处填写业务逻辑代码
				return null;
			}
		});

- 基于ISession接口的数据库操作：

	示例代码是围绕用户(User)数据实体完成的CRUD(新增、查询、修改、删除)操作来展示如何使用ISession对象，数据实体如下：
	
			@Entity("user")
	    	public static class User extends BaseEntity<User, String> {
	
		        @Id
		        @Property
		        private String id;
		
		        @Property(name = "user_name")
		        private String username;
		
		        @Property(name = "pwd")
		        private String pwd;
		
		        @Property(name = "sex")
		        private String sex;
		
		        @Property(name = "age")
		        private Integer age;
		
		        // 忽略Getter和Setter方法
		
		        public static class FIELDS {
		            public static final String ID = "id";
		            public static final String USER_NAME = "username";
		            public static final String PWD = "pwd";
		            public static final String SEX = "sex";
		            public static final String AGE = "age";
		        }
		        public static final String TABLE_NAME = "user";
		    }

	+ 插入（Insert）：
	
			User _user = new User();
            _user.setId(UUIDUtils.UUID());
            _user.setUsername("suninformation");
            _user.setPwd(DigestUtils.md5Hex("123456"));
            _user.setAge(20);
            _user.setSex("F");
            // 执行数据插入
            session.insert(_user);
            
            // 或者在插入时也可以指定/排除某些字段
            session.insert(_user, Fields.create(User.FIELDS.SEX, User.FIELDS.AGE).excluded(true));
	
	+ 更新（Update）：

			User _user = new User();
            _user.setId("bc19f5645aa9438089c5e9954e5f1ac5");
            _user.setPwd(DigestUtils.md5Hex("654321"));
            // 更新指定的字段
            session.update(_user, Fields.create(User.FIELDS.PWD));
	
	+ 查询（Find）：

		- 方式一：通过数据实体设置条件，查询所有符合条件的记录；
		
				User _user = new User();
	            _user.setUsername("suninformation");
	            _user.setPwd(DigestUtils.md5Hex("123456"));
	            // 返回所有字段
	            IResultSet<User> _users = session.find(_user);
	            // 或者返回指定的字段
	            _users = session.find(_user, Fields.create(User.FIELDS.ID, User.FIELDS.AGE));
	    
		- 方式二：通过自定义条件，查询所有符合条件的记录；
		
				IResultSet<User> _users = session.find(
                        EntitySQL.create(User.class)
                                .field(User.FIELDS.ID)
                                .field(User.FIELDS.SEX), 
                        // 设置Order By条件
                        Where.create()
                                .orderDesc(User.FIELDS.USER_NAME));
	
		- 方式三：分页查询；

				IResultSet<User> _users = session.find(
                        EntitySQL.create(User.class)
                                .field(User.FIELDS.ID)
                                .field(User.FIELDS.SEX),
                        Where.create()
                                .orderDesc(User.FIELDS.USER_NAME),
                        // 查询第1页，每页10条记录，统计总记录数
                        Page.create(1).pageSize(10).count(true));
		
		- 方式四：仅返回符合条件的第一条记录(FindFirst)；

				// 查询用户名称和密码都匹配的第一条记录
                User _user = session.findFirst(EntitySQL.create(User.class), 
                        Where.create(
                                Cond.create()
                                        .eq(User.FIELDS.USER_NAME).param("suninformation")
                                        .eq(User.FIELDS.PWD).param(DigestUtils.md5Hex("123456"))));

		**注**：更多的查询方式将在后面的“***查询（Query）***”章节中详细阐述；
	
	+ 删除（Delete）：

		- 根据实体主键删除记录：

				User _user = new User();
                _user.setId("bc19f5645aa9438089c5e9954e5f1ac5");
                //
                session.delete(_user);
                
                //
                session.delete(User.class, "bc19f5645aa9438089c5e9954e5f1ac5");
        
        - 根据条件删除记录：

        		// 删除年龄大于20岁的用户记录
        		session.executeForUpdate(
                        SQL.create(
                                Delete.create(User.class).where(
                                        Where.create(
                                                Cond.create()
                                                        .lt(User.FIELDS.AGE).param(20)))));
	
	+ 统计（Count）：

				// 统计年龄大于20岁的用户记录总数
				
				// 方式一：
				long _count = session.count(User.class, 
                        Where.create(
                                Cond.create()
                                        .lt(User.FIELDS.AGE).param(20)));

				// 方式二：
                _count = session.count(
                        SQL.create(
                                Delete.create(User.class).where(
                                        Where.create(
                                                Cond.create()
                                                        .lt(User.FIELDS.AGE).param(20)))));
	
	
	+ 执行更新类操作（ExecuteForUpdate）：

		该方法用于执行ISession接口中并未提供对应的方法封装且执行操作会对数据库产生变化的SQL语句，执行该方法后将返回受影响记录行数，如上面执行的删除年龄大于20岁的用户记录：
		
				int _effectCount =session.executeForUpdate(
                        SQL.create(
                                Delete.create(User.class).where(
                                        Where.create(
                                                Cond.create()
                                                        .lt(User.FIELDS.AGE).param(20)))));
	
	**注**：以上操作均支持批量操作，具体使用请阅读API接口文档和相关源码；

- 基于继承BaseEntity的数据库操作：

	上面阐述的是基于ISession会话对象完成一系列数据库操作，接下来介绍的操作过程更加简单直接，完全基于数据实体对象；
	
	> 需要注意的是，本小节所指的数据实体对象必须通过继承框架提供BaseEntity抽象类；
	
	+ 插入（Insert）：

			User _user = new User();
            _user.setId(UUIDUtils.UUID());
            _user.setUsername("suninformation");
            _user.setPwd(DigestUtils.md5Hex("123456"));
            _user.setAge(20);
            _user.setSex("F");
            // 执行数据插入
            _user.save();
            
            // 或者在插入时也可以指定/排除某些字段
            _user.save(Fields.create(User.FIELDS.SEX, User.FIELDS.AGE).excluded(true));
            
            // 或者插入前判断记录是否已存在，若已存在则执行记录更新操作
            _user.saveOrUpdate();
            
            // 或者执行记录更新操作时仅更新指定的字段
            _user.saveOrUpdate(Fields.create(User.FIELDS.SEX, User.FIELDS.AGE));

	+ 更新（Update）：

			User _user = new User();
            _user.setId("bc19f5645aa9438089c5e9954e5f1ac5");
            _user.setPwd(DigestUtils.md5Hex("654321"));
            _user.setAge(20);
            _user.setSex("F");
            // 执行记录更新
            _user.update();
            
            // 或者仅更新指定的字段
            _user.update(Fields.create(User.FIELDS.SEX, User.FIELDS.AGE));

	+ 查询（Find）：

		- 根据记录ID加载：
		
				User _user = new User();
				_user.setId("bc19f5645aa9438089c5e9954e5f1ac5");
				// 根据记录ID加载全部字段
				_user = _user.load();
				
				// 或者根据记录ID加载指定的字段
				_user = _user.load(Fields.create(User.FIELDS.USER_NAME, User.FIELDS.SEX, User.FIELDS.AGE));
		
		- 通过数据实体设置条件，查询所有符合条件的记录；
		
				User _user = new User();
	            _user.setUsername("suninformation");
	            _user.setPwd(DigestUtils.md5Hex("123456"));
	            // 返回所有字段
	            IResultSet<User> _users = _user.find();
	            
	            // 或者返回指定的字段
	            _users = _user.find(Fields.create(User.FIELDS.ID, User.FIELDS.AGE));
	            
	            // 或者分页查询
                _users = _user.find(Page.create(1).pageSize(10));
	
		- 分页查询：

				User _user = new User();
				_user.setSex("F");
				
				// 分页查询，返回全部字段
				IResultSet<User> _users = _user.find(Page.create(1).pageSize(10));
			
				// 或者分页查询，返回指定的字段
                _users = _user.find(Fields.create(User.FIELDS.ID, User.FIELDS.AGE), Page.create(1).pageSize(10));

		- 仅返回符合条件的第一条记录(FindFirst)：

				User _user = new User();
                _user.setUsername("suninformation");
                _user.setPwd(DigestUtils.md5Hex("123456"));
                
                // 返回与用户名称和密码匹配的第一条记录
                _user = _user.findFirst();
                
                // 或者返回与用户名称和密码匹配的第一条记录的ID和AGE字段
                _user = _user.findFirst(Fields.create(User.FIELDS.ID, User.FIELDS.AGE));
	
	+ 删除（Delete）：

			User _user = new User();
			_user.setId("bc19f5645aa9438089c5e9954e5f1ac5");
			
			// 根据实体主键删除记录
			_user.delete();

	**注**：以上介绍的两种数据库操作方式各有特点，请根据实际情况选择更适合的方式，亦可混合使用；

####查询（Query）：





####高级特性：


