package com.example.pos_printer;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Color;

public class Utils {

    // UNICODE 0x23 = #
    public static final byte[] UNICODE_TEXT = new byte[] { 0x23, 0x23, 0x23,
            0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23,
            0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23, 0x23,
            0x23, 0x23, 0x23 };

    private static final String hexStr = "0123456789ABCDEF";
    private static final String[] binaryArray = { "0000", "0001", "0010", "0011",
            "0100", "0101", "0110", "0111", "1000", "1001", "1010", "1011",
            "1100", "1101", "1110", "1111" };

    public static byte[] decodeBitmap(Bitmap bmp) {
        int bmpWidth = bmp.getWidth();
        int bmpHeight = bmp.getHeight();

        List<String> list = new ArrayList<String>(); // binaryString list
        StringBuffer sb;

        int bitLen = bmpWidth / 8;
        int zeroCount = bmpWidth % 8;

        String zeroStr = "";
        if (zeroCount > 0) {
            bitLen = bmpWidth / 8 + 1;
            for (int i = 0; i < (8 - zeroCount); i++) {
                zeroStr = zeroStr + "0";
            }
        }

        for (int i = 0; i < bmpHeight; i++) {
            sb = new StringBuffer();
            for (int j = 0; j < bmpWidth; j++) {
                int color = bmp.getPixel(j, i);

                int r = (color >> 16) & 0xff;
                int g = (color >> 8) & 0xff;
                int b = color & 0xff;

                // if color close to white，bit='0', else bit='1'
                if (r > 160 && g > 160 && b > 160)
                    sb.append("0");
                else
                    sb.append("1");
            }
            if (zeroCount > 0) {
                sb.append(zeroStr);
            }
            list.add(sb.toString());
        }

        List<String> bmpHexList = binaryListToHexStringList(list);
        String commandHexString = "1D763000";
        String widthHexString = Integer
                .toHexString(bmpWidth % 8 == 0 ? bmpWidth / 8
                        : (bmpWidth / 8 + 1));
        if (widthHexString.length() > 10) {
            Log.e("decodeBitmap error", " width is too large");
            return null;
        } else if (widthHexString.length() == 1) {
            widthHexString = "0" + widthHexString;
        }
        widthHexString = widthHexString + "00";

        String heightHexString = Integer.toHexString(bmpHeight);
        if (heightHexString.length() > 10) {
            Log.e("decodeBitmap error", " height is too large");
            return null;
        } else if (heightHexString.length() == 1) {
            heightHexString = "0" + heightHexString;
        }
        heightHexString = heightHexString + "00";

        List<String> commandList = new ArrayList<String>();
        commandList.add(commandHexString + widthHexString + heightHexString);
        commandList.addAll(bmpHexList);

        return hexList2Byte(commandList);
    }

    public static List<String> binaryListToHexStringList(List<String> list) {
        List<String> hexList = new ArrayList<String>();
        for (String binaryStr : list) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < binaryStr.length(); i += 8) {
                String str = binaryStr.substring(i, i + 8);

                String hexString = myBinaryStrToHexString(str);
                sb.append(hexString);
            }
            hexList.add(sb.toString());
        }
        return hexList;

    }

    public static String myBinaryStrToHexString(String binaryStr) {
        String hex = "";
        String f4 = binaryStr.substring(0, 4);
        String b4 = binaryStr.substring(4, 8);
        for (int i = 0; i < binaryArray.length; i++) {
            if (f4.equals(binaryArray[i]))
                hex += hexStr.substring(i, i + 1);
        }
        for (int i = 0; i < binaryArray.length; i++) {
            if (b4.equals(binaryArray[i]))
                hex += hexStr.substring(i, i + 1);
        }

        return hex;
    }

    public static byte[] hexList2Byte(List<String> list) {
        List<byte[]> commandList = new ArrayList<byte[]>();

        for (String hexStr : list) {
            commandList.add(hexStringToBytes(hexStr));
        }

        byte[] bytes = sysCopy(commandList);
        return bytes;
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    public static byte[] sysCopy(List<byte[]> srcArrays) {
        int len = 0;
        for (byte[] srcArray : srcArrays) {
            len += srcArray.length;
        }
        byte[] destArray = new byte[len];
        int destLen = 0;
        for (byte[] srcArray : srcArrays) {
            System.arraycopy(srcArray, 0, destArray, destLen, srcArray.length);
            destLen += srcArray.length;
        }
        return destArray;
    }

    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    // Resize Image based on printer Paper Size Like 58 mm , 55 mm

    // public static Bitmap resizeImage(String imagePath) {
    // Log.i("ResizeImage", "resizeImage: " + imagePath);
    // try {
    // Log.i("Resize image", "Resizing image-===: " + imagePath);
    // Bitmap originalBitmap = BitmapFactory.decodeFile(imagePath);

    // System.out.println("Original Bitmap width: " + originalBitmap.getWidth());
    // System.out.println("Original Bitmap height: " + originalBitmap.getHeight());

    // // Set the target width for the printer
    // int printerWidth = 58; // in millimeters
    // int printerDPI = 175; // dots per inch, This will maintain width

    // // Convert the target width from millimeters to pixels (assuming 72 pixels
    // per inch density)
    // int targetWidthPixels = (int) (printerWidth / 25.4 * printerDPI);

    // int originalWidth = originalBitmap.getWidth();
    // int originalHeight = originalBitmap.getHeight();

    // // Calculate the target height while maintaining the aspect ratio
    // int targetHeight = (int) (originalHeight * ((float) targetWidthPixels /
    // originalWidth));

    // Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap,
    // targetWidthPixels, targetHeight, true);

    // return resizedBitmap;

    // } catch (Exception e) {
    // System.out.println("Error resizing image: " + e.getMessage());
    // }
    // return null;
    // }
    public static Bitmap resizeImage(String imagePath) {
        try {
            Bitmap originalBitmap = BitmapFactory.decodeFile(imagePath);
    
            if (originalBitmap == null) {
                Log.e("ProcessImage", "Error: Could not decode the image file.");
                return null;
            }
    
            // Convert the image to grayscale
            Bitmap grayscaleBitmap = convertToGrayscale(originalBitmap);
            // Resize the image to fit the printer's resolution
            int printerWidthPixels = 385; // Change this value according to your printer's max width
            int originalWidth = grayscaleBitmap.getWidth();
            int originalHeight = grayscaleBitmap.getHeight();
            float aspectRatio = (float) originalHeight / originalWidth;
            int targetHeightPixels = (int) (printerWidthPixels * aspectRatio);
    
            Bitmap resizedBitmap = Bitmap.createScaledBitmap(grayscaleBitmap, printerWidthPixels, targetHeightPixels, true);
    
            // Return the processed bitmap
            return resizedBitmap;
    
        } catch (Exception e) {
            Log.e("ProcessImage", "Error processing image: " + e.getMessage());
        }
        return null;
    }
    private static Bitmap convertToGrayscale(Bitmap bmpOriginal) {
        int width = bmpOriginal.getWidth();
        int height = bmpOriginal.getHeight();
    
        Bitmap bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmpGrayscale);
        Paint paint = new Paint();
        ColorMatrix cm = new ColorMatrix();
        cm.setSaturation(0);
        ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
        paint.setColorFilter(f);
        c.drawBitmap(bmpOriginal, 0, 0, paint);
    
        return bmpGrayscale;
    }
    

}
