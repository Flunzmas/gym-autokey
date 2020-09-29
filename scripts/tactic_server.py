#!/usr/bin/python3
import signal
import json
import sys
import socket

from scripts.tactic_selector import TacticSelector
import gym_autokey.envs.config as cf


class TacticServer:
    """
    This class provides the functionality to listen to incoming KeY tactic selection
    requests and responds by providing a tactic.
    """

    terminated = False
    selector = None
    sock = None
    recData = None

    def __init__(self, tactic_selector):
        """
        Setup of server, including the creation of a socket connection.
        """

        # tactic selector used by the server.
        self.selector = tactic_selector

        # socket server setup
        self.sock = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server_address = (cf.TACTIC_SERVER_ADDRESS, cf.TACTIC_SERVER_PORT)
        print('starting up on %s port %s' % server_address)
        self.sock.bind(server_address)
        self.sock.listen(1)

        # Set the signal handlers
        signal.signal(signal.SIGTERM, self.disconnect)
        signal.signal(signal.SIGINT, self.disconnect)

        self.recData = False

    def run(self):
        """
        Runs the server loop that listens to and handles incoming tactic selection requests
        by KeY. This loop continues until self.terminate() is invoked externally, at which point
        the selection history of the server is finalized.
        """

        # server loop
        while not self.terminated:

            # Wait for a connection
            print('waiting for a connection')
            connection, client_address = self.sock.accept()

            try:
                print('connection from', client_address)

                # Receive the data in small chunks and retransmit it
                while True:
                    data = self.recvall(connection)
                    if not data:
                        print('no more data from', client_address)
                        break
                    if data == "quit":
                        print("exiting ...")
                        connection.close()
                        self.sock.shutdown(socket.SHUT_RDWR)
                        self.sock.close()
                        exit()

                    # print('received "%s"' % data)
                    response = json.loads(data)
                    print('goal no', response["id"])

                    tactic = self.selector.predict(response) + '\n'
                    print('sending data back to the client: ', tactic)
                    connection.sendall(tactic.encode())

            finally:
                # Clean up the connection
                connection.close()
                self.selector.reset()

        self.selector_hist = self.selector.selector_hist

    def disconnect(self, signum, frame):
        """
        Shuts down the socket connection.
        """
        print('Closing connection', file=sys.stderr)
        self.sock.shutdown(socket.SHUT_RDWR)
        self.sock.close()

    def recvall(self, sock):
        """
        helper function to smoothly receive messages via a socket connection.
        """

        BUFF_SIZE = 8  # 192
        data = ''

        while True:
            if not self.recData:
                part = False
                try:
                    part = sock.recv(BUFF_SIZE)
                except ConnectionResetError as cee:
                    print('connection reset...')
                    return False
                if not part:
                    return False
                self.recData = part.decode("utf-8")

            nlIdx = self.recData.find("\n")

            if nlIdx >= 0:
                data += self.recData[0:nlIdx]
                self.recData = self.recData[nlIdx + 1:]
                return data
            else:
                data += self.recData
                self.recData = False

    def terminate(self):
        """
        Terminates the server run.
        """

        self.terminated = True

        # cleanup
        from termios import tcflush, TCIOFLUSH
        tcflush(sys.stdin, TCIOFLUSH)


if __name__ == '__main__':
    selector = TacticSelector()
    server = TacticServer(selector)
    server.run()