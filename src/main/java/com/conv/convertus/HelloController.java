package com.conv.convertus;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HelloController implements javafx.fxml.Initializable {
    @FXML
    private Label fileChooseLabel;
    @FXML
    private TextArea textConsole;
    @FXML
    private ChoiceBox<String> choiceBox;
    @FXML
    private ProgressBar barProgress;
    List<File> selectedFiles;
    String convTo = "webp";

    @Override
    public void initialize(java.net.URL location, java.util.ResourceBundle resources) {
        System.out.println("Форма загружена!");
        choiceBox.setItems(FXCollections.observableArrayList(
                "webp",
                "png",
                "jpeg",
                "gif",
                "bmp"
        ));

        if (!choiceBox.getItems().isEmpty()) {
            choiceBox.setValue(choiceBox.getItems().get(0));
        }
    }

    @FXML
    protected void fileChoose(ActionEvent event) {
        // Создание экземпляра FileChooser и добавление фильтров файлов
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Все файлы", "*"));

        // Получение ссылки на сцену, связанную с событием (нажатие кнопки)
        javafx.scene.Node source = (javafx.scene.Node) event.getSource();
        javafx.stage.Window stage = source.getScene().getWindow();

        // Показ диалогового окна выбора файла
        selectedFiles = fileChooser.showOpenMultipleDialog(stage);
        if (selectedFiles != null) {
            textConsole.setText("");
            for (File selectedFile : selectedFiles) {
                System.out.println("Выбранный файл: " + selectedFile.getAbsolutePath());
                log(selectedFile.getAbsolutePath());
            }
        } else {
            System.out.println("Файлы не выбраны.");
        }
    }

    @FXML
    protected void startConverter() {
        // Создаем экземпляр Task для выполнения длительной операции
        Task<Void> backgroundTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                convert();
                return null;
            }
        };

        // Запускаем задачу в отдельном фоновом потоке
        Thread backgroundThread = new Thread(backgroundTask);
        backgroundThread.setDaemon(true); // Настройка фонового потока как демона
        backgroundThread.start();

        // Можно добавить обработчик события завершения задачи
        backgroundTask.setOnSucceeded(event -> {
            log("Завершено!");
        });
    }

    protected void convert() throws IOException, InterruptedException {
        int count = 0;
        log("--------------------------------");
        convTo = choiceBox.getValue();
        if (selectedFiles != null) {
            String root = selectedFiles.get(0).getAbsolutePath().replace(selectedFiles.get(0).getName(), "");
            String outFolderPath = root + "results";

            // Создание объекта Path с указанным путем
            Path folder = Paths.get(outFolderPath);

            // Проверка наличия папки и создание, если она не существует
            if (!Files.exists(folder)) {
                try {
                    Files.createDirectories(folder);
                    System.out.println("Папка успешно создана.");
                } catch (Exception e) {
                    System.out.println("Не удалось создать папку: " + e.getMessage());
                }
            } else {
                System.out.println("Папка уже существует.");
            }

            System.out.println(root);

            for (File selectedFile : selectedFiles) {
                String name = outFolderPath + "/" + selectedFile.getName().toString();
                String ext = selectedFile.getName().toString().substring(selectedFile.getName().toString().lastIndexOf("."));
                int lastIndex = name.lastIndexOf(".");
                if (lastIndex > -1 && Arrays
                        .stream(ImageMIMETypes.values())
                        .anyMatch(types -> Objects.equals("." + types.getExtension(), ext))) {
                    name = name.substring(0, lastIndex);
                    name += ("." + convTo);

                    boolean isWrite = encodeWebpFromArr(selectedFile, name, convTo);
                    if (isWrite) {
                        log(selectedFile.getName() + ": OK");
                    } else {
                        log(selectedFile.getName() + ": ERROR");
                    }

                    count += 1;
                    System.out.println(selectedFile.getAbsolutePath());
                    barProgress.setProgress(((double) 1 / selectedFiles.size()) * count);
                }
            }
        } else {
            System.out.println("Файлы не выбраны.");
            log("Файлы не выбраны.");
        }
    }

    protected void log(String message) {
        textConsole.appendText(message + "\n");
    }

    public static boolean encodeWebpFromArr(File file, String outputImagePath, String conv) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);


        BufferedImage image = ImageIO.read(inputStream);

        if (image == null) {
            System.out.println("ERROR: " + file.getName());
            return false;
        }
        FileOutputStream outputStream = new FileOutputStream(outputImagePath);

        boolean write = ImageIO.write(image, conv, outputStream);

        outputStream.close();
        inputStream.close();
        return write;
    }
}