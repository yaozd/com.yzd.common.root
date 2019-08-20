## 令牌桶lua脚本
- 获取令牌
```
local key = KEYS[1] --限流KEY（一秒一个）
local limit = tonumber(ARGV[1]) --限流大小
local current = tonumber(redis.call('get', key) or "0")
if current + 1 > limit then --如果超出限流大小
    return 0
else --请求数+1，并设置2秒过期
    redis.call("INCRBY", key,"1")
    redis.call("expire", key,"2")
    return 1
end
```
- 释放令牌
```
local key = KEYS[1] --限流KEY（一秒一个）
local current = tonumber(redis.call('get', key) or "0")
if current<2 then --如果当前值小于2的话，则删除当前KEY,重新计数。
     redis.call("DEL", key)
    return 1
else --请求数-1
    redis.call("DECRBY", key,"1")
    return 1
end
```

## 参考
- [高并发系统限流-漏桶算法和令牌桶算法](https://www.cnblogs.com/xuwc/p/9123078.html)