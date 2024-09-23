package com.zmy.base.datasource.pooled;

import com.zmy.base.datasource.unpooled.ZUnpooledDataSourceFactory;

/**
 * @autho Clinton Begin
 */
public class ZPooledDataSourceFactory extends ZUnpooledDataSourceFactory {

  public ZPooledDataSourceFactory() {
    this.dataSource = new ZPooledDataSource();
  }

}
