package com.example.sistema_rentacar.Controllers.Cliente;

import com.example.sistema_rentacar.Repository.ClienteRepository;
import com.example.sistema_rentacar.Modelos.Cliente;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Date;
import java.time.LocalDate;
import java.util.regex.Pattern;

public class PerfilClienteController {

    @FXML private Label lblTitulo;
    @FXML private Label lblNombreCompleto;
    @FXML private Label lblEmail;
    @FXML private Label lblTelefono;
    @FXML private Label lblDireccion;
    @FXML private Label lblDui;
    @FXML private Label lblLicencia;
    @FXML private Label lblFechaNacimiento;
    @FXML private Label lblUsuario;
    @FXML private Label lblFechaRegistro;

    @FXML private VBox vboxVisualizacion;
    @FXML private VBox vboxEdicion;

    @FXML private TextField txtNombre;
    @FXML private TextField txtApellido;
    @FXML private TextField txtEmail;
    @FXML private TextField txtTelefono;
    @FXML private TextArea txtDireccion;
    @FXML private TextField txtDui;
    @FXML private TextField txtLicencia;
    @FXML private DatePicker dpFechaNacimiento;

    @FXML private CheckBox chkCambiarContrasena;
    @FXML private VBox vboxContrasena;
    @FXML private PasswordField txtContrasenaActual;
    @FXML private PasswordField txtContrasenaNueva;
    @FXML private PasswordField txtConfirmarContrasena;

    @FXML private Button btnEditar;
    @FXML private Button btnGuardar;
    @FXML private Button btnCancelar;
    @FXML private Button btnVolver;

    private Cliente clienteActual;
    private ClienteRepository clienteDAO;
    private boolean modoEdicion = false;

    private static final Pattern TELEFONO_PATTERN = Pattern.compile("^\\d{4}-\\d{4}$");

    @FXML
    public void initialize() {
        clienteDAO = new ClienteRepository();

        // Ocultar campos de edición inicialmente
        vboxEdicion.setVisible(false);
        vboxEdicion.setManaged(false);
        vboxContrasena.setVisible(false);
        vboxContrasena.setManaged(false);

        //
        chkCambiarContrasena.selectedProperty().addListener((obs, old, nuevo) -> {
            vboxContrasena.setVisible(nuevo);
            vboxContrasena.setManaged(nuevo);

            // Limpiar campos si se desmarca
            if (!nuevo) {
                txtContrasenaActual.clear();
                txtContrasenaNueva.clear();
                txtConfirmarContrasena.clear();
            }
        });
    }

    public void setCliente(Cliente cliente) {
        this.clienteActual = cliente;
        cargarDatos();
    }

    private void cargarDatos() {
        // Actualizar información del cliente desde la BD
        Cliente clienteActualizado = clienteDAO.obtenerPorId(clienteActual.getIdCliente());
        if (clienteActualizado != null) {
            this.clienteActual = clienteActualizado;
        }

        // Mostrar en label
        lblTitulo.setText("Perfil de " + clienteActual.getNombreCompleto());
        lblNombreCompleto.setText(clienteActual.getNombreCompleto());
        lblEmail.setText(clienteActual.getEmail());
        lblTelefono.setText(clienteActual.getTelefono());
        lblDireccion.setText(clienteActual.getDireccion() != null ? clienteActual.getDireccion() : "No especificada");
        lblDui.setText(clienteActual.getDui());
        lblLicencia.setText(clienteActual.getLicencia());

        if (clienteActual.getFechaNacimiento() != null) {
            lblFechaNacimiento.setText(clienteActual.getFechaNacimiento().toString());
        }

        lblUsuario.setText(clienteActual.getUsuario());

        if (clienteActual.getFechaRegistro() != null) {
            lblFechaRegistro.setText(clienteActual.getFechaRegistro().toString());
        }

        // Cargar en campos de edición
        txtNombre.setText(clienteActual.getNombre());
        txtApellido.setText(clienteActual.getApellido());
        txtEmail.setText(clienteActual.getEmail());
        txtTelefono.setText(clienteActual.getTelefono());
        txtDireccion.setText(clienteActual.getDireccion());
        txtDui.setText(clienteActual.getDui());
        txtLicencia.setText(clienteActual.getLicencia());

        if (clienteActual.getFechaNacimiento() != null) {
            dpFechaNacimiento.setValue(clienteActual.getFechaNacimiento().toLocalDate());
        }
    }

    @FXML
    private void handleEditar() {
        activarModoEdicion();
    }

    private void activarModoEdicion() {
        modoEdicion = true;

        // Mostrar campos de edición
        vboxVisualizacion.setVisible(false);
        vboxVisualizacion.setManaged(false);
        vboxEdicion.setVisible(true);
        vboxEdicion.setManaged(true);

        // Cambiar botones
        btnEditar.setVisible(false);
        btnGuardar.setVisible(true);
        btnCancelar.setVisible(true);

        // Cambiar título
        lblTitulo.setText("Editar Perfil");
    }

    @FXML
    private void handleGuardar() {
        if (!validarCampos()) {
            return;
        }

        // Actualizar datos del cliente
        clienteActual.setNombre(txtNombre.getText().trim());
        clienteActual.setApellido(txtApellido.getText().trim());
        clienteActual.setEmail(txtEmail.getText().trim());
        clienteActual.setTelefono(txtTelefono.getText().trim());
        clienteActual.setDireccion(txtDireccion.getText().trim());
        clienteActual.setDui(txtDui.getText().trim());
        clienteActual.setLicencia(txtLicencia.getText().trim());

        if (dpFechaNacimiento.getValue() != null) {
            clienteActual.setFechaNacimiento(Date.valueOf(dpFechaNacimiento.getValue()));
        }

        // Actualizar en la base de datos
        boolean exito = clienteDAO.actualizar(clienteActual);

        // Si marcó cambiar contraseña, actualizarla
        if (chkCambiarContrasena.isSelected() && exito) {
            String nuevaContrasena = txtContrasenaNueva.getText();
            exito = clienteDAO.actualizarContrasena(clienteActual.getIdCliente(), nuevaContrasena);
        }

        if (exito) {
            mostrarExito("Perfil actualizado correctamente");
            desactivarModoEdicion();
            cargarDatos(); // Recargar datos actualizados
        } else {
            mostrarError("Error al actualizar el perfil");
        }
    }

    @FXML
    private void handleCancelar() {
        // Confirmar cancelación si hay cambios
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Cancelar Edición");
        confirmacion.setHeaderText("¿Descartar cambios?");
        confirmacion.setContentText("Los cambios no guardados se perderán");

        if (confirmacion.showAndWait().get() == ButtonType.OK) {
            desactivarModoEdicion();
            cargarDatos();
        }
    }

    private void desactivarModoEdicion() {
        modoEdicion = false;

        // Mostrar modo visualización
        vboxVisualizacion.setVisible(true);
        vboxVisualizacion.setManaged(true);
        vboxEdicion.setVisible(false);
        vboxEdicion.setManaged(false);

        // Cambiar botones
        btnEditar.setVisible(true);
        btnGuardar.setVisible(false);
        btnCancelar.setVisible(false);

        // Ocultar sección de contraseña
        chkCambiarContrasena.setSelected(false);

        // Cambiar título
        lblTitulo.setText("Perfil de " + clienteActual.getNombreCompleto());
    }

    private boolean validarCampos() {
        String nombre = txtNombre.getText().trim();
        String apellido = txtApellido.getText().trim();
        String email = txtEmail.getText().trim();
        String telefono = txtTelefono.getText().trim();
        String dui = txtDui.getText().trim();
        String licencia = txtLicencia.getText().trim();

        if (nombre.isEmpty() || apellido.isEmpty() || email.isEmpty() ||
                telefono.isEmpty() || dui.isEmpty() || licencia.isEmpty()) {
            mostrarError("Complete todos los campos obligatorios");
            return false;
        }

        if (!email.contains("@")) {
            mostrarError("Email inválido");
            return false;
        }

        if (dpFechaNacimiento.getValue() == null) {
            mostrarError("Seleccione su fecha de nacimiento");
            return false;
        }

        if (dpFechaNacimiento.getValue().isAfter(LocalDate.now().minusYears(18))) {
            mostrarError("Debe ser mayor de 18 años");
            return false;
        }

        // Validar formato de licencia salvadoreña (ejemplo: A123456789)
        if (!licencia.matches("^[A-Z]{1}\\d{9}$")) {
            mostrarError("Formato de licencia inválido. Debe ser: Letra + 9 dígitos (Ej: A123456789)");
            txtLicencia.requestFocus();
            return false;
        }

        // Validar formato completo del DUI salvadoreño
        if (!dui.matches("^\\d{8}-\\d{1}$")) {
            mostrarError("Formato de DUI inválido. Use: 12345678-9");
            txtDui.requestFocus();
            return false;
        }

        // **VALIDACIÓN 4: Formato de teléfono**
        if (!TELEFONO_PATTERN.matcher(telefono).matches()) {
            mostrarError("El teléfono debe tener el formato: 0000-0000 (ejemplo: 7890-1234)");
            txtTelefono.requestFocus();

            return false;
        }

        // Validar cambio de contraseña si está marcado
        if (chkCambiarContrasena.isSelected()) {
            String contrasenaActual = txtContrasenaActual.getText();
            String nuevaContrasena = txtContrasenaNueva.getText();
            String confirmar = txtConfirmarContrasena.getText();

            if (contrasenaActual.isEmpty() || nuevaContrasena.isEmpty() || confirmar.isEmpty()) {
                mostrarError("Complete todos los campos de contraseña");
                return false;
            }

            // Verificar contraseña actual usando BCrypt
            if (!clienteDAO.verificarContrasena(clienteActual.getIdCliente(), contrasenaActual)) {
                mostrarError("La contraseña actual es incorrecta");
                return false;
            }

            if (nuevaContrasena.length() < 6) {
                mostrarError("La nueva contraseña debe tener al menos 6 caracteres");
                return false;
            }

            if (!nuevaContrasena.equals(confirmar)) {
                mostrarError("Las contraseñas nuevas no coinciden");
                return false;
            }

            // Verificar que no sea igual a la actual
            if (clienteDAO.verificarContrasena(clienteActual.getIdCliente(), nuevaContrasena)) {
                mostrarError("La nueva contraseña debe ser diferente a la actual");
                return false;
            }
        }

        return true;
    }

    @FXML
    private void handleVolver() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/sistema_rentacar/Views/cliente/CatalogoCliente.fxml"));
            Parent root = loader.load();

            CatalogoClienteController controller = loader.getController();
            controller.setDatosCliente(clienteActual.getIdCliente(), clienteActual.getNombreCompleto());

            Stage stage = (Stage) btnVolver.getScene().getWindow();
            stage.setScene(new Scene(root, 1200, 800));
            stage.setTitle("Catálogo - Renta Car");

        } catch (Exception e) {
            System.err.println("Error al volver al catálogo: " + e.getMessage());
            e.printStackTrace();
        }
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