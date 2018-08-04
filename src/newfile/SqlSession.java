package newfile;

public class SqlSession {

    private Configuration configuration;
    private Excutor excutor;

    public SqlSession(Configuration configuration, Excutor excutor){
        this.configuration = configuration;
        this.excutor = excutor;
    }


    /**
     * getMapper
     * @param clazz
     */
    public <T> T getMapper(Class<T> clazz){
        return configuration.getMapper(clazz, this);
    }


    /**
     * @param statement sql语句
     * @param parameter sql参数
     * @param <T>
     * @return
     */
    public <T> T selectOne(String statement, String parameter){
        return excutor.query(statement, parameter);
    }
}
