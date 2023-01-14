import numpy as np
import pandas as pd
from os.path import dirname, join
np.seterr(divide='ignore',invalid='ignore')

def main(filename):
    att=['a','b','c']
    #filename = join(dirname(__file__), "cylinder_100.csv")
    df=pd.read_csv(filename,names=att)
    #sum1 = np.sum(df)
    #sum2 = np.sum([1, 2, 3, 4, 5])
    A=df['a'].astype(float)
    B=df['b'].astype(float)
    C=df['c'].astype(float)
    point_data = np.stack([A,B,C], axis=0).transpose((1, 0))
    point_data=point_data[point_data[:,1].argsort()]
    point_data=volumetric_analysis(point_data)
    return point_data



# (X[i], Y[i]) are coordinates of i'th point.
def polygonArea(X, Y, n):
    # Initialize area
    area = 0.0

    # Calculate value of shoelace formula
    j = n - 1
    for i in range(0,n):
        area += (X[j] + X[i]) * (Y[j] - Y[i])
        j = i # j is previous vertex to i


    # Return absolute value
    return (abs(area / 2.0))

#Clockwise sorting
def sort_xy(x, y):

    x0 = np.mean(x)
    y0 = np.mean(y)

    r = np.sqrt((x-x0)**2 + (y-y0)**2)

    np.seterr(divide='ignore', invalid='ignore')

    angles = np.where((y-y0) > 0, np.arccos((x-x0)/r), 2*np.pi-np.arccos((x-x0)/r))

    mask = np.argsort(angles)

    x_sorted = x[mask]

    y_sorted = y[mask]

    return x_sorted, y_sorted


def volumetric_analysis(point_data):
    volume=0

    X_array=point_data[:,0]

    Y_array=point_data[:,1]

    Z_array=point_data[:,2]

    X_crd=[point_data[0,0]]
    Z_crd=[point_data[0,0]]

    for i in range(0,Y_array.size):
        if (Y_array[i]==Y_array[i-1]):


            X_crd.append(X_array[i])

            Z_crd.append(Z_array[i])



        else:

            X_c,Z_c=sort_xy(np.array(X_crd),np.array(Z_crd))


            volume+=polygonArea(X_c,Z_c,len(X_c)) * (Y_array[i]-Y_array[i-1])

            X_crd.clear()

            Z_crd.clear()

            X_crd.append(X_array[i])

            Z_crd.append(Z_array[i])


            continue

    return volume
