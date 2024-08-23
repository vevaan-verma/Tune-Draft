package com.cladcobra.tunedraft.util;

import android.widget.Space;

import androidx.constraintlayout.widget.ConstraintLayout;

public class SquadElementLayout {

    private final ConstraintLayout layout;
    private final Space space;

    public SquadElementLayout(ConstraintLayout layout, Space space) {

        this.layout = layout;
        this.space = space;

    }

    public ConstraintLayout getLayout() {
        return layout;
    }

    public Space getSpace() {
        return space;
    }

}