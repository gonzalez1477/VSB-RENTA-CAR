package com.example.sistema_rentacar.Controllers.Empleado;

import com.example.sistema_rentacar.Repository.TipoVehiculoRepository;
import com.example.sistema_rentacar.Modelos.TipoVehiculo;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class FormularioTipoVehiculoController {

    @FXML private TextField txtNombre;
    @FXML private TextField txtTarifa;
    @FXML private TextArea txtDescripcion;
    @FXML private Label lblTitulo;
    @FXML private Label lblNombreCarpeta;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;

    private TipoVehiculoRepository tipoVehiculoDAO;
    private TipoVehiculo tipoActual;
    private DashboardEmpleadoController dashboardController;
    private boolean esEdicion;

    @FXML
    public void initialize() {
        tipoVehiculoDAO = new TipoVehiculoRepository();

        // actualizar el nombre de carpeta en tiempo real
        txtNombre.textProperty().addListener((obs, old, nuevo) -> {
            if (nuevo != null && !nuevo.isEmpty()) {
                String carpeta = normalizarNombreCarpeta(nuevo);
                lblNombreCarpeta.setText("Carpeta de imágenes: /images/" + carpeta + "/");
            } else {
                lblNombreCarpeta.setText("Carpeta de imágenes: /images/");
            }
        });
    }

    public void setDatos(TipoVehiculo tipo, DashboardEmpleadoController controller) {
        this.dashboardController = controller;
        this.tipoActual = tipo;
        this.esEdicion = (tipo != null);

        if (esEdicion) {
            lblTitulo.setText("Editar Categoría de Vehículo");
            cargarDatos();
        } else {
            lblTitulo.setText("Nueva Categoría de Vehículo");
        }
    }

    private void cargarDatos() {
        txtNombre.setText(tipoActual.getNombreTipo());
        txtTarifa.setText(String.valueOf(tipoActual.getTarifaPorDia()));
        txtDescripcion.setText(tipoActual.getDescripcion());

        // Actualizar label de carpeta
        String carpeta = normalizarNombreCarpeta(tipoActual.getNombreTipo());
        lblNombreCarpeta.setText("Carpeta de imágenes: /images/" + carpeta + "/");
    }

    @FXML
    private void handleGuardar() {
        if (!validarCampos()) {
            return;
        }

        TipoVehiculo tipo;
        if (esEdicion) {
            tipo = tipoActual;
        } else {
            tipo = new TipoVehiculo();
        }

        tipo.setNombreTipo(txtNombre.getText().trim());
        tipo.setTarifaPorDia(Double.parseDouble(txtTarifa.getText().trim()));
        tipo.setDescripcion(txtDescripcion.getText().trim());

        boolean exito;
        if (esEdicion) {
            exito = tipoVehiculoDAO.actualizar(tipo);
        } else {
            // Verificar que no exista el nombre
            if (tipoVehiculoDAO.existeNombre(tipo.getNombreTipo())) {
                mostrarError("Ya existe una categoría con ese nombre");
                return;
            }
            exito = tipoVehiculoDAO.insertar(tipo);
        }

        if (exito) {
            mostrarExito(esEdicion ? "Categoría actualizada correctamente" : "Categoría creada correctamente");

            // Crear carpeta de imágenes
            crearCarpetaImagenes(tipo.getNombreTipo());

            if (dashboardController != null) {
                dashboardController.refrescarTiposVehiculo();
            }
            cerrar();
        } else {
            mostrarError("Error al guardar la categoría");
        }
    }

    private void crearCarpetaImagenes(String nombreTipo) {
        try {
            String rutaProyecto = System.getProperty("user.dir");
            String carpeta = normalizarNombreCarpeta(nombreTipo);

            java.nio.file.Path directorioDestino = java.nio.file.Paths.get(
                    rutaProyecto, "src", "main", "resources", "images", carpeta
            );

            if (!java.nio.file.Files.exists(directorioDestino)) {
                java.nio.file.Files.createDirectories(directorioDestino);
                System.out.println("✓ Carpeta creada: " + directorioDestino);
            }
        } catch (Exception e) {
            System.err.println("Advertencia: No se pudo crear carpeta de imágenes: " + e.getMessage());
            // No es un error crítico, continuar
        }
    }

    private String normalizarNombreCarpeta(String nombre) {
        return nombre.toLowerCase()
                .replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ú", "u")
                .replace(" ", "_")
                .replace("ñ", "n")
                .replaceAll("[^a-z0-9_]", "");
    }

    private boolean validarCampos() {
        String nombre = txtNombre.getText().trim();
        String tarifaStr = txtTarifa.getText().trim();

        if (nombre.isEmpty()) {
            mostrarError("Ingrese el nombre de la categoría");
            return false;
        }

        if (tarifaStr.isEmpty()) {
            mostrarError("Ingrese la tarifa por día");
            return false;
        }

        try {
            double tarifa = Double.parseDouble(tarifaStr);
            if (tarifa <= 0) {
                mostrarError("La tarifa debe ser mayor a 0");
                return false;
            }
        } catch (NumberFormatException e) {
            mostrarError("La tarifa debe ser un número válido");
            return false;
        }

        return true;
    }

    @FXML
    private void handleCancelar() {
        cerrar();
    }

    private void cerrar() {
        Stage stage = (Stage) btnGuardar.getScene().getWindow();
        stage.close();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarExito(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }
}
