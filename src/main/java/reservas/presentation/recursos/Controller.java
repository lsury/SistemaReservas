package reservas.presentation.recursos;

import reservas.logic.Categoria;
import reservas.logic.Recurso;
import reservas.logic.Service;
import reservas.presentation.reportes.PdfUtil;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableModel;
import java.util.List;

public class Controller {

    private Model model;
    private View view;

    public Controller(Model model, View view) {

        this.model = model;
        this.view = view;

        cargarCategorias();

        cargarTabla(
                Service.instance().getRecursos()
        );

        view.getGuardar()
                .addActionListener(e -> guardar());

        view.getModificar()
                .addActionListener(e -> modificar());

        view.getEliminar()
                .addActionListener(e -> eliminar());

        view.getLimpiar()
                .addActionListener(e -> limpiar());

        view.getFiltrar()
                .addActionListener(e -> filtrar());

        view.getPdf()
                .addActionListener(e -> generarPdf());

        view.getTabla()
                .getSelectionModel()
                .addListSelectionListener(e -> {

                    if (!e.getValueIsAdjusting()) {

                        int fila =
                                view.getTabla()
                                        .getSelectedRow();

                        if (fila != -1) {
                            cargarSeleccion(fila);
                        }
                    }
                });

        // Actualiza las categorías cada vez que se abre
        // el combo principal.
        view.getCategoria()
                .addPopupMenuListener(
                        new PopupMenuListener() {

                            @Override
                            public void popupMenuWillBecomeVisible(
                                    PopupMenuEvent e) {

                                cargarCategorias();
                            }

                            @Override
                            public void popupMenuWillBecomeInvisible(
                                    PopupMenuEvent e) {
                            }

                            @Override
                            public void popupMenuCanceled(
                                    PopupMenuEvent e) {
                            }
                        }
                );

        // Actualiza también las categorías del filtro.
        view.getFiltroCategoria()
                .addPopupMenuListener(
                        new PopupMenuListener() {

                            @Override
                            public void popupMenuWillBecomeVisible(
                                    PopupMenuEvent e) {

                                cargarCategorias();
                            }

                            @Override
                            public void popupMenuWillBecomeInvisible(
                                    PopupMenuEvent e) {
                            }

                            @Override
                            public void popupMenuCanceled(
                                    PopupMenuEvent e) {
                            }
                        }
                );
    }

    private void cargarCategorias() {

        Categoria categoriaSeleccionada =
                (Categoria) view.getCategoria()
                        .getSelectedItem();

        Categoria filtroSeleccionado =
                (Categoria) view.getFiltroCategoria()
                        .getSelectedItem();

        String idCategoria =
                categoriaSeleccionada != null
                        ? categoriaSeleccionada.getId()
                        : null;

        String idFiltro =
                filtroSeleccionado != null
                        ? filtroSeleccionado.getId()
                        : null;

        view.getCategoria()
                .removeAllItems();

        view.getFiltroCategoria()
                .removeAllItems();

        for (Categoria categoria :
                Service.instance()
                        .getCategorias()) {

            view.getCategoria()
                    .addItem(categoria);

            view.getFiltroCategoria()
                    .addItem(categoria);
        }

        if (idCategoria != null) {

            for (int i = 0;
                 i < view.getCategoria()
                         .getItemCount();
                 i++) {

                Categoria categoria =
                        view.getCategoria()
                                .getItemAt(i);

                if (categoria.getId()
                        .equals(idCategoria)) {

                    view.getCategoria()
                            .setSelectedIndex(i);

                    break;
                }
            }
        }

        if (idFiltro != null) {

            for (int i = 0;
                 i < view.getFiltroCategoria()
                         .getItemCount();
                 i++) {

                Categoria categoria =
                        view.getFiltroCategoria()
                                .getItemAt(i);

                if (categoria.getId()
                        .equals(idFiltro)) {

                    view.getFiltroCategoria()
                            .setSelectedIndex(i);

                    break;
                }
            }
        }
    }

    private void guardar() {

        String id =
                view.getId()
                        .getText()
                        .trim();

        String descripcion =
                view.getDescripcion()
                        .getText()
                        .trim();

        Categoria categoria =
                (Categoria) view.getCategoria()
                        .getSelectedItem();

        if (id.isEmpty()) {

            mostrarError(
                    "Debe ingresar el ID / activo del recurso"
            );

            return;
        }

        if (id.contains(" ")) {

            mostrarError(
                    "El ID / activo no puede contener espacios"
            );

            return;
        }

        if (descripcion.isEmpty()) {

            mostrarError(
                    "Debe ingresar una descripción"
            );

            return;
        }

        if (categoria == null) {

            mostrarError(
                    "Debe seleccionar una categoría"
            );

            return;
        }

        Recurso recurso =
                new Recurso(
                        id,
                        categoria,
                        descripcion
                );

        try {

            Service.instance()
                    .agregarRecurso(recurso);

            limpiar();

            JOptionPane.showMessageDialog(
                    view,
                    "Recurso agregado correctamente",
                    "Recurso agregado",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IllegalArgumentException e) {

            mostrarError(
                    e.getMessage()
            );

        } catch (RuntimeException e) {

            mostrarError(
                    "No fue posible agregar el recurso.\n"
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
                    "Seleccione un recurso"
            );

            return;
        }

        String id =
                view.getId()
                        .getText()
                        .trim();

        String descripcion =
                view.getDescripcion()
                        .getText()
                        .trim();

        Categoria categoria =
                (Categoria) view.getCategoria()
                        .getSelectedItem();

        if (descripcion.isEmpty()) {

            mostrarError(
                    "Debe ingresar una descripción"
            );

            return;
        }

        if (categoria == null) {

            mostrarError(
                    "Debe seleccionar una categoría"
            );

            return;
        }

        Recurso recurso =
                Service.instance()
                        .buscarRecursoPorId(id);

        if (recurso == null) {

            mostrarError(
                    "No se encontró el recurso"
            );

            return;
        }

        Categoria categoriaAnterior =
                recurso.getCategoria();

        String descripcionAnterior =
                recurso.getDescripcion();

        try {

            recurso.setDescripcion(
                    descripcion
            );

            recurso.setCategoria(
                    categoria
            );

            Service.instance()
                    .actualizarRecurso(recurso);

            limpiar();

            JOptionPane.showMessageDialog(
                    view,
                    "Recurso modificado correctamente",
                    "Recurso modificado",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IllegalArgumentException e) {

            recurso.setCategoria(
                    categoriaAnterior
            );

            recurso.setDescripcion(
                    descripcionAnterior
            );

            mostrarError(
                    e.getMessage()
            );

        } catch (RuntimeException e) {

            recurso.setCategoria(
                    categoriaAnterior
            );

            recurso.setDescripcion(
                    descripcionAnterior
            );

            mostrarError(
                    "No fue posible modificar el recurso.\n"
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
                    "Seleccione un recurso"
            );

            return;
        }

        String id =
                view.getTabla()
                        .getValueAt(fila, 0)
                        .toString();

        Recurso recurso =
                Service.instance()
                        .buscarRecursoPorId(id);

        if (recurso == null) {

            mostrarError(
                    "No se encontró el recurso"
            );

            return;
        }

        int opcion =
                JOptionPane.showConfirmDialog(
                        view,
                        "¿Desea eliminar el recurso?",
                        "Confirmar eliminación",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            Service.instance()
                    .eliminarRecurso(recurso);

            limpiar();

            JOptionPane.showMessageDialog(
                    view,
                    "Recurso eliminado correctamente",
                    "Recurso eliminado",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (IllegalArgumentException e) {

            mostrarError(
                    e.getMessage()
            );

        } catch (RuntimeException e) {

            mostrarError(
                    "No fue posible eliminar el recurso.\n"
                            + e.getMessage()
            );
        }
    }

    private void filtrar() {

        Categoria categoria =
                (Categoria) view.getFiltroCategoria()
                        .getSelectedItem();

        try {

            if (categoria == null) {

                cargarTabla(
                        Service.instance()
                                .getRecursos()
                );

                return;
            }

            cargarTabla(
                    Service.instance()
                            .buscarRecursosPorCategoria(
                                    categoria
                            )
            );

        } catch (RuntimeException e) {

            mostrarError(
                    "No fue posible filtrar los recursos.\n"
                            + e.getMessage()
            );
        }
    }

    private void limpiar() {

        view.getId()
                .setText("");

        view.getDescripcion()
                .setText("");

        view.getId()
                .setEditable(true);

        cargarCategorias();

        if (view.getCategoria()
                .getItemCount() > 0) {

            view.getCategoria()
                    .setSelectedIndex(0);
        }

        view.getTabla()
                .clearSelection();

        cargarTabla(
                Service.instance()
                        .getRecursos()
        );
    }

    private void cargarSeleccion(
            int fila) {

        String id =
                view.getTabla()
                        .getValueAt(
                                fila,
                                0
                        )
                        .toString();

        Recurso recurso =
                Service.instance()
                        .buscarRecursoPorId(id);

        if (recurso == null) {
            return;
        }

        // Antes de seleccionar la categoría,
        // refrescamos el combo.
        cargarCategorias();

        view.getId()
                .setText(
                        recurso.getId()
                );

        view.getDescripcion()
                .setText(
                        recurso.getDescripcion()
                );

        view.getCategoria()
                .setSelectedItem(
                        recurso.getCategoria()
                );

        view.getId()
                .setEditable(false);
    }

    private void cargarTabla(
            List<Recurso> recursos) {

        DefaultTableModel tablaModel =
                new DefaultTableModel(
                        new String[]{
                                "ID / Activo",
                                "Categoría",
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

        for (Recurso recurso :
                recursos) {

            tablaModel.addRow(
                    new Object[]{
                            recurso.getId(),

                            recurso.getCategoria() != null
                                    ? recurso.getCategoria()
                                    .getDescripcion()
                                    : "",

                            recurso.getDescripcion()
                    }
            );
        }

        view.getTabla()
                .setModel(
                        tablaModel
                );
    }

    private void generarPdf() {

        List<Recurso> recursos =
                Service.instance()
                        .getRecursos();

        String[] columnas = {
                "ID / Activo",
                "Categoría",
                "Descripción"
        };

        Object[][] datos =
                new Object[
                        recursos.size()
                        ][3];

        for (int i = 0;
             i < recursos.size();
             i++) {

            Recurso recurso =
                    recursos.get(i);

            datos[i][0] =
                    recurso.getId();

            datos[i][1] =
                    recurso.getCategoria() != null
                            ? recurso.getCategoria()
                            .getDescripcion()
                            : "";

            datos[i][2] =
                    recurso.getDescripcion();
        }

        try {

            PdfUtil.generarReporte(
                    "Reporte de Recursos",
                    columnas,
                    datos,
                    "recursos.pdf"
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
            String mensaje) {

        JOptionPane.showMessageDialog(
                view,
                mensaje,
                "Error",
                JOptionPane.ERROR_MESSAGE
        );
    }
}