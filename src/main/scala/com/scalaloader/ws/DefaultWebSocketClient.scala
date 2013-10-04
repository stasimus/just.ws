package com.scalaloader.ws

import io.netty.handler.codec.http.websocketx.{TextWebSocketFrame, CloseWebSocketFrame, WebSocketClientHandshaker}
import io.netty.channel.Channel
import io.netty.bootstrap.Bootstrap
import WebSocketClient._

/**
 * User: stas
 * Date: 06/08/13, 11:05
 */
private[ws] class DefaultWebSocketClient(val bootstrap: Bootstrap,
                                         val handShaker: WebSocketClientHandshaker,
                                         val reader: FrameReader = defaultFrameReader)
                                        (val payloadHandler: WebSocketClient.Handler) extends WebSocketClient {
  var channel: Option[Channel] = None

  val handler: WebSocketClient.Handler = payloadHandler orElse { case message => println(s"Unhandled message $message") }

  def connect = bootstrap
    .connect()
    .addListener (futureListener {
      future =>
        if (future.isSuccess) channel = Some(future.channel) else handler(ConnectionFailed(this, Option(future.cause)))
    })

  def disconnect = channel.filter(_.isOpen).foreach {
    c =>
      handler(Disconnecting)
      c.writeAndFlush(new CloseWebSocketFrame())
  }

  def send(message: String) = channel
      .getOrElse(throw new IllegalStateException("WS is closed"))
      .writeAndFlush(new TextWebSocketFrame(message))
      .addListener(futureListener {
        fut =>
          if (!fut.isSuccess)
            handler(WriteFailed(this, message, Option(fut.cause)))
      })
}
