package MinRi2.ContentsEditor.ui;

import arc.scene.ui.ScrollPane.*;
import mindustry.gen.*;
import mindustry.ui.*;

/**
 * @author minri2
 * Create by 2024/2/15
 */
public class EStyles{
    public static ScrollPaneStyle cardGrayPane, cardPane;

    public static void init(){
        cardGrayPane = new ScrollPaneStyle(){{
            background = Styles.grayPanel;
        }};

        cardPane = new ScrollPaneStyle(){{
            background = Tex.pane2;
        }};
    }
}
