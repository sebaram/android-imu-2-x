import pandas as pd

import os
root = "230717_jyTestData"

dfs = []
for one_csv in [a for a in os.listdir(root) if a.endswith("192.168.0.67.csv")]:
    dfs.append( pd.read_csv(os.path.join(root, one_csv)))
all_df = pd.concat(dfs, ignore_index=True)

#%%
touch_df = all_df.loc[all_df.DeviceID==11]
sensor_df = all_df.loc[all_df.DeviceID!=11]
#%%
