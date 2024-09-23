package com.zmy.base.builder.xml;

import com.zmy.base.builder.ZBaseBuilder;
import com.zmy.base.datasource.ZDataSourceFactory;
import com.zmy.base.transaction.ZTransactionFactory;
import com.zmy.core.plugin.ZInterceptor;
import com.zmy.core.session.ZAutoMappingUnknownColumnBehavior;
import com.zmy.core.session.ZConfiguration;
import com.zmy.core.mapping.ZEnvironment;
import com.zmy.core.session.ZExecutorType;
import org.apache.ibatis.builder.BuilderException;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.datasource.DataSourceFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.logging.Log;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.MetaClass;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.apache.ibatis.session.LocalCacheScope;
import org.apache.ibatis.type.JdbcType;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.Properties;

public class ZXMLConfigBuilder extends ZBaseBuilder {

    // 标识是否解析过
    private boolean parsed;
    // 用于解析mybatis-config.xml 配置文件的 XPathParser对象
    private final XPathParser parser;
    // 标识 <environment>
    private String environment;
    // 反射工厂
    private final ReflectorFactory localReflectorFactory = new DefaultReflectorFactory();

    public ZXMLConfigBuilder(InputStream inputStream, String environment, Properties props) {
        // EntityResolver的实现类是XMLMapperEntityResolver 来完成配置文件的校验，根据对应的DTD文件来实现
        this(new XPathParser(inputStream, true, null, new XMLMapperEntityResolver()), environment, props);
    }

    private ZXMLConfigBuilder(XPathParser parser, String environment, Properties props) {
        super(new ZConfiguration()); // 完成了Configuration的初始化
        ErrorContext.instance().resource("SQL Mapper Configuration");
        this.configuration.setVariables(props); // 设置对应的Properties属性
        this.parsed = false; // 设置 是否解析的标志为 false
        this.environment = environment; // 初始化environment
        this.parser = parser; // 初始化 解析器
    }

    public ZConfiguration parse() {
        if (parsed) {
            throw new BuilderException("每个 XMLConfigBuilder 只能用一次");
        }
        parsed = true;
        // 解析configuration几点及其子节点 XPathParser，dom 和 SAX 都有用到
        parseConfiguration(parser.evalNode("/configuration"));
        return configuration;
    }

    private void parseConfiguration(XNode root) {
        try {
            // 对于全局配置文件各种标签的解析
            propertiesElement(root.evalNode("properties"));
            // 解析 settings 标签
            Properties settings = settingsAsProperties(root.evalNode("settings"));
            // 日志设置
            loadCustomLogImpl(settings);
            // 类型别名
            typeAliasesElement(root.evalNode("typeAliases"));
//            // 插件
            pluginElement(root.evalNode("plugins"));
            // 用于创建对象
//            objectFactoryElement(root.evalNode("objectFactory"));
//            // 用于对对象进行加工
//            objectWrapperFactoryElement(root.evalNode("objectWrapperFactory"));
//            // 反射工具箱
//            reflectorFactoryElement(root.evalNode("reflectorFactory"));
            // settings 子标签赋值，默认值就是在这里提供的 >>
            settingsElement(settings);
            // 在 objectFactory 和 objectWrapperFactory 之后读取
            // 创建了数据源
            environmentsElement(root.evalNode("environments"));
//            databaseIdProviderElement(root.evalNode("databaseIdProvider"));
//            typeHandlerElement(root.evalNode("typeHandlers"));
            // 解析引用的Mapper映射器
            mapperElement(root.evalNode("mappers"));
        } catch (Exception e) {
            throw new BuilderException("解析 SQL Mapper 配置出错， 错误信息: " + e, e);
        }
    }

    /**
     * 加载插件
     * @param parent
     * @throws Exception
     */
    private void pluginElement(XNode parent) throws Exception {
        if (parent != null) {
            for (XNode child : parent.getChildren()) {
                // 获取<plugin> 节点的 interceptor 属性的值
                String interceptor = child.getStringAttribute("interceptor");
                // 获取<plugin> 下的所有的properties子节点
                Properties properties = child.getChildrenAsProperties();
                // 获取 Interceptor 对象
                ZInterceptor interceptorInstance = (ZInterceptor) resolveClass(interceptor).getDeclaredConstructor().newInstance();
                // 设置 interceptor的 属性
                interceptorInstance.setProperties(properties);
                // Configuration中记录 Interceptor
                configuration.addInterceptor(interceptorInstance);
            }
        }
    }

    /**
     * 加载日志配置
     * @param props
     */
    private void loadCustomLogImpl(Properties props) {
        // 获取 logImpl设置的 日志 类型
        Class<? extends Log> logImpl = resolveClass(props.getProperty("logImpl"));
        // 设置日志
        configuration.setLogImpl(logImpl);
    }

    /**
     * 解析 resource 节点和 url 节点
     * 合并 properties 属性
     * @param context
     * @throws Exception
     */
    private void propertiesElement(XNode context) throws Exception {
        if (context != null) {
            // 创建了一个 Properties 对象，后面可以用到
            Properties defaults = context.getChildrenAsProperties();
            String resource = context.getStringAttribute("resource");
            String url = context.getStringAttribute("url");
            if (resource != null && url != null) {
                // url 和 resource 不能同时存在
                throw new BuilderException("url 和 resource 不能同时存在");
            }
            // 加载resource或者url属性中指定的 properties 文件
            if (resource != null) {
                defaults.putAll(Resources.getResourceAsProperties(resource));
            } else if (url != null) {
                defaults.putAll(Resources.getUrlAsProperties(url));
            }
            // 更新对应的属性信息
            parser.setVariables(defaults);
            configuration.setVariables(defaults);
        }
    }

    private Properties settingsAsProperties(XNode context) {
        if (context == null) {
            return new Properties();
        }
        // 获取settings节点下的所有的子节点
        Properties props = context.getChildrenAsProperties();
        // 检查配置类是否知道所有设置
        MetaClass metaConfig = MetaClass.forClass(ZConfiguration.class, localReflectorFactory);
        for (Object key : props.keySet()) {
            //
            if (!metaConfig.hasSetter(String.valueOf(key))) {
                throw new BuilderException("setting 中 " + key + " 未知，确保拼写正确（区分大小写）。");
            }
        }
        return props;
    }

    private void typeAliasesElement(XNode parent) {
        // 放入 TypeAliasRegistry
        if (parent != null) {
            for (XNode child : parent.getChildren()) {
                if ("package".equals(child.getName())) {
                    String typeAliasPackage = child.getStringAttribute("name");
                    configuration.getTypeAliasRegistry().registerAliases(typeAliasPackage);
                } else {
                    String alias = child.getStringAttribute("alias");
                    String type = child.getStringAttribute("type");
                    try {
                        Class<?> clazz = Resources.classForName(type);
                        if (alias == null) {
                            // 扫描 @Alias 注解使用
                            typeAliasRegistry.registerAlias(clazz);
                        } else {
                            // 直接注册
                            typeAliasRegistry.registerAlias(alias, clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        throw new BuilderException("别名 " + alias + " 注册失败，错误信息: " + e, e);
                    }
                }
            }
        }
    }

    /**
     * settings 子标签赋值，默认值就是在这里提供的
     * @param props
     */
    private void settingsElement(Properties props) {
//        configuration.setAutoMappingBehavior(AutoMappingBehavior.valueOf(props.getProperty("autoMappingBehavior", "PARTIAL")));
        configuration.setAutoMappingUnknownColumnBehavior(ZAutoMappingUnknownColumnBehavior.valueOf(props.getProperty("autoMappingUnknownColumnBehavior", "NONE")));
//        configuration.setCacheEnabled(booleanValueOf(props.getProperty("cacheEnabled"), true));
//        configuration.setProxyFactory((ProxyFactory) createInstance(props.getProperty("proxyFactory")));
//        configuration.setLazyLoadingEnabled(booleanValueOf(props.getProperty("lazyLoadingEnabled"), false));
//        configuration.setAggressiveLazyLoading(booleanValueOf(props.getProperty("aggressiveLazyLoading"), false));
//        configuration.setMultipleResultSetsEnabled(booleanValueOf(props.getProperty("multipleResultSetsEnabled"), true));
//        configuration.setUseColumnLabel(booleanValueOf(props.getProperty("useColumnLabel"), true));
//        configuration.setUseGeneratedKeys(booleanValueOf(props.getProperty("useGeneratedKeys"), false));
        configuration.setDefaultExecutorType(ZExecutorType.valueOf(props.getProperty("defaultExecutorType", "SIMPLE")));
//        configuration.setDefaultStatementTimeout(integerValueOf(props.getProperty("defaultStatementTimeout"), null));
//        configuration.setDefaultFetchSize(integerValueOf(props.getProperty("defaultFetchSize"), null));
        configuration.setDefaultResultSetType(resolveResultSetType(props.getProperty("defaultResultSetType")));
        configuration.setMapUnderscoreToCamelCase(booleanValueOf(props.getProperty("mapUnderscoreToCamelCase"), false));
//        configuration.setSafeRowBoundsEnabled(booleanValueOf(props.getProperty("safeRowBoundsEnabled"), false));
        configuration.setLocalCacheScope(LocalCacheScope.valueOf(props.getProperty("localCacheScope", "SESSION")));
        configuration.setJdbcTypeForNull(JdbcType.valueOf(props.getProperty("jdbcTypeForNull", "OTHER")));
//        configuration.setLazyLoadTriggerMethods(stringSetValueOf(props.getProperty("lazyLoadTriggerMethods"), "equals,clone,hashCode,toString"));
//        configuration.setSafeResultHandlerEnabled(booleanValueOf(props.getProperty("safeResultHandlerEnabled"), true));
//        configuration.setDefaultScriptingLanguage(resolveClass(props.getProperty("defaultScriptingLanguage")));
//        configuration.setDefaultEnumTypeHandler(resolveClass(props.getProperty("defaultEnumTypeHandler")));
//        configuration.setCallSettersOnNulls(booleanValueOf(props.getProperty("callSettersOnNulls"), false));
//        configuration.setUseActualParamName(booleanValueOf(props.getProperty("useActualParamName"), true));
        configuration.setReturnInstanceForEmptyRow(booleanValueOf(props.getProperty("returnInstanceForEmptyRow"), false));
        configuration.setLogPrefix(props.getProperty("logPrefix"));
//        configuration.setConfigurationFactory(resolveClass(props.getProperty("configurationFactory")));
    }

    private void environmentsElement(XNode context) throws Exception {
        if (context != null) {
            if (environment == null) {
                environment = context.getStringAttribute("default");
            }
            for (XNode child : context.getChildren()) {
                String id = child.getStringAttribute("id");
                if (isSpecifiedEnvironment(id)) {
                    // 事务工厂
                    ZTransactionFactory txFactory = transactionManagerElement(child.evalNode("transactionManager"));
                    // 数据源工厂（例如 DruidDataSourceFactory ）
                    ZDataSourceFactory dsFactory = dataSourceElement(child.evalNode("dataSource"));
                    // 数据源
                    DataSource dataSource = dsFactory.getDataSource();
                    // 包含了 事务工厂和数据源的 Environment
                    ZEnvironment.Builder environmentBuilder = new ZEnvironment.Builder(id)
                            .transactionFactory(txFactory)
                            .dataSource(dataSource);
                    // 放入 Configuration
                    configuration.setEnvironment(environmentBuilder.build());
                }
            }
        }
    }

    private boolean isSpecifiedEnvironment(String id) {
        if (environment == null) {
            throw new BuilderException("未指定环境");
        } else if (id == null) {
            throw new BuilderException("环境需要 id 属性");
        } else if (environment.equals(id)) {
            return true;
        }
        return false;
    }

    private ZDataSourceFactory dataSourceElement(XNode context) throws Exception {
        if (context != null) {
            String type = context.getStringAttribute("type");
            Properties props = context.getChildrenAsProperties();
            ZDataSourceFactory factory = (ZDataSourceFactory) resolveClass(type).getDeclaredConstructor().newInstance();
            factory.setProperties(props);
            return factory;
        }
        throw new BuilderException("Environment 声明需要 DataSourceFactory。");
    }

    /**
     * 解析 mappers 节点及其子节点
     * @param parent
     * @throws Exception
     */
    private void mapperElement(XNode parent) throws Exception {
        if (parent != null) {
            for (XNode child : parent.getChildren()) {
                // 不同的定义方式的扫描，最终都是调用 addMapper()方法（添加到 MapperRegistry）。这个方法和 getMapper() 对应
                // package包
                if ("package".equals(child.getName())) {
                    String mapperPackage = child.getStringAttribute("name");
                    configuration.addMappers(mapperPackage);
                } else {
                    String resource = child.getStringAttribute("resource");
                    String url = child.getStringAttribute("url");
                    String mapperClass = child.getStringAttribute("class");
                    if (resource != null && url == null && mapperClass == null) {
                        // resource	相对路径
                        ErrorContext.instance().resource(resource);
                        InputStream inputStream = Resources.getResourceAsStream(resource);
                        ZXMLMapperBuilder mapperParser = new ZXMLMapperBuilder(inputStream, configuration, resource, configuration.getSqlFragments());
                        //

                        /* 解析 Mapper.xml，总体上做了两件事情
                            1、具体增删改查标签的解析。一个标签一个MappedStatement
                            2、把namespace（接口类型）和工厂类绑定起来，放到一个map。
                               一个namespace 一个 MapperProxyFactory
                         */
                        mapperParser.parse();
                    } else if (resource == null && url != null && mapperClass == null) {
//                        // TODO url	绝对路径
//                        ErrorContext.instance().resource(url);
//                        InputStream inputStream = Resources.getUrlAsStream(url);
//                        XMLMapperBuilder mapperParser = new XMLMapperBuilder(inputStream, configuration, url, configuration.getSqlFragments());
//                        mapperParser.parse();
                    } else if (resource == null && url == null && mapperClass != null) {
                        // class 	单个接口
                        Class<?> mapperInterface = Resources.classForName(mapperClass);
                        configuration.addMapper(mapperInterface);
                    } else {
                        throw new BuilderException("mapper 元素只能指定一个 url、资源或类，但不能指定多个");
                    }
                }
            }
        }
    }

    private ZTransactionFactory transactionManagerElement(XNode context) throws Exception {
        if (context != null) {
            String type = context.getStringAttribute("type");
            Properties props = context.getChildrenAsProperties();
            ZTransactionFactory factory = (ZTransactionFactory) resolveClass(type).getDeclaredConstructor().newInstance();
            factory.setProperties(props);
            return factory;
        }
        throw new BuilderException("Environment 声明需要 TransactionFactory。");
    }
}
