package com.example.tabledatataker;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

public class HelloApplication extends Application {
    private final List<Product> products = new ArrayList<>();
    private final ComboBox<Integer> yearComboBox = new ComboBox<>();
    private final BorderPane root = new BorderPane();
    private final Label statusLabel = new Label("Please select an Excel file with product data");

    @Override
    public void start(Stage primaryStage) {
        setupUI(primaryStage);
        primaryStage.setTitle("Product Sales Analyzer");
        primaryStage.setScene(new Scene(root, 900, 650));
        primaryStage.show();
    }

    private void setupUI(Stage stage) {
        VBox topContainer = new VBox(10);
        topContainer.getChildren().addAll(statusLabel, yearComboBox);
        root.setTop(topContainer);

        yearComboBox.setPromptText("Select year");
        yearComboBox.setOnAction(e -> {
            if (yearComboBox.getValue() != null) {
                updateChart(yearComboBox.getValue());
            }
        });

        openFileChooser(stage);
    }

    private void openFileChooser(Stage stage) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Product Data File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls")
        );

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                loadProductData(file);
                setupYearComboBox();
            } catch (IOException e) {
                statusLabel.setText("Error: " + e.getMessage());
            } catch (Exception e) {
                statusLabel.setText("Unexpected error: " + e.getMessage());
            }
        }
    }

    private void loadProductData(File file) throws IOException {
        products.clear();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            boolean firstRow = true;

            for (Row row : sheet) {
                if (firstRow) {
                    firstRow = false;
                    continue; // Skip header row
                }

                try {
                    Product product = createProductFromRow(row);
                    products.add(product);
                } catch (Exception e) {
                    System.out.println("Skipping row " + row.getRowNum() + ": " + e.getMessage());
                }
            }
        }
        statusLabel.setText("Loaded " + products.size() + " products from " + file.getName());
    }

    private Product createProductFromRow(Row row) {
        int id = safeGetIntCellValue(row.getCell(0), 0);
        String name = safeGetStringCellValue(row.getCell(1), "");
        double price = safeGetNumericCellValue(row.getCell(2), 0.0);
        int quantity = safeGetIntCellValue(row.getCell(3), 0);
        double finalPrice = safeGetNumericCellValue(row.getCell(4), 0.0);
        String date = safeGetDateCellValue(row.getCell(5));

        return new Product(id, name, price, quantity, finalPrice, date);
    }
    private double safeGetNumericCellValue(Cell cell, double defaultValue) {
        if (cell == null) return defaultValue;
        try {
            return cell.getNumericCellValue();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String safeGetStringCellValue(Cell cell, String defaultValue) {
        if (cell == null) return defaultValue;
        try {
            return cell.getStringCellValue();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private String safeGetDateCellValue(Cell cell) {
        if (cell == null) return "";
        try {
            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
                return cell.getDateCellValue().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                        .toString();
            }
            return cell.toString();
        } catch (Exception e) {
            return "";
        }
    }

    private int safeGetIntCellValue(Cell cell, int defaultValue) {
        if (cell == null) return defaultValue;
        try {
            return (int) Math.round(cell.getNumericCellValue());
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private void setupYearComboBox() {
        Set<Integer> years = new TreeSet<>();
        for (Product product : products) {
            try {
                LocalDate date = LocalDate.parse(product.getDate());
                years.add(date.getYear());
            } catch (Exception e) {
                // Skip invalid dates
            }
        }

        if (!years.isEmpty()) {
            yearComboBox.setItems(FXCollections.observableArrayList(years));
            yearComboBox.getSelectionModel().selectFirst();
        } else {
            statusLabel.setText("No valid years found in the data");
        }
    }

    private void updateChart(int selectedYear) {
        double[] monthlySales = new double[12];

        for (Product product : products) {
            try {
                LocalDate date = LocalDate.parse(product.getDate());
                if (date.getYear() == selectedYear) {
                    int month = date.getMonthValue() - 1;
                    monthlySales[month] += product.getFinalPrice();
                }
            } catch (Exception e) {
                // Skip products with invalid dates
            }
        }

        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);

        chart.setTitle("Monthly Product Sales for " + selectedYear);
        xAxis.setLabel("Month");
        yAxis.setLabel("Total Sales");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Sales");

        for (int i = 0; i < 12; i++) {
            String monthName = LocalDate.of(2000, i + 1, 1)
                    .getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
            series.getData().add(new XYChart.Data<>(monthName, monthlySales[i]));
        }

        chart.getData().clear();
        chart.getData().add(series);
        root.setCenter(chart);
    }

    public static void main(String[] args) {
        launch(args);
    }
}