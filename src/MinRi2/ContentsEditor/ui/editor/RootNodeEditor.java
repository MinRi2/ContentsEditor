package MinRi2.ContentsEditor.ui.editor;

import MinRi2.ContentsEditor.node.*;
import MinRi2.ContentsEditor.ui.*;
import MinRi2.ModCore.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import cf.wayzer.contentsTweaker.*;
import mindustry.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;

/**
 * @author minri2
 * Create by 2024/2/15
 */
public class RootNodeEditor extends NodeEditor{
    private final NodeCard card;

    public RootNodeEditor(){
        super();

        nodeData = NodeData.getRootData();

        card = new NodeCard(nodeData);
    }

    @Override
    protected void rebuild(){
        cont.clearChildren();

        card.rebuild();
        cont.add(card).pad(16f).padTop(8f).grow();
    }

    private static class NodeCard extends Table{
        public final NodeData nodeData;
        protected final Table nodesTable;
        public boolean isChild;

        protected NodeCard parent;

        public NodeCard(NodeData nodeData){
            this.nodeData = nodeData;
            nodesTable = new Table();

            top().left();
            nodesTable.top();
        }

        public void setParent(NodeCard parent){
            isChild = parent != null;
            this.parent = parent;
        }

        public void rebuild(){
            clearChildren();

            defaults().growX();

            buildTitle(this);

            row();

            rebuildNodesTable();
            pane(EStyles.cardGrayPane, nodesTable).scrollY(false).grow();
        }

        private void rebuildNodesTable(){
            if(!isChild){
                rebuildNodesTableDefault();
            }else{
                rebuildNodesTableChild();
            }
        }

        protected void rebuildNodesTableDefault(){
            nodesTable.clearChildren();
            nodesTable.defaults().minWidth(450f / Scl.scl()).pad(8f).margin(8f).growY();

            for(NodeData child : nodeData){
                NodeCard childCard = new NodeCard(child);

                childCard.setParent(this);
                childCard.rebuild();

                nodesTable.add(childCard);
            }
        }

        protected void rebuildNodesTableChild(){
            nodesTable.clearChildren();

            nodesTable.pane(EStyles.cardPane, pane -> {
                pane.defaults().pad(8f).margin(8f).growX();

                int index = 0;
                for(NodeData child : nodeData){
                    pane.button(b -> {
                        NodeDisplay.display(b, child);
                    }, Styles.cleari, () -> {

                    }).growY();

                    if(++index % 2 == 0){
                        pane.row();
                    }
                }
            }).scrollX(false).growX();
        }

        private void buildTitle(Table table){
            CTNode node = nodeData.node;

            table.table(MinTex.getColoredRegion(Pal.lightishGray), nodeTitle -> {
                String name = nodeData.getDisplayName();
                nodeTitle.add(name).labelAlign(Align.left).pad(8f).growX();

                nodeTitle.table(Styles.black3, buttonTable -> {
                    buttonTable.defaults().size(Vars.iconXLarge);

                    if(!nodeData.isRoot()){
                        buttonTable.button(Icon.trash, Styles.cleari, () -> {
                            nodeData.remove();
                            parent.rebuildNodesTable();
                        });
                    }

                    buttonTable.button(Icon.add, Styles.cleari, () -> {
                        EUI.selector.select(node,
                        (otherNodeName, otherNode) -> !nodeData.has(otherNodeName),
                        (otherNodeName, otherNode) -> {
                            nodeData.getOrCreate(otherNodeName);
                            rebuildNodesTable();
                            return false;
                        });
                    });
                }).growY();
            });
        }
    }
}
