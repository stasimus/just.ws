package com.scalaloader.ws

import io.netty.handler.codec.http.websocketx.{CloseWebSocketFrame, PongWebSocketFrame, TextWebSocketFrame, WebSocketClientHandshaker}
import io.netty.channel.{ChannelHandlerContext, SimpleChannelInboundHandler}
import io.netty.handler.codec.http.FullHttpResponse
import io.netty.util.CharsetUtil


/**
 * User: stas
 * Date: 06/08/13, 10:40
 */
private class WebSocketClientHandler(handshaker: WebSocketClientHandshaker, client: WebSocketClient)
  extends SimpleChannelInboundHandler[AnyRef] {

  def channelRead0(ctx: ChannelHandlerContext, msg: AnyRef): Unit = msg match {
    case msg: FullHttpResponse if !handshaker.isHandshakeComplete =>
      handshaker.finishHandshake(ctx.channel, msg)
      client.handler(Connected(client))
    case m: FullHttpResponse =>
      client.handler(Error(client, new IllegalStateException(
        s"Unexpected FullHttpResponse (getStatus=${m.getStatus}, content=${m.content.toString(CharsetUtil.UTF_8)}")))
    case m if !handshaker.isHandshakeComplete =>
      client.handler(Error(client, new IllegalStateException(
        s"Unexpected FullHttpResponse (content=$m")))
    case f: TextWebSocketFrame =>
      client.handler(TextMessage(client, f.text))
    case _: PongWebSocketFrame =>
    case _: CloseWebSocketFrame => ctx.channel.close
  }

  override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable): Unit =
    if (!handshaker.isHandshakeComplete)
      client.handler(ConnectionFailed(client, Some(cause)))
    else
      client.handler(Error(client, cause))

  override def handlerAdded(ctx: ChannelHandlerContext): Unit = client.handler(Connecting)

  override def channelActive(ctx: ChannelHandlerContext): Unit = handshaker.handshake(ctx.channel)

  override def channelInactive(ctx: ChannelHandlerContext): Unit = client.handler(Disconnected(client))
}
