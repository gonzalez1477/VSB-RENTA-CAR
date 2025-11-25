package com.example.sistema_rentacar;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;

//importamos el servicio de programador
import com.example.sistema_rentacar.Servicios.AlquilerScheduler;

import com.example.sistema_rentacar.Controllers.Cliente.CatalogoClienteController;
import com.example.sistema_rentacar.Conexion.ConexionDB;

import java.io.IOException;

public class MainApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {

        // Probar conexión a base de datos
        if (!ConexionDB.testConnection()) {
            mostrarErrorConexion();
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("Views/Inicio-View.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        //scene.getStylesheets().add(getClass().getResource("css/login.css").toExternalForm());

        stage.setTitle("VSB RENTA CAR!");
        stage.setScene(scene);

        /* obtengo las dimensiones porque estoy usando una sola escena asi que el
        tamaño de la scena inicial sera el que va a mostrar en las demas,
        si no tomo ese mañaño las demas se van a buguear el taaño*/


        // Obtener dimensiones de la pantalla
        Screen screen = Screen.getPrimary();
        Rectangle2D bounds = screen.getVisualBounds();

        // Establecer posición y tamaño de pantalla completa
        stage.setX(bounds.getMinX());
        stage.setY(bounds.getMinY());
        stage.setWidth(bounds.getWidth());
        stage.setHeight(bounds.getHeight());

        // Agregar icono del sistema
        try {
            stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/example/sistema_rentacar/images/logo/logo.png")));
        } catch (Exception e) {
            System.out.println("No se pudo cargar el icono de la aplicación");
        }

        stage.show();

        // INICIAR EL SCHEDULER DE ALQUILERES
        AlquilerScheduler.iniciar();

        //Maximizar después en el siguiente ciclo
        Platform.runLater(() -> stage.setMaximized(true));
    }

    private void mostrarErrorConexion() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error de Conexión");
        alert.setHeaderText("No se pudo conectar a la base de datos");
        alert.setContentText("Verificar credenciales de la base de datos");
        alert.showAndWait();
        System.exit(1);
    }

    @Override
    public void stop() {
        //DETENER EL SCHEDULER AL CERRAR
        AlquilerScheduler.detener();
        System.out.println("Aplicación cerrada");
    }

    public static void main(String[] args) {
        launch();
    }
}
