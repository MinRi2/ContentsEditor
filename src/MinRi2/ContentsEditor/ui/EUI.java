package MinRi2.ContentsEditor.ui;

import MinRi2.ContentsEditor.ui.editor.*;

/**
 * @author minri2
 * Create by 2024/2/15
 */
public class EUI{
    public static PatchManager manager;
    public static ContentSelector selector;

    public static void init(){
        EStyles.init();

        manager = new PatchManager();
        selector = new ContentSelector();

        manager.addUI();
    }

}
