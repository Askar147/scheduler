import os
import shutil

def createsubdirs(src, dst, num_clients):
    for i in range(num_clients):
        shutil.copytree(src, dst + str(i))
    return

src = "rapidAC"
dst = "clients/"
num_clients = 700
createsubdirs(src, dst, num_clients)