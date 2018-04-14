package ro.cercetare.codificare;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RsaAesCypher {

    private SecureRandom secureRandom = new SecureRandom();

    private void processFile(Cipher ci, InputStream in, OutputStream out) throws IOException, BadPaddingException, IllegalBlockSizeException {
        byte[] ibuf = new byte[1024];
        int len;
        while ((len = in.read(ibuf)) != -1) {
            byte[] obuf = ci.update(ibuf, 0, len);
            if ( obuf != null ) out.write(obuf);
        }
        byte[] obuf = ci.doFinal();
        if ( obuf != null ) out.write(obuf);

    }

    public void doGenkey(String filePath)
            throws java.security.NoSuchAlgorithmException,
            java.io.IOException
    {

        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        KeyPair kp = kpg.generateKeyPair();
        try (FileOutputStream out = new FileOutputStream(filePath + ".key")) {
            out.write(kp.getPrivate().getEncoded());
        }

        try (FileOutputStream out = new FileOutputStream(filePath + ".pub")) {
            out.write(kp.getPublic().getEncoded());
        }
    }

    public void encryptAesWithRsa(String rsaKeyFileName, String file) throws  NoSuchAlgorithmException,            InvalidKeySpecException,            IOException,            NoSuchPaddingException,            InvalidKeyException,            BadPaddingException,            IllegalBlockSizeException,            InvalidAlgorithmParameterException {
        //Checking if values are not null
        if(rsaKeyFileName == null || file == null){
            System.err.println("encryptAesWithRsa -- something missing");
            return;
        }

        //Loading RSA public key
        byte[] bytes = Files.readAllBytes(Paths.get(rsaKeyFileName));
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(bytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey pvt = keyFactory.generatePublic(keySpec);

        /*
        We will generate the AES key with the help of Java built-in libraries.
        The generated key will be of 128 bits.
         */
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        SecretKey secretKey = keyGenerator.generateKey();

        /*
        We need the initialization vector of the same size as the key
         */
        byte[] iv = new byte[128/8];
        secureRandom.nextBytes(iv);
        IvParameterSpec ivspec = new IvParameterSpec(iv);


        try(FileOutputStream out = new FileOutputStream(file + ".enc")){
            {
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.ENCRYPT_MODE, pvt);
                byte[] b = cipher.doFinal(secretKey.getEncoded());
                out.write(b); // Saving our AES key encrypted with RSA
                System.err.println("AES Key Length: " + b.length);
            }
            out.write(iv); // Saving our initialization vector
            System.err.println("IV Length: " + iv.length);

            //Placing our AES message in the same file
            Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
            ci.init(Cipher.ENCRYPT_MODE, secretKey, ivspec);
            try(FileInputStream in = new FileInputStream(file)){
                processFile(ci, in, out);
            }
        }

    }

    public void decryptAesWithRsa(String rsaKeyFileName, String file) throws
            IOException,
            NoSuchAlgorithmException,
            InvalidKeySpecException,
            NoSuchPaddingException,
            InvalidKeyException,
            BadPaddingException,
            IllegalBlockSizeException,
            InvalidAlgorithmParameterException {

        //Checking if values are not null
        if(rsaKeyFileName == null || file == null){
            System.err.println("decryptAesWithRsa -- something missing");
            return;
        }

        // Getting our private RSA key from file
        byte[] bytes = Files.readAllBytes(Paths.get(rsaKeyFileName));
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey publicKey = keyFactory.generatePrivate(keySpec);

        try(FileInputStream in = new FileInputStream(file)){
            SecretKeySpec secretKeySpec = null;
            {
                Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
                cipher.init(Cipher.DECRYPT_MODE, publicKey);
                byte[] b = new byte[256];
                in.read(b);
                byte[] keyb = cipher.doFinal(b);
                secretKeySpec = new SecretKeySpec(keyb, "AES");
            }

            byte[] iv = new byte[128/8];
            in.read(iv);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            Cipher ci = Cipher.getInstance("AES/CBC/PKCS5Padding");
            ci.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

            try(FileOutputStream out = new FileOutputStream(file + ".ver")){
                processFile(ci, in, out);
            }

        }

    }

}
