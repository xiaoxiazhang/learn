/**
 * 
 */
package com.immoc.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import org.junit.Test;

public class SocketTest {

	@Test
	public void testInetAddress() throws UnknownHostException {
		InetAddress address = InetAddress.getLocalHost();
		System.out.println("主机名：" + address.getHostName());
		System.out.println("ip地址：" + address.getHostAddress());
		
		address = InetAddress.getByName("172.17.100.34");
		System.out.println("主机名：" + address.getHostName());
		System.out.println("ip地址：" + address.getHostAddress());
	}

	@Test
	public void testURL() throws MalformedURLException {

		URL immoc = new URL("http://www.imooc.com/");
		URL url = new URL(immoc, "index.html?username=tom#test");
		System.out.println("协议：" + url.getProtocol());
		System.out.println("主机："+url.getHost());
		System.out.println("端口："+url.getPort());
		System.out.println("文件路径："+url.getPath());
		System.out.println("文件名："+url.getFile());
		System.out.println("相对路径："+url.getRef());
		System.out.println("查询字符串："+url.getQuery());
				

	}
	
	@Test
	public void testGetPageByURL(){
		try {
			URL url = new URL("http://www.baidu.com");
			InputStream is = url.openStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String data = null;
			while(( data= br.readLine())!=null){
				System.out.println(data);
			}
			is.close();
			isr.close();
			br.close();
		} catch (MalformedURLException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}
	}
	
	
	

}
