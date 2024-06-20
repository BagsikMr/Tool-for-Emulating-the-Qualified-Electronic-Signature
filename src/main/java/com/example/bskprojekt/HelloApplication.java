package com.example.bskprojekt;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.TextInputDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public class HelloApplication extends Application {

    FileChooser keyChooser = new FileChooser();
    FileChooser fileChooser = new FileChooser();
    Stage userAStage = new Stage();
    Stage userBStage = new Stage();
    Stage mainStage;
    @Override
    public void start(Stage stage) throws IOException {

        Label menuLabel = new Label("Pick option: ");
        Button button1 = new Button("1. User A");
        Button button2 = new Button("2. Open File");
        Button button3 = new Button("3. User B");
        Button button4 = new Button("4. EXIT");

        mainStage = stage;

        keyChooser.setInitialDirectory(new File("."));
        keyChooser.setTitle("Wybierz klucz");
        keyChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Klucze (*.key)", "*.key"),
                new FileChooser.ExtensionFilter("Wszystkie pliki","*.*")
        );

        fileChooser.setInitialDirectory(new File("."));
        fileChooser.setTitle("Wybierz plik");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Dokumenty (*.pdf)", "*.pdf"),
                new FileChooser.ExtensionFilter("Pliki cpp (*.cpp)","*.cpp"),
                new FileChooser.ExtensionFilter("Wszystkie pliki","*.*")
        );


        button1.setOnAction(actionEvent -> handleChoice(1,stage));
        button2.setOnAction(actionEvent -> handleChoice(2,stage));
        button3.setOnAction(actionEvent -> handleChoice(3,stage));
        button4.setOnAction(actionEvent -> handleChoice(4,stage));

        VBox layout = new VBox(10, menuLabel, button1, button2, button3,button4);
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout,320,240);

        userXStageInit(userAStage,true);
        userXStageInit(userBStage,false);

        stage.setTitle("Projekt BSK");
        stage.setScene(scene);
        stage.show();


    }

    public void handleChoice(int choice, Stage stage){
        switch (choice){
            case 1:

                stage.hide();
                userAStage.show();

                break;
            case 2:
                File selectedFile = keyChooser.showOpenDialog(stage);
                if (selectedFile != null)
                {
                    System.out.println("Wybrano: " + selectedFile.getAbsolutePath());
                }
                else
                {
                    System.out.println("Anulowano. ");
                }
                break;
            case 3:
                stage.hide();
                userBStage.show();
                break;
            case 4:
                System.out.println("Exiting...");
                System.exit(0);
                break;
        }
    }
    private void userXStageInit(final Stage userXStage,boolean canSignDocuments)
    {

        Label menuLabel = new Label("Pick option: ");
        Button button1 = new Button("1. Sign file.");
        Button button2 = new Button("2. Signature verification.");
        Button button3 = new Button("3. Encrypt");
        Button button4 = new Button("4. Decrypt");
        Button button5 = new Button("5. Back to main menu");

        if(canSignDocuments)
        {
            button1.setOnAction(actionEvent -> {
                try {
                    signDocument(userXStage);
                } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException | InvalidKeyException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        else {
            button1.setDisable(true);
            button1.setText("Pay 4ECTS to unlock");
        }
        button2.setOnAction(actionEvent -> {
            try {
                verifySignature();
            } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException | InvalidKeyException e) {
                throw new RuntimeException(e);
            }
        });
        button3.setOnAction(actionEvent -> {
            try {
                encryptDocument(userXStage);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

        button4.setOnAction(actionEvent -> {
            try {
                decryptDocument(userXStage);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        button5.setOnAction(actionEvent ->{
            userXStage.close();
            mainStage.show();
        });

        VBox layout = new VBox(10, menuLabel, button1, button2, button3, button4, button5);
        layout.setAlignment(Pos.CENTER);
        Scene scene = new Scene(layout,320,240);
        userXStage.setTitle("Projekt BSK");
        userXStage.setScene(scene);

    }
    private void signDocument(Stage stage) throws NoSuchAlgorithmException, IOException, InvalidKeySpecException, InvalidKeyException {
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null)
        {
            String pin = getUserPin();
            try (
                FileInputStream in = new FileInputStream(selectedFile);
                FileOutputStream out = new FileOutputStream(selectedFile + ".sig"))
                {

                    System.out.println("Wybrano: " + selectedFile.getAbsolutePath());
                    Signature sign = Signature.getInstance("SHA256withRSA");
                    PrivateKey pvt = restorePrivateKey(pin);
                    sign.initSign(pvt);

                    byte[] buf = new byte[4096];
                    int len;
                    while ((len = in.read(buf)) != -1) {
                        sign.update(buf, 0, len);
                    }

                    byte[] signature = sign.sign();
                    out.write(signature);

                    System.out.println("Plik: " + selectedFile.getAbsolutePath()+ " zostal podpisany.");

            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Wystąpił błąd podczas podpisywania pliku.");
            }


        }
        else
        {
            System.out.println("Anulowano. ");
            return;
        }

    }
    private PrivateKey restorePrivateKey(String pin) throws Exception{
        Path path = Paths.get("F:/privateKey.enc");
        byte[] encryptedBytes = Files.readAllBytes(path);

        byte[] decryptedBytes = decryptPrivateKey(encryptedBytes, pin);
        PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(decryptedBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(ks);
    }
    private PublicKey restorePublicKey() throws InvalidKeySpecException, IOException, NoSuchAlgorithmException {
        Path path = Paths.get("publicKey.pub");
        byte[] bytes = Files.readAllBytes(path);
        X509EncodedKeySpec ks = new X509EncodedKeySpec(bytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (kf.generatePublic(ks));
    }
    public void verifySignature() throws NoSuchAlgorithmException, InvalidKeySpecException, IOException, InvalidKeyException {
        File selectedFile = fileChooser.showOpenDialog(mainStage);
        if (selectedFile != null) {
            try (FileInputStream in = new FileInputStream(selectedFile)) {
                System.out.println("Wybrano: " + selectedFile.getAbsolutePath());
                Signature sign = Signature.getInstance("SHA256withRSA");
                PublicKey pub = restorePublicKey();

                sign.initVerify(pub);

                byte[] buf = new byte[4096];
                int len;
                while ((len = in.read(buf)) != -1) {
                    sign.update(buf, 0, len);
                }

                String signatureFilePath = selectedFile.getAbsolutePath() + ".sig";
                Path signaturePath = Paths.get(signatureFilePath);
                byte[] signatureBytes = Files.readAllBytes(signaturePath);

                System.out.println(selectedFile.getAbsolutePath() + ": Signature " +
                        (sign.verify(signatureBytes) ? "OK" : "Not OK"));
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Error verifying signature: " + e.getMessage());
            }
        }
        else{
            System.out.println("Anulowano. ");
            return;
        }
    }
    private byte[] decryptPrivateKey(byte[] encryptedKey, String pin) throws Exception {
        try {
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] key = sha.digest(pin.getBytes(StandardCharsets.UTF_8));
            SecretKeySpec secretKey = new SecretKeySpec(key, "AES");

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            return cipher.doFinal(encryptedKey);
        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }
    private String getUserPin() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Wprowadź PIN");
        dialog.setHeaderText("Wprowadź PIN do deszyfrowania klucza prywatnego");
        dialog.setContentText("PIN:");

        Optional<String> result = dialog.showAndWait();
        return result.orElse("");
    }
    private void encryptDocument(Stage stage) throws Exception {
        File inputFile = fileChooser.showOpenDialog(stage);
        if (inputFile != null) {
            File outputFile = new File(inputFile.getAbsolutePath() + ".enc");
            PublicKey publicKey = restorePublicKey();
            encryptFile(inputFile, outputFile, publicKey);
            System.out.println("Plik został zaszyfrowany: " + outputFile.getAbsolutePath());
        } else {
            System.out.println("Anulowano.");
        }
    }

    private void decryptDocument(Stage stage) throws Exception {
        File inputFile = fileChooser.showOpenDialog(stage);
        if (inputFile != null) {
            String pin = getUserPin(); // Metoda do pobierania PIN-u od użytkownika
            File outputFile = new File(inputFile.getAbsolutePath().replace(".enc", ""));
            PrivateKey privateKey = restorePrivateKey(pin);
            decryptFile(inputFile, outputFile, privateKey);
            System.out.println("Plik został odszyfrowany: " + outputFile.getAbsolutePath());
        } else {
            System.out.println("Anulowano.");
        }
    }
    private void encryptFile(File inputFile, File outputFile, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);

        try (FileInputStream inputStream = new FileInputStream(inputFile);
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);

            byte[] outputBytes = cipher.doFinal(inputBytes);

            outputStream.write(outputBytes);
        }
    }

    private void decryptFile(File inputFile, File outputFile, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        try (FileInputStream inputStream = new FileInputStream(inputFile);
             FileOutputStream outputStream = new FileOutputStream(outputFile)) {
            byte[] inputBytes = new byte[(int) inputFile.length()];
            inputStream.read(inputBytes);

            byte[] outputBytes = cipher.doFinal(inputBytes);

            outputStream.write(outputBytes);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}