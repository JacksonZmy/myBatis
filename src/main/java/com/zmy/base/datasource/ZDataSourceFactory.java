package com.zmy.base.datasource;

import javax.sql.DataSource;
import java.util.Properties;

public interface ZDataSourceFactory {

    void setProperties(Properties props);

    DataSource getDataSource();
}
