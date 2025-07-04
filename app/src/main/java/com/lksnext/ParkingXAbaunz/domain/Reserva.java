package com.lksnext.ParkingXAbaunz.domain;

import java.io.Serializable;

public class Reserva implements Serializable {

    String fecha, id;

    Coche coche;

    Plaza plaza;

    Hora hora;

    public Reserva() {

    }

    public Reserva(String fecha, Coche coche, String id, Plaza plaza, Hora hora) {
        this.fecha = fecha;
        this.coche = coche;
        this.plaza = plaza;
        this.hora = hora;
        this.id = id;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public Coche getCoche() {
        return coche;
    }

    public void setCoche(Coche coche) {
        this.coche = coche;
    }

    public Plaza getPlazaId() {
        return plaza;
    }

    public void setPlazaId(Plaza plaza) {
        this.plaza = plaza;
    }

    public Hora getHora() {
        return hora;
    }

    public void setHora(Hora hora) {
        this.hora = hora;
    }

    public long getHoraInicio() {
        return hora != null ? hora.getHoraInicio() : 0;
    }

    public void setHoraInicio(long horaInicio) {
        if (hora == null) {
            hora = new Hora(horaInicio, horaInicio);
        } else {
            hora.setHoraInicio(horaInicio);
        }
    }

    public long getHoraFin() {
        return hora != null ? hora.getHoraFin() : 0;
    }

    public void setHoraFin(long horaFin) {
        if (hora == null) {
            hora = new Hora(0, horaFin);
        } else {
            hora.setHoraFin(horaFin);
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}