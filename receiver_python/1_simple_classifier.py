from sklearn.ensemble import RandomForestClassifier
from sklearn.model_selection import cross_val_score, cross_val_predict
from sklearn.metrics import confusion_matrix, classification_report

import matplotlib.pyplot as plt
import seaborn as sns

import numpy as np
import pandas as pd

from collections import OrderedDict

target_data = ["230718_jyTestData", "230718_jyTestData2", "230718_JSS2"]

dfs = []
for root in target_data:
    dfs.append(pd.read_pickle(f"{root}.pickle"))
combined_df = pd.concat(dfs, ignore_index=True)
combined_df = combined_df.loc[combined_df.len_touch>0]

combined_df.loc[:,"data_check"] = combined_df["touch"].apply(lambda x: x.iloc[0].Gz==0 and x.iloc[-1].Gz==1)
combined_df = combined_df.reset_index(drop=True)
#%%
# position of first,last xy
def distance_mid(x,y,mid=(227,227)):
    return ((x-mid[0])**2 + (y-mid[1])**2)**0.5
def direction_firstlast(x0,y0,x1,y1):
    theta = np.arctan2(y1-y0, x1-x0)
    return theta
def get_first_last(df):
    result = OrderedDict()
    first_x, first_y = df.iloc[0].Gx, df.iloc[0].Gy
    last_x, last_y = df.iloc[-1].Gx, df.iloc[-1].Gy

    first_distance = distance_mid(first_x, first_y)
    last_distance = distance_mid(last_x, last_y)

    result["first_x"] = first_x
    result["first_y"] = first_y
    result["first_distance"] = first_distance
    result["last_x"] = last_x
    result["last_y"] = last_y
    result["last_distance"] = last_distance
    result["first_last_theta"] = direction_firstlast(first_x, first_y, last_x, last_y)
    result["duration"] = df.iloc[-1].DeviceTime - df.iloc[0].DeviceTime
    return result

def get_from_movement(df):
    move_df = df.loc[df.Gz==2]
    result = OrderedDict()
    result["move_count"] = len(move_df)

    dt = move_df.DeviceTime.diff()
    dx = move_df.Gx.diff()
    dy = move_df.Gy.diff()

    result["move_speed_x"] = dx.mean() / dt.mean()
    result["move_speed_y"] = dy.mean() / dt.mean()
    result["move_speed"] = (dx**2 + dy**2).mean() / dt.mean()

    result["move_speed_sd_x"] = dx.std() / dt.mean()
    result["move_speed_sd_y"] = dy.std() / dt.mean()
    result["move_speed_sd"] = (dx**2 + dy**2).std() / dt.mean()

    result["move_accel_x"] = dx.diff().mean() / dt.mean()
    result["move_accel_y"] = dy.diff().mean() / dt.mean()
    result["move_accel"] = (dx.diff()**2 + dy.diff()**2).mean() / dt.mean()

    return result

target_str = dict()
Xdict = []
for i,row in combined_df.iterrows():
    features = get_first_last(row["touch"])
    features.update(get_from_movement(row["touch"]))
    Xdict.append(features)

    target_str[row.Target] = row.TargetName

X = np.array([list(one.values()) for one in Xdict])
X_feature_names = list(Xdict[0].keys())

y = combined_df["Target"].values
y_str = [target_str[one] for one in y]

target_order = [target_str[i] for i in range(len(target_str))]





#%%

rf = RandomForestClassifier(n_estimators = 100)

scores = cross_val_score(rf, X, y, cv=5)
print("FULL cross_val_score(cv=5): ", scores)


#%%
# 1. confusion matrix

# full crossvalidation
# y_true = y
# y_predict = cross_val_predict(rf, X, y, cv=5)

# train/test split: user independent
train_idx = combined_df.loc[combined_df["Name"]=="JSS"].index
test_idx = combined_df.loc[combined_df["Name"]!="JSS"].index

rf = RandomForestClassifier(n_estimators = 100)
rf.fit(X[train_idx], y[train_idx])
y_predict = rf.predict(X[test_idx])
y_true = y[test_idx]


print(classification_report(y_true, y_predict, target_names=target_order))


cm = confusion_matrix(y_true, y_predict)
cm_p = cm.astype('float') / cm.sum(axis=1)[:, np.newaxis]
cm_str = np.array(cm, dtype=object)
true = y_true==y_predict

tot_cnt = cm.copy()
true_cnt = cm.copy()
for i in range(len(target_str)):
    for j in range(len(target_str)):
        # tot_cnt[i,j] = sum( (y==i) & (y_predict==j))
        cm_str[i,j] = f"{100*cm_p[i,j]:.1f}% \n({cm[i,j]})"

plt.figure()
sns.heatmap(cm, annot=cm_str, fmt="", cmap="Blues", xticklabels=target_order, yticklabels=target_order)
plt.xlabel('Predicted')
plt.ylabel('True')
plt.savefig("confusion_matrix.png", dpi=300, bbox_inches='tight')
plt.show()




#%% 2. feature importance
rf.fit(X,y)
importances = rf.feature_importances_
std = np.std([tree.feature_importances_ for tree in rf.estimators_], axis=0)
indices = np.argsort(importances)[::-1]
plt.figure()
plt.title("Feature importances")
plt.bar(range(X.shape[1]), importances[indices], color="r", yerr=std[indices], align="center")
plt.xticks(range(X.shape[1]), [X_feature_names[i] for i in indices], rotation=60, ha='right')
plt.xlim([-1, X.shape[1]])
plt.savefig("feature_importance.png", dpi=300, bbox_inches='tight')
plt.show()

#%%
