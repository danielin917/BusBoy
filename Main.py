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

#import LSTMAnomalyDetector
# LSTMADModel = LSTMAnomalyDetector.trainLSTMAnomalyDetector(df, 25, 50)
# LSTMADModel.save_weights('LSTMADModel.h5', overwrite=True)

import Noise

df_t = Noise.dropout_noise(df, 0.2)

print(sum(df_t == df))

if not os.path.exists('./SOM_IMAGES'):
    os.makedirs('./SOM_IMAGES')

import SOM
som = SOM.trainSOM(df, 10, 10)

neuron_heatmap = SOM.getNeuronHeatMap(som)
neuron_heatmap = neuron_heatmap.resize((1024, 768))
neuron_heatmap.save('./SOM_IMAGES/Neuron_Heatmap.png')

from tqdm import tqdm
import numpy as np

for i in tqdm(range(100)):
    cue_distance_heatmap = SOM.getDistanceMap(som, df.loc[np.random.randint(0, df.shape[0])])
    cue_distance_heatmap = cue_distance_heatmap.resize((1024, 768))
    cue_distance_heatmap.save("./SOM_IMAGES/Cue_Distance_Heatmap_" + str(i) + ".png")





