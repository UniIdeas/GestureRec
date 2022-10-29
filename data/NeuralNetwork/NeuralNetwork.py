import os
#Add your path to the DLL search path.
ddlpath="C:\\Program Files\\NVIDIA GPU Computing Toolkit\\CUDA\\v11.2\\bin"
os.add_dll_directory(ddlpath)

import cv2

from tensorflow.keras.callbacks import ModelCheckpoint
from tensorflow.keras.callbacks import EarlyStopping
import numpy as np


import tensorflow as tf
assert tf.__version__.startswith('2')

from tensorflow import keras
from tensorflow.keras import layers
from keras import models
from keras import utils
import matplotlib.pyplot as plt
from keras import regularizers
from tensorflow.keras.losses import SparseCategoricalCrossentropy

from keras.layers.core import Dense, Activation, Flatten

from sklearn.metrics import accuracy_score
from keras.layers import Conv2D, BatchNormalization, MaxPooling2D, ZeroPadding2D, Dropout
#CONV-> BatchNorm-> RELU block

#Here you can specify the resolution of the images
batch_size = 64
img_height = 200
img_width = 200
IMG_SIZE=200



def get_file_size(file_path):
    size = os.path.getsize(file_path)
    return size

def convert_bytes(size, unit=None):
    if unit == "KB":
        return print('File size: ' + str(round(size / 1024, 3)) + ' Kilobytes')
    elif unit == "MB":
        return print('File size: ' + str(round(size / (1024 * 1024), 3)) + ' Megabytes')
    else:
        return print('File size: ' + str(size) + ' bytes')

#image_path is the path that contains the images used to train the model
image_path = ""




##for other information: https://keras.rstudio.com/reference/fit.html

train_ds = tf.keras.preprocessing.image_dataset_from_directory(
  image_path,
  validation_split=0.2,
  subset="training",
  seed=123,
  image_size=(img_height, img_width),##here we apply the resize of the image
  batch_size=batch_size,
  shuffle=True)
  

val_ds = tf.keras.preprocessing.image_dataset_from_directory(
  image_path,
  validation_split=0.2,
  subset="validation",
  seed=123,
  image_size=(img_height, img_width),
  batch_size=batch_size,
  shuffle=True)

class_names = train_ds.class_names
print(class_names)

num_classes = len(class_names)

##Configure the performance dataset
# Configura il set di dati per le prestazioni

##Dataset.cache keeps the images in memory after they're loaded off disk during the first epoch. 
# This will ensure the dataset does not become a bottleneck while training your model. 
# If your dataset is too large to fit into memory, you can also use this method to create a performant on-disk cache.
#Dataset.prefetch overlaps data preprocessing and model execution while training.
AUTOTUNE = tf.data.AUTOTUNE

train_ds = train_ds.shuffle(1000).cache().prefetch(buffer_size=AUTOTUNE)
val_ds = val_ds.cache().prefetch(buffer_size=AUTOTUNE)


num_classes = len(class_names)




checkpoint = ModelCheckpoint("NeuralNetwork.h5",#The model is called NeuralNetwork.h5
                             monitor="val_loss",
                             mode="min",
                             save_best_only=True,
                             verbose=1)

earlystop = EarlyStopping(monitor='val_loss',
                          restore_best_weights=True,
                          patience=5, 
                          verbose=1)

reduce_lr = tf.keras.callbacks.ReduceLROnPlateau(monitor='val_loss', factor=0.2,
                              patience=3, min_lr=0.001)

callbacks = [reduce_lr,earlystop,checkpoint]

'''
##Data Augmentation //we don't use this
data_augmentation = keras.Sequential(
  [
    layers.RandomFlip("horizontal",input_shape=(img_height,img_width,3)),
    layers.RandomRotation(0.1),
    layers.RandomZoom(0.1),
  ]
)'''

##Another way to create the model in which Augmentation and dropout is applied
'''
model = keras.models.Sequential([
  data_augmentation,
  layers.Rescaling(1./255, input_shape=(img_height, img_width, 3)),
  layers.Conv2D(16, 3, padding='same', activation='relu'),
  layers.MaxPooling2D(),
  layers.Conv2D(32, 3, padding='same', activation='relu'),
  layers.MaxPooling2D(),
  layers.Conv2D(64, 3, padding='same', activation='relu'),
  layers.MaxPooling2D(),
  layers.Dropout(0.2),
  layers.Flatten(),
  layers.Dense(512, activation='relu'),##non 512 perchè erano 30.000.000 di parametri e la rete risultava piu pesante
  layers.Dense(num_classes)
])

##Compile the model

model.compile(optimizer='adam',
              loss=tf.keras.losses.SparseCategoricalCrossentropy(from_logits=True),
              metrics=['accuracy'])'''
'''
def keras_cnn_v2(input_shape):
    #input layer
    X_input = Input(input_shape)
    #32 filters, with 5x5 kernel size
    X = conv_bn_relu_block(X_input, 10, (5, 5))
    #Maxpooling and dropout
    X = MaxPooling2D((2, 2))(X)
    X = Dropout(0.5)(X)
    #run another CONV -> BN -> RELU block
    X = ZeroPadding2D((1, 1))(X)
    X = conv_bn_relu_block(X, 15)
    X = MaxPooling2D((2, 2))(X)
    X = Dropout(0.5)(X)
    #run another CONV -> BN -> RELU block
    X = ZeroPadding2D((1, 1))(X)
    X = conv_bn_relu_block(X, 20)
    X = MaxPooling2D((2, 2))(X)
    X = Dropout(0.5)(X)
    #flatten
    X = Flatten()(X)
    #dense layer
    X = Dense(num_classes, activation='softmax')(X)
    model = models.Model(inputs = X_input, outputs = X, name='keras_lr')
    return model
model = keras_cnn_v2((200, 200, 3))
'''

input_shape=(img_height, img_width, 3)
'''
##foundation model
model=tf.keras.applications.resnet50.ResNet50(
    include_top=True, weights=None, input_tensor=None,
    input_shape=input_shape, classes=4,##poi mettere 29
    classifier_activation=None
)'''


mobile=tf.keras.applications.MobileNet(
    input_shape=input_shape,
    alpha=1.0,
    include_top=True,
    weights=None,
    classes=num_classes,
    classifier_activation="softmax"
)


# Keras will add a input for the model behind the scene.
model = models.Sequential([
  layers.Rescaling(1./255, input_shape=(img_height, img_width, 3)),
  mobile
])



model.compile(optimizer = 'adam', 
              loss = tf.keras.losses.SparseCategoricalCrossentropy(from_logits=False), 
              metrics = ["accuracy"])


##View all the layers of the network using the model's Model.summary method
model.summary()

##Train the model
epochs=10
history = model.fit(
  train_ds,
  validation_data=val_ds,
  callbacks=callbacks,
  epochs=epochs,
)

##Visualize training results
##Create plots of loss and accuracy on the training and validation sets:

acc = history.history['accuracy']
val_acc = history.history['val_accuracy']

loss = history.history['loss']
val_loss = history.history['val_loss']

##There are multiple ways to fight overfitting in the training process. In this tutorial, you'll use data augmentation and add Dropout to your model

##Visualize training results
#After applying data augmentation and tf.keras.layers.Dropout, there is less overfitting than before, and training and validation accuracy are closer aligned:

'''
acc = history.history['accuracy']
val_acc = history.history['val_accuracy']

loss = history.history['loss']
val_loss = history.history['val_loss']

epochs_range = range(epochs)

plt.figure(figsize=(8, 8))
plt.subplot(1, 2, 1)
plt.plot(epochs_range, acc, label='Training Accuracy')
plt.plot(epochs_range, val_acc, label='Validation Accuracy')
plt.legend(loc='lower right')
plt.title('Training and Validation Accuracy')

plt.subplot(1, 2, 2)
plt.plot(epochs_range, loss, label='Training Loss')
plt.plot(epochs_range, val_loss, label='Validation Loss')
plt.legend(loc='upper right')
plt.title('Training and Validation Loss')
plt.show()
'''

#Instruction to save the model
keras_file = "NeuralNetwork.h5"
model.save(keras_file)

convert_bytes(get_file_size(keras_file), "MB")
