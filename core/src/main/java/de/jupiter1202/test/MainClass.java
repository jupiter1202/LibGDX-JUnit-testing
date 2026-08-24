package de.jupiter1202.test;

import com.badlogic.gdx.Game;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class MainClass extends Game {
    @Override
    public void create() {
        setScreen(new FirstScreen());
    }
}