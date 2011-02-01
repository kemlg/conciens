package com.github.consciens.gameenactor;

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
		ServerSocket	ssin, ssout;
		Socket			sin ,sout;
		InputStream		is;
		BufferedReader	br;
		String			line;
		
		ssin = new ServerSocket(Constants.SOCK_PORT_IN);
		ssout = new ServerSocket(Constants.SOCK_PORT_OUT);
		while(!ssin.isClosed())
		{
			sin = ssin.accept();
			System.out.println("Connection in!");
			sout = ssout.accept();
			System.out.println("Connection out!");
			
			is = sin.getInputStream();
			br = new BufferedReader(new InputStreamReader(is));
			while(!sin.isClosed())
			{
				line = br.readLine();
				System.out.println("Message: [" + line + "]");
				line = line + "\n";
				sout.getOutputStream().write(line.getBytes());
			}
		}
	}
}
