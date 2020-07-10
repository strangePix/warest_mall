package com.warest.mall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class TokenCache {

//    声明日志
    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);

    //前缀  命名空间  用于区分
    public static final String TOKEN_PREFIX = "token_";

    //本地缓存      静态代码块  guava中
    private static LoadingCache<String,String> localCache =
            CacheBuilder.newBuilder()
                    .initialCapacity(1000) //缓存初始化容量
                    .maximumSize(10000) //缓存最大容量，超过用LRU算法（缓存淘汰）移除缓存项
                    .expireAfterAccess(12, TimeUnit.HOURS) //有效期 12小时
                    .build(new CacheLoader<String, String>() { //匿名实现类
                        //默认的数据加载实现,当调用get取值的时候,如果key没有对应的值,就调用这个方法进行加载.
                        @Override
                        public String load(String s) throws Exception {
                            return "null";  //避免空指针，设定没命中返回特定字符串
                        }
                    });

    //存储token
    public static void setKey(String key,String value){
        localCache.put(key,value);
    }

    //获取token
    public static String getKey(String key){
        String value = null; //初始化
        try {
            value = localCache.get(key);
            if("null".equals(value)){
                return null;
            }
            return value;
        }catch (Exception e){
            logger.error("localCache get error",e);  //打印异常
        }
        return null;
    }
}
