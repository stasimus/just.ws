package com.scalaloader.ws

import java.net.URI
import javax.net.ssl.SSLContext
import io.netty.handler.codec.http.websocketx.{WebSocketClientHandshakerFactory, WebSocketVersion}
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.bootstrap.Bootstrap
import io.netty.handler.codec.http.{HttpObjectAggregator, HttpClientCodec, DefaultHttpHeaders, HttpHeaders}
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.channel.ChannelInitializer
import io.netty.handler.ssl.SslHandler

/**
 * User: stas
 * Date: 25/09/13, 12:13
 */
case class WebSocketClientFactory(maxWorkersCount: Int,
                                  sslContext: Option[() => SSLContext] = None) {

  val group = new NioEventLoopGroup(maxWorkersCount)

  def newClient(url: URI, initialHeaders: Map[String, String] = Map.empty[String, String])
               (handler: WebSocketClient.Handler): WebSocketClient = {
    val tgt = buildUri(url)

    val bootstrap = new Bootstrap().remoteAddress(tgt.getHost, tgt.getPort)
    val clientHandshaker = WebSocketClientHandshakerFactory.newHandshaker(tgt, WebSocketVersion.V08, null, false, initialHeaders)
    val client = new DefaultWebSocketClient(bootstrap, clientHandshaker)(handler)
    val clientHandler = new WebSocketClientHandler(clientHandshaker, client)

    bootstrap
      .group(group)
      .channel(classOf[NioSocketChannel])
      .handler(new ChannelInitializer[NioSocketChannel]() {
        def initChannel(ch: NioSocketChannel) {
          val pipeline = ch.pipeline()

          sslContext.map(_()).foreach {
            sslContext =>
              val sslEngine = sslContext.createSSLEngine
              sslEngine.setUseClientMode(true)
              pipeline.addLast("ssl", new SslHandler(sslEngine))
          }

          pipeline.addLast("http-codec", new HttpClientCodec())
          pipeline.addLast("aggregator", new HttpObjectAggregator(8192))
          pipeline.addLast("ws-handler", clientHandler)
        }
      })

    client
  }

  def shutdownAll() = group.shutdownGracefully().get()

  private[this] def buildUri(url: URI): URI = {
    val normalized = url.normalize

    if (normalized.getPath == null || normalized.getPath.trim.isEmpty) {
      new URI(normalized.getScheme, normalized.getAuthority, "/", normalized.getQuery, normalized.getFragment)
    } else {
      normalized
    }
  }

  private[this] implicit def mapToHeader(m: Map[String, String]): HttpHeaders =
    m.foldLeft((new DefaultHttpHeaders).asInstanceOf[HttpHeaders]) {
      case (result, (k, v)) => result.add(k, v)
    }
}