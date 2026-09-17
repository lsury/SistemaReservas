package reservas.logic;

import reservas.data.Data;
import reservas.data.XmlPersister;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Service {

    private static Service instance;

    private Data data;
    private XmlPersister persister;

    private Service() {
        this("data/datos.xml");
    }

    Service(String path) {

        persister = new XmlPersister(path);

        try {

            data = persister.load();

        } catch (Exception e) {

            System.out.println(
                    "No se pudieron cargar los datos: "
                            + e.getMessage()
            );

            data = new Data();
        }

        crearDatosIniciales();
    }

    public static Service instance() {

        if (instance == null) {
            instance = new Service();
        }

        return instance;
    }

    private void crearDatosIniciales() {

        if (data.getUsuarios().isEmpty()) {

            Usuario admin =
                    new Usuario(
                            "admin",
                            "admin",
                            Rol.ADMINISTRADOR
                    );

            data.getUsuarios().add(admin);

            guardar();
        }
    }

    public void guardar() {

        try {

            persister.store(data);

        } catch (Exception e) {

            System.out.println(
                    "Error guardando los datos: "
                            + e.getMessage()
            );
        }
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    // =========================================================
    // USUARIOS Y LOGIN
    // =========================================================

    public Usuario login(
            String id,
            String clave) {

        for (Usuario usuario : data.getUsuarios()) {

            if (usuario.getId().equals(id)
                    && usuario.getClave().equals(clave)) {

                return usuario;
            }
        }

        return null;
    }

    public Usuario buscarUsuarioPorId(
            String id) {

        for (Usuario usuario : data.getUsuarios()) {

            if (usuario.getId()
                    .equalsIgnoreCase(id)) {

                return usuario;
            }
        }

        return null;
    }

    // =========================================================
    // CATEGORÍAS
    // =========================================================

    public List<Categoria> getCategorias() {
        return data.getCategorias();
    }

    public void agregarCategoria(
            Categoria categoria) {

        if (categoria == null) {

            throw new IllegalArgumentException(
                    "La categoría no puede ser nula"
            );
        }

        if (categoria.getDescripcion() == null
                || categoria.getDescripcion().isBlank()) {

            throw new IllegalArgumentException(
                    "Debe ingresar una descripción"
            );
        }

        for (Categoria existente :
                data.getCategorias()) {

            if (existente.getDescripcion()
                    .equalsIgnoreCase(
                            categoria.getDescripcion().trim()
                    )) {

                throw new IllegalArgumentException(
                        "Ya existe una categoría con esa descripción"
                );
            }
        }

        data.getCategorias()
                .add(categoria);

        guardar();
    }

    public void eliminarCategoria(
            Categoria categoria) {

        if (categoria == null) {

            throw new IllegalArgumentException(
                    "La categoría no existe"
            );
        }

        for (Recurso recurso :
                data.getRecursos()) {

            if (recurso.getCategoria() != null
                    && recurso.getCategoria()
                    .getId()
                    .equals(categoria.getId())) {

                throw new IllegalArgumentException(
                        "No se puede eliminar la categoría "
                                + "porque tiene recursos asociados"
                );
            }
        }

        boolean eliminada =
                data.getCategorias()
                        .remove(categoria);

        if (!eliminada) {

            throw new IllegalArgumentException(
                    "No se pudo eliminar la categoría"
            );
        }

        guardar();
    }

    public Categoria buscarCategoriaPorId(
            String id) {

        for (Categoria categoria :
                data.getCategorias()) {

            if (categoria.getId()
                    .equals(id)) {

                return categoria;
            }
        }

        return null;
    }

    public List<Categoria> buscarCategorias(
            String texto) {

        List<Categoria> resultado =
                new ArrayList<>();

        String criterio =
                texto.toLowerCase();

        for (Categoria categoria :
                data.getCategorias()) {

            if (categoria.getDescripcion()
                    .toLowerCase()
                    .contains(criterio)) {

                resultado.add(categoria);
            }
        }

        return resultado;
    }

    public void actualizarCategoria(
            Categoria categoria) {

        if (categoria == null) {

            throw new IllegalArgumentException(
                    "La categoría no existe"
            );
        }

        if (categoria.getDescripcion() == null
                || categoria.getDescripcion().isBlank()) {

            throw new IllegalArgumentException(
                    "Debe ingresar una descripción"
            );
        }

        for (Categoria existente :
                data.getCategorias()) {

            if (!existente.getId()
                    .equals(categoria.getId())
                    &&
                    existente.getDescripcion()
                            .equalsIgnoreCase(
                                    categoria
                                            .getDescripcion()
                                            .trim()
                            )) {

                throw new IllegalArgumentException(
                        "Ya existe otra categoría con esa descripción"
                );
            }
        }

        guardar();
    }

    public String generarIdCategoria() {

        int mayor = 0;

        for (Categoria categoria :
                data.getCategorias()) {

            try {

                int numero =
                        Integer.parseInt(
                                categoria.getId()
                        );

                if (numero > mayor) {
                    mayor = numero;
                }

            } catch (NumberFormatException ignored) {
            }
        }

        return String.valueOf(
                mayor + 1
        );
    }

    // =========================================================
    // FUNCIONARIOS
    // =========================================================

    public List<Funcionario> getFuncionarios() {

        List<Funcionario> resultado =
                new ArrayList<>();

        for (Usuario usuario :
                data.getUsuarios()) {

            if (usuario instanceof Funcionario funcionario) {
                resultado.add(funcionario);
            }
        }

        return resultado;
    }

    public void agregarFuncionario(
            Funcionario funcionario) {

        if (funcionario == null) {

            throw new IllegalArgumentException(
                    "El funcionario no puede ser nulo"
            );
        }

        if (funcionario.getId() == null
                || funcionario.getId().isBlank()) {

            throw new IllegalArgumentException(
                    "Debe indicar el ID del funcionario"
            );
        }

        if (buscarUsuarioPorId(
                funcionario.getId()) != null) {

            throw new IllegalArgumentException(
                    "Ya existe un usuario con ese ID"
            );
        }

        funcionario.setClave(
                funcionario.getId()
        );

        funcionario.setRol(
                Rol.FUNCIONARIO
        );

        data.getUsuarios()
                .add(funcionario);

        guardar();
    }

    public Funcionario buscarFuncionarioPorId(
            String id) {

        Usuario usuario =
                buscarUsuarioPorId(id);

        if (usuario instanceof Funcionario funcionario) {
            return funcionario;
        }

        return null;
    }

    public List<Funcionario> buscarFuncionarios(
            String texto) {

        List<Funcionario> resultado =
                new ArrayList<>();

        String criterio =
                texto.toLowerCase();

        for (Funcionario funcionario :
                getFuncionarios()) {

            boolean coincideId =
                    funcionario.getId()
                            .toLowerCase()
                            .contains(criterio);

            boolean coincideNombre =
                    funcionario.getNombre()
                            .toLowerCase()
                            .contains(criterio);

            if (coincideId
                    || coincideNombre) {

                resultado.add(funcionario);
            }
        }

        return resultado;
    }

    public void actualizarFuncionario(
            Funcionario funcionario) {

        if (funcionario == null) {

            throw new IllegalArgumentException(
                    "El funcionario no existe"
            );
        }

        guardar();
    }

    public void eliminarFuncionario(
            Funcionario funcionario) {

        if (funcionario == null) {

            throw new IllegalArgumentException(
                    "El funcionario no existe"
            );
        }

        for (Reserva reserva :
                data.getReservas()) {

            if (reserva.getFuncionario() != null
                    && reserva.getFuncionario()
                    .getId()
                    .equalsIgnoreCase(
                            funcionario.getId()
                    )) {

                throw new IllegalArgumentException(
                        "No se puede eliminar el funcionario "
                                + "porque tiene reservas asociadas"
                );
            }
        }

        boolean eliminado =
                data.getUsuarios()
                        .remove(funcionario);

        if (!eliminado) {

            throw new IllegalArgumentException(
                    "No se pudo eliminar el funcionario"
            );
        }

        guardar();
    }

    // =========================================================
    // RECURSOS
    // =========================================================

    public List<Recurso> getRecursos() {
        return data.getRecursos();
    }

    public void agregarRecurso(
            Recurso recurso) {

        if (recurso == null) {

            throw new IllegalArgumentException(
                    "El recurso no puede ser nulo"
            );
        }

        if (recurso.getId() == null
                || recurso.getId().isBlank()) {

            throw new IllegalArgumentException(
                    "Debe indicar el ID / activo del recurso"
            );
        }

        if (recurso.getDescripcion() == null
                || recurso.getDescripcion().isBlank()) {

            throw new IllegalArgumentException(
                    "Debe indicar una descripción"
            );
        }

        if (recurso.getCategoria() == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar una categoría"
            );
        }

        if (buscarRecursoPorId(
                recurso.getId()) != null) {

            throw new IllegalArgumentException(
                    "Ya existe un recurso con ese ID"
            );
        }

        data.getRecursos()
                .add(recurso);

        guardar();
    }
    public Recurso buscarRecursoPorId(
            String id) {

        for (Recurso recurso :
                data.getRecursos()) {

            if (recurso.getId()
                    .equalsIgnoreCase(id)) {

                return recurso;
            }
        }

        return null;
    }

    public List<Recurso> buscarRecursosPorCategoria(
            Categoria categoria) {

        List<Recurso> resultado =
                new ArrayList<>();

        for (Recurso recurso :
                data.getRecursos()) {

            if (recurso.getCategoria() != null
                    && recurso.getCategoria()
                    .getId()
                    .equals(categoria.getId())) {

                resultado.add(recurso);
            }
        }

        return resultado;
    }

    public void actualizarRecurso(
            Recurso recurso) {

        if (recurso == null) {

            throw new IllegalArgumentException(
                    "El recurso no existe"
            );
        }

        if (recurso.getDescripcion() == null
                || recurso.getDescripcion().isBlank()) {

            throw new IllegalArgumentException(
                    "Debe indicar una descripción"
            );
        }

        if (recurso.getCategoria() == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar una categoría"
            );
        }

        guardar();
    }

    public void eliminarRecurso(
            Recurso recurso) {

        if (recurso == null) {

            throw new IllegalArgumentException(
                    "El recurso no existe"
            );
        }

        for (Reserva reserva :
                data.getReservas()) {

            if (reserva.getRecursos() == null) {
                continue;
            }

            for (Recurso reservado :
                    reserva.getRecursos()) {

                if (reservado != null
                        && reservado.getId()
                        .equalsIgnoreCase(
                                recurso.getId()
                        )) {

                    throw new IllegalArgumentException(
                            "No se puede eliminar el recurso "
                                    + "porque tiene reservas asociadas"
                    );
                }
            }
        }

        boolean eliminado =
                data.getRecursos()
                        .remove(recurso);

        if (!eliminado) {

            throw new IllegalArgumentException(
                    "No se pudo eliminar el recurso"
            );
        }

        guardar();
    }

    // =========================================================
    // RESERVAS
    // =========================================================

    public List<Reserva> getReservas() {
        return data.getReservas();
    }

    public List<Reserva> getReservasFuncionario(
            Funcionario funcionario) {

        List<Reserva> resultado =
                new ArrayList<>();

        for (Reserva reserva :
                data.getReservas()) {

            if (reserva.getFuncionario() != null
                    && reserva.getFuncionario()
                    .getId()
                    .equals(funcionario.getId())) {

                resultado.add(reserva);
            }
        }

        return resultado;
    }

    public Reserva buscarReservaPorId(
            String id) {

        for (Reserva reserva :
                data.getReservas()) {

            if (reserva.getId()
                    .equals(id)) {

                return reserva;
            }
        }

        return null;
    }

    public boolean recursoDisponible(
            Recurso recurso,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin) {

        for (Reserva reserva :
                data.getReservas()) {

            if (reserva.getEstado()
                    != EstadoReserva.ACTIVA) {

                continue;
            }

            if (!reserva.getFecha()
                    .equals(fecha)) {

                continue;
            }

            boolean usaRecurso =
                    false;

            for (Recurso recursoReservado :
                    reserva.getRecursos()) {

                if (recursoReservado.getId()
                        .equals(recurso.getId())) {

                    usaRecurso = true;
                    break;
                }
            }

            if (!usaRecurso) {
                continue;
            }

            boolean hayChoque =
                    horaInicio.isBefore(
                            reserva.getHoraFin()
                    )
                            &&
                            horaFin.isAfter(
                                    reserva.getHoraInicio()
                            );

            if (hayChoque) {
                return false;
            }
        }

        return true;
    }

    public Recurso buscarPrimerRecursoDisponible(
            Categoria categoria,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin) {

        for (Recurso recurso :
                data.getRecursos()) {

            if (recurso.getCategoria() == null) {
                continue;
            }

            if (!recurso.getCategoria()
                    .getId()
                    .equals(categoria.getId())) {

                continue;
            }

            if (recursoDisponible(
                    recurso,
                    fecha,
                    horaInicio,
                    horaFin)) {

                return recurso;
            }
        }

        return null;
    }

    public Reserva crearReserva(
            String actividad,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            Funcionario funcionario,
            List<Categoria> categoriasSolicitadas) {

        validarReserva(
                actividad,
                fecha,
                horaInicio,
                horaFin,
                funcionario,
                categoriasSolicitadas
        );

        List<Recurso> recursosAsignados =
                new ArrayList<>();

        List<String> categoriasNoDisponibles =
                new ArrayList<>();

        for (Categoria categoria :
                categoriasSolicitadas) {

            Recurso recurso =
                    buscarPrimerRecursoDisponible(
                            categoria,
                            fecha,
                            horaInicio,
                            horaFin
                    );

            if (recurso == null) {

                categoriasNoDisponibles.add(
                        categoria.getDescripcion()
                );

            } else {

                recursosAsignados.add(
                        recurso
                );
            }
        }

        if (!categoriasNoDisponibles
                .isEmpty()) {

            throw new IllegalArgumentException(
                    "No hay recursos disponibles para: "
                            + String.join(
                            ", ",
                            categoriasNoDisponibles
                    )
            );
        }

        String id =
                generarIdReserva();

        Reserva reserva =
                new Reserva(
                        id,
                        actividad,
                        fecha,
                        horaInicio,
                        horaFin,
                        funcionario,
                        recursosAsignados,
                        EstadoReserva.ACTIVA
                );

        data.getReservas()
                .add(reserva);

        guardar();

        return reserva;
    }

    private void validarReserva(
            String actividad,
            LocalDate fecha,
            LocalTime horaInicio,
            LocalTime horaFin,
            Funcionario funcionario,
            List<Categoria> categoriasSolicitadas) {

        if (actividad == null
                || actividad.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Debe indicar la actividad"
            );
        }

        if (fecha == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar una fecha"
            );
        }

        if (horaInicio == null
                || horaFin == null) {

            throw new IllegalArgumentException(
                    "Debe indicar la hora de inicio y fin"
            );
        }

        if (!horaInicio
                .isBefore(horaFin)) {

            throw new IllegalArgumentException(
                    "La hora de inicio debe ser anterior "
                            + "a la hora de finalización"
            );
        }

        if (fecha.isBefore(
                LocalDate.now())) {

            throw new IllegalArgumentException(
                    "No se puede reservar en una fecha pasada"
            );
        }

        if (funcionario == null) {

            throw new IllegalArgumentException(
                    "No existe un funcionario autenticado"
            );
        }

        if (categoriasSolicitadas == null
                || categoriasSolicitadas.isEmpty()) {

            throw new IllegalArgumentException(
                    "Debe seleccionar al menos una categoría"
            );
        }
    }

    public void cancelarReserva(
            Reserva reserva,
            Funcionario funcionario) {

        if (reserva == null) {

            throw new IllegalArgumentException(
                    "Debe seleccionar una reserva"
            );
        }

        if (funcionario == null) {

            throw new IllegalArgumentException(
                    "No existe un funcionario autenticado"
            );
        }

        if (reserva.getFuncionario() == null
                || !reserva.getFuncionario()
                .getId()
                .equals(funcionario.getId())) {

            throw new IllegalArgumentException(
                    "No puede cancelar una reserva "
                            + "de otro funcionario"
            );
        }

        if (reserva.getEstado()
                == EstadoReserva.CANCELADA) {

            throw new IllegalArgumentException(
                    "La reserva ya está cancelada"
            );
        }

        if (!esReservaFutura(
                reserva)) {

            throw new IllegalArgumentException(
                    "Solo se pueden cancelar reservas futuras"
            );
        }

        reserva.setEstado(
                EstadoReserva.CANCELADA
        );

        guardar();
    }

    private boolean esReservaFutura(
            Reserva reserva) {

        LocalDate hoy =
                LocalDate.now();

        LocalTime ahora =
                LocalTime.now();

        if (reserva.getFecha()
                .isAfter(hoy)) {

            return true;
        }

        if (reserva.getFecha()
                .equals(hoy)) {

            return reserva.getHoraInicio()
                    .isAfter(ahora);
        }

        return false;
    }

    public String generarIdReserva() {

        int mayor = 0;

        for (Reserva reserva :
                data.getReservas()) {

            String id =
                    reserva.getId();

            if (id == null) {
                continue;
            }

            try {

                if (id.startsWith("R")) {

                    int numero =
                            Integer.parseInt(
                                    id.substring(1)
                            );

                    if (numero > mayor) {
                        mayor = numero;
                    }
                }

            } catch (NumberFormatException ignored) {
            }
        }

        return "R" + (mayor + 1);
    }

    // =========================================================
    // CAMBIO DE CLAVE
    // =========================================================

    public void cambiarClave(
            Usuario usuario,
            String claveActual,
            String claveNueva,
            String confirmarClave) {

        if (usuario == null) {

            throw new IllegalArgumentException(
                    "No existe un usuario autenticado"
            );
        }

        if (claveActual == null
                || claveActual.isBlank()) {

            throw new IllegalArgumentException(
                    "Debe indicar la clave actual"
            );
        }

        if (!usuario.getClave()
                .equals(claveActual)) {

            throw new IllegalArgumentException(
                    "La clave actual es incorrecta"
            );
        }

        if (claveNueva == null
                || claveNueva.isBlank()) {

            throw new IllegalArgumentException(
                    "Debe indicar la nueva clave"
            );
        }

        if (claveNueva.length() < 4) {

            throw new IllegalArgumentException(
                    "La nueva clave debe tener "
                            + "al menos 4 caracteres"
            );
        }

        if (!claveNueva
                .equals(confirmarClave)) {

            throw new IllegalArgumentException(
                    "La confirmación de la clave no coincide"
            );
        }

        if (claveNueva
                .equals(claveActual)) {

            throw new IllegalArgumentException(
                    "La nueva clave debe ser diferente "
                            + "a la clave actual"
            );
        }

        usuario.setClave(
                claveNueva
        );

        guardar();
    }
}