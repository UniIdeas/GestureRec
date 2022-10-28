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

#sunflower_url = "https://storage.googleapis.com/download.tensorflow.org/example_images/592px-Red_sunflower.jpg"
##sunflower_path = tf.keras.utils.get_file('A_test', origin="C:\\Users\\marco\\OneDrive\\Desktop\\ToolsReteNeurale\\ProgettoReteNeurale\\asl_alphabet_test\\A_test")


#Two different two to process an image
'''
img = tf.keras.utils.load_img(
    "C:\\Users\\marco\\OneDrive\\Desktop\\ToolsReteNeurale\\ProgettoReteNeurale\\asl_alphabet_test\\U_test.jpg", target_size=(img_height, img_width)
)
img_array = tf.keras.utils.img_to_array(img)
img_array = tf.expand_dims(img_array, 0) # Create a batch
'''

'''
image = tf.keras.preprocessing.image.load_img(
  "C:\\Users\\marco\\OneDrive\\Desktop\\ToolsReteNeurale\\ELuca.png",
   target_size=(img_height, img_width,3))

input_arr = tf.keras.preprocessing.image.img_to_array(image)
input_arr = np.array([input_arr])  # Convert single image to a batch.
#input_arr=np.reshape(image,(-1,28,28,1))

'''


##Test the model with the photos in the directory 
directory="C:\\Users\\marco\\OneDrive\\Desktop\\ToolsReteNeurale\\foto"
for filename in os.listdir(directory):
    f = os.path.join(directory, filename)
    # checking if it is a file
    if os.path.isfile(f):
        print(f)


        image = tf.keras.preprocessing.image.load_img(
        f,
          target_size=(img_height, img_width,3))



        input_arr = tf.keras.preprocessing.image.img_to_array(image)

        input_arr = np.array([input_arr])  # Convert single image to a batch.
        #input_arr=np.reshape(image,(-1,28,28,1))
        #image.show()


        model=keras.models.load_model("C:\\Users\\marco\\NeuralNetwork.h5")
        ##predictions = model.predict(img_array)
        predictions = model.predict(input_arr)
        score = predictions[0]
        batch_size = 64
        class_names = ['A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','del','nothing','space']

        print("Immagine: ", filename)
        print(
            "This image most likely belongs to {} with a {:.2f} percent confidence."
            .format(class_names[np.argmax(score)], 100 * np.max(score))
        )
        print("\n\n")
        
