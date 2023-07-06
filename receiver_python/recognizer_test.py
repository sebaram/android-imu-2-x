import numpy as np
import pandas as pd

df = pd.read_pickle("test_data.pkl")
this_df = df.iloc[0]["df"]
#%%

"""
looking for last 150ms before Touch UP(event 1)
- average speed
- angle
- direction
- sum of distance

- distance from the startpoint?
- distance from the center?

add vibration feedback to know user that it is start to recognize


"""

this_df = df.loc[df.input=="up"].iloc[2]["df"]

this_df["dt"] = this_df.time.diff() # in ms
this_df["dx"] = this_df.x.diff()
this_df["dy"] = this_df.y.diff()
this_df["speed"] = this_df[["dx", "dy","dt"]].apply(lambda x: (x[0]**2+x[1]**2)**0.5/x[2] if x[2] is not np.nan else 0, axis=1)

linear_regression(this_df)
#%%
def get_last_n(df, n):
    return df.iloc[-n:]

def linear_regression(df):
    x = df[["x", "y"]].values
    y = df["time"].values

    res = np.linalg.lstsq(x, y, rcond=None)
    print(res)
    return res[0]
