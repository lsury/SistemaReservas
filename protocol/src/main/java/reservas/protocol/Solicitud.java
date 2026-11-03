
package reservas.protocol;

public class Solicitud {

    private String operacion;
    private String datos;

    public Solicitud() {
    }

    public Solicitud(String operacion, String datos) {
        this.operacion = operacion;
        this.datos = datos;
    }

    public String getOperacion() {
        return operacion;
    }

    public void setOperacion(String operacion) {
        this.operacion = operacion;
    }

    public String getDatos() {
        return datos;
    }

    public void setDatos(String datos) {
        this.datos = datos;
    }
}
