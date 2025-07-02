package com.lksnext.ParkingXAbaunz.domain;

import java.util.ArrayList;

public class Usuario {
    String email;
    ArrayList<Coche> coches;

    public Usuario() {}

    public Usuario(String email) {
        this.email = email;
        this.coches = new ArrayList<>();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
