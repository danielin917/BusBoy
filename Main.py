import pandas
from tqdm import tqdm
import tensorflow as tf
import numpy as np
import math
import random

dataframes = []
row_ind = 0
with open("Sample Data", 'r') as f:
    while True:
        lines = f.readlines()
        if not lines:
            break
        for line in tqdm(lines):
            line = line.replace("\n", "")
            line = line.replace(", ", ",")
            arr = line.split(",")
            row = {}
            for i in range(len(arr) - 1):
                row[arr[i].split("=")[0]] = arr[i].split("=")[1]
            for i in range(len(arr[len(arr) - 1].split(" "))):
                row[arr[len(arr) - 1].split(" ")[i].split("=")[0]] = arr[len(arr) - 1].split(" ")[i].split("=")[1]
            dataframes.append(row)

df = pandas.DataFrame(dataframes)
df = df.applymap(str)
from sklearn.preprocessing import LabelEncoder
df = df.apply(LabelEncoder().fit_transform)
df = df.apply(lambda x: (x - np.mean(x)) / (np.max(x) - np.min(x)))

print("Completed Data Parsing!")

def create(x, layer_sizes):
    # Build the encoding layers
    next_layer_input = x

    encoding_matrices = []
    for dim in layer_sizes:
        input_dim = int(next_layer_input.get_shape()[1])

        # Initialize W using random values in interval [-1/sqrt(n) , 1/sqrt(n)]
        W = tf.Variable(tf.random_uniform([input_dim, dim], -1.0 / math.sqrt(input_dim), 1.0 / math.sqrt(input_dim)))

        # Initialize b to zero
        b = tf.Variable(tf.zeros([dim]))

        # We are going to use tied-weights so store the W matrix for later reference.
        encoding_matrices.append(W)

        output = tf.nn.tanh(tf.matmul(next_layer_input,W) + b)

        # the input into the next layer is the output of this layer
        next_layer_input = output

    # The fully encoded x value is now stored in the next_layer_input
    encoded_x = next_layer_input

    # build the reconstruction layers by reversing the reductions
    layer_sizes.reverse()
    encoding_matrices.reverse()


    for i, dim in enumerate(layer_sizes[1:] + [ int(x.get_shape()[1])]) :
        # we are using tied weights, so just lookup the encoding matrix for this step and transpose it
        W = tf.transpose(encoding_matrices[i])
        b = tf.Variable(tf.zeros([dim]))
        output = tf.nn.tanh(tf.matmul(next_layer_input,W) + b)
        next_layer_input = output

    # the fully encoded and reconstructed value of x is here:
    reconstructed_x = next_layer_input

    return {
		'encoded': encoded_x,
		'decoded': reconstructed_x,
		'cost' : tf.sqrt(tf.reduce_mean(tf.square(x-reconstructed_x)))
	}

def train_AE():
    sess = tf.Session()
    x = tf.placeholder("float", [None, 96])
    autoencoder = create(x, [100, 250, 500])
    train_step = tf.train.AdamOptimizer(1e-4).minimize(autoencoder['cost'])
    batchsize = 100
    iteration_count = 100000
    init = tf.initialize_all_variables()
    sess.run(init)
    for i in range(iteration_count):
        batch = [df.loc[random.randint(0, df.shape[0] - 1)] for i in range(batchsize)]
        sess.run(train_step, feed_dict={x : np.array(batch)})
        if i % 100 == 0:
            print(i, " cost", sess.run(autoencoder['cost'], feed_dict={x : batch}))

train_AE()