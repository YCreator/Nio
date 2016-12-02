import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;

public class NioHttpServer {

	public void start(int port) throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workerGroup)
					.channel(NioServerSocketChannel.class)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch)
								throws Exception {
							// server端发送的是httpResponse，所以要使用HttpResponseEncoder进行编码
							ch.pipeline().addLast(new HttpResponseEncoder());
							// server端接收到的是httpRequest，所以要使用HttpRequestDecoder进行解码
							ch.pipeline().addLast(new HttpRequestDecoder());
							ch.pipeline().addLast(
									new BlogServerInboundHandler());
						}
					}).option(ChannelOption.SO_BACKLOG, 128)
					.childOption(ChannelOption.SO_KEEPALIVE, true);

			ChannelFuture f = b.bind(port).sync();

			f.channel().closeFuture().sync();
		} finally {
			workerGroup.shutdownGracefully();
			bossGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception {
		NioHttpServer server = new NioHttpServer();
		if (args.length < 1) {
			server.start(8844);
			System.out.println("Http Server listening on default port 8844 ...");
		} else {
			int PORT = Integer.parseInt(args[0]);
			server.start(PORT);
			System.out.println("Http Server listening on " + PORT);
		}
		// NioHttpServer server = new NioHttpServer();
		// log.info("Http Server listening on 8844 ...");
		/*
		 * if(args.length<1){ String message="prot null";
		 * System.out.println(message); return; } int
		 * PORT=Integer.parseInt(args[0]);
		 */

		/*
		 * server.start(8844);
		 * System.out.println("Http Server listening on 8844 ...");
		 */
	}
}