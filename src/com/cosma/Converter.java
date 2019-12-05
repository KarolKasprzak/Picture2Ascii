package com.cosma;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


class Converter {

    private HashMap<Integer,String> asciiMap;


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

        asciiMap = new HashMap<>();
        String[] characters = {" ",".",":",",",";","o","x","%","#","@"};

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
        Font font = new Font("Consolas", Font.PLAIN, 8);
        graphic.setFont(font);
        graphic.setColor(Color.GREEN);

        graphic.drawImage(image, 0, 0, Color.WHITE, null);
        ArrayList<AsciiData> asciiArray = new ArrayList<>();

        //Convert to grayscale & collect date to draw

        int pH = 12;
        int pW = 14;
        for (int i = 0; i < result.getHeight() / pH; i++) {
            for (int j = 0; j < result.getWidth() / pW; j++) {
                float averageBrightness = 0;

                int lastX = 0;
                int lastY = 0;

                for (int i1 = i * pH; i1 < i * pH + pH; i1++) {
                    for (int j1 = j * pW; j1 < j * pW + pW; j1++) {
                        Color c = new Color(result.getRGB(j1, i1));
//                        float luminance = (c.getRed() * 0.21f + c.getGreen() * 0.71f + c.getBlue() * 0.07f) / 255;
                        float luminance = (c.getRed() * 0.21f + c.getGreen() * 0.71f + c.getBlue() * 0.07f);
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
//            graphic.drawString(String.valueOf(asciiData.luminance), asciiData.x, asciiData.y);
//            if (asciiData.luminance <= 0.2f && asciiData.luminance >= 0.08f) {
//                graphic.drawString("''", asciiData.x, asciiData.y);
//                continue;
//            }
            if (asciiData.luminance <= 0.6f && asciiData.luminance >= 0.35f) {
                graphic.drawString("j", asciiData.x, asciiData.y);
                continue;
            }
            if (asciiData.luminance <= 0.65f && asciiData.luminance >= 0.6f) {
                graphic.drawString("/", asciiData.x, asciiData.y);
                continue;
            }
            if (asciiData.luminance <= 0.75f && asciiData.luminance >= 0.65f) {
                graphic.drawString("^", asciiData.x, asciiData.y);
                continue;
            }
            if (asciiData.luminance <= 0.85f && asciiData.luminance >= 0.75f) {
                graphic.drawString(";", asciiData.x, asciiData.y);
                continue;
            }
            if (asciiData.luminance <= 0.9f && asciiData.luminance >= 0.85f) {
                graphic.drawString(",", asciiData.x, asciiData.y);
                continue;
            }
            if (asciiData.luminance <= 0.97f && asciiData.luminance >= 0.9f) {
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
