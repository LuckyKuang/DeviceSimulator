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

package com.luckykuang.devicesimulator.handler.tcp;

import com.luckykuang.devicesimulator.util.TcpUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

import static com.luckykuang.devicesimulator.util.TcpUtils.TCP_DEVICE_CACHE;


/**
 * @author luckykuang
 * @date 2024/2/19 16:01
 */
@Slf4j
@ChannelHandler.Sharable
public class TcpServerHandler extends SimpleChannelInboundHandler<String> {
    private final String ip;
    public TcpServerHandler(String ip) {
        this.ip = ip;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        Map<String, String> execCache = TCP_DEVICE_CACHE.get(ip);
        for (Map.Entry<String, String> entry : execCache.entrySet()) {
            String exec = entry.getKey();
            String execRsp = entry.getValue();
            if (msg.contains(exec)){
                log.info("tcp ip:{},receive:{},return:{}",ip,msg,execRsp);
                ctx.channel().writeAndFlush(execRsp);
                if (msg.contains("AT+System=On")){
                    execCache.put("AT+System?","AT+System#On");
                } else if (msg.contains("AT+System=Off")){
                    execCache.put("AT+System?","AT+System#Off");
                    execCache.put("AT+LightSource?","AT+LightSource#Off");
                } else if (msg.contains("AT+LightSource=On")){
                    execCache.put("AT+LightSource?","AT+LightSource#On");
                } else if (msg.contains("AT+LightSource=Off")){
                    execCache.put("AT+LightSource?","AT+LightSource#Off");
                }
                break;
            }
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        String clientIp = TcpUtils.getClientIp(ctx);
        log.info("tcp clientIp: [{}] connect...",clientIp);
        ctx.channel().writeAndFlush("PJLINK");
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        String clientIp = TcpUtils.getClientIp(ctx);
        log.info("tcp clientIp: [{}] disconnect...",clientIp);
        ctx.close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        String clientIp = TcpUtils.getClientIp(ctx);
        log.error("tcp clientIp: [{}] exception", clientIp);
        ctx.close();
    }
}
