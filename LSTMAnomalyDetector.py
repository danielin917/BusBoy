from keras.models import Sequential  
from keras.layers.core import Dense, Activation  
from keras.layers.recurrent import LSTM
from keras.optimizers import Adam
from keras.callbacks import EarlyStopping

def trainLSTMAnomalyDetector(df, n_prev, hidden_neurons):
    in_out_neurons = df.shape[1]

    model = Sequential()
    model.add(LSTM(input_length=n_prev, input_dim=in_out_neurons, output_dim=hidden_neurons, return_sequences=False))
    model.add(Dense(input_dim=hidden_neurons, output_dim=in_out_neurons))
    model.add(Activation("sigmoid"))
    model.compile(loss="mean_squared_error", optimizer=Adam(lr=0.05))

    import numpy as np
    from tqdm import tqdm

    docX, docY = [], []
    for i in tqdm(range(len(df) - n_prev)):
        docX.append(df.iloc[i:i+n_prev].as_matrix())
        docY.append(df.iloc[i+n_prev].as_matrix())
    alsX = np.array(docX)
    alsY = np.array(docY)

    model.fit(alsX, alsY, batch_size=250, nb_epoch=3, validation_split=0.05, verbose=True, shuffle=True,
              callbacks=[EarlyStopping()])

    return model