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

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author luckykuang
 * @date 2024/2/19 16:06
 */
@Slf4j
public final class UdpUtils {
    public static final Map<String,Map<String,String>> UDP_DEVICE_CACHE = new ConcurrentHashMap<>();
    private UdpUtils(){}

    public static void startUdpServer(EventLoopGroup workerGroup, String ip, Integer port, String codec) {
        try {
            if ("ascii".equalsIgnoreCase(codec)){
                UDP_DEVICE_CACHE.put(ip,getAsciiExecCache());
            } else if ("hex".equalsIgnoreCase(codec)){
                UDP_DEVICE_CACHE.put(ip,getHexExecCache());
            } else {
                throw new RuntimeException("Unsupported encoding");
            }
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

    private static Map<String,String> getAsciiExecCache() {
        Map<String,String> execCache = new HashMap<>();
        execCache.put("AT+System?\r\n","AT+System#Off\r\n");
        execCache.put("AT+System=On\r\n","AT+System#Ok\r\n");
        execCache.put("AT+System=Off\r\n","AT+System#Ok\r\n");
        execCache.put("AT+LightSource?\r\n","AT+LightSource#Off\r\n");
        execCache.put("AT+LightSource=On\r\n","AT+LightSource#Ok\r\n");
        execCache.put("AT+LightSource=Off\r\n","AT+LightSource#Ok\r\n");
        return execCache;
    }

    private static Map<String,String> getHexExecCache() {
        Map<String,String> execCache = new HashMap<>();
        execCache.put("41542b53797374656d3f0d0a","41542b53797374656d234f66660d0a");
        execCache.put("41542b53797374656d3d4f6e0d0a","41542b53797374656d234f6b0d0a");
        execCache.put("41542b53797374656d3d4f66660d0a","41542b53797374656d234f6b0d0a");
        execCache.put("41542b4c69676874536f757263653f0d0a","41542b4c69676874536f75726365234f66660d0a");
        execCache.put("41542b4c69676874536f757263653d4f6e0d0a","41542b4c69676874536f75726365234f6b0d0a");
        execCache.put("41542b4c69676874536f757263653d4f66660d0a","41542b4c69676874536f75726365234f6b0d0a");
        return execCache;
    }

    public static boolean clientResp(ChannelHandlerContext ctx, String msg, String exec, String execResp, String respIp,
                                     Integer port, Map<String, String> execCache, String ip, String codec) {
        if ("ascii".equalsIgnoreCase(codec)){
            if (msg.equals(exec)){
                log.info("udp ip:{},receive:{},return:{}",ip, msg, execResp);
                byte[] bytes = execResp.getBytes(CharsetUtil.US_ASCII);
                ctx.channel().writeAndFlush(UdpUtils.getDatagramPacket(bytes, respIp, port));
                if (msg.equals("AT+System=On\r\n")){
                    execCache.put("AT+System?\r\n","AT+System#On\r\n");
                } else if (msg.equals("AT+System=Off\r\n")){
                    execCache.put("AT+System?\r\n","AT+System#Off\r\n");
                    execCache.put("AT+LightSource?\r\n","AT+LightSource#Off\r\n");
                } else if (msg.equals("AT+LightSource=On\r\n")){
                    execCache.put("AT+LightSource?\r\n","AT+LightSource#On\r\n");
                } else if (msg.equals("AT+LightSource=Off\r\n")){
                    execCache.put("AT+LightSource?\r\n","AT+LightSource#Off\r\n");
                }
                return true;
            }
        } else if ("hex".equalsIgnoreCase(codec)) {
            if (msg.equals(exec)){
                log.info("udp ip:{},receive:{},return:{}",ip, msg, execResp);
                byte[] bytes = execResp.getBytes(CharsetUtil.US_ASCII);
                ctx.channel().writeAndFlush(UdpUtils.getDatagramPacket(bytes, respIp, port));
                if (msg.equals("41542b53797374656d3d4f6e0d0a")){
                    execCache.put("41542b53797374656d3f0d0a","41542b53797374656d234f6e0d0a");
                } else if (msg.equals("41542b53797374656d3d4f66660d0a")){
                    execCache.put("41542b53797374656d3f0d0a","41542b53797374656d234f66660d0a");
                    execCache.put("41542b4c69676874536f757263653f0d0a","41542b4c69676874536f75726365234f66660d0a");
                } else if (msg.equals("41542b4c69676874536f757263653d4f6e0d0a")){
                    execCache.put("41542b4c69676874536f757263653f0d0a","41542b4c69676874536f75726365234f6e0d0a");
                } else if (msg.equals("41542b4c69676874536f757263653d4f66660d0a")){
                    execCache.put("41542b4c69676874536f757263653f0d0a","41542b4c69676874536f75726365234f66660d0a");
                }
                return true;
            }
        } else {
            throw new RuntimeException("Unsupported encoding");
        }
        return false;
    }
}
