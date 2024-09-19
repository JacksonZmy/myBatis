package com.zmy.core.mapping;

import org.apache.ibatis.mapping.Environment;
import org.apache.ibatis.transaction.TransactionFactory;

import javax.sql.DataSource;

public final class ZEnvironment {
    private final String id;
    private final DataSource dataSource;
    private final TransactionFactory transactionFactory;


    public TransactionFactory getTransactionFactory() {
        return this.transactionFactory;
    }

    public ZEnvironment(String id, DataSource dataSource, TransactionFactory transactionFactory) {
        if (id == null) {
            throw new IllegalArgumentException("Parameter 'id' must not be null");
        }
        this.id = id;
        if (dataSource == null) {
            throw new IllegalArgumentException("Parameter 'dataSource' must not be null");
        }
        if (transactionFactory == null) {
            throw new IllegalArgumentException("Parameter 'transactionFactory' must not be null");
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
        private TransactionFactory transactionFactory;

        public Builder(String id) {
            this.id = id;
        }

        public ZEnvironment.Builder dataSource(DataSource dataSource) {
            this.dataSource = dataSource;
            return this;
        }
        public ZEnvironment.Builder transactionFactory(TransactionFactory transactionFactory) {
            this.transactionFactory = transactionFactory;
            return this;
        }

        public String id() {
            return this.id;
        }

        public ZEnvironment build() {
            return new ZEnvironment(this.id, this.dataSource, this.transactionFactory);
        }

        public TransactionFactory getTransactionFactory() {
            return this.transactionFactory;
        }
    }
}
