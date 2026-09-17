package reservas.presentation.funcionarios;

import reservas.logic.Funcionario;
import reservas.logic.Rol;
import reservas.logic.Service;
import reservas.presentation.reportes.PdfUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class Controller {

    private Model model;
    private View view;

    public Controller(Model model, View view) {

        this.model = model;
        this.view = view;

        view.getGuardar().addActionListener(e -> guardar());
        view.getModificar().addActionListener(e -> modificar());
        view.getEliminar().addActionListener(e -> eliminar());
        view.getLimpiar().addActionListener(e -> limpiar());
        view.getBuscarBoton().addActionListener(e -> buscar());
        view.getPdf().addActionListener(e -> generarPdf());

        view.getTabla()
                .getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {

                        int fila =
                                view.getTabla()
                                        .getSelectedRow();

                        if (fila != -1) {

                            view.getId().setText(
                                    view.getTabla()
                                            .getValueAt(fila, 0)
                                            .toString()
                            );

                            view.getNombre().setText(
                                    view.getTabla()
                                            .getValueAt(fila, 1)
                                            .toString()
                            );

                            view.getTelefono().setText(
                                    view.getTabla()
                                            .getValueAt(fila, 2)
                                            .toString()
                            );

                            view.getId().setEditable(false);
                        }
                    }
                });

        cargarTabla(
                Service.instance().getFuncionarios()
        );
    }

    private void guardar() {

        String id =
                view.getId()
                        .getText()
                        .trim();

        String nombre =
                view.getNombre()
                        .getText()
                        .trim();

        String telefono =
                view.getTelefono()
                        .getText()
                        .trim();

        // CAMPOS OBLIGATORIOS
        if (id.isEmpty()
                || nombre.isEmpty()
                || telefono.isEmpty()) {

            mostrarError(
                    "Debe completar todos los campos"
            );

            return;
        }

        // ID SIN ESPACIOS
        if (id.contains(" ")) {

            mostrarError(
                    "El ID no puede contener espacios"
            );

            return;
        }

        // TELÉFONO SOLO NUMÉRICO
        if (!telefono.matches("\\d+")) {

            mostrarError(
                    "El teléfono solo puede contener números"
            );

            return;
        }

        Funcionario funcionario =
                new Funcionario(
                        id,
                        id,
                        Rol.FUNCIONARIO,
                        nombre,
                        telefono
                );

        try {

            Service.instance()
                    .agregarFuncionario(funcionario);

            limpiar();

            JOptionPane.showMessageDialog(
                    view,
                    "Funcionario agregado correctamente.\n"
                            + "La clave inicial es igual al ID.",
                    "Funcionario agregado",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IllegalArgumentException e) {

            mostrarError(
                    e.getMessage()
            );

        } catch (RuntimeException e) {

            mostrarError(
                    "No fue posible agregar el funcionario.\n"
                            + e.getMessage()
            );
        }
    }

    private void modificar() {

        int fila =
                view.getTabla()
                        .getSelectedRow();

        if (fila == -1) {

            mostrarError(
                    "Seleccione un funcionario"
            );

            return;
        }

        String id =
                view.getId()
                        .getText()
                        .trim();

        String nombre =
                view.getNombre()
                        .getText()
                        .trim();

        String telefono =
                view.getTelefono()
                        .getText()
                        .trim();

        // CAMPOS OBLIGATORIOS
        if (nombre.isEmpty()
                || telefono.isEmpty()) {

            mostrarError(
                    "Debe completar nombre y teléfono"
            );

            return;
        }

        // TELÉFONO SOLO NUMÉRICO
        if (!telefono.matches("\\d+")) {

            mostrarError(
                    "El teléfono solo puede contener números"
            );

            return;
        }

        Funcionario funcionario =
                Service.instance()
                        .buscarFuncionarioPorId(id);

        if (funcionario == null) {

            mostrarError(
                    "No se encontró el funcionario"
            );

            return;
        }

        try {

            funcionario.setNombre(nombre);
            funcionario.setTelefono(telefono);

            Service.instance()
                    .actualizarFuncionario(funcionario);

            limpiar();

            JOptionPane.showMessageDialog(
                    view,
                    "Funcionario modificado correctamente",
                    "Funcionario modificado",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IllegalArgumentException e) {

            mostrarError(
                    e.getMessage()
            );

        } catch (RuntimeException e) {

            mostrarError(
                    "No fue posible modificar el funcionario.\n"
                            + e.getMessage()
            );
        }
    }

    private void eliminar() {

        int fila =
                view.getTabla()
                        .getSelectedRow();

        if (fila == -1) {

            mostrarError(
                    "Seleccione un funcionario"
            );

            return;
        }

        String id =
                view.getTabla()
                        .getValueAt(fila, 0)
                        .toString();

        Funcionario funcionario =
                Service.instance()
                        .buscarFuncionarioPorId(id);

        if (funcionario == null) {

            mostrarError(
                    "No se encontró el funcionario"
            );

            return;
        }

        int opcion =
                JOptionPane.showConfirmDialog(
                        view,
                        "¿Desea eliminar el funcionario?",
                        "Confirmar eliminación",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            Service.instance()
                    .eliminarFuncionario(funcionario);

            limpiar();

            JOptionPane.showMessageDialog(
                    view,
                    "Funcionario eliminado correctamente",
                    "Funcionario eliminado",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IllegalArgumentException e) {

            mostrarError(
                    e.getMessage()
            );

        } catch (RuntimeException e) {

            mostrarError(
                    "No fue posible eliminar el funcionario.\n"
                            + e.getMessage()
            );
        }
    }

    private void buscar() {

        String texto =
                view.getBuscar()
                        .getText()
                        .trim();

        try {

            cargarTabla(
                    Service.instance()
                            .buscarFuncionarios(texto)
            );

        } catch (RuntimeException e) {

            mostrarError(
                    "No fue posible realizar la búsqueda.\n"
                            + e.getMessage()
            );
        }
    }

    private void limpiar() {

        view.getId().setText("");
        view.getNombre().setText("");
        view.getTelefono().setText("");
        view.getBuscar().setText("");

        view.getId().setEditable(true);

        view.getTabla().clearSelection();

        cargarTabla(
                Service.instance()
                        .getFuncionarios()
        );
    }

    private void cargarTabla(
            List<Funcionario> funcionarios) {

        DefaultTableModel tablaModel =
                new DefaultTableModel(
                        new String[]{
                                "ID",
                                "Nombre",
                                "Teléfono"
                        },
                        0
                ) {

                    @Override
                    public boolean isCellEditable(
                            int row,
                            int column) {

                        return false;
                    }
                };

        for (Funcionario funcionario : funcionarios) {

            tablaModel.addRow(
                    new Object[]{
                            funcionario.getId(),
                            funcionario.getNombre(),
                            funcionario.getTelefono()
                    }
            );
        }

        view.getTabla()
                .setModel(tablaModel);
    }

    private void generarPdf() {

        List<Funcionario> funcionarios =
                Service.instance()
                        .getFuncionarios();

        String[] columnas = {
                "ID",
                "Nombre",
                "Teléfono",
                "Rol"
        };

        Object[][] datos =
                new Object[funcionarios.size()][4];

        for (int i = 0;
             i < funcionarios.size();
             i++) {

            Funcionario funcionario =
                    funcionarios.get(i);

            datos[i][0] =
                    funcionario.getId();

            datos[i][1] =
                    funcionario.getNombre();

            datos[i][2] =
                    funcionario.getTelefono();

            datos[i][3] =
                    funcionario.getRol();
        }

        try {

            PdfUtil.generarReporte(
                    "Reporte de Funcionarios",
                    columnas,
                    datos,
                    "funcionarios.pdf"
            );

            JOptionPane.showMessageDialog(
                    view,
                    "PDF generado correctamente.",
                    "Reporte generado",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (RuntimeException e) {

            mostrarError(
                    "No fue posible generar el PDF.\n"
                            + e.getMessage()
            );
        }
    }

    private void mostrarError(
            String mensaje
    ) {

        JOptionPane.showMessageDialog(
                view,
                mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}