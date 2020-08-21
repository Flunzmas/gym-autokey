#!/usr/bin/python3
import socket
import random
import signal
import json
import sys

PORT = 6767
TACTICS = ("AUTO", "AUTO_NOSPLIT", "MODELSEARCH", "NOTHING",
           "INT", "HEAP", "QUANT", "DEPENDENCY", "EXPAND") # , "---unknown---")

# https://pymotw.com/2/socket/tcp.html
# Create a TCP/IP socket
sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)

# Then bind() is used to associate the socket with the server address.
# In this case, the address is localhost

# Bind the socket to the port
server_address = ('localhost', PORT)
print('starting up on %s port %s' % server_address)
sock.bind(server_address)

# Calling listen() puts the socket into server mode,
# and accept() waits for an incoming connection.
# Listen for incoming connections
sock.listen(1)

def disconnect(signum, frame):
    global sock
    print('Closing connection', file=sys.stderr)
    # tcflush(sys.stdin, TCIOFLUSH)
    sock.shutdown(socket.SHUT_RDWR)
    sock.close()

# Set the signal handler and a 5-second alarm
signal.signal(signal.SIGTERM, disconnect)
signal.signal(signal.SIGINT, disconnect)


recData = False
def recvall(sock):
    global recData
    BUFF_SIZE = 8 #192
    data = ''

    while True:
        if not recData:
            part = sock.recv(BUFF_SIZE)
            if not part:
                return False
            recData = part.decode("utf-8")

        nlIdx = recData.find("\n")
        
        if nlIdx >= 0:
            data += recData[0:nlIdx]
            recData = recData[nlIdx+1:]
            return data
        else:
            data += recData
            recData = False



seen_ids = set()
while True:
    # Wait for a connection
    print('waiting for a connection')
    connection, client_address = sock.accept()

    # accept() returns an open connection between the server and
    # client, along with the address of the client. The connection
    # is actually a different socket on another port (assigned by
    # the kernel). Data is read from the connection with recv() and
    # transmitted with sendall().

    try:
        print('connection from', client_address)

        # Receive the data in small chunks and retransmit it
        while True:
            data = recvall(connection)
            if not data:
                print('no more data from', client_address)
                break
            if data == "quit":
                print("exiting ...")
                connection.close()
                sock.shutdown(socket.SHUT_RDWR)
                sock.close()
                exit()
                
            print('received "%s"' % data)
            response = json.loads(data)
            print('goal no', response["id"])

            if response["id"] in seen_ids:
                print('id already seen. send -NONE-', seen_ids)                        
                connection.sendall("-NONE-\n".encode())
            else:
                index = random.randrange(len(TACTICS))
                tactic = TACTICS[index] + "\n"
                #tactic = "QUANT\n"
                print('sending data back to the client', tactic)
                connection.sendall(tactic.encode())
                seen_ids.add(response["id"])
    finally:
        # Clean up the connection
        connection.close()

from termios import tcflush, TCIOFLUSH
tcflush(sys.stdin, TCIOFLUSH)
