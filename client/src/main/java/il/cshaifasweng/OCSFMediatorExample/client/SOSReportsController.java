package il.cshaifasweng.OCSFMediatorExample.client;
import il.cshaifasweng.OCSFMediatorExample.entities.SOS;
import il.cshaifasweng.OCSFMediatorExample.entities.Message;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javafx.scene.chart.XYChart;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class SOSReportsController {
    private static SOSReportsController currentInstance;
    @FXML
    private DatePicker startDatePicker;

    @FXML
    private DatePicker endDatePicker;

    @FXML
    private ComboBox<String> communityComboBox;

    @FXML
    private BarChart<String, Number> sosHistogram;

    @FXML
    private CategoryAxis xAxis;

    @FXML
    private NumberAxis yAxis;

    @FXML
    private Button LoadDataButton;

    public static void onServerResponse(Message message) {
    }

    @FXML
    public void handleloadDataButton(javafx.event.ActionEvent actionEvent) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String startDate = startDatePicker.getValue().format(formatter);
        String endDate = endDatePicker.getValue().format(formatter);

        // Assuming "My Community" selection requires fetching the community of the current user
        String community = communityComboBox.getValue();
        if (community.equals("My Community")) {
            community = SimpleClient.getCurrentUser().getCommunityManager(); // Implement this method according to your application's structure
        } else if (community.equals("All Communities")) {
            community = "all"; // Use a special identifier for all communities
        }

        Message message = new Message("#showSOS" , startDate +" " + endDate +" " +community);
        try {
            SimpleClient.getClient().sendToServer(message);
            System.out.println("(SOS) Sending message to server with dates: " + startDate + " to " + endDate + " and community: " + community);
        } catch (IOException e) {
            System.out.println("Failed to connect to the server.");
            e.printStackTrace();
        }
    }
    public void initialize() {
        currentInstance = this; // Update the current instance reference
        communityComboBox.getItems().addAll("All Communities", "My Community");
        communityComboBox.getSelectionModel().select("All Communities");
    }

    public static void updateHistogramFromMessage(Message message) {
        if (currentInstance != null && message != null) {
            // This runs the update on the JavaFX Application Thread
            Platform.runLater(() -> {
                // Assuming message.getObject() returns a List<SOS>, requires casting
                List<SOS> sosRecords = (List<SOS>) message.getObject();
                currentInstance.updateHistogram(sosRecords); // Call instance method to update the histogram
            });
        }
    }

    // This method can be called with the SOS data to update the histogram
    public void updateHistogram(List<SOS> sosRecords) {
        // Clear existing data
        sosHistogram.getData().clear();

        // Process records to count SOS calls per day
        Map<String, Integer> sosCountByDay = new HashMap<>();
        for (SOS sos : sosRecords) {
            String day = sos.getDate(); // This should match the actual method name
            sosCountByDay.put(day, sosCountByDay.getOrDefault(day, 0) + 1);
        }

        // Create a series for the histogram
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (Map.Entry<String, Integer> entry : sosCountByDay.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        // Add series to histogram
        sosHistogram.getData().add(series);
    }

    @FXML
    private void switchToPrimary() throws IOException {
        App.setRoot("primary");
    }
}