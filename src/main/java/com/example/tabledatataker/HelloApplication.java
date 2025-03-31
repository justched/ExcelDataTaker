package com.example.tabledatataker;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.scene.control.Button;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HelloApplication extends Application {
    private static final List<Product> products = new ArrayList<>();

    @Override
    public void start(Stage stage) throws IOException {
        VBox root = new VBox();
        Button button = new Button("Загрузить файл");
        root.getChildren().add(button);

        button.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
            File selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile != null) {
                try {
                    readExcelFile(selectedFile);
                } catch (IOException | InvalidFormatException ex) {
                    ex.printStackTrace();
                }
            }
        });

        Scene scene = new Scene(root, 640, 360);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }

    public static void readExcelFile(File file) throws IOException, InvalidFormatException {
        FileInputStream fis = new FileInputStream(file);
        XSSFWorkbook workbook = new XSSFWorkbook(fis);
        XSSFSheet sheet = workbook.getSheetAt(0);

        for (int i = 1; i < sheet.getLastRowNum(); i++) {
            int id = (int) sheet.getRow(i).getCell(0).getNumericCellValue();
            String name = sheet.getRow(i).getCell(1).getStringCellValue();
            double price = sheet.getRow(i).getCell(2).getNumericCellValue();
            int quantity = (int) sheet.getRow(i).getCell(3).getNumericCellValue();
            double finalPrice = sheet.getRow(i).getCell(4).getNumericCellValue();
            String date = sheet.getRow(i).getCell(5).getStringCellValue();

            Product product = new Product(id, name, price, quantity, finalPrice, date);
            products.add(product);
        }
        workbook.close();

        System.out.println(products.get(0).getName());
        System.out.println(products.get(0).getPrice());
        System.out.println(products.get(0).getFinalPrice());
        System.out.println(products.get(0).getQuantity());
        System.out.println(products.get(0).getDate());
    }

    public static void main(String[] args) {
        launch();
    }
}
