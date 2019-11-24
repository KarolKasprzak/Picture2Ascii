package com.cosma;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


class Converter {

    class AsciiData {
        int x;
        int y;
        float luminance;

        AsciiData(int x, int y, float lu) {
            this.luminance = lu;
            this.x = x;
            this.y = y;
        }

    }


    Converter() throws IOException {

        //Find all images

        try (Stream<Path> walk = Files.walk(Paths.get(""))) {

            List<String> result = walk.map(x -> x.toString())
                    .filter(f -> f.endsWith(".jpg")).collect(Collectors.toList());
            System.out.println(result.size() + " images found");
            System.out.println("start processing..");
            int total = result.size();
            int currentTotal = 0;
            for (String name : result) {
                createAsciiImage(name);
                currentTotal ++;
                System.out.println("done " + currentTotal+"/"+total);
            }
            System.out.println("finish");


        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private void createAsciiImage(String imageName) throws IOException {

        File input = new File(imageName);
        BufferedImage image = ImageIO.read(input);
        BufferedImage result = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        Graphics2D graphic = result.createGraphics();
        Font font = new Font("Droid Sans Mono", Font.PLAIN, 9);
        graphic.setFont(font);
        graphic.setColor(Color.GREEN);

        graphic.drawImage(image, 0, 0, Color.WHITE, null);
        ArrayList<AsciiData> asciiArray = new ArrayList<>();

        //Convert to grayscale & collect date to draw

        int pH = 8;
        int pW = 6;
        for (int i = 0; i < result.getHeight() / pH; i++) {
            for (int j = 0; j < result.getWidth() / pW; j++) {
                float averageBrightness = 0;

                int lastX = 0;
                int lastY = 0;

                for (int i1 = i * pH; i1 < i * pH + pH; i1++) {
                    for (int j1 = j * pW; j1 < j * pW + pW; j1++) {
                        Color c = new Color(result.getRGB(j1, i1));
                        float luminance = (c.getRed() * 0.2126f + c.getGreen() * 0.7152f + c.getBlue() * 0.0722f) / 255;
                        averageBrightness = averageBrightness + luminance;
                        lastX = j1;
                        lastY = i1;
                    }
                }
                averageBrightness = averageBrightness / (pH * pW);
                averageBrightness = round(averageBrightness, 1);
                AsciiData asciiData = new AsciiData(lastX, lastY, averageBrightness);
                asciiArray.add(asciiData);
            }
        }

        //Draw black background
        for (int i = 0; i < result.getHeight(); i++) {
            for (int j = 0; j < result.getWidth(); j++) {
                result.setRGB(j, i, Color.BLACK.getRGB());
            }
        }

        //Draw ascii
        for (AsciiData asciiData : asciiArray) {
            if (asciiData.luminance <= 0.1f && asciiData.luminance >= 0.0f) {
                graphic.drawString("0", asciiData.x, asciiData.y);
            }
            if (asciiData.luminance == 0.2f) {
                graphic.drawString("@", asciiData.x, asciiData.y);
            }
            if (asciiData.luminance == 0.3f) {
                graphic.drawString("$", asciiData.x, asciiData.y);
            }
            if (asciiData.luminance == 0.4f) {
                graphic.drawString("%", asciiData.x, asciiData.y);
            }
            if (asciiData.luminance == 0.5f) {
                graphic.drawString("*", asciiData.x, asciiData.y);
            }
            if (asciiData.luminance == 0.6) {
                graphic.drawString(";", asciiData.x, asciiData.y);
            }
            if (asciiData.luminance == 0.7f) {
                graphic.drawString(",", asciiData.x, asciiData.y);
            }
            if (asciiData.luminance == 0.8f) {
                graphic.drawString("^", asciiData.x, asciiData.y);
            }
            if (asciiData.luminance == 0.9f) {
                graphic.drawString(".", asciiData.x, asciiData.y);
            }
        }

        File output = new File("convert_" + imageName);
        ImageIO.write(result, "png", output);
    }

    private static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
}
