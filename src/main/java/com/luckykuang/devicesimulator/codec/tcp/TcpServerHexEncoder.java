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

package com.luckykuang.devicesimulator.codec.tcp;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;

/**
 * @author luckykuang
 * @date 2024/2/19 16:51
 */
@Slf4j
public class TcpServerHexEncoder extends MessageToByteEncoder<String> {
    @Override
    protected void encode(ChannelHandlerContext ctx, String in, ByteBuf out) throws Exception {
        try {
            log.info("tcp encode send msg:[{}]",in);
            byte[] bytes = Hex.decodeHex(in.toLowerCase());
            out.writeBytes(bytes);
        } catch (Exception e){
            log.error("tcp encode exception",e);
        }
    }
}
