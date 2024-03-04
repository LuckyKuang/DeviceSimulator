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

package com.luckykuang.devicesimulator.server;

import com.luckykuang.devicesimulator.config.TcpConfig;
import com.luckykuang.devicesimulator.config.UdpConfig;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.luckykuang.devicesimulator.util.TcpUtils.startTcpServer;
import static com.luckykuang.devicesimulator.util.UdpUtils.startUdpServer;

/**
 * @author luckykuang
 * @date 2024/2/23 17:52
 */
@Slf4j
@Component
@AllArgsConstructor
public class InitServer {
    private static final EventLoopGroup tcpBossGroup = new NioEventLoopGroup();
    private static final EventLoopGroup tcpWorkerGroup = new NioEventLoopGroup();
    private final TcpConfig tcpConfig;
    private static final EventLoopGroup udpWorkerGroup = new NioEventLoopGroup();
    private final UdpConfig udpConfig;

    @PostConstruct
    public void init() {
        try(ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor()) {
            // tcp device emulation
            if (Boolean.TRUE.equals(tcpConfig.getEnable())) {
                for (int i = tcpConfig.getStartRouterIp(); i <= tcpConfig.getEndRouterIp(); i++) {
                    for (int j = tcpConfig.getStartIp(); j <= tcpConfig.getEndIp(); j++) {
                        String ip = tcpConfig.getIpPrefix() + "." + i + "." + j;
                        executorService.execute(() -> startTcpServer(tcpBossGroup, tcpWorkerGroup, ip, tcpConfig.getPort(),
                                tcpConfig.getCodec()));
                    }
                }
            }
            // udp device emulation
            if (Boolean.TRUE.equals(udpConfig.getEnable())) {
                for (int i = udpConfig.getStartRouterIp(); i <= udpConfig.getEndRouterIp(); i++) {
                    for (int j = udpConfig.getStartIp(); j <= udpConfig.getEndIp(); j++) {
                        String ip = udpConfig.getIpPrefix() + "." + i + "." + j;
                        executorService.execute(() -> startUdpServer(udpWorkerGroup, ip, udpConfig.getPort(),
                                udpConfig.getCodec()));
                    }
                }
            }
        }
    }

    @PreDestroy
    private void destroy(){
        tcpBossGroup.shutdownGracefully();
        tcpWorkerGroup.shutdownGracefully();
        udpWorkerGroup.shutdownGracefully();
    }
}
