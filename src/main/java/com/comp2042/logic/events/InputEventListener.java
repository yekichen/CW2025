package com.comp2042.logic.events;

import com.comp2042.logic.ViewData;

public interface InputEventListener {

    DownData onDownEvent(MoveEvent event);

    ViewData onLeftEvent(MoveEvent event);

    ViewData onRightEvent(MoveEvent event);

    ViewData onRotateEvent(MoveEvent event);

    DownData onHardDrop();


    void createNewGame();

    void onHold();


}
