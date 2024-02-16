package MinRi2.ContentsEditor.ui.editor;

import MinRi2.ContentsEditor.node.*;
import MinRi2.ContentsEditor.ui.*;
import MinRi2.ModCore.ui.*;
import mindustry.ui.dialogs.*;

/**
 * @author minri2
 * Create by 2024/2/15
 */
public class NodeEditor extends BaseDialog{
    protected NodeData nodeData;
    private NodeEditor nodeEditor;

    public NodeEditor(){
        super("");

        setup();

        resized(this::rebuild);
        shown(this::rebuild);
    }

    protected void setup(){
        titleTable.clearChildren();
        ElementUtils.addTitle(titleTable, "@contents-editor", EPalettes.purpleAccent1);

        cont.top();

        addCloseButton();
        makeButtonOverlay();
    }

    protected void rebuild(){

    }

    public void show(NodeData nodeData){
        this.nodeData = nodeData;

        show();
    }

    protected void editChildNode(NodeData childNode){
        if(nodeEditor == null){
            nodeEditor = new NodeEditor();
        }

        nodeEditor.show(childNode);
    }
}
