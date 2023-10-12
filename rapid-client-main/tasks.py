import matplotlib.pyplot as plt
import numpy as np
import math
from scipy.stats import truncnorm
from scipy.integrate import quad
import pandas as pd
from collections import Counter
import pickle as pkl

# 563 tasks
duration = 600
deadline = 87
sigma = 100
mean = duration // 2
mean_vcpu = 62
jetson_num = 20
xavier_num = 6
xavier_cores_num = 6

def save_data(x, file_name):
    file_object = open(file_name, 'wb')
    pkl.dump(x, file_object)
    file_object.close()
    return

def load_data(file_name):
    file_object = open(file_name, 'rb')
    x = pkl.load(file_object)
    file_object.close()
    return x

def generate_tasks_arrival():
    def normalProbabilityDensity(x):
        constant = 1.0 / np.sqrt(2*np.pi*sigma**2)
        return(constant * np.exp((-((x-mean)**2)) / 2.0 / (sigma**2)) )
    all, _ = quad(normalProbabilityDensity, 0, mean*2, limit = 1000)
    region, _ = quad(normalProbabilityDensity, mean-deadline, mean, limit = 1000)
    clients_in_deadline = jetson_num * (400 // mean_vcpu) + xavier_num * (100 * xavier_cores_num // mean_vcpu)
    total_clients_num = int(clients_in_deadline * all / region)
    print("total percent in range 0 to duration ", all)
    print("mean-deadline to mean percent of total is ", region)
    print("generated number of tasks", total_clients_num)

    def get_truncated_normal(mean=0, sd=1, low=0, upp=10):
        return truncnorm(
            (low - mean) / sd, (upp - mean) / sd, loc=mean, scale=sd)
    X = get_truncated_normal(mean=mean, sd=sigma, low=0, upp=duration-deadline)
    x = X.rvs(total_clients_num)
    x = np.sort(x)
    x = x.astype(int)
    print(x)
    #craete graph just to check
    sorted_list = sorted(x)
    sorted_counted = Counter(sorted_list)
    range_length = list(range(duration)) # Get the largest value to get the range.
    data_series = {}
    for i in range_length:
        data_series[i] = 0 # Initialize series so that we have a template and we just have to fill in the values.
    for key, value in sorted_counted.items():
        data_series[key] = value
    data_series = pd.Series(data_series)
    x_values = data_series.index
    plt.bar(x_values, data_series.values)
    plt.show()
    return x