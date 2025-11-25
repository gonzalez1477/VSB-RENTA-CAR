package com.example.sistema_rentacar.Controllers.Cliente;

import com.example.sistema_rentacar.Modelos.Alquiler;
import com.example.sistema_rentacar.Repository.AlquilerRepository;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.function.Consumer;

public class DialogoFinalizarAlquilerController {

    @FXML private Label lblTitulo;
    @FXML private Label lblVehiculo;
    @FXML private Label lblCliente;
    @FXML private Label lblFechaInicio;
    @FXML private Label lblFechaLimite;
    @FXML private Label lblFechaDevolucion;
    @FXML private VBox boxAlertaRetraso;
    @FXML private Label lblDiasRetraso;
    @FXML private Label lblTarifaDiaria;
    @FXML private Label lblRecargoDiario;
    @FXML private Label lblCostoAlquiler;
    @FXML private Label lblDeposito;
    @FXML private Label lblTextoPenalizacion;
    @FXML private Label lblPenalizacion;
    @FXML private Label lblTotalPagar;
    @FXML private Label lblNotaRetraso;
    @FXML private TextArea txtObservaciones;
    @FXML private Button btnConfirmar;
    @FXML private Button btnCancelar;

    private Alquiler alquiler;
    private AlquilerRepository alquilerRepo;
    private Consumer<Boolean> onConfirmar;

    private boolean esRetraso = false;
    private int diasRetraso = 0;
    private double penalizacion = 0.0;
    private double totalPagar = 0.0;

    private static final double FACTOR_PENALIZACION_NORMAL = 1.5; // 150%
    private static final double FACTOR_PENALIZACION_MODERADO = 2.0; // 200% (4-7 días)
    private static final double FACTOR_PENALIZACION_ALTO = 2.5; // 250% (8+ días)

    @FXML
    public void initialize() {
        alquilerRepo = new AlquilerRepository();
    }

    public void setDatos(Alquiler alquiler, Consumer<Boolean> onConfirmar) {
        this.alquiler = alquiler;
        this.onConfirmar = onConfirmar;

        cargarDatosAlquiler();
        verificarRetraso();
        calcularTotales();
    }

    private void cargarDatosAlquiler() {
        lblVehiculo.setText(alquiler.getVehiculo() + " - " + alquiler.getPlaca());
        lblCliente.setText(alquiler.getNombreCliente());


        LocalDateTime fechaInicio = alquiler.getFechaInicio().toLocalDateTime();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        lblFechaInicio.setText(fechaInicio.format(formatter));


        LocalDate fechaLimite = alquiler.getFechaFinEstimada().toLocalDateTime().toLocalDate();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        lblFechaLimite.setText(fechaLimite.format(dateFormatter));

        lblFechaDevolucion.setText(LocalDate.now().format(dateFormatter) + " (HOY)");
    }

    private void verificarRetraso() {

        LocalDate fechaLimite = alquiler.getFechaFinEstimada().toLocalDateTime().toLocalDate();
        LocalDate hoy = LocalDate.now();

        if (hoy.isAfter(fechaLimite)) {
            esRetraso = true;

            diasRetraso = (int) ChronoUnit.DAYS.between(fechaLimite, hoy);

            double factor;
            if (diasRetraso >= 8) {
                factor = FACTOR_PENALIZACION_ALTO;
            } else if (diasRetraso >= 4) {
                factor = FACTOR_PENALIZACION_MODERADO;
            } else {
                factor = FACTOR_PENALIZACION_NORMAL;
            }

            double recargoPorDia = alquiler.getTarifaDiaria() * factor;
            penalizacion = recargoPorDia * diasRetraso;

            boxAlertaRetraso.setVisible(true);
            boxAlertaRetraso.setManaged(true);

            lblDiasRetraso.setText(diasRetraso + " día(s)");
            lblTarifaDiaria.setText(String.format("$%.2f", alquiler.getTarifaDiaria()));
            lblRecargoDiario.setText(String.format("$%.2f (%.0f%% de recargo)",
                    recargoPorDia, factor * 100));

            lblTextoPenalizacion.setVisible(true);
            lblTextoPenalizacion.setManaged(true);
            lblPenalizacion.setVisible(true);
            lblPenalizacion.setManaged(true);
            lblPenalizacion.setText(String.format("+ $%.2f", penalizacion));

            lblNotaRetraso.setVisible(true);
            lblNotaRetraso.setManaged(true);

            lblTitulo.setText("Finalizar Alquiler con Retraso");
            lblTitulo.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #c62828;");
        }
    }

    private void calcularTotales() {
        double costoAlquiler = alquiler.getCostoTotal();
        double deposito = alquiler.getDeposito();
        double saldoPendiente = costoAlquiler - deposito;

        lblCostoAlquiler.setText(String.format("$%.2f", costoAlquiler));
        lblDeposito.setText(String.format("- $%.2f", deposito));

        if (esRetraso) {
            totalPagar = saldoPendiente + penalizacion;
            lblTotalPagar.setText(String.format("$%.2f", totalPagar));
            lblTotalPagar.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #e74c3c;");
        } else {
            totalPagar = saldoPendiente;
            lblTotalPagar.setText(String.format("$%.2f", totalPagar));
            lblTotalPagar.setStyle("-fx-font-weight: bold; -fx-font-size: 18px; -fx-text-fill: #27ae60;");
        }
    }

    @FXML
    private void handleConfirmar() {
        // Confirmar acción
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Devolución");

        if (esRetraso) {
            confirmacion.setHeaderText("¿Confirmar devolución con retraso?");
            confirmacion.setContentText(String.format(
                    "ATENCIÓN: Este alquiler tiene retraso\n\n" +
                            "Días de retraso: %d\n" +
                            "Penalización: $%.2f\n" +
                            "Total a cobrar: $%.2f\n\n" +
                            "¿Ha recibido el pago completo del cliente?",
                    diasRetraso,
                    penalizacion,
                    totalPagar
            ));
        } else {
            confirmacion.setHeaderText("¿Confirmar devolución del vehículo?");
            confirmacion.setContentText(String.format(
                    "Vehículo: %s\n" +
                            "Cliente: %s\n" +
                            "Total a cobrar: $%.2f\n\n" +
                            "Esta acción finalizará el alquiler.",
                    alquiler.getVehiculo(),
                    alquiler.getNombreCliente(),
                    totalPagar
            ));
        }

        ButtonType btnSi = new ButtonType("Sí, finalizar");
        ButtonType btnNo = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        confirmacion.getButtonTypes().setAll(btnSi, btnNo);

        confirmacion.showAndWait().ifPresent(response -> {
            if (response == btnSi) {
                finalizarAlquiler();
            }
        });
    }

    private void finalizarAlquiler() {
        String observaciones = txtObservaciones.getText().trim();
        String estado = esRetraso ? "Finalizado con Retraso" : "Finalizado";

        StringBuilder obsCompletas = new StringBuilder();

        if (esRetraso) {
            obsCompletas.append("[FINALIZADO CON RETRASO]\n");
            obsCompletas.append(String.format("Días de retraso: %d\n", diasRetraso));
            obsCompletas.append(String.format("Penalización cobrada: $%.2f\n", penalizacion));
            obsCompletas.append(String.format("Total cobrado: $%.2f\n", totalPagar));
            obsCompletas.append(String.format("Fecha devolución: %s\n",
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        }

        if (!observaciones.isEmpty()) {
            obsCompletas.append("\nObservaciones de devolución:\n");
            obsCompletas.append(observaciones);
        }

        // Finalizar alquiler
        if (alquilerRepo.finalizar(alquiler.getIdAlquiler())) {
            // Actualizar estado específico
            alquilerRepo.cambiarEstado(alquiler.getIdAlquiler(), estado);

            // Actualizar penalización si hay retraso
            if (esRetraso) {
                alquilerRepo.actualizarPenalizacion(
                        alquiler.getIdAlquiler(),
                        penalizacion,
                        diasRetraso
                );
            }

            // Actualizar observaciones
            if (obsCompletas.length() > 0) {
                alquilerRepo.actualizarObservaciones(
                        alquiler.getIdAlquiler(),
                        obsCompletas.toString()
                );
            }

            mostrarExito();

            if (onConfirmar != null) {
                onConfirmar.accept(esRetraso);
            }

            cerrarDialogo();
        } else {
            mostrarError("No se pudo finalizar el alquiler. Intente nuevamente.");
        }
    }

    @FXML
    private void handleCancelar() {
        cerrarDialogo();
    }

    private void mostrarExito() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Alquiler Finalizado");

        if (esRetraso) {
            alert.setHeaderText("Devolución con retraso procesada");
            alert.setContentText(String.format(
                    "El alquiler ha sido finalizado con retraso.\n\n" +
                            "Días de retraso: %d\n" +
                            "Penalización cobrada: $%.2f\n" +
                            "Total cobrado: $%.2f\n\n" +
                            "El vehículo está disponible nuevamente.",
                    diasRetraso,
                    penalizacion,
                    totalPagar
            ));
        } else {
            alert.setHeaderText("Vehículo devuelto exitosamente");
            alert.setContentText(String.format(
                    "El alquiler ha sido finalizado.\n\n" +
                            "Total cobrado: $%.2f\n\n" +
                            "El vehículo está disponible nuevamente.",
                    totalPagar
            ));
        }

        alert.showAndWait();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void cerrarDialogo() {
        Stage stage = (Stage) btnConfirmar.getScene().getWindow();
        stage.close();
    }
}
