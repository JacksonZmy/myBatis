package com.zmy.base.scripting.xmltags;

import com.zmy.core.session.ZConfiguration;

import java.util.*;

public class ZTrimSqlNode implements ZSqlNode{
    private final ZSqlNode contents;
    private final String prefix;
    private final String suffix;
    private final List<String> prefixesToOverride;
    private final List<String> suffixesToOverride;
    private final ZConfiguration configuration;

    public ZTrimSqlNode(ZConfiguration configuration, ZSqlNode contents, String prefix, String prefixesToOverride, String suffix, String suffixesToOverride) {
        this(configuration, contents, prefix, parseOverrides(prefixesToOverride), suffix, parseOverrides(suffixesToOverride));
    }

    protected ZTrimSqlNode(ZConfiguration configuration, ZSqlNode contents, String prefix, List<String> prefixesToOverride, String suffix, List<String> suffixesToOverride) {
        this.contents = contents;
        this.prefix = prefix;
        this.prefixesToOverride = prefixesToOverride;
        this.suffix = suffix;
        this.suffixesToOverride = suffixesToOverride;
        this.configuration = configuration;
    }

    public boolean apply(ZDynamicContext context) {
        ZTrimSqlNode.FilteredDynamicContext filteredDynamicContext = new ZTrimSqlNode.FilteredDynamicContext(context);
        boolean result = this.contents.apply(filteredDynamicContext);
        filteredDynamicContext.applyAll();
        return result;
    }

    private static List<String> parseOverrides(String overrides) {
        if (overrides == null) {
            return Collections.emptyList();
        } else {
            StringTokenizer parser = new StringTokenizer(overrides, "|", false);
            List<String> list = new ArrayList(parser.countTokens());

            while(parser.hasMoreTokens()) {
                list.add(parser.nextToken().toUpperCase(Locale.ENGLISH));
            }

            return list;
        }
    }

    private class FilteredDynamicContext extends ZDynamicContext {
        private ZDynamicContext delegate;
        private boolean prefixApplied;
        private boolean suffixApplied;
        private StringBuilder sqlBuffer;

        public FilteredDynamicContext(ZDynamicContext delegate) {
            super(configuration, (Object)null);
            this.delegate = delegate;
            this.prefixApplied = false;
            this.suffixApplied = false;
            this.sqlBuffer = new StringBuilder();
        }

        public void applyAll() {
            this.sqlBuffer = new StringBuilder(this.sqlBuffer.toString().trim());
            String trimmedUppercaseSql = this.sqlBuffer.toString().toUpperCase(Locale.ENGLISH);
            if (trimmedUppercaseSql.length() > 0) {
                this.applyPrefix(this.sqlBuffer, trimmedUppercaseSql);
                this.applySuffix(this.sqlBuffer, trimmedUppercaseSql);
            }

            this.delegate.appendSql(this.sqlBuffer.toString());
        }

        public Map<String, Object> getBindings() {
            return this.delegate.getBindings();
        }

        public void bind(String name, Object value) {
            this.delegate.bind(name, value);
        }

        public int getUniqueNumber() {
            return this.delegate.getUniqueNumber();
        }

        public void appendSql(String sql) {
            this.sqlBuffer.append(sql);
        }

        public String getSql() {
            return this.delegate.getSql();
        }

        private void applyPrefix(StringBuilder sql, String trimmedUppercaseSql) {
            if (!this.prefixApplied) {
                this.prefixApplied = true;
                if (ZTrimSqlNode.this.prefixesToOverride != null) {
                    Iterator var3 = ZTrimSqlNode.this.prefixesToOverride.iterator();

                    while(var3.hasNext()) {
                        String toRemove = (String)var3.next();
                        if (trimmedUppercaseSql.startsWith(toRemove)) {
                            sql.delete(0, toRemove.trim().length());
                            break;
                        }
                    }
                }

                if (ZTrimSqlNode.this.prefix != null) {
                    sql.insert(0, " ");
                    sql.insert(0, ZTrimSqlNode.this.prefix);
                }
            }

        }

        private void applySuffix(StringBuilder sql, String trimmedUppercaseSql) {
            if (!this.suffixApplied) {
                this.suffixApplied = true;
                if (ZTrimSqlNode.this.suffixesToOverride != null) {
                    label33: {
                        Iterator var3 = ZTrimSqlNode.this.suffixesToOverride.iterator();

                        String toRemove;
                        do {
                            if (!var3.hasNext()) {
                                break label33;
                            }

                            toRemove = (String)var3.next();
                        } while(!trimmedUppercaseSql.endsWith(toRemove) && !trimmedUppercaseSql.endsWith(toRemove.trim()));

                        int start = sql.length() - toRemove.trim().length();
                        int end = sql.length();
                        sql.delete(start, end);
                    }
                }

                if (ZTrimSqlNode.this.suffix != null) {
                    sql.append(" ");
                    sql.append(ZTrimSqlNode.this.suffix);
                }
            }

        }
    }
}
