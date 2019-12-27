package com.cosma;

import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


class Converter {
    private ArrayList<AsciiDataAdv> renderArray = new ArrayList<>();
    private ArrayList<AsciiDataAdv> characterRenderArray = new ArrayList<>();
    private ArrayList<String> characterArray = new ArrayList<>();


    private String[] characters;

    class AsciiDataAdv {
        String character;
        int index;
        int x;
        int y;
        int iL = 0;
        int iR = 0;
        int iC = 0;
        int iU = 0;
        int iD = 0;

        AsciiDataAdv() {
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
            characterArray.add("B");

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

    private void createAsciiImage(String imageName) throws IOException {

        File input = new File(imageName);
        BufferedImage image = ImageIO.read(input);
        BufferedImage finalImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphic = finalImage.createGraphics();
        Font font = new Font("Consolas", Font.PLAIN, 16);
        graphic.setFont(font);
        graphic.setColor(Color.GREEN);
        graphic.drawImage(image, 0, 0, Color.WHITE, null);

        //Calculate height and width ratio
        FontMetrics metrics = graphic.getFontMetrics(font);
        Rectangle2D rect = metrics.getStringBounds("A", graphic);
        int widthRatio = (int) Math.round(finalImage.getWidth() / metrics.getStringBounds("A", graphic).getWidth());
        int heightRatio = (int) Math.round((finalImage.getWidth() / metrics.getStringBounds("A", graphic).getHeight()) * 1.6);

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
                if (i + ySize < finalImage.getHeight() && j + xSize < finalImage.getWidth()) {
                    calculateIntensityOfRectangle(finalImage, j, i, xSize, ySize,false,"",0);
                }
            }
        }

        //Calculate average intensity of every symbol
        int index = 0;
        for (String string : characterArray) {
            fillWhite(finalImage);
            index ++;
            int x = finalImage.getWidth()-100;
            int y = finalImage.getHeight()-100;
            graphic.drawString(string, x, y+ySize);
            calculateIntensityOfRectangle(finalImage, x, y, xSize, ySize,true,string,index);
            fillWhite(finalImage);
        }

        //Draw string
        for(AsciiDataAdv asciiDataAdv: renderArray){
            findClosestCharacter(asciiDataAdv,graphic);
        }



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

    private int test(int l1, int l2){
        int x = Math.abs(l1 - l2);
        if(x == 0){
            return 10;
        }
        if(x > 0 && x < 10){
            return 9;
        }
        if(x > 10 && x < 20){
            return 8;
        }
        if(x > 20 && x < 30){
            return 7;
        }
        if(x > 30 && x < 40){
            return 6;
        }
        if(x > 40 && x < 55){
            return 5;
        }
        if(x > 55 && x < 70){
            return 4;
        }
        if(x > 70 && x < 90){
            return 3;
        }
        if(x > 90 && x < 130){
            return 2;
        }
        if(x >130 && x < 160){
            return 1;
        }

        return 0;
    }


    private void findClosestCharacter(AsciiDataAdv asciiDataAdv,Graphics2D graphic){


        HashMap<Integer,String> map = new HashMap<Integer,String>();
        System.out.println("-------------------------------------------------");
        for(AsciiDataAdv characters: characterRenderArray){
            int totalPoint = 0;
            totalPoint += test(asciiDataAdv.iL,characters.iL);
            totalPoint += test(asciiDataAdv.iR,characters.iR);
            totalPoint += test(asciiDataAdv.iU,characters.iU);
            totalPoint += test(asciiDataAdv.iD,characters.iD);
            map.put(totalPoint,characters.character);
            System.out.println(totalPoint);
        }


        System.out.println("-------------------------------------------------");

        TreeMap<Integer, String> treeMap = new TreeMap<Integer, String>(map);
        System.out.println(treeMap.lastEntry());
        graphic.drawString(treeMap.lastEntry().getValue(),asciiDataAdv.x,asciiDataAdv.y);
    }

    private void calculateIntensityOfRectangle(BufferedImage finalImage, int x, int y, int xSize, int ySize,boolean calculateCharacter, String string,int index) {
        AsciiDataAdv asciiDataAdv = new AsciiDataAdv();

        asciiDataAdv.x = x;
        asciiDataAdv.y = y;
        asciiDataAdv.index = index;

        //calculate left side
        int totalPixel = 0;
        for (int y1 = asciiDataAdv.y; y1 < asciiDataAdv.y + ySize; y1++) {
            for (int x1 = asciiDataAdv.x; x1 < asciiDataAdv.x + (xSize * 0.33f); x1++) {
                Color c = new Color(finalImage.getRGB(x1, y1));
                int luminance = Math.round(c.getRed() * 0.21f + c.getGreen() * 0.71f + c.getBlue() * 0.07f);
                asciiDataAdv.iL = asciiDataAdv.iL + luminance;
                totalPixel++;
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
            }
        }
        asciiDataAdv.iR = asciiDataAdv.iR / totalPixel;

        //calculate up side

        totalPixel = 0;
        for (int x1 = asciiDataAdv.x; x1 < asciiDataAdv.x + xSize; x1++) {
            for (int y1 = asciiDataAdv.y; y1 < asciiDataAdv.y + (ySize * 0.33f); y1++) {
                Color c = new Color(finalImage.getRGB(x1, y1));
                int luminance = Math.round(c.getRed() * 0.21f + c.getGreen() * 0.71f + c.getBlue() * 0.07f);
                asciiDataAdv.iU = asciiDataAdv.iU + luminance;
                totalPixel++;
            }
        }
        asciiDataAdv.iU = asciiDataAdv.iU / totalPixel;

        //calculate down side

        totalPixel = 0;
        for (int x1 = asciiDataAdv.x; x1 < asciiDataAdv.x + xSize; x1++) {
            for (int y1 = asciiDataAdv.y + ySize; y1 > asciiDataAdv.y + (ySize * 0.66); y1--) {
                    Color c = new Color(finalImage.getRGB(x1, y1));
                    int luminance = Math.round(c.getRed() * 0.21f + c.getGreen() * 0.71f + c.getBlue() * 0.07f);
                    asciiDataAdv.iD = asciiDataAdv.iD + luminance;
                    totalPixel++;
            }
        }
        if(totalPixel != 0){
            asciiDataAdv.iD = asciiDataAdv.iD / totalPixel;
        }
        if(!calculateCharacter){
            renderArray.add(asciiDataAdv);
        }else{
            asciiDataAdv.character = string;
            characterRenderArray.add(asciiDataAdv);
        }


    }


    private void fillWhite(BufferedImage finalImage) {
        for (int i = 0; i < finalImage.getHeight(); i++) {
            for (int j = 0; j < finalImage.getWidth(); j++) {
                Color color = new Color(0, 0, 255, 0);
                finalImage.setRGB(j, i, Color.white.getRGB());
            }
        }
    }
}
