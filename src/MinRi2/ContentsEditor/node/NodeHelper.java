package MinRi2.ContentsEditor.node;

import arc.struct.*;
import cf.wayzer.contentsTweaker.*;
import cf.wayzer.contentsTweaker.CTNode.*;
import mindustry.ctype.*;

import java.util.*;
import java.util.Map.*;

/**
 * @author minri2
 * Create by 2024/2/15
 */
public class NodeHelper{
    private static final ObjectMap<CTNode, ObjInfo<?>> objInfoCache = new ObjectMap<>();
    public static CTNode root = CTNode.Companion.getRoot();

    public static ObjInfo<?> getObjectInfo(CTNode node){
        ObjInfo<?> objInfo = objInfoCache.get(node);

        if(objInfo != null) return objInfo;

        List<CTExtInfo> mixins = node.getMixins();
        for(Object mixin : mixins){
            if(mixin instanceof ObjInfo<?> info){
                objInfoCache.put(node, info);
                return info;
            }
        }

        return null;
    }

    public static String getDisplayName(CTNode node){
        ObjInfo<?> objInfo = getObjectInfo(node);

        if(objInfo == null) return "";

        Object obj = objInfo.getObj();
        if(obj instanceof UnlockableContent content){
            return content.localizedName;
        }

        return "";
    }

    public static void getEntries(CTNode node, Seq<Entry<String, CTNode>> out){
        node.collectAll();

        Set<Entry<String, CTNode>> set = node.getChildren().entrySet();

        @SuppressWarnings("unchecked")
        Entry<String, CTNode>[] entries = new Entry[set.size()];
        set.toArray(entries);

        out.set(entries);
    }
}
