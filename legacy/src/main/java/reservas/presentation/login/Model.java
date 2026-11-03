package reservas.presentation.login;

import reservas.logic.Usuario;

public class Model {

    private Usuario usuario;

    public Model() {
        usuario = new Usuario();
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}