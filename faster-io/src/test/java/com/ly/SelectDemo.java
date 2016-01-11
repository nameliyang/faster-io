package com.ly;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class SelectDemo {
	
	public static void main(String[] args) throws IOException {
		
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.bind(new InetSocketAddress(80));
		serverChannel.configureBlocking(false);
		Selector selector = Selector.open();
		
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		
		while(true){
			
			int n = selector.select();
			
			if(n==0	){
				continue;
			}
			
			Set<SelectionKey> selectedKeys = selector.selectedKeys();
			Iterator<SelectionKey> iterator = selectedKeys.iterator();
			
			while(iterator.hasNext()){
				
				SelectionKey key = iterator.next();
				
				if(key.isAcceptable()){
					
					System.out.println("---------------accept a new connection------------");
					ServerSocketChannel serverSocketChannel = (ServerSocketChannel) key.channel();
					SocketChannel socketChannel = serverSocketChannel.accept();
					socketChannel.configureBlocking(false);
					SimpleSession session = new SimpleSession();
					session.setAttribute("msg", new StringBuilder());
					
					socketChannel.register(selector, SelectionKey.OP_READ,session);
					
				}else if(key.isReadable()){
					System.out.println("----------------------readable--------------------");
					
					SocketChannel channel = (SocketChannel) key.channel();
					
					SimpleSession session = (SimpleSession) key.attachment();
					
					
					ByteBuffer byteBuffer = ByteBuffer.allocate(20);
				    channel.read(byteBuffer);
					StringBuilder msg = (StringBuilder) session.getAttribute("msg");
					byteBuffer.flip();
					while(byteBuffer.hasRemaining()){
						msg.append((char)byteBuffer.get());
					}
					System.out.println(msg);
					if(msg.lastIndexOf("\n")>0){
						System.out.println("msg :" + msg);
						msg = null;
						session.setAttribute("msg", "");
						channel.close();
					}
				}
				
				iterator.remove();
			}
			
		}
		
	}
}

class SimpleSession implements Serializable{
	private static final long serialVersionUID = 154409563306918078L;
	
	private final HashMap<String,Object> sessionMap = new HashMap<String,Object>();
	
    public void setAttribute(String name, Object value){
    	sessionMap.put(name, value);
    }
	
    public void removeAttribute(String name){
    	sessionMap.remove(name);
    }
	
    public Object getAttribute(String name){
    	return sessionMap.get(name);
    }
	
}
