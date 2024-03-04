/*
 * Copyright 2015-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.luckykuang.devicesimulator.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.luckykuang.devicesimulator.handler.tcp.TcpServeInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.luckykuang.devicesimulator.constant.Constants.*;

/**
 * @author luckykuang
 * @date 2024/2/19 16:06
 */
@Slf4j
public final class TcpUtils {
    public static final Map<String,Map<String,String>> TCP_DEVICE_CACHE = new ConcurrentHashMap<>();
    private static JSONObject asciiJsonObj;
    private static JSONObject hexJsonObj;
    private TcpUtils(){}

    static {
        String asciiFilePath = System.getProperty("user.dir") + "/data/asciiCommandSetup.json";
        String asciiJsonStr = readFileStringByFiles(asciiFilePath);
        asciiJsonObj = JSON.parseObject(asciiJsonStr);

        String hexFilePath = System.getProperty("user.dir") + "/data/hexCommandSetup.json";
        String hexJsonStr = readFileStringByFiles(hexFilePath);
        hexJsonObj = JSON.parseObject(hexJsonStr);
    }

    public static void startTcpServer(EventLoopGroup bossGroup, EventLoopGroup workerGroup, String ip, Integer port, String codec) {
        try {
            TCP_DEVICE_CACHE.put(ip,getExecCache(codec));
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            serverBootstrap.option(ChannelOption.SO_REUSEADDR,true);
            serverBootstrap.childOption(ChannelOption.TCP_NODELAY,true);
            serverBootstrap.childHandler(new TcpServeInitializer(ip,codec));
            ChannelFuture channelFuture = serverBootstrap.bind(ip,port).sync();
            log.info("tcp server start success, ip:{}, port:{}",ip,port);
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("tcp server start exception",e);
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    public static String getClientIp(ChannelHandlerContext ctx) {
        InetSocketAddress inSocket = (InetSocketAddress) ctx.channel().remoteAddress();
        return inSocket.getAddress().getHostAddress();
    }

    private static Map<String,String> getExecCache(String codec) {
        Map<String,String> execCache = new HashMap<>();
        String filePath;
        if (ASCII.equalsIgnoreCase(codec)){
            filePath = System.getProperty("user.dir") + "/data/asciiRespData.json";
        } else if (HEX.equalsIgnoreCase(codec)){
            filePath = System.getProperty("user.dir") + "/data/hexRespData.json";
        } else {
            throw new RuntimeException(UNSUPPORTED);
        }
        String jsonStr = readFileStringByFiles(filePath);
        JSONObject jsonObj = JSON.parseObject(jsonStr);
        jsonObj.forEach((key,value) -> execCache.put(key,String.valueOf(value)));
        return execCache;
    }

    public static boolean clientResp(ChannelHandlerContext ctx, String msg, String exec, String execResp,
                                     Map<String, String> execCache, String ip, String codec) {
        if (ASCII.equalsIgnoreCase(codec)){
            if (msg.equals(exec)){
                log.info("tcp ip:{},receive:{},return:{}",ip, msg, execResp);
                ctx.channel().writeAndFlush(execResp);
                asciiJsonObj.forEach((key,value) -> {
                    if (msg.equals(key)){
                        JSONObject valueObj = JSON.parseObject(String.valueOf(value));
                        valueObj.forEach((k,v) -> execCache.put(k,String.valueOf(v)));
                    }
                });
                return true;
            }
        } else if (HEX.equalsIgnoreCase(codec)) {
            if (msg.equals(exec)){
                log.info("tcp ip:{},receive:{},return:{}",ip, msg, execResp);
                ctx.channel().writeAndFlush(execResp);
                hexJsonObj.forEach((key,value) -> {
                    if (msg.equals(key)){
                        JSONObject valueObj = JSON.parseObject(String.valueOf(value));
                        valueObj.forEach((k,v) -> execCache.put(k,String.valueOf(v)));
                    }
                });
                return true;
            }
        } else {
            throw new RuntimeException(UNSUPPORTED);
        }
        return false;
    }

    private static String readFileStringByFiles(String fromFilePath){
        try {
            return Files.readString(Paths.get(fromFilePath));
        } catch (IOException e){
            log.error("readFileStringByFiles exception",e);
            return null;
        }
    }
}
