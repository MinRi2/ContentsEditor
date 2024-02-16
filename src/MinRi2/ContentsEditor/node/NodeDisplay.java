package MinRi2.ContentsEditor.node;

import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import cf.wayzer.contentsTweaker.*;
import cf.wayzer.contentsTweaker.CTNode.*;
import mindustry.*;
import mindustry.ctype.*;

/**
 * @author minri2
 * Create by 2024/2/15
 */
public class NodeDisplay{
    private static Table table;
    private static CTNode node;
    private static String nodeName;

    public static void display(Table table, NodeData nodeData){
        display(table, nodeData.node, nodeData.nodeName);
    }

    public static void display(Table table, CTNode node, String nodeName){
        NodeDisplay.table = table;
        NodeDisplay.node = node;
        NodeDisplay.nodeName = nodeName;

        ObjInfo<?> objInfo = NodeHelper.getObjectInfo(node);

        if(objInfo == null){
            displayDefault();
            return;
        }

        Object object = objInfo.getObj();
        displayObject(object);
    }

    private static void displayObject(Object object){
        if(object instanceof UnlockableContent content){
            displayContent(content);
        }else if(object instanceof ContentType contentType
        && contentType.contentClass != null
        && UnlockableContent.class.isAssignableFrom(contentType.contentClass)){
            displayContentType(contentType);
        }else{
            displayDefault();
        }
    }

    private static void displayDefault(){
        table.add(nodeName).labelAlign(Align.left).pad(4f).grow();
    }

    private static void displayContent(UnlockableContent content){
        table.top();
        table.image(content.uiIcon).scaling(Scaling.fit).size(Vars.iconLarge).pad(4f);
        table.add(content.localizedName).labelAlign(Align.left).pad(4f).grow();
    }

    private static void displayContentType(ContentType contentType){
        Seq<?> seq = Vars.content.getBy(contentType);
        if(seq.isEmpty()){
            displayDefault();
            return;
        }

        UnlockableContent symbol = (UnlockableContent)seq.first();
        table.image(symbol.uiIcon).scaling(Scaling.fit).size(Vars.iconLarge).pad(4f);
        table.add(contentType.name()).labelAlign(Align.left).pad(4f).grow();
    }

}
