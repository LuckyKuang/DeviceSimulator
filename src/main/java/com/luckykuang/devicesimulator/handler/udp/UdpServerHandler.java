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

package com.luckykuang.devicesimulator.handler.udp;

import com.luckykuang.devicesimulator.entity.UdpMessageResp;
import com.luckykuang.devicesimulator.util.UdpUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.luckykuang.devicesimulator.util.UdpUtils.UDP_DEVICE_CACHE;


/**
 * @author luckykuang
 * @date 2024/2/19 16:01
 */
@Slf4j
@ChannelHandler.Sharable
public class UdpServerHandler extends SimpleChannelInboundHandler<UdpMessageResp> {
    private final String ip;
    private final String codec;
    public UdpServerHandler(String ip,String codec) {
        this.ip = ip;
        this.codec = codec;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, UdpMessageResp resp) throws Exception {
        String respIp = resp.getIp();
        Integer port = resp.getPort();
        String msg = resp.getData();
        Map<String, String> execCache = UDP_DEVICE_CACHE.get(ip);
        for (Map.Entry<String, String> entry : execCache.entrySet()) {
            String exec = entry.getKey();
            String execResp = entry.getValue();
            if (UdpUtils.clientResp(ctx, msg, exec, execResp, respIp, port, execCache, ip, codec)) break;
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        String clientIp = UdpUtils.getClientIp(ctx);
        log.info("udp clientIp:{} connect...",clientIp);
        ctx.channel().writeAndFlush("connect success...");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        log.info("udp disconnect...");
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        log.error("udp exception",cause);
        ctx.close();
    }
}
