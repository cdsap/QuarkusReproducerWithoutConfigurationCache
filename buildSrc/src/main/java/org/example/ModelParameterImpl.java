package org.example;

import java.io.Serializable;

public class ModelParameterImpl implements ModelParameter, Serializable {

    private static final long serialVersionUID = 4617775770506785059L;

    private String mode;

    @Override
    public String getMode() {
        return mode;
    }

    @Override
    public void setMode(String mode) {
        this.mode = mode;
    }
}
