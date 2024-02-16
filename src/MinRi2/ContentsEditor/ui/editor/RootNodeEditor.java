package MinRi2.ContentsEditor.ui.editor;

import MinRi2.ContentsEditor.node.*;

/**
 * @author minri2
 * Create by 2024/2/15
 */
public class RootNodeEditor extends NodeEditor{

    public RootNodeEditor(){
        super();

        setNodeData(NodeData.getRootData());
    }

    @Override
    public void show(NodeData nodeData){
        show();
    }
}
