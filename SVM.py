from sklearn import svm
import numpy as np

def trainOCSVM(dataframe, shrinking=False, cache_size=200, verbose=False,
               max_iter=-1, nu=0.5, kernel='rbf', degree=3, tol=0.001):
    OCSVM = svm.OneClassSVM(cache_size=cache_size, verbose=verbose,
                    max_iter=max_iter, nu=nu, kernel=kernel, degree=degree,
                    shrinking=shrinking, tol=tol)
    OCSVM.fit(dataframe.as_matrix())
    return OCSVM

def testOCSVM(OCSVM, dataframe):
    return OCSVM.predict(np.array(dataframe))
