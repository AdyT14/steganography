package ro.cercetare.ui;

import ro.cercetare.codificare.RsaAesCypher;
import ro.cercetare.steganografie.Steganografie;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@SuppressWarnings("unused")
public class UserInterface {
    private JTabbedPane tabbedPane1;
    private JPanel mainPanel;
    private JButton imageFileButton;
    private JTextField imageFilePathTextField;
    private JTextArea textArea1;
    private JTextField messageFilePathTextField;
    private JButton ORCHOOSEFILEButton;
    private JButton ENCODEButton;
    private JButton createNewRSAKeyButton;
    private JButton RSAPublicKeyButton;
    private JCheckBox RSAPublicKeyCheckBox;
    private JButton IMAGEFILEButton;
    private JTextField textField1;
    private JTextArea textArea2;
    private JButton RSAPRIVATEKEYButton;
    private JCheckBox RSAPRIVATEKEYCheckBox;
    private JButton DECODEButton;
    private File imageFile = null;
    private File imageEncodedFile = null;
    private File publicKeyFile = null;
    private File messageFile = null;
    private File privateKeyFile = null;

    public UserInterface(RsaAesCypher rsaAesCypher, Steganografie steganografie) {
        imageFileButton.addActionListener(e -> {
                    imageFile = null;
                    try{
                        imageFile = getFile("Choose what image to use to encode our message into");
                        imageFilePathTextField.setText(imageFile.getPath());
                    }catch (IllegalArgumentException er){
                        System.out.println(er.getMessage());
                    }

                });

        RSAPublicKeyButton.addActionListener(e -> {
            publicKeyFile = null;
            try{
                publicKeyFile = getFile("Choose the RSA public key");
                RSAPublicKeyCheckBox.setVisible(true);
            }catch (IllegalArgumentException er){
                System.out.println(er.getMessage());
            }

        });

        ORCHOOSEFILEButton.addActionListener(e -> {
            messageFile = null;
            try{
                messageFile = getFile("Choose the txt message");
                messageFilePathTextField.setText(messageFile.getPath());
            }catch (IllegalArgumentException er){
                System.out.println(er.getMessage());
            }
        });

        createNewRSAKeyButton.addActionListener(e -> {
            File file = null;
            try{
                file = getFile("Choose where to place and name for rsaKeys ");
            }catch (IllegalArgumentException er){
                System.out.println(er.getMessage());
            }
            try {
                if (file != null) {
                    rsaAesCypher.doGenkey(file.getName());
                }
                JOptionPane.showMessageDialog(null,
                        "Keys created", "Message", JOptionPane.INFORMATION_MESSAGE);
            } catch (NoSuchAlgorithmException | IOException e1) {
                e1.printStackTrace();
                JOptionPane.showMessageDialog(null,
                        e1.getMessage(), "Message", JOptionPane.ERROR_MESSAGE);
            }
        });

        ENCODEButton.addActionListener(e -> {
            if(imageFilePathTextField.getText().length() == 0){
                JOptionPane.showMessageDialog(null,
                        "Please select an image", "Message", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if(!RSAPublicKeyCheckBox.isVisible()){
                JOptionPane.showMessageDialog(null,
                        "Please provide a public key to be used", "Message", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if(messageFilePathTextField.getText().length() == 0 && textArea1.getText().length() == 0){
                JOptionPane.showMessageDialog(null,
                        "Please provide a message to be used", "Message", JOptionPane.INFORMATION_MESSAGE);
                return;
            }else if(messageFilePathTextField.getText().length() == 0){
                String file = System.getProperty("user.dir") + "\\message.txt";
                messageFile = new File(file);
                writeToFile(textArea1.getText().getBytes(), file);
            }
            try {
                rsaAesCypher.encryptAesWithRsa(publicKeyFile.getPath(), messageFile.getPath());
                JOptionPane.showMessageDialog(null,
                        "The message was encripted and was added in the folder of this app",
                        "Message", JOptionPane.INFORMATION_MESSAGE);
                textArea1.setText("");
                imageFilePathTextField.setText("");
                messageFilePathTextField.setText("");
                RSAPublicKeyCheckBox.setVisible(false);

                byte[] message = readFromFile(messageFile.getPath() + ".enc");
                String[] imageFilePath = imageFile.getName().split("\\.");
                steganografie.encode(imageFile.getParent(), imageFilePath[0],
                        imageFilePath[1], imageFilePath[0]+ "_enc", message);
                JOptionPane.showMessageDialog(null,
                        "The message was encripted into the image",
                        "Message", JOptionPane.INFORMATION_MESSAGE);
            } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException | NoSuchPaddingException | InvalidKeyException | BadPaddingException | IllegalBlockSizeException | InvalidAlgorithmParameterException e1) {
                e1.printStackTrace();
            }

        });
        IMAGEFILEButton.addActionListener(e -> {
            try{
                imageEncodedFile = getFile("Choose what image to extract our message from");
                textField1.setText(imageEncodedFile.getPath());
            }catch (IllegalArgumentException er){
                System.out.println(er.getMessage());
            }
        });
        RSAPRIVATEKEYButton.addActionListener(e -> {
            try{
                privateKeyFile = getFile("Choose the RSA private key");
                RSAPRIVATEKEYCheckBox.setVisible(true);
            }catch (IllegalArgumentException er){
                System.out.println(er.getMessage());
            }
        });
        DECODEButton.addActionListener(e -> {
            if(textField1.getText().length() == 0){
                JOptionPane.showMessageDialog(null,
                        "Please select an image", "Message", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if(!RSAPRIVATEKEYCheckBox.isVisible()){
                JOptionPane.showMessageDialog(null,
                        "Please provide a private key to be used", "Message", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            String[] imageFilePath = imageEncodedFile.getName().split("\\.");
            byte[] codedMessage = steganografie.decode(imageEncodedFile.getParent(), imageFilePath[0]);

            String file = System.getProperty("user.dir") + "\\messageFromImage";
            writeToFile(codedMessage, file);
            JOptionPane.showMessageDialog(null,
                    "The encoded text was extracted from image",
                    "Message", JOptionPane.INFORMATION_MESSAGE);

            textField1.setText("");
            imageFilePathTextField.setText("");
            RSAPRIVATEKEYCheckBox.setVisible(false);

            try {
                rsaAesCypher.decryptAesWithRsa(privateKeyFile.getPath(), file);
                byte[] message = readFromFile(file + ".ver");
                textArea2.setText(new String(message));
            } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeySpecException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException | IllegalBlockSizeException e1) {
                e1.printStackTrace();
            }

        });
    }

    private File getFile(String titleMessage){
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
        fileChooser.setDialogTitle(titleMessage);
        int returnVal = fileChooser.showOpenDialog(mainPanel);
        if(returnVal == JFileChooser.APPROVE_OPTION){
            return fileChooser.getSelectedFile();
        }else{
            throw new IllegalArgumentException("Please choose a correct file");
        }
    }

    private byte[] readFromFile(String filePath){
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "File could not be read from", "Error", JOptionPane.ERROR_MESSAGE);
            throw new NullPointerException("Was not able to read from file");
        }
    }

    private void writeToFile(byte[] message, String path){
        try {
            Files.write(Paths.get(path), message);
            JOptionPane.showMessageDialog(null,
                    "A file with the specified message was created in the folder of this app",
                    "Message", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null,
                    "File could not be created with our message", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public JTabbedPane getTabbedPane1() {
        return tabbedPane1;
    }

}
