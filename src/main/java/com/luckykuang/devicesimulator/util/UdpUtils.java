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

    public static void startUdpServer(EventLoopGroup workerGroup, String ip, Integer port) {
        try {
            UDP_DEVICE_CACHE.put(ip,getExecCache(ip));
            Bootstrap serverBootstrap = new Bootstrap();
            serverBootstrap.group(workerGroup);
            serverBootstrap.channel(NioDatagramChannel.class);
            serverBootstrap.option(ChannelOption.SO_BROADCAST, true);
            serverBootstrap.option(ChannelOption.SO_REUSEADDR,true);
            serverBootstrap.handler(new UdpServeInitializer(ip));
            ChannelFuture channelFuture = serverBootstrap.bind(ip,port).sync();
            log.info("udp server start success, ip:{}, port:{}",ip,port);
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e) {
            log.error("udp server start exception",e);
            workerGroup.shutdownGracefully();
        }
    }

    public static DatagramPacket getDatagramPacket(String data,String ip,Integer port){
        byte[] bytes = data.getBytes(CharsetUtil.US_ASCII);
        ByteBuf byteBuf = Unpooled.copiedBuffer(bytes);
        InetSocketAddress address = new InetSocketAddress(ip,port);
        return new DatagramPacket(byteBuf, address);
    }

    public static String getClientIp(ChannelHandlerContext ctx) {
        SocketAddress socketAddress = ctx.pipeline().channel().localAddress();
        return socketAddress.toString();
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
}
