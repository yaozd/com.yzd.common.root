### 1.REDIS相关操作
```
Redis操作字符串工具类封装：http://fanshuyao.iteye.com/blog/2326221
Redis操作Hash工具类封装：http://fanshuyao.iteye.com/blog/2327134
Redis操作List工具类封装：http://fanshuyao.iteye.com/blog/2327137
Redis操作Set工具类封装：http://fanshuyao.iteye.com/blog/2327228
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
