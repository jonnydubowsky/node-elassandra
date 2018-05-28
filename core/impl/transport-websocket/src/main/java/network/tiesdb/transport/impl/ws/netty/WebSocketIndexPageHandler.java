/*
 * Copyright 2012 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package network.tiesdb.transport.impl.ws.netty;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.INTERNAL_SERVER_ERROR;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.File;
import java.net.URL;
import java.util.HashMap;

/**
 * Outputs index page content.
 */
public class WebSocketIndexPageHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private final String websocketPath;
    private final URL staticResourcesUrl = getClass().getClassLoader().getResource("static/");

    public WebSocketIndexPageHandler(String websocketPath) {
        this.websocketPath = websocketPath;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest req) throws Exception {
        // Handle a bad request.
        if (!req.getDecoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        // Allow only GET methods.
        if (req.getMethod() != GET) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, METHOD_NOT_ALLOWED));
            return;
        }

        // Find resource file
        String requestUriStr = req.getUri();
        if (null == requestUriStr || requestUriStr.isEmpty() || "/".equals(requestUriStr)) {
            requestUriStr = "/index.html";
        }
        requestUriStr = requestUriStr.substring(1);

        File resourceFile = new File(staticResourcesUrl.toURI().resolve(requestUriStr));
        if (!resourceFile.exists() || !resourceFile.isFile()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
            return;
        }

        // Send the resource data
        String resourceFileExtension = getFileExtension(resourceFile);
        ByteBuf content = null;
        if ("html".equals(resourceFileExtension)) {
            HashMap<String, Object> templateMap = new HashMap<>();
            templateMap.put("websocketLocation", getWebSocketLocation(ctx.pipeline(), req, websocketPath));
            content = WebSocketServerIndexPage.getContent(resourceFile, templateMap);
        } else {
            content = WebSocketServerIndexPage.getContent(resourceFile);
        }
        if (null == content) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, INTERNAL_SERVER_ERROR));
            return;
        }
        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);

        switch (resourceFileExtension) {
        case "html":
            res.headers().set(HttpHeaders.Names.CONTENT_TYPE, "text/html; charset=UTF-8");
            break;
        case "js":
            res.headers().set(HttpHeaders.Names.CONTENT_TYPE, "application/javascript; charset=utf-8");
            break;
        default:
            break;
        }
        HttpHeaders.setContentLength(res, content.readableBytes());

        sendHttpResponse(ctx, req, res);
    }

    private String getFileExtension(File resourceFile) {
        int i = resourceFile.getName().lastIndexOf('.');
        if (i > 0) {
            return resourceFile.getName().substring(i + 1);
        }
        return "";
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpHeaders.setContentLength(res, res.content().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static String getWebSocketLocation(ChannelPipeline cp, HttpRequest req, String path) {
        String protocol = "ws";
        if (null != cp.get(SslHandler.class)) {
            // SSL in use so use Secure WebSockets
            protocol = "wss";
        }
        return protocol + "://" + req.headers().get(HttpHeaders.Names.HOST) + path;
    }
}
