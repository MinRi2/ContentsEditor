package MinRi2.ContentsEditor.ui.editor;

import arc.func.*;
import arc.scene.ui.layout.*;
import cf.wayzer.contentsTweaker.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

import java.util.*;

/**
 * @author minri2
 * Create by 2024/2/15
 */
public class NodeSelector extends BaseDialog{
    private final Table selectPane;
    private CTNode node;
    private Boolf2<String, CTNode> selectable, selectConsumer;

    public NodeSelector(){
        super("@NodeSelector");

        selectPane = new Table();

        setup();

        shown(this::rebuild);
        resized(this::rebuild);
    }

    private void setup(){
        cont.pane(Styles.noBarPane, selectPane).grow();

        addCloseButton();
        makeButtonOverlay();
    }

    public void select(CTNode node, Boolf2<String, CTNode> selectable, Boolf2<String, CTNode> selectConsumer){
        this.node = node;
        this.selectable = selectable;
        this.selectConsumer = selectConsumer;

        show();
    }

    private void rebuild(){
        selectPane.clearChildren();
        node.collectAll();

        Map<String, CTNode> children = node.getChildren();

        final int[] index = {0};
        children.forEach((nodeName, child) -> {
            child.collectAll();

            if(!selectable.get(nodeName, child)){
                return;
            }

            selectPane.button(b -> {
                NodeDisplay.display(b, child, nodeName);
            }, () -> {
                if(selectConsumer.get(nodeName, child)){
                    hide();
                }else{
                    rebuild();
                }
            }).pad(8f).growX();

            if(++index[0] % 5 == 0){
                selectPane.row();
            }
        });
    }
}
