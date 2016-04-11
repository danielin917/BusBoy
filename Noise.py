import numpy as np
import pandas

def add_gaussian_noise(df, mean, variance):
    noise = np.random.normal(mean, variance, df.shape)
    return pandas.DataFrame(df + noise)

def dropout_noise(df, prob):
    df_t = df.copy(deep=True)
    df_t.applymap(lambda x: 0.0 if np.random.random() < prob else x)
    return df_t

def get_random_hex_float():
    return np.random.choice(
        [
         float(0x0/0xF),
         float(0x1/0xF),
         float(0x2/0xF),
         float(0x3/0xF),
         float(0x4/0xF),
         float(0x5/0xF),
         float(0x6/0xF),
         float(0x7/0xF),
         float(0x8/0xF),
         float(0x9/0xF),
         float(0xA/0xF),
         float(0xB/0xF),
         float(0xC/0xF),
         float(0xD/0xF),
         float(0xE/0xF),
         float(0xE/0xF)
         ], size = 1)

def switch_hex(df, prob):
    df_t = df.copy(deep=True)
    time_col = df_t[0]
    df_t = df_t[1:]
    df_t.applymap(lambda x: get_random_hex_float() if np.random.random() < prob else x)
    return pandas.concat(time_col, df_t)

def exponential_transmission_errors(df, scale_parameter, time_only, func,
                                    prob=None,
                                    gaussian_mean=None, gaussian_stddev=None):
    df_t = df.copy(deep=True)
    cumulative_last_error = 0
    while cumulative_last_error < df.shape[0]:
        cumulative_last_error += np.random.exponential(scale=scale_parameter)
        if time_only and func == dropout_noise:
            df_t[int(cumulative_last_error)][0] = 0.0
        elif time_only and func == add_gaussian_noise:
            assert(gaussian_mean != None and gaussian_stddev != None)
            df_t[int(cumulative_last_error)][0] = \
                add_gaussian_noise(df_t[int(cumulative_last_error)][0], gaussian_mean, gaussian_stddev)
        elif not time_only and func == dropout_noise:
            assert(prob)
            df_t[int(cumulative_last_error)] = dropout_noise(df_t[int(cumulative_last_error)], prob)
        elif not time_only and func == add_gaussian_noise:
            assert(gaussian_mean != None and gaussian_stddev != None)
            df_t[int(cumulative_last_error)] = \
                add_gaussian_noise(df_t[int(cumulative_last_error)], gaussian_mean, gaussian_stddev)
        elif not time_only and func == switch_hex:
            df_t[int(cumulative_last_error)] = \
                switch_hex(df_t[int(cumulative_last_error)], prob)