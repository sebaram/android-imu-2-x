import pandas as pd

import os
root = "230717_jyTestData"
root = "230718_jyTestData"
target_ip = "192.168.0.67"

# root = "SensorDATA"
# # target_ip = "192.168.0.131"
# target_ip = "192.168.0.67"

dfs = []
for one_csv in [a for a in os.listdir(root) if a.endswith(f"{target_ip}.csv")]:
    dfs.append( pd.read_csv(os.path.join(root, one_csv)))
all_df = pd.concat(dfs, ignore_index=True)

#%%
touch_df = all_df.loc[all_df.DeviceID==11]
touch_df["dt"] = touch_df.DeviceTime.diff()

print("Touch dt mean: {:.1f}ms".format(touch_df.loc[touch_df.dt<100]["dt"].mean()))
touch_df = touch_df.set_index("ServerTime")


sensor_df = all_df.loc[(all_df.DeviceID!=11) & (all_df.DeviceTime>0)]
sensor_df["dt"] = sensor_df.DeviceTime.diff()
print("Sensor dt mean: {:.1f}ms".format(sensor_df.loc[sensor_df.dt<1000]["dt"].mean()))
sensor_df = sensor_df.set_index("ServerTime")

#%%
s_path = os.path.join(root, [a for a in os.listdir(root) if a.endswith("EXP1.csv") ][0])
schedule_df = pd.read_csv(s_path, index_col=0)

schedule_df['sensor'] = schedule_df.apply(lambda x: sensor_df.loc[x["StartTime"]:x["CurrentTime"]], axis=1)
schedule_df['len_sensor'] = schedule_df.apply(lambda x: len(x['sensor']), axis=1)
schedule_df['touch'] = schedule_df.apply(lambda x: touch_df.loc[x["StartTime"]:x["CurrentTime"]], axis=1)
schedule_df['len_touch'] = schedule_df.apply(lambda x: len(x['touch']), axis=1)

#%%

"""Plot trial"""
WIDTH, HEIGHT = 454, 454
import matplotlib.pyplot as plt
if not os.path.exists(os.path.join(root, "trial_touch")):
    os.mkdir(os.path.join(root, "trial_touch"))
fig, ax = plt.subplots()
for i, row in schedule_df.iterrows():
    ax.cla()
    df = row["touch"]
    ax.plot(df.Gx, df.Gy, "o-")
    ax.set_aspect('equal', adjustable='box')

    ax.set_title(f"Trial {i} | {row.TargetName} | {df.DeviceTime.max()-df.DeviceTime.min()}ms | {row.len_touch}frames")
    ax.set_xlim(0, WIDTH)
    ax.set_ylim(HEIGHT, 0)

    Drawing_uncolored_circle = plt.Circle((WIDTH/2, HEIGHT/2), WIDTH/2,fill=False)
    ax.set_aspect(1)
    ax.add_artist(Drawing_uncolored_circle)

    fig.savefig(os.path.join(root, "trial_touch", f"trial_{i}_{row.TargetName}.png"), dpi=300)

