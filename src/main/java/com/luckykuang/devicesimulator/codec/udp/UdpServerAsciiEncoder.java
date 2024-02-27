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

package com.luckykuang.devicesimulator.codec.udp;

import com.luckykuang.devicesimulator.util.UdpUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author luckykuang
 * @date 2024/2/19 16:51
 */
@Slf4j
public class UdpServerAsciiEncoder extends MessageToMessageEncoder<DatagramPacket> {

    @Override
    protected void encode(ChannelHandlerContext ctx, DatagramPacket in, List<Object> out) throws Exception {
        try {
            String clientIp = in.recipient().getAddress().getHostAddress();
            int port = in.recipient().getPort();
            ByteBuf byteBuf = in.content();
            int readableBytes = byteBuf.readableBytes();
            if (readableBytes > 0) {
                byte[] bytes = new byte[readableBytes];
                byteBuf.readBytes(bytes);
                String send = new String(bytes, CharsetUtil.US_ASCII);
                log.info("udp encode send msg:{},ip:{},port:{}",send,clientIp,port);
                byte[] data = send.getBytes(CharsetUtil.US_ASCII);
                out.add(UdpUtils.getDatagramPacket(data,clientIp,port));
            }
        } catch (Exception e){
            log.error("udp encode exception",e);
        }
    }
}
