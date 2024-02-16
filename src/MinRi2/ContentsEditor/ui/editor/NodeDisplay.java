package MinRi2.ContentsEditor.ui.editor;

import MinRi2.ContentsEditor.node.*;
import MinRi2.ContentsEditor.ui.*;
import arc.graphics.g2d.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import cf.wayzer.contentsTweaker.*;
import cf.wayzer.contentsTweaker.CTNode.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.gen.*;

/**
 * @author minri2
 * Create by 2024/2/15
 */
public class NodeDisplay{
    public static final float labelWidth = 120f;
    private static ObjectMap<ContentType, Drawable> contentSymbolMap;

    private static Table table;
    private static CTNode node;
    private static String nodeName;
    private static ObjInfo<?> objInfo;

    private static void intiSymbol(){
        contentSymbolMap = ObjectMap.of(
        ContentType.item, new TextureRegionDrawable(Items.copper.uiIcon),
        ContentType.block, new TextureRegionDrawable(Blocks.duo.uiIcon),
        ContentType.liquid, new TextureRegionDrawable(Liquids.water.uiIcon),
        ContentType.status, new TextureRegionDrawable(StatusEffects.overclock.uiIcon),
        ContentType.unit, new TextureRegionDrawable(UnitTypes.alpha.uiIcon),
        ContentType.planet, Icon.icons.get(Planets.serpulo.icon)
        );
    }

    public static void reset(){
        table = null;
        node = null;
        nodeName = null;
        objInfo = null;
    }

    public static void display(Table table, NodeData nodeData){
        display(table, nodeData.node, nodeData.nodeName);
    }

    public static void display(Table table, CTNode node, String nodeName){
        NodeDisplay.table = table;
        NodeDisplay.node = node;
        NodeDisplay.nodeName = nodeName;

        objInfo = NodeHelper.getObjectInfo(node);

        if(objInfo == null){
            table.add(nodeName).wrap().width(labelWidth).pad(4f).expandX().left();
            return;
        }

        displayObject(objInfo.getObj());
        reset();
    }

    private static void displayObject(Object object){
        if(object == null){
            displayDefault();
            return;
        }

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
        table.table(nodeInfoTable -> {
            nodeInfoTable.defaults().expandX().left();

            Class<?> type = objInfo.getType();
            if(type.isAnonymousClass()){
                type = type.getSuperclass();
            }
            String typeInfo = type.getSimpleName();

            nodeInfoTable.add(nodeName).wrap().width(labelWidth);
            nodeInfoTable.row();
            nodeInfoTable.add(typeInfo).fontScale(0.85f).color(EPalettes.typePurple).wrap().width(labelWidth).padTop(4f);
        }).pad(4f).left();
    }

    private static void displayInfo(Drawable icon, String value){
        table.table(valueTable -> {
            valueTable.defaults().expandX().right();

            valueTable.image(icon).scaling(Scaling.fit).size(Vars.iconLarge);
            valueTable.row();
            valueTable.add(value).labelAlign(Align.right).ellipsis(true).width(labelWidth).padTop(4f);
        }).pad(4f).right();
    }

    private static void displayContent(UnlockableContent content){
        table.top();

        displayDefault();

        displayInfo(new TextureRegionDrawable(content.uiIcon), content.localizedName);
    }

    private static void displayContentType(ContentType contentType){
        displayDefault();

        Seq<?> seq = Vars.content.getBy(contentType);
        if(seq.isEmpty()){
            return;
        }

        if(contentSymbolMap == null){
            intiSymbol();
        }

        TextureRegion region = ((UnlockableContent)seq.first()).uiIcon;
        Drawable icon = new TextureRegionDrawable(region);
        icon = contentSymbolMap.get(contentType, icon);

        displayInfo(icon, Strings.capitalize(contentType.name()));
    }

}
