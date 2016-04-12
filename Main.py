import Parser

# For checking if file exists
import os

# For Data Analysis
import pandas

filename = "Sample Data"
XMLStructureFilename = "canstructure.xml"
raw_data = True

df = pandas.DataFrame()

if raw_data:
    if not os.path.isfile(filename + ".parsed_raw_data"):
        df = Parser.get_raw_data(filename, XMLStructureFilename)
        Parser.save_parsed_data_to_file(dataframe=df, filename=filename + ".parsed_raw_data")
    else:
        df = Parser.load_parsed_data_from_file(filename + ".parsed_raw_data")

else:
    if not os.path.isfile(filename + ".parsed_encoded_data"):
        df = Parser.get_label_encoded_data(filename)
        Parser.save_parsed_data_to_file(dataframe=df, filename=filename + ".parsed_encoded_data")
    else:
        df = Parser.load_parsed_data_from_file(filename + ".parsed_encoded_data")

training_set_fraction = 0.7
training_data = df.loc[:training_set_fraction * float(df.shape[0])]

###### ###### ###### ###### ###### ###### START TRAINING ###### ###### ###### ###### ###### ######

import time
stime = time.time()

### One Class Support Vector Machine ###

import SVM
from sklearn.externals import joblib

OCSVM = SVM.trainOCSVM(training_data, tol=0.001, cache_size=2000, shrinking=False, nu=0.05, verbose=True)
joblib.dump(OCSVM, filename=filename + ".fitted_SVM_model")
clf = joblib.load(filename + ".fitted_SVM_model")

########################################

### Autoassociative NN ###

import Autoencoder
import tensorflow as tf
sess = tf.Session()
x = tf.placeholder("float", [None, df.shape[1]])
autoencoder = Autoencoder.create(x, [48, 24, 12])
EWMACost = 0
Autoencoder.train_AE(df=training_data, sess=sess, x=x,
                     denoising=False, verbose=False, autoencoder=autoencoder)

##########################

### Self Organizing Map ###

if not os.path.exists('./SOM_IMAGES'):
    os.makedirs('./SOM_IMAGES')

import SOM
som = SOM.trainSOM(training_data, 10, 10)

neuron_heatmap = SOM.getNeuronHeatMap(som)
neuron_heatmap = neuron_heatmap.resize((1024, 768))
neuron_heatmap.save('./SOM_IMAGES/Neuron_Heatmap.png')

from tqdm import tqdm
import numpy as np

for i in tqdm(range(100)):
    cue_distance_heatmap = SOM.getDistanceMap(som, training_data.loc[np.random.randint(0, training_data.shape[0])])
    cue_distance_heatmap = cue_distance_heatmap.resize((1024, 768))
    cue_distance_heatmap.save("./SOM_IMAGES/Cue_Distance_Heatmap_" + str(i) + ".png")

###########################

### Long Short Term Memory Anomaly Detector ###

import LSTMAnomalyDetector
LSTMADModel = LSTMAnomalyDetector.trainLSTMAnomalyDetector(training_data, 25, 50)
LSTMADModel.save_weights('LSTMADModel.h5', overwrite=True)

###############################################

etime = time.time()

print("Training took: " + str(etime - stime) + "seconds.")

###### ###### ###### ###### ###### ###### END TRAINING ###### ###### ###### ###### ###### ######

testing_set_fraction = 1 - training_set_fraction
testing_data_non_anomalous =  df[training_set_fraction * float(df.shape[0]):]

# testing_data_gaussian_noise_added =
# testing_data_with_dropout =
# testing_data_with_switched_hex_values =

# testing_data_with_exponential_transmission_errors =

import Noise

# from sklearn import cross_validation
# unsupervised_training_set, unsupervised_testing_set = \
#   cross_validation.train_test_split(df)


# import SVM
# scores = cross_validation.cross_val_score(SVM.trainOCSVM(df, tol=0.01, cache_size=2000, shrinking=False, nu=0.1), df,
#                                 [-1] * df.shape[0], scoring="accuracy")
# print(scores)
# from sklearn.externals import joblib
# print(df)
# OCSVM = SVM.trainOCSVM(df, tol=0.001, cache_size=2000, shrinking=False, nu = 0.05)
# joblib.dump(OCSVM, filename=filename + ".fitted_SVM_model")

# clf = joblib.load(filename + ".fitted_SVM_model")
# import numpy as np

# df_t = df.copy(deep=True)
# for i in range(df_t.shape[0]):
#    df_t.loc[i][np.random.choice(df_t.shape[1], int(0.2 * df_t.shape[1]))] = 0.0

# print(sum(clf.predict(df_t) == 1))









