package it.unipi.ing.mim.server;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
public class HTTPPOSTServer 
	{ 
	public static void main(String[] args) throws Exception{
		HttpServer server = HttpServer.create(new InetSocketAddress(8091), 0);
        server.createContext("/test", new MyHandler());
        server.setExecutor(null); // creates a default executor
        server.start();
		}
	
	public static Map<String, String> queryToMap(String query) {
	    Map<String, String> result = new HashMap<>();
	    for (String param : query.split("&")) {
	        String[] entry = param.split("=");
	        if (entry.length > 1) {
	            result.put(entry[0], entry[1]);
	        }else{
	            result.put(entry[0], "");
	        }
	    }
	    return result;
	}
	
	static class MyHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange t) throws IOException {
            String response = "<form method='get'><input type='text' value='A' name='name'><input type='submit'></form>";
            t.sendResponseHeaders(200, response.length());
            OutputStream os = t.getResponseBody();
            os.write(response.getBytes());
            os.close();
            
            String params = t.getRequestURI().getQuery(); 
            System.out.println("param A=" + params);
        }
    }

}