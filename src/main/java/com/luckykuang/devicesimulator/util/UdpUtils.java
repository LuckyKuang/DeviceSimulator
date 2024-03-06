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
import com.luckykuang.devicesimulator.handler.udp.UdpServeInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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
public final class UdpUtils {
    public static final Map<String,Map<String,String>> UDP_DEVICE_CACHE = new ConcurrentHashMap<>();
    private static JSONObject asciiJsonObj;
    private static JSONObject hexJsonObj;
    private UdpUtils(){}

    static {
        String asciiFilePath = System.getProperty("user.dir") + "/data/asciiCommandSetup.json";
        String asciiJsonStr = readFileStringByFiles(asciiFilePath);
        asciiJsonObj = JSON.parseObject(asciiJsonStr);

        String hexFilePath = System.getProperty("user.dir") + "/data/hexCommandSetup.json";
        String hexJsonStr = readFileStringByFiles(hexFilePath);
        hexJsonObj = JSON.parseObject(hexJsonStr);
    }

    public static void startUdpServer(EventLoopGroup workerGroup, String ip, Integer port, String codec) {
        try {
            UDP_DEVICE_CACHE.put(ip,getExecCache(codec));
            Bootstrap serverBootstrap = new Bootstrap();
            serverBootstrap.group(workerGroup);
            serverBootstrap.channel(NioDatagramChannel.class);
            serverBootstrap.option(ChannelOption.SO_BROADCAST, true);
            serverBootstrap.option(ChannelOption.SO_REUSEADDR,true);
            serverBootstrap.handler(new UdpServeInitializer(ip,codec));
            ChannelFuture channelFuture = serverBootstrap.bind(ip,port).sync();
            log.info("udp server start success, ip:{}, port:{}",ip,port);
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("udp server start exception",e);
            workerGroup.shutdownGracefully();
        }
    }

    public static DatagramPacket getDatagramPacket(byte[] bytes,String ip,Integer port){
        ByteBuf byteBuf = Unpooled.copiedBuffer(bytes);
        InetSocketAddress address = new InetSocketAddress(ip,port);
        return new DatagramPacket(byteBuf, address);
    }

    public static String getClientIp(ChannelHandlerContext ctx) {
        SocketAddress socketAddress = ctx.pipeline().channel().localAddress();
        return socketAddress.toString();
    }

    public static boolean clientResp(ChannelHandlerContext ctx, String msg, String exec, String execResp, String respIp,
                                     Integer port, Map<String, String> execCache, String ip, String codec) {
        if (ASCII.equalsIgnoreCase(codec)){
            if (msg.equals(exec)){
                log.info("udp ip:{},receive:{},return:{}",ip, msg, execResp);
                byte[] bytes = execResp.getBytes(CharsetUtil.US_ASCII);
                ctx.channel().writeAndFlush(UdpUtils.getDatagramPacket(bytes, respIp, port));
                asciiJsonObj.forEach((key,value) -> {
                    if (msg.equals(key)){
                        JSONObject valueObj = JSON.parseObject(String.valueOf(value));
                        valueObj.forEach((k,v) -> execCache.put(k,String.valueOf(v)));
                    }
                });
                return true;
            }
        } else if (HEX.equalsIgnoreCase(codec)) {
            if (msg.equals(exec.toLowerCase())){
                log.info("udp ip:{},receive:{},return:{}",ip, msg, execResp);
                byte[] bytes = execResp.getBytes(CharsetUtil.US_ASCII);
                ctx.channel().writeAndFlush(UdpUtils.getDatagramPacket(bytes, respIp, port));
                hexJsonObj.forEach((key,value) -> {
                    if (msg.equals(key.toLowerCase())){
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

    private static String readFileStringByFiles(String fromFilePath){
        try {
            return Files.readString(Paths.get(fromFilePath));
        } catch (IOException e){
            log.error("readFileStringByFiles exception",e);
            return null;
        }
    }
}
