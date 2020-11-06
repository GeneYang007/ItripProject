package itrip.common;

import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

@Component
public class JredisApi {
    Jedis redis=new Jedis("127.0.0.1",6379);
    //data是过期时间
    public void SetRedis(String key,String value,int data)
    {
        redis.auth("12345");
        redis.setex(key,data,value);
    }

    public String getRedis(String key)
    {
        redis.auth("12345");
        return redis.get(key);
    }

}