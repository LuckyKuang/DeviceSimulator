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

import com.luckykuang.devicesimulator.util.TcpUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * @author luckykuang
 * @date 2024/2/19 17:13
 */
@Slf4j
public class TcpServerAsciiDecoder extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        try {
            int readableBytes = in.readableBytes();
            if (readableBytes > 0) {
                byte[] bytes = new byte[readableBytes];
                in.readBytes(bytes);
                String received = new String(bytes, CharsetUtil.US_ASCII);
                String clientIp = TcpUtils.getClientIp(ctx);
                log.info("tcp decode received clientIp:{},msg:{}",clientIp,received);
                out.add(received);
            }
        } catch (Exception e){
            log.error("tcp decode exception",e);
        }
    }
}
