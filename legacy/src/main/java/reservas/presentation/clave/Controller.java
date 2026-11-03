package reservas.presentation.clave;

import reservas.logic.Service;
import reservas.logic.Usuario;

import javax.swing.*;
import java.awt.*;

public class Controller {

    private final Model model;
    private final View view;

    public Controller(
            Model model,
            View view
    ) {

        this.model = model;
        this.view = view;

        view.getCambiar()
                .addActionListener(
                        e -> cambiarClave()
                );

        view.getLimpiar()
                .addActionListener(
                        e -> limpiar()
                );
    }

    private void cambiarClave() {

        Usuario usuario =
                model.getUsuario();

        if (usuario == null) {

            JOptionPane.showMessageDialog(
                    view,
                    "No se encontró el usuario.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );

            return;
        }

        String claveActual =
                new String(
                        view.getClaveActual()
                                .getPassword()
                );

        String claveNueva =
                new String(
                        view.getClaveNueva()
                                .getPassword()
                );

        String confirmarClave =
                new String(
                        view.getConfirmarClave()
                                .getPassword()
                );

        try {

            Service.instance()
                    .cambiarClave(
                            usuario,
                            claveActual,
                            claveNueva,
                            confirmarClave
                    );

            JOptionPane.showMessageDialog(
                    view,
                    "Clave cambiada correctamente.",
                    "Cambio de clave",
                    JOptionPane.INFORMATION_MESSAGE
            );

            limpiar();

            Window ventana =
                    SwingUtilities.getWindowAncestor(
                            view
                    );

            if (ventana != null) {
                ventana.dispose();
            }

        } catch (IllegalArgumentException e) {

            JOptionPane.showMessageDialog(
                    view,
                    e.getMessage(),
                    "No se pudo cambiar la clave",
                    JOptionPane.ERROR_MESSAGE
            );

        } catch (RuntimeException e) {

            JOptionPane.showMessageDialog(
                    view,
                    "Ocurrió un error al cambiar la clave.\n"
                            + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void limpiar() {

        view.getClaveActual()
                .setText("");

        view.getClaveNueva()
                .setText("");

        view.getConfirmarClave()
                .setText("");
    }
}