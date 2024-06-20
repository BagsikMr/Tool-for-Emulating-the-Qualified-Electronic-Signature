# Tool for Emulating the Qualified Electronic Signature

## Overview
This project comprises two Java applications, `TTPApp` and `mainApplication`, which utilize JavaFX for the GUI and RSA for encryption, decryption, and digital signatures. The second application (`TTPApp`) checks for existing RSA key pairs, generates new ones if necessary, and allows the user to perform encryption, decryption, and digital signing operations via `mainApplication`.

## Features

1. **Key Management:**
   - Detect existing RSA key pairs.
   - Generate new RSA key pairs if none are found.
   - Encrypt and store the private key securely using a user-provided PIN.

2. **File Operations:**
   - Sign documents.
   - Verify document signatures.
   - Encrypt files using the public key.
   - Decrypt files using the private key.

## Usage

### Prerequisites
- Java Development Kit (JDK) 8 or higher.
- JavaFX SDK (required for JavaFX applications).

### Setup

1. **Clone the Repository:**
   ```sh
   git clone https://github.com/BagsikMr/Tool-for-Emulating-the-Qualified-Electronic-Signature.git
   cd Tool-for-Emulating-the-Qualified-Electronic-Signature
   ```
  
2. **Compile the Project:**
    Ensure you have all dependencies in your classpath, especially the JavaFX SDK.
    ```sh
    javac -cp /path/to/javafx-sdk/lib/*:. *.java
    ```
3. **Run the application:**
   Make sure to include JavaFX modules when running the application.
   ```sh
   java -cp /path/to/javafx-sdk/lib/*:. --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls TTPApp
   ```

### Application Flow
1. **TTPApp:***
  - Launches and checks for existing RSA key pairs.
  - If keys are missing, prompts the user to generate new keys.
  - Once keys are ready, allows the user to proceed to the main application
2. MainApplication:
- Provides a menu with options for User A and User B operations.
- User A can sign documents.
- Both User A and User B can encrypt and decrypt documents, as well as verify signatures.


## Notes

- Ensure the **publicKey.pub** and **privateKey.enc** files are stored in the correct locations.
- The encrypted private key is stored on a removable drive **(F:/)**, adjust the path as necessary.
- Handle exceptions appropriately, especially for file I/O and cryptographic operations.

## License

This project is licensed under the MIT License. See the LICENSE file for details.
















