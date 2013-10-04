package com.scalaloader.ws

import io.netty.handler.codec.http.websocketx.{TextWebSocketFrame, WebSocketFrame}
import io.netty.channel.{ChannelFutureListener, ChannelFuture}

/**
 * User: stas
 * Date: 06/08/13, 10:29
 */
sealed trait WebSocketClientMessage

case object Connecting extends WebSocketClientMessage

case class ConnectionFailed(client: WebSocketClient, reason: Option[Throwable] = None) extends WebSocketClientMessage

case class Connected(client: WebSocketClient) extends WebSocketClientMessage

case class TextMessage(client: WebSocketClient, text: String) extends WebSocketClientMessage

case class WriteFailed(client: WebSocketClient, message: String, reason: Option[Throwable]) extends WebSocketClientMessage

case object Disconnecting extends WebSocketClientMessage

case class Disconnected(client: WebSocketClient, reason: Option[Throwable] = None) extends WebSocketClientMessage

case class Error(client: WebSocketClient, th: Throwable) extends WebSocketClientMessage

object WebSocketClient {
  type Handler = PartialFunction[AnyRef, Unit]
  type FrameReader = WebSocketFrame => String

  val defaultFrameReader = (_: WebSocketFrame) match {
    case f: TextWebSocketFrame => f.text
    case _ => throw new UnsupportedOperationException("Only single text frames are supported.")
  }

  def futureListener(handleWith: ChannelFuture => Unit) = new ChannelFutureListener {
    def operationComplete(future: ChannelFuture) {
      handleWith(future)
    }
  }
}

trait WebSocketClient {

  def reader: WebSocketClient.FrameReader

  def handler: WebSocketClient.Handler

  def connect: ChannelFuture

  def disconnect()

  def send(message: String)
}
