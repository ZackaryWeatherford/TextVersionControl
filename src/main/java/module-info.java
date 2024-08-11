module com.example.textversioncontrol {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.eclipse.jgit;
    requires java.sql;
    requires org.xerial.sqlitejdbc;


    opens com.example.textversioncontrol to javafx.fxml;
    exports com.example.textversioncontrol;
    exports com.example.textversioncontrol.controllers;
    opens com.example.textversioncontrol.controllers to javafx.fxml;
    exports com.example.textversioncontrol.models;
    opens com.example.textversioncontrol.models to javafx.fxml;
    exports com.example.textversioncontrol.managers;
    opens com.example.textversioncontrol.managers to javafx.fxml;
}