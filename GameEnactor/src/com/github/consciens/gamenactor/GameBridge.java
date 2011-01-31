package com.github.consciens.gamenactor;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class GameBridge
{
	public static void main(String args[]) throws IOException
	{
		ServerSocket	ss;
		Socket			s;
		InputStream		is;
		BufferedReader	br;
		
		ss = new ServerSocket(Constants.SOCK_PORT);
		while(!ss.isClosed())
		{
			s = ss.accept();
			System.out.println("Connection!");
			
			is = s.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			while(!s.isClosed())
			{
				System.out.println("Message: [" + br.readLine() + "]");
			}
		}
	}
}
