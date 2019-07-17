package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 使用Google guava 提供的缓存机制 实现本地缓存
 */
public class TokenCache {
    //日志
    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);
    // 缓存前缀
    public static  final  String TOKEN_PREFIX = "token_";
    // 使用LRU算法  创建本地缓存对象
    // LRU（Least recently used，最近最少使用）算法根据数据的历史访问记录来进行淘汰数据，其核心思想是“如果数据最近被访问过，那么将来被访问的几率也更高”。
    private static LoadingCache<String,String>  localCache = CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(10000).expireAfterAccess(12, TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(String s) throws Exception {
                    return "null";
                }
            });
    // 设置缓存数据
    public  static void setKey(String key,String  value){
        localCache.put(key,value);
    }
    //根据缓存key  获取缓存对象数据
    public static String getKey(String key){
        String value = null;
        try {
            value= localCache.get(key);
            if("null".equals(value)){
              return  null;
            }
            return value;
        } catch (ExecutionException e) {
            logger.error("localcache get error" ,e);
        }
        return  null;
    }
}
