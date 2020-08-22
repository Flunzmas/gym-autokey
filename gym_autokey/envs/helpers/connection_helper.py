def socket_receive(sock):
    """
    Minimal socket.recv wrapper capable of reading very long streams of data.
    """
    received_bytes = b""
    while True:
        chunk = sock.recv(8192)
        if not chunk:
            break
        received_bytes += chunk
    return received_bytes
