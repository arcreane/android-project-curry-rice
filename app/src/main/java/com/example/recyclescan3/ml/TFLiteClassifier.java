package com.example.recyclescan3.ml;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;

import com.example.recyclescan3.WasteCategory;

import org.tensorflow.lite.Interpreter;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

public class TFLiteClassifier implements Closeable {

    public static class Result {
        public final String        label;
        public final float         confidence;
        public final WasteCategory category;

        Result(String label, float confidence, WasteCategory category) {
            this.label      = label;
            this.confidence = confidence;
            this.category   = category;
        }
    }


    private final Interpreter  interpreter;
    private final List<String> labels;


    public TFLiteClassifier(Context context) throws IOException {
        interpreter = new Interpreter(loadModelFile(context));
        labels      = loadLabels(context);
    }


    public Result classify(Bitmap bitmap) {
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 224, 224, true);

        float[][][][] input  = new float[1][224][224][3];
        float[][]     output = new float[1][labels.size()];

        for (int y = 0; y < 224; y++) {
            for (int x = 0; x < 224; x++) {
                int px = scaled.getPixel(x, y);
                input[0][y][x][0] = Color.red(px)   / 255f;
                input[0][y][x][1] = Color.green(px) / 255f;
                input[0][y][x][2] = Color.blue(px)  / 255f;
            }
        }

        interpreter.run(input, output);

        int   best  = 0;
        float score = output[0][0];
        for (int i = 1; i < labels.size(); i++) {
            if (output[0][i] > score) { score = output[0][i]; best = i; }
        }

        String label = labels.get(best);
        return new Result(label, score, mapLabel(label));
    }


    @Override
    public void close() {
        interpreter.close();
    }


    private MappedByteBuffer loadModelFile(Context ctx) throws IOException {
        AssetFileDescriptor fd = ctx.getAssets().openFd("model_unquant.tflite");
        FileInputStream fis = new FileInputStream(fd.getFileDescriptor());
        FileChannel ch = fis.getChannel();
        return ch.map(FileChannel.MapMode.READ_ONLY, fd.getStartOffset(), fd.getDeclaredLength());
    }


    private List<String> loadLabels(Context ctx) throws IOException {
        List<String> list = new ArrayList<>();
        BufferedReader br = new BufferedReader(
                new InputStreamReader(ctx.getAssets().open("labels.txt")));
        String line;
        while ((line = br.readLine()) != null) {
            // format: "0 poubelle_jaune" — take the part after the index
            String[] parts = line.trim().split("\\s+", 2);
            if (parts.length == 2) list.add(parts[1]);
        }
        br.close();
        return list;
    }


    private static WasteCategory mapLabel(String label) {
        switch (label) {
            case "poubelle_jaune":               return WasteCategory.RECYCLABLE;
            case "poubelle_verre":               return WasteCategory.RECYCLABLE;
            case "ordures_menageres":            return WasteCategory.GENERAL_WASTE;
            case "decheterie_collecte_speciale": return WasteCategory.HAZARDOUS;
            case "compost_biodechets":           return WasteCategory.COMPOST;
            default:                             return WasteCategory.GENERAL_WASTE;
        }
    }
}
