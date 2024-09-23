package com.zmy.inter.objectfactory;

import com.zmy.inter.beans.User;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;

public class ZObjectFactory extends DefaultObjectFactory {

    /**
     * 重写通过无参构造方法创建实例的方法
     * @param type
     * @param <T>
     * @return
     */
    @Override
    public <T> T create(Class<T> type) {
        System.out.println("Object Factory .... create ");
        if(type.equals(User.class)){
            // 创建的类型如果是User类型 我们就自己来创建
            User user = new User();
            user.setName("ObjectFactory 测试");
            return (T) user;
        }
        return super.create(type);
    }
}
