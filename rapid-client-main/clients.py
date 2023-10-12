import os
import shutil
import subprocess
import tasks as ts
import numpy as np
from multiprocessing import Process
import time

def run_client(clients_path, i):
    cmd = "docker"
    p = subprocess.Popen([cmd, 'run', '-d', '-m=64m', '--network=rapid-ac-network', '--name=rapid-client-' + str(i), 'rapid-ac'], cwd=clients_path + str(i))
    p.wait()

def run_tasks():
    time_ms = 0
    interval_ms = 100
    clients_path = "clients/"
    start_times = ts.load_data("data")
    current = 0
    while current < start_times.size:
        while current < start_times.size and time_ms // 1000 == start_times[current]:
            print("Running client #" + str(current))
            p = Process(target=run_client, args=(clients_path, current))
            p.daemon = True
            p.start()
            current += 1
        time.sleep(interval_ms / 1000)
        time_ms += interval_ms
    time.sleep(150)

run_tasks()

# x = ts.generate_tasks_arrival()
# ts.save_data(x,"data")
# y = ts.load_data("data")
# print(y)


# clients_path = "clients/"
# num_clients = 10
# run_client(clients_path, 1)
