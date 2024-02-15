package MinRi2.ContentsEditor;

import MinRi2.ContentsEditor.ui.*;
import arc.*;
import mindustry.game.EventType.*;
import mindustry.mod.*;

/**
 * @author minri2
 * Create by 2024/2/14
 */
public class Main extends Mod{
    public Main(){
        Events.on(ClientLoadEvent.class, e -> {
            EUI.init();
            EUI.editor.show();
        });
    }
}
