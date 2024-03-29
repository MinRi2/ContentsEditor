package MinRi2.ContentsEditor.ui;

import MinRi2.ModCore.ui.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.ScrollPane.*;
import mindustry.gen.*;
import mindustry.ui.*;

import static mindustry.ui.Styles.cleari;

/**
 * @author minri2
 * Create by 2024/2/15
 */
public class EStyles{
    public static ImageButtonStyle cardButtoni, cardModifiedButtoni;
    public static ScrollPaneStyle cardGrayPane, cardPane;

    public static void init(){
        cardButtoni = new ImageButtonStyle(cleari){{
            up = MinTex.getColoredRegion(EPalettes.gray);
            down = over = MinTex.getColoredRegion(EPalettes.purpleAccent4);
        }};

        cardModifiedButtoni = new ImageButtonStyle(cardButtoni){{
            up = MinTex.getColoredRegion(EPalettes.modified);
        }};

        cardGrayPane = new ScrollPaneStyle(){{
            background = Styles.grayPanel;
        }};

        cardPane = new ScrollPaneStyle(){{
            background = Tex.pane2;
        }};
    }
}
