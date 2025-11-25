package com.example.sistema_rentacar.Controllers.Cliente;

import com.example.sistema_rentacar.Repository.AlquilerRepository;
import com.example.sistema_rentacar.Repository.PagoRepository;
import com.example.sistema_rentacar.Modelos.Alquiler;
import com.example.sistema_rentacar.Modelos.Pago;
import com.example.sistema_rentacar.Modelos.Vehiculo;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import com.example.sistema_rentacar.Servicios.EmailService;
import com.example.sistema_rentacar.Repository.ClienteRepository;
import com.example.sistema_rentacar.Modelos.Cliente;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.io.IOException;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class DialogoAlquilerController {

    @FXML private Label lblVehiculo;
    @FXML private Label lblTarifa;
    @FXML private DatePicker dpFechaInicio;
    @FXML private DatePicker dpFechaFin;
    @FXML private Spinner<Integer> spinnerDias;
    @FXML private Label lblCostoTotal;
    @FXML private TextField txtDeposito;
    @FXML private TextArea txtObservaciones;
    @FXML private RadioButton rbEfectivo;
    @FXML private RadioButton rbTarjeta;
    @FXML private Label lblInfoMetodoPago;
    @FXML private Button btnConfirmar;
    @FXML private Button btnCancelar;

    private Vehiculo vehiculo;
    private int idCliente;
    private CatalogoClienteController catalogoController;
    private AlquilerRepository alquilerDAO;
    private PagoRepository pagoDAO;
    private double tarifaDiaria;
    private Pago pagoTarjeta; // Para almacenar el pago procesado con tarjeta
    private ToggleGroup toggleMetodoPago;

    @FXML
    public void initialize() {
        alquilerDAO = new AlquilerRepository();
        pagoDAO = new PagoRepository();

        // ToggleGroup para los RadioButtons
        toggleMetodoPago = new ToggleGroup();
        rbEfectivo.setToggleGroup(toggleMetodoPago);
        rbTarjeta.setToggleGroup(toggleMetodoPago);
        rbEfectivo.setSelected(true);

        // fecha inicio como hoy
        dpFechaInicio.setValue(LocalDate.now());
        dpFechaInicio.setDisable(true);

        // spinner de días
        SpinnerValueFactory<Integer> valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 90, 1);
        spinnerDias.setValueFactory(valueFactory);

        // calcular costo
        spinnerDias.valueProperty().addListener((obs, oldVal, newVal) -> {
            actualizarFechaFin();
            calcularCosto();
        });

        dpFechaFin.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                calcularDias();
                calcularCosto();
            }
        });

        //cambiar metodo de pago
        rbEfectivo.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                lblInfoMetodoPago.setText("El depósito se pagará en efectivo al confirmar el alquiler.");
            }
        });

        rbTarjeta.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                lblInfoMetodoPago.setText("Se abrirá una ventana segura para procesar el pago con tarjeta.");
            }
        });
    }

    public void setDatos(Vehiculo vehiculo, int idCliente, CatalogoClienteController catalogoController) {
        this.vehiculo = vehiculo;
        this.idCliente = idCliente;
        this.catalogoController = catalogoController;
        this.tarifaDiaria = vehiculo.getTarifaPorDia();

        lblVehiculo.setText(vehiculo.getNombreCompleto());
        lblTarifa.setText(String.format("$%.2f por día", tarifaDiaria));
        txtDeposito.setText(String.format("%.2f", tarifaDiaria * 0.5)); // 50% de depósito

        actualizarFechaFin();
        calcularCosto();
    }

    private void actualizarFechaFin() {
        int dias = spinnerDias.getValue();
        LocalDate fechaFin = LocalDate.now().plusDays(dias);
        dpFechaFin.setValue(fechaFin);
    }

    private void calcularDias() {
        LocalDate inicio = dpFechaInicio.getValue();
        LocalDate fin = dpFechaFin.getValue();

        if (inicio != null && fin != null && fin.isAfter(inicio)) {
            long dias = ChronoUnit.DAYS.between(inicio, fin);
            spinnerDias.getValueFactory().setValue((int) dias);
        }
    }

    private void calcularCosto() {
        int dias = spinnerDias.getValue();
        double costoTotal = tarifaDiaria * dias;
        double deposito = costoTotal * 0.5; // 50% de depósito

        lblCostoTotal.setText(String.format("$%.2f", costoTotal));
        txtDeposito.setText(String.format("%.2f", deposito));
    }

    @FXML
    private void handleConfirmar() {
        // Validaciones básicas
        if (dpFechaFin.getValue() == null) {
            mostrarError("Seleccione una fecha de finalización");
            return;
        }

        if (dpFechaFin.getValue().isBefore(LocalDate.now())) {
            mostrarError("La fecha de finalización no puede ser anterior a hoy");
            return;
        }

        String depositoStr = txtDeposito.getText().trim();
        if (depositoStr.isEmpty()) {
            mostrarError("Ingrese el monto del depósito");
            return;
        }

        double deposito;
        try {
            deposito = Double.parseDouble(depositoStr);
            if (deposito < 0) {
                mostrarError("El depósito debe ser un valor positivo");
                return;
            }
        } catch (NumberFormatException e) {
            mostrarError("Depósito inválido");
            return;
        }

        //metodo de pago seleccionado
        if (rbTarjeta.isSelected()) {
            // Procesar pago con tarjeta
            procesarPagoConTarjeta(deposito);
        } else {
            // Procesar pago en efectivo
            procesarPagoEnEfectivo(deposito);
        }
    }

    private void procesarPagoConTarjeta(double deposito) {
        try {
            // Cargar el diálogo de pago con tarjeta
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/sistema_rentacar/Views/Cliente/DialogoPagoTarjeta.fxml"));
            Parent root = loader.load();

            DialogoPagoTarjetaController controller = loader.getController();

            // Configurar datos del pago
            controller.setDatos(
                    deposito,
                    "Depósito inicial de alquiler - " + vehiculo.getNombreCompleto(),
                    (pago) -> {
                        // Callback cuando el pago es exitoso
                        this.pagoTarjeta = pago;
                        confirmarAlquilerConPago();
                    }
            );

            // Mostrar diálogo modal
            Stage stage = new Stage();
            stage.setTitle("Pago con Tarjeta");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(btnConfirmar.getScene().getWindow());
            stage.setResizable(false);
            stage.showAndWait();

        } catch (IOException e) {
            mostrarError("Error al cargar el módulo de pago: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void procesarPagoEnEfectivo(double deposito) {
        // Confirmar el pago en efectivo
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar Pago en Efectivo");
        confirmacion.setHeaderText("Pago del Depósito en Efectivo");
        confirmacion.setContentText(String.format(
                "Monto a pagar: $%.2f\n\n" +
                        "El cliente deberá pagar este monto en efectivo.\n" +
                        "¿Confirmar el alquiler?",
                deposito
        ));

        if (confirmacion.showAndWait().get() == ButtonType.OK) {
            // Crear objeto de pago en efectivo
            pagoTarjeta = new Pago(0, deposito, "Efectivo", "PAGO-EF-" + System.currentTimeMillis());

            pagoTarjeta.setFechaPago(new Timestamp(System.currentTimeMillis()));

            confirmarAlquilerConPago();
        }
    }

    private void confirmarAlquilerConPago() {
        // Crear objeto Alquiler
        int dias = spinnerDias.getValue();
        double costoTotal = tarifaDiaria * dias;
        double deposito = Double.parseDouble(txtDeposito.getText().trim());

        Alquiler alquiler = new Alquiler(
                idCliente,
                vehiculo.getIdVehiculo(),
                Timestamp.valueOf(dpFechaInicio.getValue().atStartOfDay()),
                Timestamp.valueOf(dpFechaFin.getValue().atStartOfDay()),
                dias,
                tarifaDiaria,
                costoTotal,
                deposito
        );
        alquiler.setObservaciones(txtObservaciones.getText());

        // Guardar alquiler en base de datos
        if (alquilerDAO.crear(alquiler)) {
            // Registrar el pago
            pagoTarjeta.setIdAlquiler(alquiler.getIdAlquiler());

            boolean pagoRegistrado = false;
            if (pagoTarjeta.esPagoConTarjeta()) {
                pagoRegistrado = pagoDAO.registrarPagoTarjeta(pagoTarjeta);
            } else {
                pagoRegistrado = pagoDAO.registrarPagoEfectivo(pagoTarjeta);
            }

            if (pagoRegistrado) {
                // Preguntar si desea enviar factura por correo
                Alert alertCorreo = new Alert(Alert.AlertType.CONFIRMATION);
                alertCorreo.setTitle("Enviar Factura");
                alertCorreo.setHeaderText("¿Desea enviar la factura por correo electrónico?");
                alertCorreo.setContentText("Se enviará una copia de la factura al correo del cliente.");

                ButtonType btnSi = new ButtonType("Sí, enviar");
                ButtonType btnNo = new ButtonType("No, gracias");
                alertCorreo.getButtonTypes().setAll(btnSi, btnNo);

                alertCorreo.showAndWait().ifPresent(response -> {
                    if (response == btnSi) {
                        enviarFacturaPorCorreo(alquiler, pagoTarjeta);
                    } else {
                        mostrarExito();
                        finalizarAlquiler();
                    }
                });
            } else {
                mostrarError("El alquiler se registró pero hubo un problema al registrar el pago.");
            }
        } else {
            mostrarError("Error al procesar el alquiler. Intente nuevamente.");
        }
    }

    @FXML
    private void handleCancelar() {
        cerrarDialogo();
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(mensaje);
        alert.showAndWait();
    }

    private void mostrarExito() {
        String metodoPago = pagoTarjeta.esPagoConTarjeta() ? "Tarjeta" : "Efectivo";
        String detallesPago = "";

        if (pagoTarjeta.esPagoConTarjeta()) {
            detallesPago = String.format(
                    "\n\nDetalles del Pago:\n" +
                            "Método: %s (%s)\n" +
                            "Tarjeta: %s\n" +
                            "Autorización: %s",
                    metodoPago,
                    pagoTarjeta.getTipoTarjeta(),
                    pagoTarjeta.getTarjetaEnmascarada(),
                    pagoTarjeta.getReferencia()
            );
        } else {
            detallesPago = String.format(
                    "\n\nDetalles del Pago:\n" +
                            "Método: %s\n" +
                            "Referencia: %s",
                    metodoPago,
                    pagoTarjeta.getReferencia()
            );
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Alquiler Confirmado");
        alert.setHeaderText("¡Alquiler realizado con éxito!");
        alert.setContentText(String.format(
                "Vehículo: %s\n" +
                        "Días: %d\n" +
                        "Costo Total: $%.2f\n" +
                        "Depósito Pagado: $%.2f%s\n\n" +
                        "¡Disfrute su viaje!",
                vehiculo.getNombreCompleto(),
                spinnerDias.getValue(),
                Double.parseDouble(lblCostoTotal.getText().replace("$", "")),
                pagoTarjeta.getMonto(),
                detallesPago
        ));
        alert.showAndWait();
    }

    private void cerrarDialogo() {
        Stage stage = (Stage) btnConfirmar.getScene().getWindow();
        stage.close();
    }

    private void enviarFacturaPorCorreo(Alquiler alquiler, Pago pago) {
        // Mostrar indicador de envío
        Alert alertEnviando = new Alert(Alert.AlertType.INFORMATION);
        alertEnviando.setTitle("Enviando correo");
        alertEnviando.setHeaderText("Enviando factura por correo...");
        alertEnviando.setContentText("Por favor espere un momento.");
        alertEnviando.show();

        Task<Boolean> enviarTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {

                ClienteRepository clienteRepo = new ClienteRepository();
                Cliente cliente = clienteRepo.obtenerPorId(idCliente);

                if (cliente == null || cliente.getEmail() == null || cliente.getEmail().isEmpty()) {
                    return false;
                }

                // Enviar el correo
                return EmailService.enviarFacturaAlquiler(
                        cliente.getEmail(),
                        cliente.getNombre() + " " + cliente.getApellido(),
                        alquiler,
                        pago,
                        vehiculo.getNombreCompleto()
                );
            }
        };

        enviarTask.setOnSucceeded(event -> {
            alertEnviando.close();

            if (enviarTask.getValue()) {
                Alert alertExito = new Alert(Alert.AlertType.INFORMATION);
                alertExito.setTitle("Correo enviado");
                alertExito.setHeaderText("Factura enviada exitosamente");
                alertExito.setContentText("El cliente recibirá la factura en su correo electrónico.");
                alertExito.showAndWait();
            } else {
                Alert alertError = new Alert(Alert.AlertType.WARNING);
                alertError.setTitle("Error al enviar");
                alertError.setHeaderText("No se pudo enviar el correo");
                alertError.setContentText("El alquiler se registró correctamente, pero hubo un problema al enviar el correo. Puede intentar enviarlo más tarde.");
                alertError.showAndWait();
            }

            mostrarExito();
            finalizarAlquiler();
        });

        enviarTask.setOnFailed(event -> {
            alertEnviando.close();
            Alert alertError = new Alert(Alert.AlertType.WARNING);
            alertError.setTitle("Error");
            alertError.setHeaderText("Error al enviar correo");
            alertError.setContentText("Ocurrió un error: " + enviarTask.getException().getMessage());
            alertError.showAndWait();

            mostrarExito();
            finalizarAlquiler();
        });

        new Thread(enviarTask).start();
    }

    private void finalizarAlquiler() {
        if (catalogoController != null) {
            catalogoController.refrescarCatalogo();
        }
        cerrarDialogo();
    }
}