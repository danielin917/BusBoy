from kohonen import Parameters
from kohonen import Map
from kohonen import euclidean_metric

from tqdm import tqdm

from matplotlib import pyplot as plt

def trainSOM(df, n_rows, n_columns):
    params = Parameters(dimension=df.shape[0], shape=(n_rows, n_columns), metric=euclidean_metric)
    SOM = Map(params=params)
    SOM.learn(df)
    plt.ion()
    plt.draw()
    return SOM

def getNeuronHeatMap(SOM):
    return SOM.neuron_heatmap()

def getDistanceMap(SOM, cue):
    print(SOM.winner(cue))
    return SOM.distance_heatmap(cue)