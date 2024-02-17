package MinRi2.ContentsEditor.ui;

import mindustry.ui.dialogs.*;

/**
 * @author minri2
 * Create by 2024/2/17
 */
public class ContentSelector extends BaseDialog{
    public ContentSelector(){
        super("");

        setup();

        resized(this::rebuild);
        shown(this::rebuild);
    }

    private void setup(){
        addCloseButton();
    }

    private void rebuild(){

    }
}
