package MinRi2.ContentsEditor.ui.editor;

import MinRi2.ContentsEditor.node.*;
import MinRi2.ContentsEditor.ui.*;
import MinRi2.ModCore.ui.*;
import arc.util.*;
import arc.util.serialization.JsonWriter.*;
import mindustry.ui.dialogs.*;

/**
 * @author minri2
 * Create by 2024/2/15
 */
public class NodeEditor extends BaseDialog{
    private final NodeCard card;

    public NodeEditor(){
        super("");

        card = new NodeCard();

        setup();

        resized(this::rebuild);
        shown(this::rebuild);
        hidden(() -> {
            Log.info("EDIT END: @", NodeData.getRootData().jsonData.toJson(OutputType.json));
        });
    }

    protected void setup(){
        titleTable.clearChildren();
        ElementUtils.addTitle(titleTable, "@contents-editor", EPalettes.purpleAccent1);

        cont.top();

        card.setNodeData(NodeData.getRootData());

        addCloseButton();
        makeButtonOverlay();
    }

    protected void rebuild(){
        cont.clearChildren();

        card.rebuild();
        cont.add(card).pad(16f).padTop(8f).grow();
    }
}
