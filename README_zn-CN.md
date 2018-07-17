# HTTP downloader RESTful server
[English](https://github.com/proxyee-down-org/pdown-rest/blob/master/README.md)

## 编译
```
git clone git@github.com:proxyee-down-org/pdown-rest.git
cd pdown-rest
mvn clean package -Dmaven.test.skip=true -Pexec
```

## 启动

启动服务需要指定根目录,若为空时会默认使用程序运行所在的目录。  

```
// 使用默认目录
java -jar pdown-rest.jar

// 使用指定的目录
java -jar pdown-rest.jar -b=f:/down/rest
```

## 接口文档

[https://proxyee-down-org.github.io/pdown-rest-doc](https://proxyee-down-org.github.io/pdown-rest-doc)


