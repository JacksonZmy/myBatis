package newfile;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public class Configuration {

    public <T> T getMapper(Class<T> clazz, SqlSession sqlSession) {
        return (T) Proxy.newProxyInstance(this.getClass().getClassLoader(),
                new Class[] {clazz},
                new MapperProxy(sqlSession));
    }

    /**
     * XML解析好了
     */
    static class TestMapperXml{
        public static final String namesapce = "newfile.TestMapper";

        public static final Map<String, String> methodSqlMapping = new HashMap<>();

        static {
            methodSqlMapping.put("selectByPrimaryKey", "select * from test where id = %d");
        }
    }
}
