module com.example.tabledatataker {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.apache.poi.ooxml;

    opens com.example.tabledatataker to javafx.fxml;
    exports com.example.tabledatataker;
}
