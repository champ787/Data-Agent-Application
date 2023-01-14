import numpy as np
import pandas as pd
from os.path import dirname, join

def first(str):
  filename = join(dirname(__file__), "cylinder_100.csv")
  att=['a','b','c']
  df=pd.read_csv(filename,names=att)
  # temp_x=df.iloc[:,0:3]
  A=df['a'].astype(float)
  B=df['b'].astype(float)
  C=df['c'].astype(float)
  point_data = np.stack([A,B,C], axis=0).transpose((1, 0))
  #Sorting data according to y axis
  point_data=point_data[point_data[:,1].argsort()]
  return "successful"



