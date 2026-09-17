package reservas.presentation.reservas;

import reservas.logic.Categoria;
import reservas.logic.Funcionario;
import reservas.logic.Recurso;
import reservas.logic.Reserva;
import reservas.logic.Service;
import reservas.presentation.Sesion;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import reservas.ai.LlmService;
import reservas.ai.ReservaInterpretada;

import reservas.presentation.reportes.PdfUtil;
import java.awt.event.HierarchyEvent;


public class Controller {

    private Model model;
    private View view;
    private Funcionario funcionario;
    private LlmService llmService;

    public Controller(
            Model model,
            View view) {

        this.model = model;
        this.view = view;
        llmService = new LlmService();

        if (Sesion.getUsuario() instanceof Funcionario) {

            funcionario =
                    (Funcionario) Sesion.getUsuario();
        }

        cargarCategorias();
        cargarReservas();

        view.getReservar()
                .addActionListener(
                        e -> reservar()
                );

        view.getCancelarReserva()
                .addActionListener(
                        e -> cancelarReserva()
                );

        view.getLimpiar()
                .addActionListener(
                        e -> limpiar()
                );

        view.getTabla()
                .getSelectionModel()
                .addListSelectionListener(
                        e -> {

                            if (!e.getValueIsAdjusting()) {

                                int fila =
                                        view.getTabla()
                                                .getSelectedRow();

                                if (fila != -1) {

                                    cargarReservaSeleccionada(
                                            fila
                                    );
                                }
                            }
                        }
                );
        view.getPdf().addActionListener(
                e -> generarPdf()
        );

        view.getInterpretarIa().addActionListener(
                e -> interpretarConIa()
        );
        view.addHierarchyListener(
                e -> {

                    if ((e.getChangeFlags()
                            & HierarchyEvent.SHOWING_CHANGED) != 0
                            && view.isShowing()) {

                        refrescarDatos();
                    }
                }
        );
    }

    private void cargarCategorias() {

        view.getCategoriasModel()
                .clear();

        List<Categoria> categorias =
                Service.instance()
                        .getCategorias();

        model.setCategorias(
                categorias
        );

        for (Categoria categoria :
                categorias) {

            view.getCategoriasModel()
                    .addElement(
                            categoria
                    );
        }
    }

    private void reservar() {

        if (funcionario == null) {

            JOptionPane.showMessageDialog(
                    view,
                    "La reserva solo puede ser realizada por un funcionario.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        String actividad =
                view.getActividad()
                        .getText()
                        .trim();

        LocalDate fecha =
                view.getFecha()
                        .getDate();

        String inicioTexto =
                (String) view.getHoraInicio()
                        .getSelectedItem();

        String finTexto =
                (String) view.getHoraFin()
                        .getSelectedItem();

        LocalTime horaInicio =
                inicioTexto == null
                        ? null
                        : LocalTime.parse(
                        inicioTexto
                );

        LocalTime horaFin =
                finTexto == null
                        ? null
                        : LocalTime.parse(
                        finTexto
                );

        List<Categoria> categoriasSeleccionadas =
                view.getCategorias()
                        .getSelectedValuesList();

        try {

            Reserva reserva =
                    Service.instance()
                            .crearReserva(
                                    actividad,
                                    fecha,
                                    horaInicio,
                                    horaFin,
                                    funcionario,
                                    categoriasSeleccionadas
                            );

            JOptionPane.showMessageDialog(
                    view,
                    "Reserva realizada correctamente.\n"
                            + "ID: "
                            + reserva.getId(),
                    "Reserva creada",
                    JOptionPane.INFORMATION_MESSAGE
            );

            limpiar();

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    view,
                    e.getMessage(),
                    "No se pudo realizar la reserva",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (RuntimeException e) {

            JOptionPane.showMessageDialog(
                    view,
                    "Ocurrió un error al realizar la reserva.\n"
                            + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void cancelarReserva() {

        if (funcionario == null) {

            JOptionPane.showMessageDialog(
                    view,
                    "No existe un funcionario autenticado.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        int fila =
                view.getTabla()
                        .getSelectedRow();

        if (fila == -1) {

            JOptionPane.showMessageDialog(
                    view,
                    "Seleccione una reserva",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        String id =
                view.getTabla()
                        .getValueAt(
                                fila,
                                0
                        )
                        .toString();

        Reserva reserva =
                Service.instance()
                        .buscarReservaPorId(id);

        if (reserva == null) {

            JOptionPane.showMessageDialog(
                    view,
                    "No se encontró la reserva",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        int opcion =
                JOptionPane.showConfirmDialog(
                        view,
                        "¿Desea cancelar la reserva "
                                + reserva.getId()
                                + "?",
                        "Confirmar cancelación",
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.QUESTION_MESSAGE
                );

        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }

        try {

            Service.instance()
                    .cancelarReserva(
                            reserva,
                            funcionario
                    );

            JOptionPane.showMessageDialog(
                    view,
                    "Reserva cancelada correctamente",
                    "Reserva cancelada",
                    JOptionPane.INFORMATION_MESSAGE
            );

            limpiar();

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    view,
                    e.getMessage(),
                    "No se pudo cancelar la reserva",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (RuntimeException e) {

            JOptionPane.showMessageDialog(
                    view,
                    "Ocurrió un error al cancelar la reserva.\n"
                            + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void cargarReservas() {

        if (funcionario == null) {

            cargarTabla(
                    List.of()
            );

            return;
        }

        List<Reserva> reservas =
                Service.instance()
                        .getReservasFuncionario(
                                funcionario
                        );

        model.setReservas(
                reservas
        );

        cargarTabla(
                reservas
        );
    }

    private void cargarTabla(List<Reserva> reservas) {

        DefaultTableModel tablaModel =
                new DefaultTableModel(
                        new Object[]{
                                "ID",
                                "Actividad",
                                "Fecha",
                                "Inicio",
                                "Fin",
                                "Recursos",
                                "Estado"
                        },
                        0
                ) {
                    @Override
                    public boolean isCellEditable(int row, int column) {
                        return false;
                    }
                };

        for (Reserva reserva : reservas) {

            String recursos = "";

            if (reserva.getRecursos() != null) {

                recursos =
                        reserva.getRecursos()
                                .stream()
                                .map(Recurso::getDescripcion)
                                .collect(Collectors.joining(", "));
            }

            tablaModel.addRow(
                    new Object[]{
                            reserva.getId(),
                            reserva.getActividad(),
                            reserva.getFecha(),
                            reserva.getHoraInicio(),
                            reserva.getHoraFin(),
                            recursos,
                            reserva.getEstado()
                    }
            );
        }

        view.getTabla().setModel(tablaModel);

        view.getTabla().revalidate();
        view.getTabla().repaint();

    }

    private void cargarReservaSeleccionada(
            int fila) {

        String id =
                view.getTabla()
                        .getValueAt(
                                fila,
                                0
                        )
                        .toString();

        Reserva reserva =
                Service.instance()
                        .buscarReservaPorId(
                                id
                        );

        if (reserva == null) {
            return;
        }

        model.setCurrent(
                reserva
        );

        view.getActividad()
                .setText(
                        reserva.getActividad()
                );

        view.getFecha()
                .setDate(
                        reserva.getFecha()
                );

        view.getHoraInicio()
                .setSelectedItem(
                        reserva.getHoraInicio()
                                .toString()
                );

        view.getHoraFin()
                .setSelectedItem(
                        reserva.getHoraFin()
                                .toString()
                );

        seleccionarCategoriasReserva(
                reserva
        );
    }

    private void seleccionarCategoriasReserva(
            Reserva reserva) {

        view.getCategorias()
                .clearSelection();

        List<Recurso> recursos =
                reserva.getRecursos();

        for (int i = 0;
             i < view.getCategoriasModel().size();
             i++) {

            Categoria categoria =
                    view.getCategoriasModel()
                            .getElementAt(i);

            boolean utilizada =
                    false;

            for (Recurso recurso :
                    recursos) {

                if (recurso.getCategoria() != null
                        && recurso.getCategoria()
                        .getId()
                        .equals(
                                categoria.getId()
                        )) {

                    utilizada =
                            true;

                    break;
                }
            }

            if (utilizada) {

                view.getCategorias()
                        .addSelectionInterval(
                                i,
                                i
                        );
            }
        }
    }

    private void limpiar() {

        model.setCurrent(
                new Reserva()
        );

        view.getActividad()
                .setText("");

        view.getFecha()
                .setDate(
                        LocalDate.now()
                );

        if (view.getHoraInicio()
                .getItemCount() > 0) {

            view.getHoraInicio()
                    .setSelectedIndex(0);
        }

        if (view.getHoraFin()
                .getItemCount() > 1) {

            view.getHoraFin()
                    .setSelectedIndex(1);
        }

        view.getCategorias()
                .clearSelection();

        view.getTabla()
                .clearSelection();

        view.getSolicitudIa()
                .setText("");

        cargarReservas();
    }

    private void generarPdf() {

        if (funcionario == null) {

            JOptionPane.showMessageDialog(
                    view,
                    "No existe un funcionario autenticado.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        List<Reserva> reservas =
                Service.instance()
                        .getReservasFuncionario(
                                funcionario
                        );

        String[] columnas = {
                "ID",
                "Actividad",
                "Funcionario",
                "Fecha",
                "Inicio",
                "Fin",
                "Recursos",
                "Estado"
        };

        Object[][] datos =
                new Object[reservas.size()][8];

        for (int i = 0;
             i < reservas.size();
             i++) {

            Reserva reserva =
                    reservas.get(i);

            String nombreFuncionario = "";

            if (reserva.getFuncionario() != null) {

                if (reserva.getFuncionario().getNombre() != null) {

                    nombreFuncionario =
                            reserva.getFuncionario()
                                    .getNombre();

                } else {

                    nombreFuncionario =
                            reserva.getFuncionario()
                                    .getId();
                }
            }

            String recursos =
                    "";

            if (reserva.getRecursos() != null) {

                recursos =
                        reserva.getRecursos()
                                .stream()
                                .map(Recurso::getDescripcion)
                                .collect(
                                        Collectors.joining(", ")
                                );
            }

            datos[i][0] =
                    reserva.getId();

            datos[i][1] =
                    reserva.getActividad();

            datos[i][2] =
                    nombreFuncionario;

            datos[i][3] =
                    reserva.getFecha();

            datos[i][4] =
                    reserva.getHoraInicio();

            datos[i][5] =
                    reserva.getHoraFin();

            datos[i][6] =
                    recursos;

            datos[i][7] =
                    reserva.getEstado();
        }

        try {

            PdfUtil.generarReporte(
                    "Reporte de Reservas",
                    columnas,
                    datos,
                    "reservas.pdf"
            );

            JOptionPane.showMessageDialog(
                    view,
                    "PDF generado correctamente."
            );

        } catch (RuntimeException e) {

            JOptionPane.showMessageDialog(
                    view,
                    e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void interpretarConIa() {

        String solicitud =
                view.getSolicitudIa()
                        .getText()
                        .trim();

        if (solicitud.isEmpty()) {

            JOptionPane.showMessageDialog(
                    view,
                    "Escriba una solicitud para interpretar.",
                    "Información",
                    JOptionPane.INFORMATION_MESSAGE
            );

            return;
        }

        try {

            List<String> categoriasDisponibles =
                    Service.instance()
                            .getCategorias()
                            .stream()
                            .map(
                                    Categoria::getDescripcion
                            )
                            .toList();

            ReservaInterpretada resultado =
                    llmService.interpretar(
                            solicitud,
                            categoriasDisponibles
                    );

            if (resultado == null) {

                throw new IllegalArgumentException(
                        "No se obtuvo una respuesta válida de la IA."
                );
            }

            if (resultado.getActividad() != null) {

                view.getActividad()
                        .setText(
                                resultado.getActividad()
                        );
            }

            if (resultado.getFecha() != null) {

                view.getFecha()
                        .setDate(
                                resultado.getFecha()
                        );
            }

            if (resultado.getHoraInicio() != null) {

                String horaInicio =
                        resultado.getHoraInicio()
                                .toString();

                view.getHoraInicio()
                        .setSelectedItem(
                                horaInicio
                        );
            }

            if (resultado.getHoraFin() != null) {

                String horaFin =
                        resultado.getHoraFin()
                                .toString();

                view.getHoraFin()
                        .setSelectedItem(
                                horaFin
                        );
            }

            if (resultado.getCategorias() != null) {

                seleccionarCategoriasIa(
                        resultado.getCategorias()
                );
            }

            JOptionPane.showMessageDialog(
                    view,
                    "Solicitud interpretada correctamente.\n"
                            + "Revise y modifique los datos si es necesario "
                            + "antes de reservar.",
                    "Interpretación completada",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception e) {

            String mensaje =
                    e.getMessage() != null
                            && !e.getMessage().isBlank()
                            ? e.getMessage()
                            : "No fue posible interpretar la solicitud.";

            JOptionPane.showMessageDialog(
                    view,
                    mensaje,
                    "Error al interpretar la solicitud",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void seleccionarCategoriasIa(
            List<String> categoriasIa) {

        view.getCategorias()
                .clearSelection();

        if (categoriasIa == null) {
            return;
        }

        for (int i = 0;
             i < view.getCategoriasModel().size();
             i++) {

            Categoria categoria =
                    view.getCategoriasModel()
                            .getElementAt(i);

            for (String nombre :
                    categoriasIa) {

                if (nombre != null
                        && categoria.getDescripcion()
                        .equalsIgnoreCase(
                                nombre.trim()
                        )) {

                    view.getCategorias()
                            .addSelectionInterval(
                                    i,
                                    i
                            );

                    break;
                }
            }
        }
    }
    private void refrescarDatos() {

        /*
         * Guardamos las categorías que estaban seleccionadas.
         */
        List<String> idsSeleccionados =
                view.getCategorias()
                        .getSelectedValuesList()
                        .stream()
                        .map(Categoria::getId)
                        .toList();

        /*
         * Volvemos a cargar las categorías actuales.
         */
        cargarCategorias();

        /*
         * Restauramos la selección si esas categorías
         * todavía existen.
         */
        view.getCategorias()
                .clearSelection();

        for (int i = 0;
             i < view.getCategoriasModel().size();
             i++) {

            Categoria categoria =
                    view.getCategoriasModel()
                            .getElementAt(i);

            if (idsSeleccionados.contains(
                    categoria.getId())) {

                view.getCategorias()
                        .addSelectionInterval(
                                i,
                                i
                        );
            }
        }

        /*
         * Volvemos a consultar las reservas actuales.
         */
        cargarReservas();
    }
}