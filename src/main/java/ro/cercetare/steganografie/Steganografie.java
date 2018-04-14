package ro.cercetare.steganografie;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

public class Steganografie {

    /**
     * Encrypt an image with the specified text, the output file will be an .png image
     * @param imagePath The path/folder containing the image which can be used to insert our text
     * @param originalImage The name of the image to be modified
     * @param extension The extension of the file, .jpg or .png
     * @param imageOutput The output name of the file
     * @param message The text which we are encoding into the image
     * @return returning a boolean value to specify if operation was successful
     */
    public boolean encode(String imagePath, String originalImage, String extension, String imageOutput, byte[] message){
        String fileName = imagePath(imagePath, originalImage, extension);
        BufferedImage imageOriginal = getImage(fileName);

        BufferedImage image = userSpace(imageOriginal);
        image = addText(image, message);

        String filePath = imagePath(imagePath, imageOutput, "png");
        return (setImage(image, new File(filePath)));
    }

    /**
     * Decrypt asumes the image being used is of type .png, extracts the hidden text
     * @param path - The path(folder) containing the image to extract the text
     * @param name - The name of the image to extract the message from
     * @return - Returns our decoded text;
     */
    public byte[] decode(String path, String name){
        byte[] decode;
        try {
            String filePath = imagePath(path, name, "png");
            BufferedImage image = userSpace(getImage(filePath));
            decode = decodeText(getByteArray(image));
            return decode;
        }catch (Exception e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "There is no hidden message in this image!", "Error",
                    JOptionPane.ERROR_MESSAGE);
            return new byte[]{};
        }
    }

    /**
     * Returns a complete filePath
     * @param path Folder location
     * @param name Name of file
     * @param extension The extension of the file
     * @return String representing the complete file name path
     */
    private String imagePath(String path, String name, String extension){
        return path + "/" + name + "." + extension;
    }

    /**
     * Getter for optaining our image
     * @param f Location and name of image
     * @return BufferedImage image required for our application
     */
    private BufferedImage getImage(String f){
        BufferedImage image = null;
        File file = new File(f);
        try {
            image = ImageIO.read(file);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "Image could not be read!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        return image;
    }

    /**
     * Setter for saving an image file
     * @param image BufferedImage - Image for saving
     * @param file File - Location and name for where to save our image
     * @return bolean value if operation was successful
     */
    private boolean setImage(BufferedImage image, File file){
        if(file.delete()){// deleting the file if it already exists
            String message = "Old file was deleted to be replaced by new modified image.";
            System.out.println(message);
            JOptionPane.showMessageDialog(null,
                    message, "Error", JOptionPane.INFORMATION_MESSAGE);
        }
        try {
            ImageIO.write(image, "png", file);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "File could not be saved", "Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * This method adds the text to our image
     * @param image BufferedImage - image to which to add the text
     * @param text String - what text to add to the image
     * @return BufferedImage which contains our modified image
     */
    private BufferedImage addText(BufferedImage image, byte[] text){

        //convert our image, message and length of it to arrays
        byte[] img = getByteArray(image);
        byte[] msg = text;
        byte[] len = bitConversion(msg.length);

        try{
            encodeText(img, len, 0);
            encodeText(img, msg, 32);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    e.getMessage(), "Message", JOptionPane.ERROR_MESSAGE);
        }
        return image;
    }

    /**
     * Creates a user space of the Buffered Image, for editing purposes
     * @param image - original image
     * @return - the image which can be safely modified
     */
    private BufferedImage userSpace(BufferedImage image){
        //create and empty BufferedImage where we will render our parameters image
        BufferedImage newImg = new BufferedImage(
                image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics2D = newImg.createGraphics();
        graphics2D.drawRenderedImage(image, null);
        graphics2D.dispose();
        return newImg;
    }

    /**
     * Gets the byte array of our image which we will use later on
     * @param image - The image from which we will get our byte array
     * @return - byte[] - A byte array containing byte values for our image
     */
    private byte[] getByteArray(BufferedImage image){
        WritableRaster raster = image.getRaster(); // for getting our pixel values
        DataBufferByte bufferByte = (DataBufferByte)raster.getDataBuffer();
        return bufferByte.getData();
    }

    /**
     * This method is used to get our byte format for an integer value
     * @param i - The integer value to convert
     * @return - 4 byte array converted from an int
     */
    private byte[] bitConversion(int i){
        byte byte3 = (byte)((i & 0xFF000000) >>> 24);
        byte byte2 = (byte)((i & 0x00FF0000) >>> 16);
        byte byte1 = (byte)((i & 0x0000FF00) >>> 8);
        byte byte0 = (byte) (i & 0x000000FF);

        return (new byte[]{byte3, byte2, byte1, byte0});
    }

    /**
     * Encode an array of bytes into another array of bytes at a supplied offset
     * @param image Array of data representing our image
     * @param addition Array of data representing our message
     * @param offset The offset into the image array to add the addition message
     */
    private void encodeText(byte[] image, byte[] addition, int offset){
        //check if the message + offset can fit in our image
        if(addition.length + offset > image.length){
            throw new IllegalArgumentException("File not long enough");
        }
        //looping through each addition byte
        for (int i = 0; i < addition.length; i++) {
            // now we must loop through each bit of the current value in addition
            int add = addition[i];
            for (int j = 7; j >= 0; --j, ++offset) { //incrementing our offset also with each addition
                // saving our value to b, shifted by j spaces AND 1
                // b represents a single bit of the current byte
                int b = (add >>> j) & 1;
                //now we assign our bit: [(original byte value) AND 0xFE] OR b - the bit which we need to add
                //this changes the last bit of the byte in the image to be the bit of the addition
                image[offset] = (byte)((image[offset] & 0xFE) | b);
            }
        }
    }

    /**
     * Retrieving the hidden text from modified image
     * @param image - Array containing our image data
     * @return - Returning our data which we obtained from the image
     */
    private byte[] decodeText(byte[] image){
        int length = 0;
        int offset = 32;

        //first we must loop through the first 32 bytes of data to determine our text length
        for(int i=0; i < 32; ++i){
            length = (length << 1) | (image[i] & 1);
        }

        byte[] result = new byte[length];

        //loop through each byte of text
        for(int i = 0; i < result.length; ++i){
            //now also loop through each bit from the current byte
            for(int j = 0; j < 8; ++j, ++offset){
                //assign bit: [(new byte value) << 1] OR [(text byte) AND 1]
                result[i] = (byte)((result[i] << 1) | (image[offset] & 1));
            }
        }
        return result;
    }

}
