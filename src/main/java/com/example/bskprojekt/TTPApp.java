package com.example.bskprojekt;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.util.Base64;

public class TTPApp extends Application {

    HelloApplication app = new HelloApplication();
    Text text = new Text();
    ProgressBar progressBar = new ProgressBar();
    VBox layout = new VBox(10);
    PasswordField pinField = new PasswordField();
    Button confirmButton = new Button("Potwierdz");
    @Override
    public void start(Stage stage) throws Exception {

        Label menuLabel = new Label("Pick option: ");
        Button button1 = new Button();

        boolean keyExist = detectKey();

        progressBar.setProgress(0.0);

        if(!keyExist) {
            text.setText("Nie znaleziono klucza prywatnego lub publicznego\n Kliknij generuj żeby wygenerować nowe");
            button1.setText("Generuj");
            button1.setOnAction(actionEvent -> {

                pinField.setMaxWidth(100);
                pinField.setPromptText("Wprowadz Pin");


                confirmButton.setOnAction(confirmEvent ->{
                    String pin = pinField.getText();
                    if(pin.isEmpty()){
                        text.setText("Pin nie moze byc pusty");
                    }else {
                        text.setText("Generowanie nowych kluczy, prosze czekac");
                        try {
                            generateKeys(button1, stage,pin);
                        } catch (NoSuchAlgorithmException | IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });

                layout.getChildren().setAll(text,pinField,confirmButton);
            });
        }
        else {
            layout.getChildren().remove(pinField);
            layout.getChildren().remove(confirmButton);
            setupButtonWithReadyKeys(button1, stage);
        }
        text.setX(50);
        text.setY(50);

        layout.getChildren().addAll(text, button1);
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout,320,240);

        stage.setTitle("Projekt BSK");
        stage.setScene(scene);
        stage.show();

    }
    private boolean detectKey()
    {
        File key = new File("./publicKey.pub");
        return key.exists();
    }
    private void generateKeys(Button button1, Stage stage, String pin) throws NoSuchAlgorithmException, IOException {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(4096);
        KeyPair kp = kpg.generateKeyPair();

        Key pub = kp.getPublic();
        Key pvt = kp.getPrivate();

        storeEncryptedPrivateKey(pvt,pin);

        String outFile = "publicKey";
        OutputStream out = new FileOutputStream(outFile + ".pub");
        out.write(pub.getEncoded());
        out.close();

        System.err.println("Private key format: " + pvt.getFormat());
        System.err.println("Public key format: " + pub.getFormat());


        if(detectKey())
        {
            layout.getChildren().remove(pinField);
            layout.getChildren().remove(confirmButton);
            layout.getChildren().add(button1);
            setupButtonWithReadyKeys(button1, stage);
        }
    }
    private void storeEncryptedPrivateKey(Key privateKey, String pin) throws IOException
    {
        try{
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] key = sha.digest(pin.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec secretKey = new SecretKeySpec(key,"AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedPrivateKey = cipher.doFinal(privateKey.getEncoded());

            Path pendrivePath = Paths.get("F:/privateKey.enc");
            Files.write(pendrivePath, encryptedPrivateKey);

        }catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    public void setupButtonWithReadyKeys(Button button1, Stage stage)
    {
        text.setText("Klucz zostal znaleziony, dziekujemy za usługe");
        button1.setText("Open app");
        button1.setOnAction(actionEvent -> {
            try {
                app.start(stage);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }



    public static void main(String[] args) {
        launch();
    }
}
