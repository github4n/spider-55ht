package com.haitao55.spider.common.service.impl;

import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.haitao55.spider.common.service.Function;

import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

/**
 * 
* Title: redis 操作service
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2016年9月19日 下午8:22:24
* @version 1.0
 */
@Service
public class RedisService {

    @Autowired(required = false)
    private ShardedJedisPool shardedJedisPool;

    private <T> T execute(Function<ShardedJedis, T> function) {
        ShardedJedis shardedJedis = null;
        try {
            // 从连接池中获取到jedis分片对象
            shardedJedis = shardedJedisPool.getResource();
            return function.execute(shardedJedis);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != shardedJedis) {
                // 关闭，检测连接是否有效，有效则放回到连接池中，无效则重置状态
                shardedJedis.close();
            }
        }
        return null;
    }

    /**
     * 保存数据到redis中
     * 
     * @param key
     * @param value
     * @return
     */
    public String set(final String key, final String value) {
        return this.execute(new Function<ShardedJedis, String>() {
            @Override
            public String execute(ShardedJedis shardedJedis) {
                return shardedJedis.set(key, value);
            }

        });
    }
    
    /**
     * 保存数据到redis中
     * 
     * @param key
     * @param value
     * @return
     */
    public String hmSet(final String key, final Map<String,String> hash) {
        return this.execute(new Function<ShardedJedis, String>() {
            @Override
            public String execute(ShardedJedis shardedJedis) {
                return shardedJedis.hmset(key, hash);
            }

        });
    }
    
    public Long hSet(final String key, final String field,final String value) {
        return this.execute(new Function<ShardedJedis, Long>() {
            @Override
            public Long execute(ShardedJedis shardedJedis) {
                return shardedJedis.hset(key, field, value);
            }

        });
    }
    
    /**
     * 保存数据到redis中，生存时间单位是：秒
     * 
     * @param key
     * @param value
     * @param seconds
     * @return
     */
    public String set(final String key, final String value, final Integer seconds) {
        return this.execute(new Function<ShardedJedis, String>() {
            @Override
            public String execute(ShardedJedis shardedJedis) {
                String result = shardedJedis.set(key, value);
                shardedJedis.expire(key, seconds);//设置生存时间
                return result;
            }

        });
    }

    /**
     * 从redis中获取数据
     * 
     * @param key
     * @return
     */
    public String get(final String key) {
        return this.execute(new Function<ShardedJedis, String>() {
            @Override
            public String execute(ShardedJedis shardedJedis) {
                return shardedJedis.get(key);
            }

        });
    }
    
    /**
     * 从redis中获取数据
     * 
     * @param key
     * @return
     */
    public String hget(final String key,final String field) {
        return this.execute(new Function<ShardedJedis, String>() {
            @Override
            public String execute(ShardedJedis shardedJedis) {
                return shardedJedis.hget(key,field);
            }

        });
    }
    public Boolean hexists(final String key,final String field) {
        return this.execute(new Function<ShardedJedis, Boolean>() {
            @Override
            public Boolean execute(ShardedJedis shardedJedis) {
                return shardedJedis.hexists(key, field);
            }

        });
    }

    /**
     * 设置key生存时间，单位：秒
     * 
     * @param key
     * @param seconds
     * @return
     */
    public Long expire(final String key, final Integer seconds) {
        return this.execute(new Function<ShardedJedis, Long>() {
            @Override
            public Long execute(ShardedJedis shardedJedis) {
                return shardedJedis.expire(key, seconds);
            }

        });
    }

    /**
     * 从redis中删除数据
     * 
     * @param key
     * @return
     */
    public Long del(final String key) {
        return this.execute(new Function<ShardedJedis, Long>() {
            @Override
            public Long execute(ShardedJedis shardedJedis) {
                return shardedJedis.del(key);
            }
        });
    }
    /**
     * 队列头部追加
     * @param key
     * @param value
     * @return
     */
    public Long lpush(final String key,final String... value) {
        return this.execute(new Function<ShardedJedis, Long>() {
            @Override
            public Long execute(ShardedJedis shardedJedis) {
                return shardedJedis.lpush(key, value);
            }
        });
    }
    /**
     * 队列头部开始消费
     * @param key
     * @return
     */
    public String lpop(final String key){
    	return this.execute(new Function<ShardedJedis, String>() {
            @Override
            public String execute(ShardedJedis shardedJedis) {
                return shardedJedis.lpop(key);
            }
        });
    }
   /**
    * 队列长度
    * @param key
    * @return
    */
   public Long llen(final String key) {
        return this.execute(new Function<ShardedJedis, Long>() {
            @Override
            public Long execute(ShardedJedis shardedJedis) {
                return shardedJedis.llen(key);
            }
        });
    }

	/**
	 * 获得队列长度
	 * 
	 * @param key
	 * @param index
	 * @return
	 */
	public String lindex(final String key, long index) {
		return this.execute(new Function<ShardedJedis, String>() {
			@Override
			public String execute(ShardedJedis shardedJedis) {
				return shardedJedis.lindex(key, index);
			}
		});
	}

	/**
	 * 删除队列长度
	 * 
	 * @param key
	 * @param count
	 * @param value
	 * @return
	 */
	public Long lrem(final String key, long count, String value) {
		return this.execute(new Function<ShardedJedis, Long>() {
			@Override
			public Long execute(ShardedJedis shardedJedis) {
				return shardedJedis.lrem(key, count, value);
			}
		});
	}

	/**
	 * 获取一个key的剩余时间
	 * 
	 * @param key
	 * @return
	 */
	public Long ttl(final String key) {
		return this.execute(new Function<ShardedJedis, Long>() {
			@Override
			public Long execute(ShardedJedis shardedJedis) {
				return shardedJedis.ttl(key);
			}
		});
	}

    public String rpop(final String key) {
        return this.execute(new Function<ShardedJedis, String>() {
            @Override
            public String execute(ShardedJedis shardedJedis) {
                return shardedJedis.rpop(key);
            }
        });
    }
    
    /**
     * sadd  添加元素到set集合
     * @param key
     * @param value
     * @return
     */
    public Long sadd(final String key,final String ...value) {
        return this.execute(new Function<ShardedJedis, Long>() {
            @Override
            public Long execute(ShardedJedis shardedJedis) {
                return shardedJedis.sadd(key, value);
            }
        });
    }
    /**
     * set集合取元素
     * @param key
     * @return
     */
    public Set<String> smembers(final String key) {
        return this.execute(new Function<ShardedJedis, Set<String>>() {
            @Override
            public Set<String> execute(ShardedJedis shardedJedis) {
                return shardedJedis.smembers(key);
            }
        });
    }
    

	public ShardedJedisPool getShardedJedisPool() {
		return shardedJedisPool;
	}

	public void setShardedJedisPool(ShardedJedisPool shardedJedisPool) {
		this.shardedJedisPool = shardedJedisPool;
	}
    
    
    

}
