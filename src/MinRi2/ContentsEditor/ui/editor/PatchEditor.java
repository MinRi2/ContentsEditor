package MinRi2.ContentsEditor.ui.editor;

import MinRi2.ContentsEditor.node.*;
import MinRi2.ContentsEditor.ui.*;
import MinRi2.ContentsEditor.ui.editor.PatchManager.*;
import MinRi2.ModCore.ui.*;
import arc.input.*;
import arc.util.serialization.JsonWriter.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

/**
 * @author minri2
 * Create by 2024/2/15
 */
public class PatchEditor extends BaseDialog{
    private final NodeData rootData;
    private final NodeCard card;

    private Patch editPatch;

    public PatchEditor(){
        super("");

        rootData = NodeData.getRootData();
        card = new NodeCard();

        setup();

        resized(this::rebuild);
        shown(this::rebuild);
        hidden(() -> {
            if(editPatch != null){
                editPatch.json = rootData.jsonData.toJson(OutputType.minimal);
            }
        });

        keyDown(KeyCode.up, () -> {
            card.getFrontCard().extractWorking();
        });

        addCloseListener();
    }

    public void edit(Patch patch){
        editPatch = patch;

        rootData.clearData();
        rootData.jsonData = editPatch.getJsonValue();
        rootData.readData();

        show();
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
        cont.pane(Styles.noBarPane, card).scrollX(false).pad(16f).padTop(8f).grow();
    }
}
