import static io.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_LENGTH;
import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.Gson;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.HttpHeaders.Values;

public class BlogServerInboundHandler extends ChannelInboundHandlerAdapter {

	private HttpRequest request;

	private Map<Integer, String> map = new HashMap<Integer, String>();

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg)
			throws Exception {
		if (msg instanceof HttpRequest) {
			request = (HttpRequest) msg;
			String uri = request.getUri();
			String htmlurl = "";
			if (!uri.equals("/favicon.ico")) {

				QueryStringDecoder decoderQuery = new QueryStringDecoder(
						request.getUri());
				Map<String, List<String>> uriAttributes = decoderQuery
						.parameters();
				for (Entry<String, List<String>> attr : uriAttributes
						.entrySet()) {
					for (String attrVal : attr.getValue()) {
						if (attr.getKey().equals("url")) {
							htmlurl = attrVal;
						}
						// System.out.println("URI: " + attr.getKey() + '=' +
						// attrVal + "\r\n");
					}
				}

				// url需要base64加密，为防止在参数过滤是将url对应链接所带参数值过滤掉
				String urls = new String(Base64.decodeBase64(htmlurl
						.getBytes("utf-8")));

				map.put(ctx.hashCode(), urls);

				System.out.println("code1:" + ctx.hashCode());
			}

		}

		if (msg instanceof HttpContent) {
			HttpContent content = (HttpContent) msg;
			ByteBuf buf = content.content();
			String newUrl = buf.toString(io.netty.util.CharsetUtil.UTF_8);
			System.out.println(newUrl);
			buf.release();

			String res = "";
			String urlstr = "";
			if (!map.get(ctx.hashCode()).equals(null)) {
				urlstr = map.get(ctx.hashCode()).toString();
			}
			System.out.println(urlstr);
			map.remove(ctx.hashCode());

			res = get(urlstr, false);
			Pattern pattern = Pattern.compile("<a\\starget=\"_blank\"\\shref=\".*?\"\\stitle=\".*?\">(.+?)</a>");
			Matcher matcher = pattern.matcher(res);
			List<Map<String, String>> list = new ArrayList<Map<String, String>>();
			while(matcher.find()) {
		    	String s = matcher.group();
		    	HashMap<String, String> maps = new HashMap<String, String>();
		    	Matcher m = Pattern.compile("href=\".*?\"").matcher(s);
		    	while(m.find()) {
		    		maps.put("url", m.group().replaceAll("href=\"|\">", "").replace("\"", ""));
		    	}
		    	m = Pattern.compile("src=\".*?\"").matcher(s);
		    	while(m.find()) {
		    		maps.put("picPath", m.group().replaceAll("src=\"|\">", "").replace("\"", "").replace("-140x98", ""));
		    	}
		    	maps.put("title", s.replaceAll("</?[^<]+>", "").replace("\t", ""));
		    
		    	String cont = get(maps.get("url"), true);
		    	Pattern p = Pattern.compile("<div class=\"article-entry\" id=\"article-entry\">([\\s\\S]*)<a\\sid=\"soft\\-link\"\\sname=\"soft\\-link\">");
			    Matcher ma = p.matcher(cont);
			    String ss = "";
			    while(ma.find()) {
			    	//System.out.println(matcher.group());
			    	ss = ss + ma.group();
			    }
			    maps.put("content", ss
			    		.replaceAll("<div class=\"article-entry\" id=\"article-entry\">", "")
			    		.replaceAll("<pre.*?>","<pre class=\"brush: javascript; gutter: true; first-line: 1 hljs\" style=\"margin: 15px auto; padding: 10px 15px; overflow-x: auto; color: rgb(51, 51, 51); word-break: break-all; word-wrap: break-word; white-space: pre-wrap; font-stretch: normal; font-size: 12px; line-height: 20px; font-family: &#39;courier new&#39;; border-width: 1px 1px 1px 4px; border-style: solid; border-color: rgb(221, 221, 221); background-color: rgb(251, 251, 251);\">")
			    		.replaceAll("<p>", "<p style=\"margin-top: 0px; margin-bottom: 15px; padding: 0px; color: rgb(68, 68, 68); font-family: &#39;microsoft yahei&#39;; font-size: 14px; line-height: 25px; white-space: normal;\">"));
			    String noTagContent = ss
		    			.replaceAll("<script[^>]*?>.*?</script>", "")
		    			.replaceAll("<[^>]*>", "")
		    			.replaceAll("\t", "")
		    			.replaceAll("\n", "");
		    	maps.put("contentNoTag", noTagContent);
		    	list.add(maps);
		    }
			 FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
	                 OK, Unpooled.wrappedBuffer(new Gson().toJson(list).getBytes("UTF-8")));
	         response.headers().set(CONTENT_TYPE, "text/plain");
	         response.headers().set(CONTENT_LENGTH,
	                 response.content().readableBytes());
	         if (HttpHeaders.isKeepAlive(request)) {
	             response.headers().set(CONNECTION, Values.KEEP_ALIVE);
	         }
	         ctx.write(response);
	         ctx.flush();
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		ctx.flush();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
			throws Exception {
		System.out.println(cause.getMessage());
        ctx.close();
	}

	public String get(String surl, boolean isContent) {
		HttpURLConnection conn = null;
		String res = "";
		try {
			URL url = new URL(surl);
			// 1.得到HttpURLConnection实例化对象
			conn = (HttpURLConnection) url.openConnection();
			// 2.设置请求信息（请求方式... ...）
			// 设置请求方式和响应时间
			conn.setRequestMethod("GET");
			// conn.setRequestProperty("encoding","UTF-8"); //可以指定编码
			conn.setConnectTimeout(30000);
			conn.setReadTimeout(30000);
			// 不使用缓存
			conn.setUseCaches(false);
			// 3.读取相应
			if (conn.getResponseCode() == 200) {

				InputStreamReader isr = new InputStreamReader(
						conn.getInputStream(), "utf-8");

				BufferedReader in = new BufferedReader(isr);

				String inputLine = "";

				StringBuilder sb = new StringBuilder();

				while ((inputLine = in.readLine()) != null) {
					sb.append(inputLine);
					if (isContent) {
						sb.append("\r\n");
					}	
				}

				res = sb.toString();
				isr.close();
				// System.out.println(res);
				System.out.println("[浏览器]成功！");
			} else {
				System.out.println("请求失败！");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			// 4.释放资源
			if (conn != null) {
				// 关闭连接 即设置 http.keepAlive = false;
				conn.disconnect();
				System.out.println("http.keepAlive = false;");
			}
		}
		return res;
	}

}
