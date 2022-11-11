import os
from re import T

from keras.layers import normalization
#Add your path to the DLL search path.
ddlpath="C:\\Program Files\\NVIDIA GPU Computing Toolkit\\CUDA\\v11.2\\bin"
os.add_dll_directory(ddlpath)

import cv2

import numpy as np


import tensorflow as tf
assert tf.__version__.startswith('2')

from tensorflow import keras
from tensorflow.keras import layers

from tensorflow.keras.layers import Flatten
from tensorflow.keras.layers import Dense
from tensorflow.keras.losses import SparseCategoricalCrossentropy
from sklearn.metrics import accuracy_score

import matplotlib.pyplot as plt

from PIL import Image
from tensorflow_hub import keras_layer

img_height = 200
img_width = 200


##Test the model with the photos in the directory 
directory=".\ToolsReteNeurale\\foto"
for filename in os.listdir(directory):
    f = os.path.join(directory, filename)
    # checking if it is a file
    if os.path.isfile(f):

        image = tf.keras.preprocessing.image.load_img(
        f,
          target_size=(img_height, img_width,3))



        input_arr = tf.keras.preprocessing.image.img_to_array(image)

        input_arr = np.array([input_arr])  # Convert single image to a batch.
        


        model=keras.models.load_model(".\\NeuralNetwork.h5")
        
        predictions = model.predict(input_arr)
        score = predictions[0]
        batch_size = 64
        class_names = ['A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','del','nothing','space']

        print("Images: ", filename)
        print(
            "This image most likely belongs to {} with a {:.2f} percent confidence."
            .format(class_names[np.argmax(score)], 100 * np.max(score))
        )
        print("\n\n")
        
