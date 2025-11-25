package com.example.sistema_rentacar.Controllers;

import com.example.sistema_rentacar.Controllers.Cliente.CatalogoClienteController;
import com.example.sistema_rentacar.Utilidades.CambiarScena;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class InicioController {

    @FXML
    private Button btnEmpleado;

    @FXML
    private Button btnCliente;

    @FXML
    private void handleMouseEntered(MouseEvent event) {
        Button btn = (Button) event.getSource();
        btn.setStyle(btn.getStyle() + "; -fx-scale-x: 1.05; -fx-scale-y: 1.05;");
    }

    @FXML
    private void handleMouseExited(MouseEvent event) {
        Button btn = (Button) event.getSource();
        String originalStyle = btn.getId().equals("btnEmpleado") ?
                "-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12 40; -fx-background-radius: 8; -fx-cursor: hand;" :
                "-fx-background-color: #2ecc71; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12 40; -fx-background-radius: 8; -fx-cursor: hand;";
        btn.setStyle(originalStyle);
    }


    @FXML
    private void onEmpleadoClick() {

        System.out.println("Acceso de Empleado");

        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/sistema_rentacar/Views/empleado/LoginEmpleado.fxml")
            );
            Parent root = loader.load();

            Stage stage = (Stage) btnEmpleado.getScene().getWindow();
            CambiarScena.cambiar(stage, root, "Login Empleado - VSB Renta Car");

        } catch (Exception e) {
            System.err.println("Error al abrir login de empleado: " + e.getMessage());
            e.printStackTrace();
        }

    }

    @FXML
    private void onClienteClick() {
        // Lógica para ir a la vista de cliente como invitado
        System.out.println("Acceso de Cliente");

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/sistema_rentacar/Views/cliente/CatalogoCliente.fxml"));
            Parent root = loader.load();
            CatalogoClienteController controller = loader.getController();
            controller.setModoInvitado();
            Stage stage = (Stage) btnCliente.getScene().getWindow();
            CambiarScena.cambiar(stage, root, "Catálogo - VSB Renta Car");

        } catch (Exception e) {
            System.err.println("Error al abrir catálogo: " + e.getMessage());
            e.printStackTrace();

        }
    }


}
