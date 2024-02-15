package MinRi2.ContentsEditor.ui;

import MinRi2.ContentsEditor.ui.editor.*;

/**
 * @author minri2
 * Create by 2024/2/15
 */
public class EUI{
    public static RootNodeEditor editor;
    public static NodeSelector selector;

    public static void init(){
        EStyles.init();

        editor = new RootNodeEditor();
        selector = new NodeSelector();
    }

}
