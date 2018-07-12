# HTTP downloader RESTful server

HTTP下载服务器，可以通过RESTful的方式对下载器进行调用.

## 启动

启动服务需要指定根路径,若为空时会默认使用程序运行所在目录。  
当服务启动成功时会读取根路径里的`config.inf`文件，里面包含服务器基本的配置信息，若不存在的话会使用默认值。

```java
// 使用默认目录，默认目录为当前程序所在目录
DownRestServer.start(null);

// 使用指定的目录
DownRestServer.start("f:/down/rest");
```

## 接口文档

> 默认服务器端口为26339

### 查看服务器配置信息

#### 请求

GET http://127.0.0.1:26339/config

#### 响应

```json
{
    "data": {
        "filePath": null,
        "connections": 16,
        "timeout": 30,
        "retryCount": 5,
        "autoRename": false,
        "speedLimit": 0,
        "port": 26339,
        "taskLimit": 3,
        "totalSpeedLimit": 0,
        "proxyConfig": {
           "proxyType": "SOCKS5",
           "host": "127.0.0.1",
           "port": 1080,
           "user": null,
           "pwd": null
       }
    },
    "msg": null
}
```

参数 | 描述 
---|---
data.filePath | 默认下载路径 
data.connections | 下载连接数
data.timeout | 超时时间(S)
data.retryCount | 失败重试次数
data.autoRename | 当有重名文件时，是否自动重命名
data.speedLimit | 单个任务下载速度限制(B/S)
data.totalSpeedLimit | 下载器总的速度限制(B/S)
data.port | 服务器端口，重启后生效
data.taskLimit | 最大同时下载的任务数量
data.proxyConfig | 二级代理设置
data.proxyConfig.proxyType | 代理类型(HTTP,SOCKS4,SOCKS5)
data.proxyConfig.host | 代理地址
data.proxyConfig.port | 代理端口
data.proxyConfig.user | 代理用户名
data.proxyConfig.pwd | 代理密码
