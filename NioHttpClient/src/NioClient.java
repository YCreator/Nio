import java.net.URL;

import org.apache.commons.codec.binary.Base64;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequestEncoder;
import io.netty.handler.codec.http.HttpResponseDecoder;
import io.netty.handler.codec.http.HttpVersion;


public class NioClient {
	
	public void connect(String host, int port) throws Exception {
		
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			Bootstrap b = new Bootstrap();
			b.group(group);
			b.channel(NioSocketChannel.class);
			b.option(ChannelOption.SO_KEEPALIVE, true);
			b.handler(new ChannelInitializer<SocketChannel>() {
	
				@Override
				protected void initChannel(SocketChannel ch) throws Exception {
					 // 客户端接收到的是httpResponse响应，所以要使用HttpResponseDecoder进行解码
	                ch.pipeline().addLast(new HttpResponseDecoder());
	                // 客户端发送的是httprequest，所以要使用HttpRequestEncoder进行编码
	                ch.pipeline().addLast(new HttpRequestEncoder());
	                ch.pipeline().addLast(new HttpClientInboundHandler());
				}
			});
			
			// Start the client.
	        ChannelFuture f = b.connect(host, port).sync();
	      //https://item.taobao.com/item.htm?id=540525125783
	        for (int i = 0; i < 10; i++) {
	        	System.out.print(i+"");
		        URL uri = new URL("https://item.taobao.com/item.htm?id=540525125783");
		       // String msg = "Are you ok?";
		        String msg = "https://item.taobao.com/item.htm?id=540525125783";
	            DefaultFullHttpRequest request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.GET,
	                    uri.toString(), Unpooled.wrappedBuffer(Base64.encodeBase64(msg.getBytes("UTF-8"))));
	
	            // 构建http请求
	            request.headers().set(HttpHeaders.Names.HOST, host);
	            request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
	            request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, request.content().readableBytes());
	            // 发送http请求
	            f.channel().write(request);
	            f.channel().flush();
	            
	        }
	        f.channel().closeFuture().sync();
		}finally {
			group.shutdownGracefully();
		}
	}
	
	public static void main(String[] args) throws Exception {
		NioClient client = new NioClient();
	    client.connect("127.0.0.1", 8845);    
	}

}
