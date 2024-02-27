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

import com.luckykuang.devicesimulator.handler.tcp.TcpServeInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
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
    private TcpUtils(){}

    public static void startTcpServer(EventLoopGroup bossGroup, EventLoopGroup workerGroup, String ip, Integer port, String codec) {
        try {
            if (ASCII.equalsIgnoreCase(codec)){
                TCP_DEVICE_CACHE.put(ip,getAsciiExecCache());
            } else if (HEX.equalsIgnoreCase(codec)){
                TCP_DEVICE_CACHE.put(ip,getHexExecCache());
            } else {
                throw new RuntimeException(UNSUPPORTED);
            }
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

    private static Map<String,String> getAsciiExecCache() {
        Map<String,String> execCache = new HashMap<>();
        execCache.put("AT+System?","AT+System#Off");
        execCache.put("AT+System=On","AT+System#Ok");
        execCache.put("AT+System=Off","AT+System#Ok");
        execCache.put("AT+LightSource?","AT+LightSource#Off");
        execCache.put("AT+LightSource=On","AT+LightSource#Ok");
        execCache.put("AT+LightSource=Off","AT+LightSource#Ok");
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

    public static boolean clientResp(ChannelHandlerContext ctx, String msg, String exec, String execResp,
                                     Map<String, String> execCache, String ip, String codec) {
        if (ASCII.equalsIgnoreCase(codec)){
            if (msg.equals(exec)){
                log.info("tcp ip:{},receive:{},return:{}",ip, msg, execResp);
                ctx.channel().writeAndFlush(execResp);
                if (msg.equals("AT+System=On")){
                    execCache.put("AT+System?","AT+System#On");
                } else if (msg.equals("AT+System=Off")){
                    execCache.put("AT+System?","AT+System#Off");
                    execCache.put("AT+LightSource?","AT+LightSource#Off");
                } else if (msg.equals("AT+LightSource=On")){
                    execCache.put("AT+LightSource?","AT+LightSource#On");
                } else if (msg.equals("AT+LightSource=Off")){
                    execCache.put("AT+LightSource?","AT+LightSource#Off");
                }
                return true;
            }
        } else if (HEX.equalsIgnoreCase(codec)) {
            if (msg.equals(exec)){
                log.info("tcp ip:{},receive:{},return:{}",ip, msg, execResp);
                ctx.channel().writeAndFlush(execResp);
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
            throw new RuntimeException(UNSUPPORTED);
        }
        return false;
    }
}
