package ro.cercetare;

import ro.cercetare.codificare.RsaAesCypher;
import ro.cercetare.steganografie.Steganografie;
import ro.cercetare.ui.UserInterface;

import javax.swing.*;
import java.awt.*;

public class App {

    public static void main(String[] args) {
        JFrame jFrame = new JFrame("Steganografie");
        jFrame.setIconImage(Toolkit.getDefaultToolkit().getImage(jFrame.getClass().getResource("/icon.png")));
        jFrame.setContentPane(new UserInterface(new RsaAesCypher(), new Steganografie()).getMainPanel());
        jFrame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        jFrame.setResizable(false);
        jFrame.pack();
        jFrame.setVisible(true);

    }



}
