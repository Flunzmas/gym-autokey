#! /usr/bin/python3

# ck is for "contact key"

import socket
import json
import sys

sys.argv.pop(0)

if len(sys.argv) < 1:
    print("At least command argument needed")
    exit(1)

if len(sys.argv) % 2 == 0:
    print("Need an odd number of arguments")
    exit(2)

order = sys.argv.pop(0)
command = { "command": order }

while len(sys.argv) > 0:
    k = sys.argv.pop(0)
    v = sys.argv.pop(0)
    command[k] = v

commandStr = json.dumps(command)

print("command:  " + commandStr);
    
with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as s:
    s.connect(('localhost', 5533))
    s.sendall(commandStr.encode('utf-8'))
    s.shutdown(1)
    data = s.recv(1048576)

print("response: " + data.decode("utf-8"))


def ast(form):
    args = []
    for child in form["children"]:
        args.append(ast(child))
    result = form["op"] + "(" + ", ".join(args) + ")"
    return result

if order == "ast":
    response = json.loads(data.decode("utf-8"))
    if response["response"] == "success":
        for form in response["antecedent"]:
            print(" " + ast(form))
        print("==>")
        for form in response["succedent"]:
            print(" " + ast(form))

if order == "print":
    response = json.loads(data.decode("utf-8"))
    if response["response"] == "success":
        print(response["sequent"])
