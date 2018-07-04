### 1.REDIS相关操作
```
Redis操作字符串工具类封装：http://fanshuyao.iteye.com/blog/2326221
Redis操作Hash工具类封装：http://fanshuyao.iteye.com/blog/2327134
Redis操作List工具类封装：http://fanshuyao.iteye.com/blog/2327137
Redis操作Set工具类封装：http://fanshuyao.iteye.com/blog/2327228
Redis操作SortedSet工具类封装：https://blog.csdn.net/a1031397017/article/details/69484193
java 操作Redis SortedSet 命令 实例（Jedis）：https://blog.csdn.net/a1031397017/article/details/69484193
```
### 2.KEY作用对应表
KEY |类型|作用
---|---|---
EvictAllKeyList | List| 删除旧的资源时间戳版本对应的所有缓存
ExpireAllKeySet | Set| 保证所有的SaveAllKeySet都设置了过期时间
SaveAllKeySet | Set| 保存资源时间戳版本对应的所有缓存
P01.Timestamp:userId:1000| String| 私有资源时间戳版本
P01.UserBaseInfo.1000:1520ben35yo3y:XX| String| 私有资源缓存KEY的名称
P01.Timestamp:publicNormal| String| 公有资源时间戳版本
P01.Other1SelectAll:1dben35yo3y8:XX| String| 公有资源缓存KEY的名称
### 3.Redis删除所有Key
```
删除所有Key
删除所有Key，可以使用Redis的flushdb和flushall命令
//删除当前数据库中的所有Key
flushdb
//删除所有数据库中的key
flushall
注：keys 指令可以进行模糊匹配，但如果 Key 含空格，就匹配不到了，暂时还没发现好的解决办法。
http://ssuupv.blog.163.com/blog/static/1461567220135610456193/
```