package MinRi2.ContentsEditor.ui.editor;

import MinRi2.ContentsEditor.node.*;
import MinRi2.ContentsEditor.ui.*;
import MinRi2.ModCore.ui.*;
import arc.*;
import arc.graphics.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import cf.wayzer.contentsTweaker.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

import java.util.Map.*;

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

    protected static class NodeCard extends Table{
        private final Table cardCont; // workingTable / childrenNodesTable
        public boolean isChild, working;
        private NodeData nodeData;
        private NodeCard parent, childCard;
        private Seq<Entry<String, CTNode>> sortedChildren;

        public NodeCard(){
            cardCont = new Table();

            top().left();
            cardCont.top();
        }

        public void setNodeData(NodeData nodeData){
            this.nodeData = nodeData;
        }

        public void setParent(NodeCard parent){
            isChild = parent != null;
            this.parent = parent;
        }

        public void rebuild(){
            clearChildren();

            // Not working.
            if(nodeData == null){
                return;
            }

            defaults().growX();

            buildTitle(this);

            row();

            rebuildCont();
            add(cardCont).grow();
        }

        private void rebuildCont(){
            cardCont.clearChildren();

            cardCont.defaults().padLeft(16f);

            if(working){
                cardCont.add(childCard).grow();
            }else{
                cardCont.table(searchTable -> {
                    searchTable.add("Search").left();
                }).growX();

                cardCont.row();

                // Set up the nodesTable next frame.
                cardCont.pane(Styles.noBarPane, t -> Core.app.post(() -> {
                    setupNodesTable(t);
                })).grow();
            }
        }

        private void setupNodesTable(Table table){
            float buttonWidth = 250f / Scl.scl();
            int columns = Math.max(1, (int)(table.getWidth() / Scl.scl() / buttonWidth));

            table.top().left();
            table.defaults().size(buttonWidth, buttonWidth / 4).pad(4f).margin(8f);

            Seq<Entry<String, CTNode>> children = getSortedChildren();

            int index = 0;
            for(Entry<String, CTNode> entry : children){
                CTNode childNode = entry.getValue();
                String childNodeName = entry.getKey();

                if(NodeHelper.settable(childNode)){
                    addEditTable(table, childNode, childNodeName);
                }else{
                    addChildButton(table, childNode, childNodeName);
                }

                if(++index % columns == 0){
                    table.row();
                }
            }
        }

        private void addEditTable(Table table, CTNode childNode, String childNodeName){
            NodeData childNodeData = nodeData.getOrCreate(childNodeName);

            table.table(MinTex.whiteuiRegion, t -> {
                t.table(editTable -> {
                    NodeDisplay.display(editTable, childNodeData);
                    editTable.add("EDIT").growX().left();
                }).grow();

                t.image().width(4f).color(Color.darkGray).growY().right();
                t.row();
                Cell<?> horizontalLine = t.image().height(4f).color(Color.darkGray).growX();
                horizontalLine.colspan(t.getColumns());
            }).color(EPalettes.editSky);
        }

        private void addChildButton(Table table, CTNode childNode, String childNodeName){
            table.button(b -> {
                NodeDisplay.display(b, childNode, childNodeName);

                b.image().width(4f).color(Color.darkGray).growY().right();
                b.row();
                Cell<?> horizontalLine = b.image().height(4f).color(Color.darkGray).growX();
                horizontalLine.colspan(b.getColumns());
            }, EStyles.cardButtoni, () -> {
                NodeData childNodeData = nodeData.getOrCreate(childNodeName);
                editChildNode(childNodeData);

                rebuildCont();
            });
        }

        private void editChildNode(NodeData childNodeData){
            if(childCard == null){
                childCard = new NodeCard();
                childCard.setParent(this);
            }else if(childCard.working){
                childCard.editChildNode(null);
            }

            working = childNodeData != null;

            childCard.setNodeData(childNodeData);
            childCard.rebuild();
            rebuild();
        }

        private void buildTitle(Table table){
            Color titleColor = !isChild ? EPalettes.purpleAccent2 : EPalettes.purpleAccent3;
            table.table(MinTex.getColoredRegion(titleColor), nodeTitle -> {
                nodeTitle.table(MinTex.getColoredRegion(Pal.darkestGray), nameTable -> {
                    NodeDisplay.display(nameTable, nodeData);

                    nameTable.image().width(4f).color(Color.darkGray).growY().right();
                    nameTable.row();
                    Cell<?> horizontalLine = nameTable.image().height(4f).color(Color.darkGray).growX();
                    horizontalLine.colspan(nameTable.getColumns());
                }).pad(8f).expandX().left();

                nodeTitle.table(Styles.black3, buttonTable -> {
                    buttonTable.defaults().size(64f);

                    // Refresh children node
                    buttonTable.button(Icon.refresh, Styles.clearNonei, () -> {
                    });

                    if(!nodeData.isRoot()){
                        buttonTable.button(Icon.cancel, Styles.clearNonei, () -> {
                            parent.editChildNode(null);
                        });
                    }
                }).growY();
            });
        }

        private Seq<Entry<String, CTNode>> getSortedChildren(){
            if(sortedChildren == null){
                sortedChildren = new Seq<>();
            }

            NodeHelper.getEntries(nodeData.node, sortedChildren);

            sortedChildren.sort((e1, e2) -> {
                CTNode n1 = e1.getValue();
                CTNode n2 = e2.getValue();

                int settable = Boolean.compare(!NodeHelper.settable(n1), !NodeHelper.settable(n2));
                if(settable != 0) return settable;

                return settable;
            });

            return sortedChildren;
        }
    }
}
