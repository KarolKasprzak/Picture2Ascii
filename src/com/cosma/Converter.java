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
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


class Converter {
    private int heightRatio = 10;
    private int widthRatio = 10;
    private ArrayList<AsciiDataAdv> renderArray = new ArrayList<>();

    private String[] characters;

    class AsciiData {
        int x;
        int y;
        int luminance;

        AsciiData(int x, int y, int lu) {
            this.luminance = lu;
            this.x = x;
            this.y = y;
        }

    }
    class AsciiDataAdv {
        int x;
        int y;
        int iL = 0;
        int iR = 0;
        int iC = 0;
        int iU = 0;
        int iD = 0;

        AsciiDataAdv(){
        }

        AsciiDataAdv(int x, int y, int iL, int iR, int iC, int iU, int iD) {
            this.iL = iL;
            this.iR = iR;
            this.iC = iC;
            this.iU = iU;
            this.iD = iD;
            this.x = x;
            this.y = y;
        }

    }


    Converter() throws IOException {


        //Find all images
        try (Stream<Path> walk = Files.list(Paths.get(""))) {
            characters = new String[]{" ", ".", ":", ",", ";", "o", "x", "%", "#", "@"};
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

    private void calculateIntensityAndShape(BufferedImage finalImage,int x, int y, int xSize, int ySize){
        AsciiDataAdv asciiDataAdv = new AsciiDataAdv();
        asciiDataAdv.x = x*widthRatio;
        asciiDataAdv.y = y*heightRatio;
        //calculate left side
        for(int x1 = asciiDataAdv.x; x1 < asciiDataAdv.x + (xSize*0.33f); x1++){
            for (int y1 = asciiDataAdv.y; y1 < asciiDataAdv.y + ySize; y1++){
                finalImage.setRGB(x1, y1, Color.RED.getRGB());
            }
        }
        //calculate right side
        for(int x1 = asciiDataAdv.x+(Math.round(xSize-xSize*0.33f)); x1 < asciiDataAdv.x + xSize; x1++){
            for (int y1 = asciiDataAdv.y; y1 < asciiDataAdv.y + ySize; y1++){
                if(x1 < finalImage.getWidth() ){
                    finalImage.setRGB(x1, y1, Color.BLUE.getRGB());
                }
            }
        }
        //calculate down side
        for(int x1 = asciiDataAdv.x; x1 < asciiDataAdv.x + xSize; x1++){
            for (int y1 = asciiDataAdv.y; y1 < asciiDataAdv.y + (ySize*0.33f); y1++){
                    finalImage.setRGB(x1, y1, Color.YELLOW.getRGB());
            }
        }


//        System.out.println("-------------------------------------------------------" );
        renderArray.add(asciiDataAdv);
    }


    private void createAsciiImage(String imageName) throws IOException {



        File input = new File(imageName);
        BufferedImage image = ImageIO.read(input);
        BufferedImage finalImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_RGB);

        Graphics2D graphic = finalImage.createGraphics();
        Font font = new Font("Consolas", Font.PLAIN, 8);
        FontMetrics metrics = graphic.getFontMetrics(font);


        graphic.setFont(font);
        graphic.setColor(Color.GREEN);

        graphic.drawImage(image, 0, 0, Color.WHITE, null);
        ArrayList<AsciiData> asciiArray = new ArrayList<>();

        //Convert to grayscale & collect date to draw




        //Calculate average intensity of every rectangular

        for (int i = 0; i < finalImage.getHeight() / heightRatio; i++) {
            for (int j = 0; j < finalImage.getWidth() / widthRatio; j++) {

                int xSize = Math.round(finalImage.getWidth() / widthRatio);
                int ySize = Math.round(finalImage.getHeight() / heightRatio);

                calculateIntensityAndShape(finalImage,j,i,xSize,ySize);






                float averageBrightness = 0;
                int lastX = 0;
                int lastY = 0;
                for (int i1 = i * heightRatio; i1 < i * heightRatio + heightRatio; i1++) {
                    for (int j1 = j * widthRatio; j1 < j * widthRatio + widthRatio; j1++) {
                        Color c = new Color(finalImage.getRGB(j1, i1));
//                        float luminance = (c.getRed() * 0.21f + c.getGreen() * 0.71f + c.getBlue() * 0.07f) / 255;
                        float luminance = (c.getRed() * 0.21f + c.getGreen() * 0.71f + c.getBlue() * 0.07f);
                        averageBrightness = averageBrightness + luminance;
                        lastX = j1;
                        lastY = i1;
                    }
                }
                averageBrightness = averageBrightness / (heightRatio * widthRatio);
                averageBrightness = round(averageBrightness, 1);
                AsciiData asciiData = new AsciiData(lastX, lastY, Math.round(averageBrightness));
                asciiArray.add(asciiData);
            }
        }

        //Draw black background
//        for (int i = 0; i < finalImage.getHeight(); i++) {
//            for (int j = 0; j < finalImage.getWidth(); j++) {
//                finalImage.setRGB(j, i, Color.BLACK.getRGB());
//            }
//        }

        //Draw ascii
        for (AsciiData asciiData : asciiArray) {

            graphic.drawString(characters[Math.round((255-asciiData.luminance)*10/256)],asciiData.x,asciiData.y);

//            graphic.drawString(String.valueOf(asciiData.luminance), asciiData.x, asciiData.y);
//            if (asciiData.luminance <= 0.2f && asciiData.luminance >= 0.08f) {
//                graphic.drawString("''", asciiData.x, asciiData.y);
//                continue;
//            }
//            if (asciiData.luminance <= 0.6f && asciiData.luminance >= 0.35f) {
//                graphic.drawString("j", asciiData.x, asciiData.y);
//                continue;
//            }
//            if (asciiData.luminance <= 0.65f && asciiData.luminance >= 0.6f) {
//                graphic.drawString("/", asciiData.x, asciiData.y);
//                continue;
//            }
//            if (asciiData.luminance <= 0.75f && asciiData.luminance >= 0.65f) {
//                graphic.drawString("^", asciiData.x, asciiData.y);
//                continue;
//            }
//            if (asciiData.luminance <= 0.85f && asciiData.luminance >= 0.75f) {
//                graphic.drawString(";", asciiData.x, asciiData.y);
//                continue;
//            }
//            if (asciiData.luminance <= 0.9f && asciiData.luminance >= 0.85f) {
//                graphic.drawString(",", asciiData.x, asciiData.y);
//                continue;
//            }
//            if (asciiData.luminance <= 0.97f && asciiData.luminance >= 0.9f) {
//                graphic.drawString(".", asciiData.x, asciiData.y);
//
//            }

        }

        File f = new File("convert_" + imageName);


        if(f.exists()) {
            if(f.delete())
            {
                System.out.println("file exist");
            }
            File output1 = new File("convert_" + imageName);
            ImageIO.write(finalImage, "png", output1);
        }
    }

    private static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
}
