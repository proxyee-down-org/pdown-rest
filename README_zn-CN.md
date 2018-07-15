# HTTP downloader RESTful server

HTTP下载服务器，可以通过RESTful的方式对下载器进行调用.

## 启动

启动服务需要指定根路径,若为空时会默认使用程序运行所在目录。  
当服务启动成功时会读取根路径里的`config.inf`文件，里面包含服务器基本的配置信息，若不存在的话会使用默认值。

```
// 使用默认目录，默认目录为当前程序所在目录
DownRestServer.start(null);

// 使用指定的目录
DownRestServer.start("f:/down/rest");
```

## 接口文档

[https://proxyee-down-org.github.io/pdown-rest-doc](https://proxyee-down-org.github.io/pdown-rest-doc)


