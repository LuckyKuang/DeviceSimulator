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

import com.luckykuang.devicesimulator.codec.udp.UdpServerDecoder;
import com.luckykuang.devicesimulator.codec.udp.UdpServerEncoder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioDatagramChannel;
import lombok.extern.slf4j.Slf4j;

/**
 * @author luckykuang
 * @date 2024/2/19 15:57
 */
@Slf4j
@ChannelHandler.Sharable
public class UdpServeInitializer extends ChannelInitializer<NioDatagramChannel> {
    private final String ip;
    public UdpServeInitializer(String ip) {
        this.ip = ip;
    }

    @Override
    protected void initChannel(NioDatagramChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(
                new UdpServerEncoder(),
                new UdpServerDecoder(),
                new UdpServerHandler(ip));
    }
}
