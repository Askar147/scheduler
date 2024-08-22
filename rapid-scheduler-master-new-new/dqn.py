from fastapi import FastAPI
from pydantic import BaseModel
from typing import List
import uvicorn
import torch
import torch.nn as nn
import torch.optim as optim
import random
import numpy as np
from collections import deque

class DQN(nn.Module):
    def __init__(self, state_size, action_size):
        super(DQN, self).__init__()
        self.fc1 = nn.Linear(state_size, 128)
        self.fc2 = nn.Linear(128, 128)
        self.fc3 = nn.Linear(128, action_size)

    def forward(self, x):
        x = torch.relu(self.fc1(x))
        x = torch.relu(self.fc2(x))
        return self.fc3(x)

state_size = 5  # Number of features: cpuload, allocatedcpu, freemem, availmem, powerusage

# Initialize global variables
dqn = None
optimizer = None
criterion = nn.MSELoss()
gamma = 0.99
epsilon = 1.0  # Start with high exploration
epsilon_min = 0.01
epsilon_decay = 0.995
learning_rate = 0.001
learning_rate_decay = 0.999
memory = deque(maxlen=10000)
batch_size = 32
decision_count = 0

def initialize_dqn(action_size):
    global dqn, optimizer
    if dqn is None:
        dqn = DQN(state_size, action_size)
        optimizer = optim.Adam(dqn.parameters(), lr=learning_rate)

def select_action(state, action_size):
    global epsilon
    if random.random() < epsilon:
        return random.randint(0, action_size - 1)
    else:
        with torch.no_grad():
            return torch.argmax(dqn(torch.FloatTensor(state))).item()

def store_experience(state, action, reward, next_state, done):
    memory.append((state, action, reward, next_state, done))

def train_model():
    if len(memory) < batch_size:
        return

    batch = random.sample(memory, batch_size)
    states, actions, rewards, next_states, dones = zip(*batch)
    states = torch.FloatTensor(states)
    actions = torch.LongTensor(actions)
    rewards = torch.FloatTensor(rewards)
    next_states = torch.FloatTensor(next_states)
    dones = torch.FloatTensor(dones)

    q_values = dqn(states).gather(1, actions.unsqueeze(1)).squeeze(1)
    next_q_values = dqn(next_states).max(1)[0]
    target_q_values = rewards + (gamma * next_q_values * (1 - dones))

    loss = criterion(q_values, target_q_values)
    optimizer.zero_grad()
    loss.backward()
    optimizer.step()

app = FastAPI()

class Stats(BaseModel):
    vmmid: List[float]
    cpuload: List[float]
    allocatedcpu: List[float]
    freemem: List[float]
    availmem: List[float]
    powerusage: List[float]

@app.post("/predict")
def predict(stats: Stats):
    global epsilon, decision_count

    action_size = len(stats.cpuload)
    initialize_dqn(action_size)

    state = np.array([stats.cpuload, stats.allocatedcpu, stats.freemem, stats.availmem, stats.powerusage]).T.astype(np.float32)

    actions = []

    for s in state:
        action = select_action(s, action_size)
        actions.append(action)

        next_state = s
        reward = -s[-1]
        done = False

        store_experience(s, action, reward, next_state, done)
        train_model()

        epsilon = max(epsilon_min, epsilon * epsilon_decay)
        decision_count += 1

    for param_group in optimizer.param_groups:
        param_group['lr'] = learning_rate * (learning_rate_decay ** decision_count)

    # Return the action (node ID) for the last state in the batch
    return {"action": stats.vmmid[actions[-1]]}

if __name__ == "__main__":
    uvicorn.run(app, host="0.0.0.0", port=8000)

