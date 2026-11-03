package reservas.presentation.login;

import reservas.logic.Service;
import reservas.logic.Usuario;
import reservas.presentation.Sesion;

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

        view.getIngresar()
                .addActionListener(
                        e -> login()
                );

        view.getCambiarClave()
                .addActionListener(
                        e -> cambiarClave()
                );

        view.getCancelar()
                .addActionListener(
                        e -> view.dispose()
                );
    }

    private void login() {

        String id =
                view.getId()
                        .getText()
                        .trim();

        String clave =
                new String(
                        view.getClave()
                                .getPassword()
                );

        if (id.isEmpty()) {

            JOptionPane.showMessageDialog(
                    view,
                    "Debe ingresar el usuario / ID.",
                    "Inicio de sesión",
                    JOptionPane.WARNING_MESSAGE
            );

            view.getId()
                    .requestFocus();

            return;
        }

        if (clave.isEmpty()) {

            JOptionPane.showMessageDialog(
                    view,
                    "Debe ingresar la contraseña.",
                    "Inicio de sesión",
                    JOptionPane.WARNING_MESSAGE
            );

            view.getClave()
                    .requestFocus();

            return;
        }

        try {

            Usuario usuario =
                    Service.instance()
                            .login(
                                    id,
                                    clave
                            );

            if (usuario == null) {

                JOptionPane.showMessageDialog(
                        view,
                        "Usuario o contraseña incorrectos.",
                        "Inicio de sesión",
                        JOptionPane.ERROR_MESSAGE
                );

                view.getClave()
                        .setText("");

                view.getClave()
                        .requestFocus();

                return;
            }

            model.setUsuario(
                    usuario
            );

            Sesion.setUsuario(
                    usuario
            );

            view.dispose();

        } catch (RuntimeException e) {

            JOptionPane.showMessageDialog(
                    view,
                    "No fue posible iniciar sesión.\n"
                            + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void cambiarClave() {

        String id =
                view.getId()
                        .getText()
                        .trim();

        if (id.isEmpty()) {

            JOptionPane.showMessageDialog(
                    view,
                    "Ingrese primero su usuario / ID.",
                    "Cambiar clave",
                    JOptionPane.INFORMATION_MESSAGE
            );

            view.getId()
                    .requestFocus();

            return;
        }

        try {

            Usuario usuario =
                    Service.instance()
                            .buscarUsuarioPorId(
                                    id
                            );

            if (usuario == null) {

                JOptionPane.showMessageDialog(
                        view,
                        "No existe un usuario con el ID indicado.",
                        "Cambiar clave",
                        JOptionPane.ERROR_MESSAGE
                );

                view.getId()
                        .requestFocus();

                return;
            }

            reservas.presentation.clave.Model modelClave =
                    new reservas.presentation.clave.Model();

            modelClave.setUsuario(
                    usuario
            );

            reservas.presentation.clave.View viewClave =
                    new reservas.presentation.clave.View();

            new reservas.presentation.clave.Controller(
                    modelClave,
                    viewClave
            );

            JDialog dialogo =
                    new JDialog(
                            view,
                            "Cambiar clave",
                            Dialog.ModalityType.APPLICATION_MODAL
                    );

            dialogo.setContentPane(
                    viewClave
            );

            dialogo.setDefaultCloseOperation(
                    JDialog.DISPOSE_ON_CLOSE
            );

            dialogo.pack();

            dialogo.setMinimumSize(
                    new Dimension(
                            450,
                            280
                    )
            );

            dialogo.setResizable(
                    false
            );

            dialogo.setLocationRelativeTo(
                    view
            );

            dialogo.setVisible(
                    true
            );

            view.getClave()
                    .setText("");

        } catch (RuntimeException e) {

            JOptionPane.showMessageDialog(
                    view,
                    "No fue posible abrir el cambio de clave.\n"
                            + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
}