package reservas.presentation;

import reservas.presentation.login.Controller;
import reservas.presentation.login.Model;
import reservas.presentation.login.View;

public class Application {

    public static void main(String[] args) {
        doLogin();

        if (Sesion.isLoggedIn()) {
            doRun();
        }
    }

    private static void doLogin() {
        Model model = new Model();
        View view = new View();
        Controller controller = new Controller(model, view);

        view.setVisible(true);
    }

    private static void doRun() {
        MainView mainView = new MainView();
        mainView.setVisible(true);
    }
}