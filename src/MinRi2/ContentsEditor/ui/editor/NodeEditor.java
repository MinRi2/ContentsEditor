package MinRi2.ContentsEditor.ui.editor;

import MinRi2.ContentsEditor.node.*;
import MinRi2.ContentsEditor.ui.*;
import MinRi2.ModCore.ui.*;
import arc.input.*;
import mindustry.ui.dialogs.*;

/**
 * @author minri2
 * Create by 2024/2/15
 */
public class NodeEditor extends BaseDialog{
    private final NodeData rootData;
    private final NodeCard card;

    public NodeEditor(){
        super("");

        rootData = NodeData.getRootData();
        card = new NodeCard();

        setup();

        resized(this::rebuild);
        shown(this::rebuild);
        hidden(() -> {

        });

        keyDown(KeyCode.up, () -> {
            card.getFrontCard().extractWorking();
        });
    }

    protected void setup(){
        titleTable.clearChildren();
        ElementUtils.addTitle(titleTable, "@contents-editor", EPalettes.purpleAccent1);

        cont.top();

        card.setNodeData(rootData);

        addCloseButton();
        makeButtonOverlay();
    }

    protected void rebuild(){
        cont.clearChildren();

        card.rebuild();
        cont.add(card).pad(16f).padTop(8f).grow();
    }
}
