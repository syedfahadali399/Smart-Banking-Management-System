import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import models.*;
import accountTypes.*;
import database.FileHandler;

import java.util.List;
import java.util.Optional;

public class BankingApp extends Application {

    // Global Data
    private List<Account> approvedAccounts;
    private List<Account> pendingAccounts;
    private int accountCounter = 1001;

    // UI Constants
    private Stage window;
    private Scene loginScene;

    // COLORS
    final String COLOR_PRIMARY = "#2980b9";
    final String COLOR_DARK    = "#2c3e50";
    final String COLOR_BG      = "#ecf0f1";
    final String COLOR_BTN     = "-fx-background-color: #2980b9; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;";
    final String COLOR_BTN_RED = "-fx-background-color: #c0392b; -fx-text-fill: white; -fx-background-radius: 5; -fx-cursor: hand;";

    @Override
    public void start(Stage primaryStage) {
        window = primaryStage;
        window.setTitle("Smart Student Banking System (Group Project)");

        loadData();
        initLoginScene();

        window.setScene(loginScene);
        window.show();
        window.setOnCloseRequest(e -> saveData());
    }

    private void loadData() {
        approvedAccounts = FileHandler.loadAccounts(false);
        pendingAccounts = FileHandler.loadAccounts(true);
        // Sync Counter
        for(Account a : approvedAccounts) if(a.getAccountNumber() >= accountCounter) accountCounter = a.getAccountNumber() + 1;
        for(Account a : pendingAccounts) if(a.getAccountNumber() >= accountCounter) accountCounter = a.getAccountNumber() + 1;
    }

    private void saveData() {
        FileHandler.saveAccounts(approvedAccounts, false);
        FileHandler.saveAccounts(pendingAccounts, true);
    }

    // ================== LOGIN SCENE ==================
    private void initLoginScene() {
        VBox layout = new VBox(20);
        layout.setAlignment(Pos.CENTER);
        layout.setStyle("-fx-background-color: " + COLOR_DARK  + ";");
        layout.setPadding(new Insets(40));

        // --- NEW LOGO CODE HERE ---
        StackPane logo = createLogo(); // Call the method we just made

        // Header
        Label title = new Label("Smart Student Banking System");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(COLOR_BG));

        // Tabs
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        Tab studentTab = new Tab("Student Login", createStudentLoginForm());
        Tab adminTab = new Tab("Admin Login", createAdminLoginForm());

        tabs.getTabs().addAll(studentTab, adminTab);

        // Register Button
        Button btnRegister = new Button("Create New Account");
        btnRegister.setStyle("-fx-background-color: transparent; -fx-text-fill: #2980b9; -fx-underline: true; -fx-cursor: hand;");
        btnRegister.setOnAction(e -> showRegisterWindow());

        // Add 'logo' to the layout
        layout.getChildren().addAll(logo, title, tabs, btnRegister);
        loginScene = new Scene(layout, 500, 650); // Increased height slightly
    }

private VBox createStudentLoginForm() {
    VBox box = new VBox(15);
    box.setPadding(new Insets(30));
    box.setAlignment(Pos.CENTER);

    TextField txtId = new TextField(); txtId.setPromptText("Account ID");
    PasswordField txtPin = new PasswordField(); txtPin.setPromptText("4-Digit PIN");

    Button btnLogin = new Button("Login");
    btnLogin.setStyle(COLOR_BTN);
    btnLogin.setPrefWidth(200);

    btnLogin.setOnAction(e -> {
        try {
            int id = Integer.parseInt(txtId.getText());
            int pin = Integer.parseInt(txtPin.getText());

            // 1. Check if Account is APPROVED
            Account activeUser = findApprovedAccount(id);

            if (activeUser != null) {
                // Account exists and is active. Now check PIN.
                if (activeUser.validatePin(pin)) {
                    window.setScene(createStudentDashboard(activeUser));
                } else {
                    showAlert(Alert.AlertType.ERROR, "Login Failed", "Wrong Password (PIN).");
                }
            }
            // 2. Check if Account is PENDING (The logic you asked for)
            else {
                Account pendingUser = findPendingAccount(id);
                if (pendingUser != null) {
                    // Account exists but is waiting for admin
                    showAlert(Alert.AlertType.WARNING, "Pending Approval", "Admin didn't approve this account yet.\nPlease wait.");
                } else {
                    // Account doesn't exist at all
                    showAlert(Alert.AlertType.ERROR, "Login Failed", "Invalid Account Number.");
                }
            }

        } catch (Exception ex) {
            showAlert(Alert.AlertType.ERROR, "Input Error", "Please enter numeric ID and PIN.");
        }
    });

    box.getChildren().addAll(new Label("Account ID"), txtId, new Label("PIN"), txtPin, btnLogin);
    return box;
}
    private VBox createAdminLoginForm() {
        VBox box = new VBox(15);
        box.setPadding(new Insets(30));
        box.setAlignment(Pos.CENTER);

        TextField txtUser = new TextField(); txtUser.setPromptText("Username");
        PasswordField txtPass = new PasswordField(); txtPass.setPromptText("Password");

        Button btnLogin = new Button("Admin Login");
        btnLogin.setStyle(COLOR_BTN_RED);
        btnLogin.setPrefWidth(200);

        btnLogin.setOnAction(e -> {
            Admin admin = new Admin();
            if (admin.login(txtUser.getText(), txtPass.getText())) {
                window.setScene(createAdminDashboard());
            } else {
                showAlert(Alert.AlertType.ERROR, "Access Denied", "Invalid Admin Credentials");
            }
        });
        box.getChildren().addAll(new Label("Username"), txtUser, new Label("Password"), txtPass, btnLogin);
        return box;
    }

    // ================== STUDENT DASHBOARD ==================
    private Scene createStudentDashboard(Account user) {
        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: " + COLOR_BG + ";");

        HBox top = new HBox(20);
        top.setPadding(new Insets(15));
        top.setAlignment(Pos.CENTER_LEFT);
        top.setStyle("-fx-background-color: " + COLOR_PRIMARY + ";");
        Label lblName = new Label("Welcome, " + user.getName());
        lblName.setTextFill(Color.WHITE);
        lblName.setFont(Font.font(18));
        Button btnLogout = new Button("Logout");
        btnLogout.setOnAction(e -> window.setScene(loginScene));
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        top.getChildren().addAll(lblName, spacer, btnLogout);
        layout.setTop(top);

        VBox center = new VBox(20);
        center.setPadding(new Insets(30));
        center.setAlignment(Pos.TOP_CENTER);

        Label lblBal = new Label("$ " + user.getBalance());
        lblBal.setFont(Font.font("Arial", FontWeight.BOLD, 40));

        GridPane grid = new GridPane();
        grid.setHgap(20); grid.setVgap(20);
        grid.setAlignment(Pos.CENTER);

        Button btnDep = createActionButton("Deposit", "#27ae60");
        Button btnWit = createActionButton("Withdraw", "#c0392b");
        Button btnTra = createActionButton("Transfer", "#e67e22");
        Button btnHis = createActionButton("History", "#2980b9");

        btnDep.setOnAction(e -> {
            String input = showInput("Deposit", "Amount:");
            if(input != null) {
                try {
                    double amt = Double.parseDouble(input);
                    user.deposit(amt);
                    lblBal.setText("$ " + user.getBalance());
                    saveData();
                } catch(Exception ex) { showAlert(Alert.AlertType.ERROR, "Error", "Invalid Amount"); }
            }
        });

        btnWit.setOnAction(e -> {
            String input = showInput("Withdraw", "Amount:");
            if(input != null) {
                try {
                    double amt = Double.parseDouble(input);
                    // GUI Check for balance
                    if(amt > user.getBalance()) {
                        showAlert(Alert.AlertType.ERROR, "Error", "Insufficient Funds");
                        return;
                    }
                    user.withdraw(amt);
                    lblBal.setText("$ " + user.getBalance());
                    saveData();
                } catch(Exception ex) { showAlert(Alert.AlertType.ERROR, "Error", ex.getMessage()); }
            }
        });

        btnTra.setOnAction(e -> showTransferDialog(user, lblBal));
        btnHis.setOnAction(e -> showHistoryWindow(user));

        grid.add(btnDep, 0, 0); grid.add(btnWit, 1, 0);
        grid.add(btnTra, 0, 1); grid.add(btnHis, 1, 1);

        center.getChildren().addAll(new Label("Current Balance"), lblBal, new Separator(), grid);
        layout.setCenter(center);

        return new Scene(layout, 600, 500);
    }

    private Button createActionButton(String text, String color) {
        Button btn = new Button(text);
        btn.setPrefSize(120, 80);
        btn.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-background-radius: 8; -fx-cursor: hand;");
        return btn;
    }

    // ================== ADMIN DASHBOARD (UPDATED) ==================
    private Scene createAdminDashboard() {
        BorderPane layout = new BorderPane();

        HBox top = new HBox(20);
        top.setPadding(new Insets(15));
        top.setStyle("-fx-background-color: #2c3e50;");
        Label title = new Label("ADMIN DASHBOARD");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        Button btnLogout = new Button("Logout");
        btnLogout.setOnAction(e -> window.setScene(loginScene));
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        top.getChildren().addAll(title, spacer, btnLogout);
        layout.setTop(top);

        TabPane tabs = new TabPane();

        // --- TAB 1: CHART ---
        VBox chartBox = new VBox(20);
        chartBox.setAlignment(Pos.CENTER);
        chartBox.setPadding(new Insets(20));

        PieChart chart = new PieChart();
        int s=0, sil=0, g=0, p=0;
        for(Account a : approvedAccounts) {
            if(a.getCardType().equals("Student")) s++;
            else if(a.getCardType().equals("Silver")) sil++;
            else if(a.getCardType().equals("Gold")) g++;
            else p++;
        }
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList(
                new PieChart.Data("Student", s),
                new PieChart.Data("Silver", sil),
                new PieChart.Data("Gold", g),
                new PieChart.Data("Premium", p)
        );
        chart.setData(pieData);
        chart.setTitle("Accounts by Type");
        chartBox.getChildren().addAll(chart, new Label("Total Active Users: " + approvedAccounts.size()));

        // --- TAB 2: PENDING (Approve/Reject) ---
        ListView<String> pendingList = new ListView<>();
        refreshPendingList(pendingList);

        Button btnApprove = new Button("Approve Selected");
        btnApprove.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");
        btnApprove.setOnAction(e -> {
            String sel = pendingList.getSelectionModel().getSelectedItem();
            if(sel != null) processPending(sel, true, pendingList, pieData);
        });

        Button btnReject = new Button("Reject Selected");
        btnReject.setStyle("-fx-background-color: #c0392b; -fx-text-fill: white;");
        btnReject.setOnAction(e -> {
            String sel = pendingList.getSelectionModel().getSelectedItem();
            if(sel != null) processPending(sel, false, pendingList, pieData);
        });

        HBox pendingActions = new HBox(10, btnApprove, btnReject);
        pendingActions.setAlignment(Pos.CENTER);
        VBox pendingBox = new VBox(10, new Label("Pending Requests"), pendingList, pendingActions);
        pendingBox.setPadding(new Insets(20));

        // --- TAB 3: ACTIVE (View Transactions) ---
        ListView<String> approvedList = new ListView<>();
        refreshApprovedList(approvedList);

        Button btnViewTrans = new Button("View User Transactions");
        btnViewTrans.setStyle(COLOR_BTN);
        btnViewTrans.setOnAction(e -> {
            String sel = approvedList.getSelectionModel().getSelectedItem();
            if(sel != null) {
                int id = Integer.parseInt(sel.split(" \\| ")[0]);
                Account target = findApprovedAccount(id);
                if(target != null) showHistoryWindow(target);
            }
        });

        VBox approvedBox = new VBox(10, new Label("Active Accounts"), approvedList, btnViewTrans);
        approvedBox.setPadding(new Insets(20));

        tabs.getTabs().addAll(new Tab("Analytics", chartBox), new Tab("Pending", pendingBox), new Tab("Active", approvedBox));
        layout.setCenter(tabs);

        return new Scene(layout, 700, 550);
    }

    // Admin Helpers
    private void processPending(String sel, boolean approve, ListView<String> list, ObservableList<PieChart.Data> chartData) {
        int id = Integer.parseInt(sel.split(" \\| ")[0]);
        Account target = findPendingAccount(id);

        if(target != null) {
            if(approve) {
                target.setStatus("ACTIVE");
                approvedAccounts.add(target);
                // Update chart data dynamically
                for(PieChart.Data d : chartData) {
                    if(d.getName().equals(target.getCardType())) d.setPieValue(d.getPieValue() + 1);
                }
            }
            pendingAccounts.remove(target);
            saveData();
            refreshPendingList(list);
            showAlert(Alert.AlertType.INFORMATION, "Done", approve ? "Account Approved!" : "Account Rejected.");
        }
    }

    private void refreshPendingList(ListView<String> list) {
        list.getItems().clear();
        for(Account a : pendingAccounts) list.getItems().add(a.getAccountNumber() + " | " + a.getName() + " | " + a.getCardType());
    }

    private void refreshApprovedList(ListView<String> list) {
        list.getItems().clear();
        for(Account a : approvedAccounts) list.getItems().add(a.getAccountNumber() + " | " + a.getName() + " | $" + a.getBalance());
    }

    // ================== DIALOGS ==================
    private void showRegisterWindow() {
        Stage regStage = new Stage();
        VBox layout = new VBox(15);
        layout.setPadding(new Insets(20));
        layout.setAlignment(Pos.CENTER);

        TextField txtName = new TextField(); txtName.setPromptText("Full Name");
        TextField txtEmail = new TextField(); txtEmail.setPromptText("Email");
        TextField txtUid = new TextField(); txtUid.setPromptText("Uni ID");
        PasswordField txtPin = new PasswordField(); txtPin.setPromptText("Set 4-Digit PIN");
        ComboBox<String> comboType = new ComboBox<>();
        comboType.getItems().addAll("1. Student", "2. Silver", "3. Gold", "4. Premium");
        comboType.setPromptText("Select Card Type");

        Button btnSub = new Button("Register");
        btnSub.setStyle(COLOR_BTN);

        btnSub.setOnAction(e -> {
            try {
                int id = accountCounter++;
                String selection = comboType.getValue();
                if(selection == null) throw new Exception("Select Card Type");

                Account newAcc = null;
                if(selection.contains("Student")) newAcc = new StudentAccount(id, txtName.getText(), txtEmail.getText(), txtUid.getText(), Integer.parseInt(txtPin.getText()));
                else if(selection.contains("Silver")) newAcc = new SilverAccount(id, txtName.getText(), txtEmail.getText(), txtUid.getText(), Integer.parseInt(txtPin.getText()));
                else if(selection.contains("Gold")) newAcc = new GoldAccount(id, txtName.getText(), txtEmail.getText(), txtUid.getText(), Integer.parseInt(txtPin.getText()));
                else newAcc = new PremiumAccount(id, txtName.getText(), txtEmail.getText(), txtUid.getText(), Integer.parseInt(txtPin.getText()));

                pendingAccounts.add(newAcc);
                saveData();
                showAlert(Alert.AlertType.INFORMATION, "Success", "Registered! ID: " + id + "\nWait for Admin Approval.");
                regStage.close();
            } catch(Exception ex) { showAlert(Alert.AlertType.ERROR, "Error", "Invalid Input."); }
        });

        layout.getChildren().addAll(new Label("New Account"), txtName, txtEmail, txtUid, txtPin, comboType, btnSub);
        regStage.setScene(new Scene(layout, 350, 450));
        regStage.show();
    }

    private void showTransferDialog(Account from, Label lblBalToUpdate) {
        TextInputDialog idDialog = new TextInputDialog();
        idDialog.setTitle("Transfer");
        idDialog.setHeaderText("Enter Target Account ID:");
        Optional<String> resId = idDialog.showAndWait();

        if(resId.isPresent()) {
            try {
                int tid = Integer.parseInt(resId.get());

                // Prevent Self Transfer in GUI Logic as well
                if(tid == from.getAccountNumber()) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Cannot transfer to yourself.");
                    return;
                }

                Account target = findApprovedAccount(tid);
                if(target == null) throw new Exception("Target account not found.");

                String amtStr = showInput("Transfer Amount", "Enter amount to send:");
                if(amtStr != null) {
                    double amt = Double.parseDouble(amtStr);
                    // Check limit
                    if(amt > from.getBalance()) throw new Exception("Insufficient Funds");

                    from.transfer(target, amt);
                    saveData();
                    lblBalToUpdate.setText("$ " + from.getBalance());
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Transfer Complete!");
                }
            } catch (Exception ex) { showAlert(Alert.AlertType.ERROR, "Transfer Failed", ex.getMessage()); }
        }
    }

    private void showHistoryWindow(Account user) {
        Stage hStage = new Stage();
        ListView<String> list = new ListView<>();
        List<Transaction> trans = user.readTransactionFile();
        for(Transaction t : trans)
            list.getItems().add(t.toString());
        VBox box = new VBox(10, new Label("Transaction History for " + user.getName()), list);
        box.setPadding(new Insets(20));
        hStage.setScene(new Scene(box, 400, 300));
        hStage.show();
    }

    // Shared Helpers
    private Account findApprovedAccount(int id) {
        for(Account a : approvedAccounts)
            if(a.getAccountNumber() == id)
                return a; return null;
    }
    private Account findPendingAccount(int id) {
        for(Account a : pendingAccounts)
            if(a.getAccountNumber() == id)
                return a; return null;
    }
    private String showInput(String title, String content) {
        TextInputDialog d = new TextInputDialog();
        d.setTitle(title);
        d.setContentText(content);
        return d.showAndWait().orElse(null);
    }
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(content);
        a.showAndWait();
    }
    // This method draws a Bank Icon using JavaFX code
    private StackPane createLogo() {
        StackPane logoContainer = new StackPane();

        // 1. The Roof (Triangle)
        javafx.scene.shape.Polygon roof = new javafx.scene.shape.Polygon();
        roof.getPoints().addAll(0.0, 30.0, 40.0, 0.0, 80.0, 30.0);
        roof.setFill(Color.web(COLOR_PRIMARY));

        // 2. The Columns (Rectangles)
        HBox columns = new HBox(5);
        columns.setAlignment(Pos.CENTER);
        for(int i=0; i<3; i++) {
            javafx.scene.shape.Rectangle col = new javafx.scene.shape.Rectangle(15, 30);
//            col.setFill(Color.web(COLOR_DARK );
            columns.getChildren().add(col);
        }

        // 3. The Base
        javafx.scene.shape.Rectangle base = new javafx.scene.shape.Rectangle(80, 10);
        base.setFill(Color.web(COLOR_PRIMARY));

        // Combine them into a Bank shape
        VBox bankShape = new VBox(0, roof, columns, base);
        bankShape.setAlignment(Pos.CENTER);

        // Add a circle background
        javafx.scene.shape.Circle bg = new javafx.scene.shape.Circle(60, Color.web("#bdc3c7"));
        logoContainer.getChildren().addAll(bg, bankShape);
        return logoContainer;
    }
    public static void main(String[] args) {
        BankingApp.main(args);
    }
}