package org.techtown.naro;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.appcompat.app.AppCompatActivity;

import org.techtown.naro.ml.ConvertedModel;
import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClassifyImage{
    private int imageSize = 229;
    public String result_class;
    Bitmap bitmap_img;
    String[] classes;

    public ClassifyImage(Bitmap bitmap_img, int imageSize, String[] classes, Context context) {
        this.bitmap_img = bitmap_img;
        this.imageSize = imageSize;
        this.classes = classes;

        this.result_class = classify(get_confidence(bitmap_img, context));
    }

    public String getResult_class(){
        return this.result_class;
    }

// image = Bitmap.createScaledBitmap(image, imageSize, imageSize, false);
// classifyImage(image);

    public String classify(HashMap<String, Float> confidence_map) {
        List<String> classes = new ArrayList<String>(confidence_map.keySet());
        List<Float> confidences = new ArrayList<Float>(confidence_map.values());

        // find the index of the class with the biggest confidence.
        int maxPos = 0;
        float maxConfidence = 0;
        for (int i = 0; i < confidences.size(); i++) {
            if (confidences.get(i) > maxConfidence) {
                maxConfidence = confidences.get(i);
                maxPos = i;
            }
        }
        return classes.get(maxPos);
    }

    public HashMap<String, Float> get_confidence(Bitmap bitmap_img, Context context) {
        HashMap<String, Float> result_confidence = new HashMap<String, Float>();
        try {
            ConvertedModel model = ConvertedModel.newInstance(context);

            // Creates inputs for reference.
            TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 299, 299, 3}, DataType.FLOAT32);
            ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * imageSize * imageSize * 3); //4: int의 byte
            byteBuffer.order(ByteOrder.nativeOrder());

            int[] intValues = new int[imageSize * imageSize];
            bitmap_img.getPixels(intValues, 0, bitmap_img.getWidth(), 0, 0, bitmap_img.getWidth(), bitmap_img.getHeight());
            int pixel = 0;
            //iterate over each pixel and extract R, G, and B values. Add those values individually to the byte buffer.
            for (int i = 0; i < imageSize; i++) {
                for (int j = 0; j < imageSize; j++) {
                    int val = intValues[pixel++]; // RGB
                    byteBuffer.putFloat(((val >> 16) & 0xFF) * (1.f / 255) * 2 - 1);
                    byteBuffer.putFloat(((val >> 8) & 0xFF) * (1.f / 255) * 2 - 1);
                    byteBuffer.putFloat((val & 0xFF) * (1.f / 255) * 2 - 1);
                }
            }

            inputFeature0.loadBuffer(byteBuffer);

            // Runs model inference and gets result.
            ConvertedModel.Outputs outputs = model.process(inputFeature0);
            TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

            float[] confidences = outputFeature0.getFloatArray();


            for (int i = 0; i < confidences.length; i++) {
                result_confidence.put(classes[i], confidences[i]);
            }

            // Releases model resources if no longer used.
            model.close();
        } catch (IOException e) {
            // TODO Handle the exception
        }
        return result_confidence;
    }

}