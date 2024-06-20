module com.example.bskprojekt {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.ikonli.javafx;

    opens com.example.bskprojekt to javafx.fxml;
    exports com.example.bskprojekt;
}