import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;


public class HttpClientInboundHandler extends ChannelInboundHandlerAdapter  {

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if (msg instanceof HttpResponse) 
        {
            HttpResponse response = (HttpResponse) msg;
            System.out.println("CONTENT_TYPE:" + response.headers().get(HttpHeaders.Names.CONTENT_TYPE));
        }
        if(msg instanceof HttpContent)
        {
            HttpContent content = (HttpContent)msg;
            ByteBuf buf = content.content();
            File f = new File("D:/mydetail.txt");
            StringBuilder builder = new StringBuilder();
            builder
            .append(System.currentTimeMillis())
            .append("\r\n===============\r\n")
            .append(buf.toString(io.netty.util.CharsetUtil.UTF_8))
            .append("\r\n");
            PrintStream ps = new PrintStream(new FileOutputStream(f));
            ps.println(builder);
           // System.out.println(buf.toString(io.netty.util.CharsetUtil.UTF_8));
            buf.release();
        }
	}

}
