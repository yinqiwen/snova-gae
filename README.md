本项目是[Snova](http://code.google.com/p/snova/)的Google Appengine服务端的Java/Go实现， 可以部署到Google Appengine平台上。

部署服务端
--------
> 服务端分发包可在googlecode上下载
> 部署到Google Appengine上参考[GAEInstallation](http://code.google.com/p/snova/wiki/GAEInstallation)
>[下载](http://code.google.com/p/snova/downloads/list)


安装客户端
--------
>客户端为snova/gsnova zip包，解压即可；目前预编译支持的有Windows（32/64位）， Linux（64位），Mac（64位）。   
用户按照[配置]一节修改配置后，即可启动gsnova。 windows用户直接执行gsnova.exe即可，Linux/Mac用户需要在命令行下启动gsnova程序。   
用户还需要修改浏览器的代理地址为127.0.0.1:48100， 或者在支持PAC设置的浏览器中设置PAC地址为http://127.0.0.1:48100/pac/gfwlist       
[下载](https://github.com/yinqiwen/gsnova/downloads)

配置
-------
主要需要修改gsnova.conf(Snova用户类似修改过程) 

#####GAE 
修改gsnova.conf中[GAE]以下部分，默认Enable为1，关闭需要修改Enable为0：   

    [GAE]   
    Enable=1   
    WorkerNode[0]=myappid   
将申请的域名填入WorkerNode[0]=后，注意必须为域名中除".appspot.com"部分，且在配置前请确保在浏览器中输入此域名能看到‘snova’相关信息（证明部署服务端成功且能直接访问）。若有多个，可以如下配置多个：

    [C4]   
    Enable=1   
    WorkerNode[0]=myapp1
    WorkerNode[1]=myapp2

此Proxy实现在SPAC中名称为GAE, 若想只用GAE作为唯一的Proxy实现，修改[SPAC]下的Default值为GAE


其它
-------
参考[Snova](http://code.google.com/p/snova/)有一些其他相关信息