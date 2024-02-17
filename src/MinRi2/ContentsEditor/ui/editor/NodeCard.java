package MinRi2.ContentsEditor.ui.editor;

import MinRi2.ContentsEditor.node.*;
import MinRi2.ContentsEditor.ui.*;
import MinRi2.ContentsEditor.ui.editor.modifier.*;
import MinRi2.ModCore.ui.*;
import MinRi2.ModCore.utils.*;
import arc.*;
import arc.graphics.*;
import arc.scene.ui.ImageButton.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import cf.wayzer.contentsTweaker.*;
import mindustry.gen.*;
import mindustry.graphics.*;
import mindustry.ui.*;

import java.util.Map.*;

/**
 * @author minri2
 * Create by 2024/2/16
 */
public class NodeCard extends Table{
    private final Table cardCont, nodesTable; // workingTable / childrenNodesTable
    public boolean isChild, working;
    public NodeCard parent, childCard;

    private NodeData nodeData;
    private Seq<Entry<String, CTNode>> sortedChildren;

    private String searchText = "";
    private final DebounceTask debounceRebuild = new DebounceTask(0.3f, this::rebuildNodesTable);

    public NodeCard(){
        cardCont = new Table();
        nodesTable = new Table();

        top().left();
        cardCont.top();
        nodesTable.top().left();
    }

    public void setNodeData(NodeData nodeData){
        this.nodeData = nodeData;
    }

    public void setParent(NodeCard parent){
        isChild = parent != null;
        this.parent = parent;
    }

    public NodeCard getFrontCard(){
        NodeCard card = this;

        while((card.parent == null || card.working) && card.childCard != null){
            card = card.childCard;
        }

        return card;
    }

    public void extractWorking(){
        if(parent != null){
            parent.editChildNode(null);
        }
    }

    public void rebuild(){
        clearChildren();

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
            childCard.rebuildCont();
            cardCont.add(childCard).grow();
        }else{
            cardCont.table(this::setupSearchTable).pad(8f).growX();

            cardCont.row();

            cardCont.pane(Styles.noBarPane, nodesTable).fill();

            // 下一帧再重构
            Core.app.post(this::rebuildNodesTable);
        }
    }

    private void setupSearchTable(Table table){
        table.image(Icon.zoom).size(64f);

        TextField field = table.field(searchText, text -> {
            searchText = text;
            debounceRebuild.run();
        }).pad(8f).growX().get();

        if(Core.app.isDesktop()){
            Core.scene.setKeyboardFocus(field);
        }

        table.button(Icon.cancel, Styles.clearNonei, () -> {
            searchText = "";
            field.setText(searchText);
            rebuildNodesTable();
        }).size(64f);
    }

    private void rebuildNodesTable(){
        nodesTable.clearChildren();

        // 下一帧可能正好被清除
        if(nodeData == null){
            return;
        }

        float buttonWidth = 250f / Scl.scl();
        int columns = Math.max(1, (int)(nodesTable.getWidth() / Scl.scl() / buttonWidth));

        nodesTable.defaults().size(buttonWidth, buttonWidth / 4).pad(4f).margin(8f).top().left();

        Seq<Entry<String, CTNode>> children = getSortedChildren();

        int index = 0;
        for(Entry<String, CTNode> entry : children){
            CTNode childNode = entry.getValue();
            String childNodeName = entry.getKey();

            if(!searchText.isEmpty() && !childNodeName.toLowerCase().contains(searchText.toLowerCase())){
                continue;
            }

            if(NodeModifier.modifiable(childNode)){
                addEditTable(nodesTable, childNodeName);
            }else{
                addChildButton(nodesTable, childNode, childNodeName);
            }

            if(++index % columns == 0){
                nodesTable.row();
            }
        }
    }

    private void addEditTable(Table table, String childNodeName){
        NodeData childNodeData = nodeData.getChild(childNodeName);

        table.add(new NodeModifier(childNodeData));
    }

    private void addChildButton(Table table, CTNode childNode, String childNodeName){
        ImageButtonStyle style = nodeData.hasData(childNodeName) ? EStyles.cardModifiedButtoni : EStyles.cardButtoni;

        table.button(b -> {
            NodeDisplay.display(b, childNode, childNodeName);

            b.image().width(4f).color(Color.darkGray).growY().right();
            b.row();
            Cell<?> horizontalLine = b.image().height(4f).color(Color.darkGray).growX();
            horizontalLine.colspan(b.getColumns());
        }, style, () -> {
            NodeData childNodeData = nodeData.getChild(childNodeName);
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
                buttonTable.defaults().width(64f).growY();

                // Clear data
                buttonTable.button(Icon.refresh, Styles.clearNonei, () -> {
                    nodeData.clearData();

                    getFrontCard().rebuildNodesTable();
                }).with(b -> {
                    ElementUtils.addTooltip(b, "@node-card.clear-data", true);
                });

                if(parent != null){
                    buttonTable.button(Icon.cancel, Styles.clearNonei, this::extractWorking).with(b -> {
                        ElementUtils.addTooltip(b, "@node-card.extract", true);
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

            return Boolean.compare(!NodeModifier.modifiable(n1), !NodeModifier.modifiable(n2));
        });

        return sortedChildren;
    }
}