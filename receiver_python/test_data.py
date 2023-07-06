#%%
import pandas as pd

txt = open("test_data.txt", "r")

str_ = ""
df = pd.DataFrame(columns=["time", "event", "x", "y"], dtype=float)
for line in txt.readlines():
    str_ = [float(one) for one in line.split("time,event,x,y: ")[1][:-1].split(",")]
    df.loc[len(df)] = str_

#%%
start_i, end_i = [], []
for i, row in df.iterrows():
    if row["event"]==0 :
        start_i.append(i)
    if row["event"]==1 :
        end_i.append(i)
#%%
trial_df = pd.DataFrame(columns=["start_i", "end_i", "duration", "input", "df"])
input_ = ["na","na",
         "up","up","up","up","up","up",
         "right", "right", "right", "right", "right",
         "down", "down", "down", "down", "down",
         "left", "left", "left", "left", "left"]
for i in range(len(start_i)):
    tmp_df = df.loc[start_i[i]:end_i[i]]
    trial_df.loc[i] = [start_i[i], end_i[i], tmp_df.time.max()-tmp_df.time.min(), input_[i], tmp_df]

trial_df.to_pickle("test_data.pkl")
#%%
# 0: Down
# 1: Up
# 2: Move
WIDTH, HEIGHT = 454, 454
import matplotlib.pyplot as plt

fig, ax = plt.subplots()
for i, row in trial_df.iterrows():
    ax.cla()

    ax.plot(row["df"].x, row["df"].y, "o-")
    ax.set_aspect('equal', adjustable='box')

    ax.set_title(f"Trial {i} | {row.input} | {row.duration}ms | {row.end_i-row.start_i+1}frames")
    ax.set_xlim(0, WIDTH)
    ax.set_ylim(HEIGHT, 0)

    fig.savefig(f"trial_{i}_{row.input}.png", dpi=300)
    # break
#%%