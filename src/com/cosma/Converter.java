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
    private ArrayList<String> characterArray = new ArrayList<>();


    private String[] characters;

    class AsciiDataAdv {
        int x;
        int y;
        int iL = 0;
        int iR = 0;
        int iC = 0;
        int iU = 0;
        int iD = 0;

        AsciiDataAdv() {
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

    class CharacterDate extends AsciiDataAdv {
        String character;

        CharacterDate() {
        }


    }

    Converter() throws IOException {

        //Find all images
        try (Stream<Path> walk = Files.list(Paths.get(""))) {
            characterArray.add(" ");
            characterArray.add(".");
            characterArray.add(",");
            characterArray.add(":");
            characterArray.add("o");
            characterArray.add("x");
            characterArray.add("%");
            characterArray.add("#");
            characterArray.add("@");
            characterArray.add("A");
            characterArray.add("B");
            characterArray.add("F");
            characterArray.add("!");
            characterArray.add("*");
            characterArray.add("-");
            characterArray.add("/");
            characterArray.add(";");

            List<String> result = walk.map(x -> x.toString())
                    .filter(f -> f.endsWith(".jpg")).collect(Collectors.toList());
            System.out.println(result.size() + " images found");
            System.out.println("start processing..");

            int total = result.size();
            int currentTotal = 0;
            for (String name : result) {
                createAsciiImage(name);
                currentTotal++;
                System.out.println("done " + currentTotal + "/" + total);
            }
            System.out.println("finish");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void calculateIntensityAndShape(BufferedImage finalImage, int x, int y, int xSize, int ySize) {
        AsciiDataAdv asciiDataAdv = new AsciiDataAdv();

        asciiDataAdv.x = x;
        asciiDataAdv.y = y;

        //calculate left side
        int totalPixel = 0;
        for (int y1 = asciiDataAdv.y; y1 < asciiDataAdv.y + ySize; y1++) {
            for (int x1 = asciiDataAdv.x; x1 < asciiDataAdv.x + (xSize * 0.33f); x1++) {
                Color c = new Color(finalImage.getRGB(x1, y1));
                int luminance = Math.round(c.getRed() * 0.21f + c.getGreen() * 0.71f + c.getBlue() * 0.07f);
                asciiDataAdv.iL = asciiDataAdv.iL + luminance;
                totalPixel++;
                finalImage.setRGB(x1, y1, Color.RED.getRGB());
            }
        }
        asciiDataAdv.iL = asciiDataAdv.iL / totalPixel;

        //calculate right side
        totalPixel = 0;
        for (int y1 = asciiDataAdv.y; y1 < asciiDataAdv.y + ySize; y1++) {
            for (int x1 = asciiDataAdv.x + (Math.round(xSize - xSize * 0.33f)); x1 < asciiDataAdv.x + xSize; x1++) {
                Color c = new Color(finalImage.getRGB(x1, y1));
                int luminance = Math.round(c.getRed() * 0.21f + c.getGreen() * 0.71f + c.getBlue() * 0.07f);
                asciiDataAdv.iR = asciiDataAdv.iR + luminance;
                totalPixel++;
                finalImage.setRGB(x1, y1, Color.YELLOW.getRGB());
            }
        }
        asciiDataAdv.iR = asciiDataAdv.iR / totalPixel;

        //calculate top side

//        totalPixel = 0;
//        for (int x1 = asciiDataAdv.x; x1 < asciiDataAdv.x + xSize; x1++) {
//            for (int y1 = asciiDataAdv.y; y1 < asciiDataAdv.y + (ySize * 0.33f); y1++) {
//                if(y1 < finalImage.getHeight()-ySize && x1 < finalImage.getWidth()){
//                    Color c = new Color(finalImage.getRGB(x1, y1));
//                    int luminance = Math.round(c.getRed() * 0.21f + c.getGreen() * 0.71f + c.getBlue() * 0.07f);
//                    asciiDataAdv.iU = asciiDataAdv.iU + luminance;
//                    totalPixel++;
//                }
//            }
//        }
//        if(totalPixel != 0){
//            asciiDataAdv.iU = asciiDataAdv.iU / totalPixel;
//        }

        //calculate down side

//        totalPixel = 0;
//        for (int x1 = asciiDataAdv.x; x1 < asciiDataAdv.x + xSize; x1++) {
//            for (int y1 = asciiDataAdv.y + ySize; y1 > asciiDataAdv.y + (ySize * 0.66); y1--) {
//                if (y1 < finalImage.getHeight() && x1 < finalImage.getWidth()) {
//                    Color c = new Color(finalImage.getRGB(x1, y1));
//                    int luminance = Math.round(c.getRed() * 0.21f + c.getGreen() * 0.71f + c.getBlue() * 0.07f);
//                    asciiDataAdv.iD = asciiDataAdv.iD + luminance;
//                    totalPixel++;
//                }
//            }
//        }
//        if(totalPixel != 0){
//            asciiDataAdv.iD = asciiDataAdv.iD / totalPixel;
//        }

//        renderArray.add(asciiDataAdv);
    }


    private void createAsciiImage(String imageName) throws IOException {

        File input = new File(imageName);
        BufferedImage image = ImageIO.read(input);
        BufferedImage finalImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D graphic = finalImage.createGraphics();
        Font font = new Font("Consolas", Font.PLAIN, 9);
        FontMetrics metrics = graphic.getFontMetrics(font);


        graphic.setFont(font);
        graphic.setColor(Color.GREEN);

        graphic.drawImage(image, 0, 0, Color.WHITE, null);

        //Convert image to grayscale
        for (int y = 0; y < finalImage.getHeight(); y++) {
            for (int x = 0; x < finalImage.getWidth(); x++) {
                int p = finalImage.getRGB(x, y);

                int a = (p >> 24) & 0xff;
                int r = (p >> 16) & 0xff;
                int g = (p >> 8) & 0xff;
                int b = p & 0xff;

                int avg = (r + g + b) / 3;
                p = (a << 24) | (avg << 16) | (avg << 8) | avg;

                finalImage.setRGB(x, y, p);
            }
        }


        //Calculate average intensity of every rectangular
        int xSize = Math.round(finalImage.getWidth() / widthRatio);
        int ySize = Math.round(finalImage.getHeight() / heightRatio);

        for (int i = 0; i < finalImage.getHeight(); i += ySize) {
            for (int j = 0; j < finalImage.getWidth(); j += xSize) {

                calculateIntensityAndShape(finalImage, j, i, xSize, ySize);
            }
        }

        //Calculate average intensity of every symbol
        for (String string : characterArray) {
//            fillWhite(finalImage);
            int x = finalImage.getWidth() - xSize;
            int y = finalImage.getHeight() - ySize;
            graphic.drawString(string, x, y);
        }
        graphic.drawString("asdsdadsa", 0, 0);

        //Draw black background
//        for (int i = 0; i < finalImage.getHeight(); i++) {
//            for (int j = 0; j < finalImage.getWidth(); j++) {
//                finalImage.setRGB(j, i, Color.BLACK.getRGB());
//            }
//        }

//
        File output1 = new File("convert_" + imageName);
        ImageIO.write(finalImage, "png", output1);

//        if (f.exists()) {
//            if (f.delete()) {
//                System.out.println("file exist");
//            }
//            File output1 = new File("convert_" + imageName);
//            ImageIO.write(finalImage, "png", output1);
//        }
    }

    private void fillWhite(BufferedImage finalImage) {
        for (int i = 0; i < finalImage.getHeight(); i++) {
            for (int j = 0; j < finalImage.getWidth(); j++) {
                Color color = new Color(0, 0, 255, 0);
                finalImage.setRGB(j, i, Color.white.getRGB());
            }
        }
    }


    private static float round(float d, int decimalPlace) {
        BigDecimal bd = new BigDecimal(Float.toString(d));
        bd = bd.setScale(decimalPlace, BigDecimal.ROUND_HALF_UP);
        return bd.floatValue();
    }
}
