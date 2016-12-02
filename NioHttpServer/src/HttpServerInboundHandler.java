
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.HttpHeaders.Values;
import io.netty.handler.codec.http.HttpRequest;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.Gson;

public class HttpServerInboundHandler extends ChannelInboundHandlerAdapter {

    //private static Log log = LogFactory.getLog(HttpServerInboundHandler.class);

    private HttpRequest request;
    
	private Map<Integer, String> map = new HashMap<Integer, String>();
	
	public  String get(String surl) {
		HttpURLConnection conn = null;
		String res="";
		try {
			URL url = new URL(surl);
			//1.得到HttpURLConnection实例化对象
			conn = (HttpURLConnection) url.openConnection();
			//2.设置请求信息（请求方式... ...）
			//设置请求方式和响应时间
			conn.setRequestMethod("GET");
			//conn.setRequestProperty("encoding","UTF-8"); //可以指定编码
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(5000);
			//不使用缓存
			conn.setUseCaches(false);
			//3.读取相应
			if (conn.getResponseCode() == 200) {
				
	            InputStreamReader isr = new InputStreamReader(conn.getInputStream(),"gbk");  
	            
	            BufferedReader in = new BufferedReader(isr);  
	             
	            String inputLine="";  
	             
	            StringBuilder sb = new StringBuilder();      
	           
	            while ((inputLine = in.readLine()) != null){  
	             	 sb.append(inputLine + "\n");    
	            }  
	      
	            res=sb.toString();
	            isr.close();
				//System.out.println(res);
				System.out.println("[浏览器]成功！");
			} else {
				System.out.println("请求失败！");
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
		//4.释放资源
			if (conn != null) {
				//关闭连接 即设置 http.keepAlive = false;
				conn.disconnect();
				System.out.println("http.keepAlive = false;");
			}
		}
		return res;
	}
	
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
    	
        if (msg instanceof HttpRequest) {
        	System.out.println("HttpRequest");
            request = (HttpRequest) msg;

            String uri = request.getUri();
            System.out.println("Uri:" + uri);
            String htmlurl="";
            
            if(!uri.equals("/favicon.ico")){
            	
	            QueryStringDecoder decoderQuery = new QueryStringDecoder(request.getUri());
	            Map<String, List<String>> uriAttributes = decoderQuery.parameters();
	            for (Entry<String, List<String>> attr : uriAttributes.entrySet()) {
	                for (String attrVal : attr.getValue()) {
	                	if(attr.getKey().equals("url")){
	                		htmlurl=attrVal;
	                	}
	                	//System.out.println("URI: " + attr.getKey() + '=' + attrVal + "\r\n");
	                }
	            }
	            
	            //url需要base64加密，为防止在参数过滤是将url对应链接所带参数值过滤掉
	            String urls=new String(Base64.decodeBase64(htmlurl.getBytes("utf-8"))); 
	            
	            map.put(ctx.hashCode(), urls); 
	            
	            System.out.println("code1:"+ctx.hashCode());
            
            }
        }
        
        if (msg instanceof HttpContent) {
        	System.out.println("HttpContent");
            HttpContent content = (HttpContent) msg;
            ByteBuf buf = content.content();
            String newUrl = buf.toString(io.netty.util.CharsetUtil.UTF_8);
            System.out.println(newUrl);
            buf.release();

            String res = "";
            String urlstr="";
            if(!map.get(ctx.hashCode()).equals(null)){
            	urlstr=map.get(ctx.hashCode()).toString();
            }
            System.out.println(urlstr);
            map.remove(ctx.hashCode());
            
            res = get(urlstr);
            
            Pattern pattern = Pattern.compile("<script[^>]*>[\\d\\D]*?</script>");
            Matcher matcher = pattern.matcher(res);
            String newRes = "";
            while(matcher.find()) {
            	newRes = newRes + matcher.group();
            }
            newRes = newRes.replace("<script>", "");
            String[] list = newRes.split("</script>");
           /* List<String> strs = new ArrayList<String>();
            for(int i = 0; i < list.length; i++) {
            	String s = list[i];
            	if (s.contains("Hub.config.set") || !s.contains("function")) {
            		strs.add(s);
            	}
            }*/
            
            String s = list[0];
            newRes = s.replace("var g_config = ", "").trim();
            String[] ss = newRes.split(";");
            /*newRes = ss[0].replaceAll("\t", "")
            		.replaceAll("\n", "")
            		.replaceAll(" ", "")
            		.replace("+newDate", "''");*/
            newRes = ss[0]
            		//.replaceAll("\t", "")
            		//.replaceAll("\n", "")
            		//.replaceAll(" ", "")
      /*      		.replaceAll("\"", "")
            		.replaceAll("\'", "")*/
            		//.replaceAll("\"(\\w+)\"(\\s*:\\s*)", "$1$2")
            		.replace("+newDate", "\"\"")/*
            		.replaceAll("(\\w+):", "\"$1\":")
            		.replaceAll(":(.*?),", ":\"$1\",")*/;
           /* Matcher m =Pattern.compile("descUrl:(.*?),").matcher(newRes);
            String sss = "";
            while(m.find()) {
            	sss = sss + m.group();
            }
            ss = sss.replaceAll("descUrl:location.protocol==='http:'?", "").split(":");
            newRes = ss[0];*/
            /*DescBean bean = new Gson().fromJson(newRes, DescBean.class);
            newRes = bean.toString();*/
           // List<String> strs = Arrays.asList(list);
            //newRes = new Gson().toJson(strs);
           /* if (newUrl != null && !"".equals(newUrl)) {
            	 res=get(new String(Base64.decodeBase64(newUrl.getBytes("utf-8"))));
            }*/
           
            /*URL url_t = new URL(urlstr);  
            HttpURLConnection httpConn = (HttpURLConnection) url_t.openConnection();
            httpConn.setConnectTimeout(5000);  
            httpConn.setReadTimeout(5000);
            httpConn.setUseCaches(false); // 不允许使用缓存
            
            InputStreamReader isr = new InputStreamReader(httpConn.getInputStream(),"gbk");  
            
            BufferedReader in = new BufferedReader(isr);  
             
            String inputLine="";  
             
            StringBuilder sb = new StringBuilder();      
           
            while ((inputLine = in.readLine()) != null){  
             	 sb.append(inputLine + "\n");    
            }  
      
            res=sb.toString();
            
            isr.close();
            
            httpConn.disconnect();*/
            
            //conn.setReadTimeout(5 * 1000); // 缓存的最长时间
            FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1,
                    OK, Unpooled.wrappedBuffer(newRes.getBytes("UTF-8")));
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
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        //log.error(cause.getMessage());
    	System.out.println(cause.getMessage());
        ctx.close();
    }

}