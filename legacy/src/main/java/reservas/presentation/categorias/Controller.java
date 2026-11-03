package reservas.presentation.categorias;

import reservas.logic.Categoria;
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

                            view.getDescripcion().setText(
                                    view.getTabla()
                                            .getValueAt(fila, 1)
                                            .toString()
                            );
                        }
                    }
                });

        cargarTabla(
                Service.instance()
                        .getCategorias()
        );
    }

    private void guardar() {

        String descripcion =
                view.getDescripcion()
                        .getText()
                        .trim();

        if (descripcion.isEmpty()) {

            mostrarError(
                    "Debe ingresar una descripción"
            );

            return;
        }

        String id =
                Service.instance()
                        .generarIdCategoria();

        Categoria categoria =
                new Categoria(
                        id,
                        descripcion
                );

        try {

            Service.instance()
                    .agregarCategoria(categoria);

            limpiar();

            JOptionPane.showMessageDialog(
                    view,
                    "Categoría agregada correctamente",
                    "Categoría agregada",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IllegalArgumentException e) {

            mostrarError(
                    e.getMessage()
            );

        } catch (RuntimeException e) {

            mostrarError(
                    "No fue posible agregar la categoría.\n"
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
                    "Seleccione una categoría"
            );

            return;
        }

        String descripcion =
                view.getDescripcion()
                        .getText()
                        .trim();

        if (descripcion.isEmpty()) {

            mostrarError(
                    "Debe ingresar una descripción"
            );

            return;
        }

        String id =
                view.getTabla()
                        .getValueAt(fila, 0)
                        .toString();

        Categoria categoria =
                Service.instance()
                        .buscarCategoriaPorId(id);

        if (categoria == null) {

            mostrarError(
                    "No se encontró la categoría"
            );

            return;
        }

        String descripcionAnterior =
                categoria.getDescripcion();

        try {

            categoria.setDescripcion(
                    descripcion
            );

            Service.instance()
                    .actualizarCategoria(categoria);

            limpiar();

            JOptionPane.showMessageDialog(
                    view,
                    "Categoría modificada correctamente",
                    "Categoría modificada",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IllegalArgumentException e) {

            categoria.setDescripcion(
                    descripcionAnterior
            );

            mostrarError(
                    e.getMessage()
            );

        } catch (RuntimeException e) {

            categoria.setDescripcion(
                    descripcionAnterior
            );

            mostrarError(
                    "No fue posible modificar la categoría.\n"
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
                    "Seleccione una categoría"
            );

            return;
        }

        String id =
                view.getTabla()
                        .getValueAt(fila, 0)
                        .toString();

        Categoria categoria =
                Service.instance()
                        .buscarCategoriaPorId(id);

        if (categoria == null) {

            mostrarError(
                    "No se encontró la categoría"
            );

            return;
        }

        int opcion =
                JOptionPane.showConfirmDialog(
                        view,
                        "¿Desea eliminar la categoría?",
                        "Confirmar eliminación",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            Service.instance()
                    .eliminarCategoria(categoria);

            limpiar();

            JOptionPane.showMessageDialog(
                    view,
                    "Categoría eliminada correctamente",
                    "Categoría eliminada",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IllegalArgumentException e) {

            mostrarError(
                    e.getMessage()
            );

        } catch (RuntimeException e) {

            mostrarError(
                    "No fue posible eliminar la categoría.\n"
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

            List<Categoria> resultado =
                    Service.instance()
                            .buscarCategorias(texto);

            cargarTabla(
                    resultado
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
        view.getDescripcion().setText("");
        view.getBuscar().setText("");

        view.getTabla()
                .clearSelection();

        cargarTabla(
                Service.instance()
                        .getCategorias()
        );
    }

    private void cargarTabla(
            List<Categoria> categorias) {

        DefaultTableModel tablaModel =
                new DefaultTableModel(
                        new String[]{
                                "ID",
                                "Descripción"
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

        for (Categoria categoria : categorias) {

            tablaModel.addRow(
                    new Object[]{
                            categoria.getId(),
                            categoria.getDescripcion()
                    }
            );
        }

        view.getTabla()
                .setModel(tablaModel);
    }

    private void generarPdf() {

        List<Categoria> categorias =
                Service.instance()
                        .getCategorias();

        String[] columnas = {
                "ID",
                "Descripción"
        };

        Object[][] datos =
                new Object[categorias.size()][2];

        for (int i = 0;
             i < categorias.size();
             i++) {

            Categoria categoria =
                    categorias.get(i);

            datos[i][0] =
                    categoria.getId();

            datos[i][1] =
                    categoria.getDescripcion();
        }

        try {

            PdfUtil.generarReporte(
                    "Reporte de Categorías",
                    columnas,
                    datos,
                    "categorias.pdf"
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