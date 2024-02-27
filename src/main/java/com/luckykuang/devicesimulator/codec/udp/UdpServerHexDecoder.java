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

import com.luckykuang.devicesimulator.entity.UdpMessageResp;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.DatagramPacket;
import io.netty.handler.codec.MessageToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

import java.util.List;

/**
 * @author luckykuang
 * @date 2024/2/19 17:13
 */
@Slf4j
public class UdpServerHexDecoder extends MessageToMessageDecoder<DatagramPacket> {
    @Override
    protected void decode(ChannelHandlerContext ctx, DatagramPacket in, List<Object> out) throws Exception {
        try {
            String clientIp = in.sender().getAddress().getHostAddress();
            int port = in.sender().getPort();
            ByteBuf byteBuf = in.content();
            int readableBytes = byteBuf.readableBytes();
            if (readableBytes > 0){
                byte[] bytes = new byte[readableBytes];
                byteBuf.readBytes(bytes);
                String received = Hex.encodeHexString(bytes);
                log.info("udp decode received clientIp:{},msg:{}",clientIp,received);
                out.add(new UdpMessageResp(clientIp,port,received));
            }
        } catch (Exception e){
            log.error("udp decode exception",e);
        }
    }
}
