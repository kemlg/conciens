/*
 * EventBridge.cpp
 *
 *  Created on: 31 Jan 2011
 *      Author: sergio
 */

#include "StdAfx.h"

#include "EventBridge.h"

const char*	endMsg	= "\n";
const int	port	= 6969;

EventBridge::EventBridge()
{
	struct hostent* host;
	struct sockaddr_in server_addr;

	Log.Notice("EventBridge", "Starting EventBridge...");
	host = gethostbyname("127.0.0.1");

	this->sock = socket(AF_INET, SOCK_STREAM, 0);

	server_addr.sin_family = AF_INET;
	server_addr.sin_port = htons(port);
	server_addr.sin_addr = *((struct in_addr *) host->h_addr);
	bzero(&(server_addr.sin_zero), 8);

	connect(sock, (struct sockaddr *) &server_addr, sizeof(struct sockaddr));
	if(sock < 1)
	{
		Log.Notice("EventBridge", "sock < 1");
	}
	else
	{
		Log.Notice("EventBridge", "sock >= 1");
	}
}

EventBridge::~EventBridge()
{
	// TODO Auto-generated destructor stub
}

void EventBridge::sendMessage(char* send_data)
{
	// int					bytes_recieved;
	//char send_data[1024]; //, recv_data[1024];

	// bytes_recieved = recv(sock, recv_data, 1024, 0);
	// recv_data[bytes_recieved] = '\0';

	//if (strcmp(recv_data, "q") == 0 || strcmp(recv_data, "Q") == 0)
	//{
	//	close(sock);
	//	break;
	//}

	//else
	//	printf("\nRecieved data = %s ", recv_data);

	//printf("\nSEND (q or Q to quit) : ");
	//gets(send_data);

	send(sock, send_data, strlen(send_data), 0);
	send(sock, endMsg, strlen(endMsg), 0);

	if(strcmp(send_data, "q") == 0 || strcmp(send_data, "Q") == 0)
	{
		send(sock, send_data, strlen(send_data), 0);
		close(sock);
	}
}
