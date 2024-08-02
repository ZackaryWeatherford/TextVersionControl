module com.example.textversioncontrol {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.eclipse.jgit;


    opens com.example.textversioncontrol to javafx.fxml;
    exports com.example.textversioncontrol;
    exports com.example.textversioncontrol.controllers;
    opens com.example.textversioncontrol.controllers to javafx.fxml;
    exports com.example.textversioncontrol.models;
    opens com.example.textversioncontrol.models to javafx.fxml;
}