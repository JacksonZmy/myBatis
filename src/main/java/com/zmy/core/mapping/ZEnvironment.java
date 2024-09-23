package com.zmy.core.mapping;

import com.zmy.base.transaction.ZTransactionFactory;
import javax.sql.DataSource;

public final class ZEnvironment {
    private final String id;
    private final DataSource dataSource;
    private final ZTransactionFactory transactionFactory;


    public ZTransactionFactory getTransactionFactory() {
        return this.transactionFactory;
    }

    public ZEnvironment(String id, DataSource dataSource, ZTransactionFactory transactionFactory) {
        if (id == null) {
            throw new IllegalArgumentException("'id' 不能为空");
        }
        this.id = id;
        if (dataSource == null) {
            throw new IllegalArgumentException("'dataSource' 不能为空");
        }
        if (transactionFactory == null) {
            throw new IllegalArgumentException("'transactionFactory' 不能为空");
        }
        this.transactionFactory = transactionFactory;
        this.dataSource = dataSource;
    }

    public String getId() {
        return this.id;
    }
    public DataSource getDataSource() {
        return this.dataSource;
    }


    public static class Builder {
        private final String id;
        private DataSource dataSource;
        private ZTransactionFactory transactionFactory;

        public Builder(String id) {
            this.id = id;
        }

        public ZEnvironment.Builder dataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }
        public ZEnvironment.Builder transactionFactory(ZTransactionFactory transactionFactory) {
            this.transactionFactory = transactionFactory;
            return this;
        }

        public String id() {
            return this.id;
        }

        public ZEnvironment build() {
            return new ZEnvironment(this.id, this.dataSource, this.transactionFactory);
        }

        public ZTransactionFactory getTransactionFactory() {
            return this.transactionFactory;
        }
    }
}
