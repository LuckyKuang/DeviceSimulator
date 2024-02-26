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

/**
 * @author luckykuang
 * @date 2024/2/19 16:06
 */
@Slf4j
public final class TcpUtils {
    public static final Map<String,Map<String,String>> TCP_DEVICE_CACHE = new ConcurrentHashMap<>();
    private TcpUtils(){}

    public static void startTcpServer(EventLoopGroup bossGroup, EventLoopGroup workerGroup, String ip, Integer port) {
        try {
            TCP_DEVICE_CACHE.put(ip,getExecCache(ip));
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup);
            serverBootstrap.channel(NioServerSocketChannel.class);
            serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            serverBootstrap.option(ChannelOption.SO_REUSEADDR,true);
            serverBootstrap.childOption(ChannelOption.TCP_NODELAY,true);
            serverBootstrap.childHandler(new TcpServeInitializer(ip));
            ChannelFuture channelFuture = serverBootstrap.bind(ip,port).sync();
            log.info("tcp server start success, ip:{}, port:{}",ip,port);
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("tcp server start exception",e);
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

    private static Map<String,String> getExecCache(String ip) {
        Map<String,String> execCache = new HashMap<>();
        execCache.put("AT+System?","AT+System#Off");
        execCache.put("AT+System=On","AT+System#Ok");
        execCache.put("AT+System=Off","AT+System#Ok");
        execCache.put("AT+LightSource?","AT+LightSource#Off");
        execCache.put("AT+LightSource=On","AT+LightSource#Ok");
        execCache.put("AT+LightSource=Off","AT+LightSource#Ok");
        execCache.put("AT+Ip?","AT+Ip#" + ip);
        return execCache;
    }

    public static String getClientIp(ChannelHandlerContext ctx) {
        InetSocketAddress inSocket = (InetSocketAddress) ctx.channel().remoteAddress();
        return inSocket.getAddress().getHostAddress();
    }
}
