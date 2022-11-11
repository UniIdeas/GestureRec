##Used by GOOGLE COLAB
import tensorflow as tf

#Path to the neural network to convert to tflite
model_dir="./NeuralNetwork.h5"

model = tf.keras.models.load_model(model_dir)
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()
# Save the model
with open('model.tflite',"wb") as f:
  f.write(tflite_model)
