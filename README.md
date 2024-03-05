# TCP/UDP 设备模拟器

该项目可用于Iot行业的TCP/UDP设备模拟，主要用来做压力测试

> 如果业务上不需要区分十进制和十六进制，可以直接使用`ascii`，它的编解码器兼容十六进制指令的接收和发送，`TCP`如果也用`ascii`发送十六进制，末尾就必须添加`0d0a`

## 技术架构

1. OpenJDK 21
2. SpringBoot 3.2.2
3. Netty 4.1.105.Final

## 功能实现

- [x] TCP设备批量模拟(十进制)
- [x] TCP设备批量模拟(十六进制)
- [x] UDP设备批量模拟(十进制)
- [x] UDP设备批量模拟(十六进制)

## 粘包/拆包问题解决方案

- TCP协议十进制解析规则：通过指定分割符`\n`或者`\r\n`来解析指令
- TCP协议十六进制解析规则：通过指定分割符`0d0a`来解析指令
- UDP协议无需处理

## 项目启动前提及教程

1. 每个路由地址需要有一台路由器支持
2. 每一个模拟地址都不能被其他程序占用
3. 满足1,2的条件后，设置本机网络地址

```shell
IP:192.168.1.2
MASK:255.255.255.0
GATEWAY:192.168.1.1
DNS:192.168.1.1
```
4. 添加其他地址，管理员打开CMD运行如下win脚本(如果需要删除批量添加的地址，只需要设置一下自动获取IP即可)

```shell
# (3,1,254) 代表192.168.1.3~192.168.1.254 第二个数值1代表步长
# "以太网" 代表网卡名称
FOR /L %I IN (3,1,254) DO netsh interface ip add address "以太网" 192.168.1.%I 255.255.255.0
```

5. 模拟超过一个路由器数量的设备，就需要多台路由器支持，具体网络配置自行研究，这里不做过多描述

## 指令配置说明

### 自定义指令设置数据举例(asciiCommandSetup.json)

```json
{
  "AT+System=On": {
    "AT+System?": "AT+System#On"
  },
  "AT+System=Off": {
    "AT+System?": "AT+System#On",
    "AT+LightSource?": "AT+LightSource#Off"
  },
  "AT+LightSource=On": {
    "AT+LightSource?": "AT+LightSource#On"
  },
  "AT+LightSource=Off": {
    "AT+LightSource?": "AT+LightSource#Off"
  }
}
```

如上`JSON`显示，此`JSON`用于修改机器响应

例一：客户端发送开机指令`AT+System=On`，就相当于设置了【开机】查询的指令，即对应的修改为`"AT+System?": "AT+System#On"`

例二：客户端发送开机指令`AT+System=Off`，就相当于设置了【关机】查询的指令，即对应的修改为`"AT+System?": "AT+System#On"`和`"AT+LightSource?": "AT+LightSource#Off"`

### 自定义指令返回数据(asciiData.json)

```json
{
  "AT+System?": "AT+System#Off",
  "AT+System=On": "AT+System#Ok",
  "AT+System=Off": "AT+System#Ok",
  "AT+LightSource?": "AT+LightSource#Off",
  "AT+LightSource=On": "AT+LightSource#Ok",
  "AT+LightSource=Off": "AT+LightSource#Ok"
}
```

如上`JSON`显示，此`JSON`用于机器立马响应

例一：客户端发送开机查询指令`AT+System?`，机器立马响应`AT+System#Off`，表示机器目前属于关机状态

例二：客户端发送开机指令`AT+System=On`，机器立马响应`AT+System#Ok`，表示机器目前一直设置成功

## 打包后运行目录结构

```
|-DeviceSimulator/                  -- 模拟器目录
|--|-data/                          -- 数据目录
|--|--|-jre/                        -- 运行环境目录
|--|--|-asciiCommandSetup.json      -- 十进制指令设置数据
|--|--|-asciiRespData.json          -- 十进制指令返回数据
|--|--|-hexCommandSetup.json        -- 十六进制指令设置数据
|--|--|-hexRespData.json            -- 十六进制指令返回数据
|--|-run.bat                        -- 运行脚本(双击即可运行)
|--|-DeviceSimulator-1.0.0.jar      -- 运行所需jar包
```

1. run.bat脚本

```shell
".\data\jre\bin\java.exe" -jar "DeviceSimulator-1.0.0.jar"
cmd.exe
```

2. 必须使用jre21及以上版本，下载地址：`https://www.azul.com/downloads/#downloads-table-zulu`
