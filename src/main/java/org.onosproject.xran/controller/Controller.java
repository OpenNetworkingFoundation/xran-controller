/*
 * Copyright 2015-present Open Networking Laboratory
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.onosproject.xran.controller;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.sctp.SctpChannel;
import io.netty.channel.sctp.nio.NioSctpServerChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by dimitris on 7/27/17.
 */
public class Controller {
    protected static final Logger log = LoggerFactory.getLogger(Controller.class);
    protected XranDeviceAgent deviceAgent;
    protected XranHostAgent hostAgent;
    protected XranPacketProcessor packetAgent;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private ChannelFuture channel;
    private int port = 8007;
    private boolean isRunning = false;

    public void run() {
        final Controller ctrl = this;
        try {
            ServerBootstrap b = createServerBootStrap();
            b.childHandler(new ChannelInitializer<SctpChannel>() {
                @Override
                public void initChannel(SctpChannel ch) throws Exception {
                    ch.pipeline().addLast(
                            //new LoggingHandler(LogLevel.INFO),
                            new XranChannelHandler(ctrl)
                    );
                }
            });
            channel = b.bind(this.port).sync();
        } catch (Exception e) {
            log.warn(e.getMessage());
            e.printStackTrace();
        }
    }

    private ServerBootstrap createServerBootStrap() {
        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioSctpServerChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO));
        return b;
    }

    public void start(XranDeviceAgent deviceAgent, XranHostAgent hostAgent, XranControllerImpl.InternalXranPacketAgent packetAgent, int port) {
        if (isRunning && this.port != port) {
            stop();
            this.deviceAgent = deviceAgent;
            this.hostAgent = hostAgent;
            this.packetAgent = packetAgent;
            this.port = port;
            run();
        } else if (!isRunning) {
            this.deviceAgent = deviceAgent;
            this.hostAgent = hostAgent;
            this.packetAgent = packetAgent;
            this.port = port;
            run();
            isRunning = true;
        }
    }


    public void stop() {
        if (isRunning) {
            channel.channel().close();
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            isRunning = false;
        }
    }
}