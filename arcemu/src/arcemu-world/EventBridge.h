/*
 * EventBridge.h
 *
 *  Created on: 31 Jan 2011
 *      Author: sergio
 */

#ifndef EVENTBRIDGE_H_
#define EVENTBRIDGE_H_

#include <sys/socket.h>
#include <sys/types.h>
#include <netinet/in.h>
#include <netdb.h>
#include <stdio.h>
#include <string.h>
#include <stdlib.h>
#include <unistd.h>
#include <errno.h>

class EventBridge
{
public:
	EventBridge();
	virtual ~EventBridge();
	void send(char*);

private:
	int	sock;
};

#endif /* EVENTBRIDGE_H_ */
