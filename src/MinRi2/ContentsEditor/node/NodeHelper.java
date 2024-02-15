package MinRi2.ContentsEditor.node;

import cf.wayzer.contentsTweaker.*;
import cf.wayzer.contentsTweaker.CTNode.*;

import java.util.*;

/**
 * @author minri2
 * Create by 2024/2/15
 */
public class NodeHelper{
    public static CTNode root = CTNode.Companion.getRoot();

    public static ObjInfo<?> getObjectInfo(CTNode node){
        List<CTExtInfo> mixins = node.getMixins();
        for(Object mixin : mixins){
            if(mixin instanceof ObjInfo<?> objInfo){
                return objInfo;
            }
        }
        return null;
    }
}
