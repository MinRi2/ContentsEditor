package MinRi2.ContentsEditor.ui.editor;

import MinRi2.ContentsEditor.ui.*;
import MinRi2.ModCore.ui.*;
import arc.*;
import arc.flabel.*;
import arc.graphics.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import arc.util.pooling.*;
import arc.util.pooling.Pool.*;
import arc.util.serialization.*;
import mindustry.*;
import mindustry.editor.*;
import mindustry.gen.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

/**
 * @author minri2
 * Create by 2024/2/17
 */
public class PatchManager extends BaseDialog implements Addable{
    private static final Pool<Patch> patchPool = Pools.get(Patch.class, Patch::new);
    public static String contentsPatchTag = "ContentsPatch", patchSuffix = "CT@";

    private final PatchEditor editor;
    private final Table patchContainer, patchTable;
    private final Seq<Patch> patchSeq = new Seq<>();

    public PatchManager(){
        super("");

        editor = new PatchEditor();

        patchContainer = new Table();
        patchTable = new Table();

        setup();

        resized(this::rebuildCont);
        shown(() -> {
            readPatch(Vars.editor.tags);
            rebuildCont();
        });
        hidden(() -> {
            savePatch(Vars.editor.tags);
//            Vars.ui.editor.save();
        });
    }

    @Override
    public void addUI(){
        MapInfoDialog infoDialog = Reflect.get(Vars.ui.editor, "infoDialog");

        infoDialog.shown(() -> Core.app.post(() -> {
            ScrollPane pane = (ScrollPane)infoDialog.cont.getChildren().get(0);
            Table table = Reflect.get(pane, "widget");

            Table buttonTable = (Table)table.getChildren().peek();

            buttonTable.row();

            buttonTable.button(b -> {
                b.add(new FLabel("{rainbow}[CT]")).pad(8f).left();
                b.add("@contents-editor").color(EPalettes.purpleAccent1).expandX();
            }, Styles.cleari, this::show)
            .colspan(buttonTable.getColumns()).width(Float.NEGATIVE_INFINITY).growX();

            buttonTable.row();
        }));
    }

    private void setup(){
        titleTable.clearChildren();
        cont.clearChildren();

        patchContainer.background(MinTex.whiteuiRegion).setColor(EPalettes.purpleAccent1);
        patchTable.background(Styles.grayPanel);

        cont.add(patchContainer);

        addCloseButton();
    }

    private void readPatch(StringMap tags){
        String contentsPatch = tags.get(contentsPatchTag);

        if(contentsPatch == null){
            return;
        }

        String[] patchNames = contentsPatch.split(";");
        for(String patchName : patchNames){
            String patchJson = tags.get(patchSuffix + patchName);

            if(patchJson == null){
                continue;
            }

            Patch patch = patchPool.obtain();
            patch.set(patchName, patchJson);
            patchSeq.add(patch);
        }

//        Log.info("Read patches: @", patchSeq.toString(";", p -> p.name));
    }

    private void savePatch(StringMap tags){
        String contentsPatch = patchSeq.toString(";", p -> p.name);

        tags.put(contentsPatchTag, contentsPatch);
        for(Patch patch : patchSeq){
            tags.put(patchSuffix + patch.name, patch.json);
        }

        patchPool.freeAll(patchSeq);
        patchSeq.clear();
//        Log.info("Save patches: @", contentsPatch);
    }

    private void rebuildCont(){
        Table table = patchContainer;
        table.clearChildren();

        table.table(Styles.grayPanel, title -> {
            title.add("@patch-manager").pad(8f).expandX().left();
        }).pad(8f).growX();

        table.row();

        rebuildPatchTable();
        table.pane(patchTable).scrollY(false).pad(8f).grow();

        table.row();

        table.table(Styles.grayPanel, buttonTable -> {
            buttonTable.defaults().minWidth(130f).height(40f).growX();

            buttonTable.button("@add-patch", Icon.add, Styles.cleart, () -> {
                String name = findPatchName();
                Patch patch = patchPool.obtain().set(name, "{}");

                patchSeq.add(patch);
                rebuildPatchTable();
            });

            buttonTable.button("@import-patch", Icon.add, Styles.cleart, () -> {
                String text = Core.app.getClipboardText();

                try{
                    new JsonReader().parse(text);

                    String name = findPatchName();
                    Patch patch = patchPool.obtain().set(name, text);

                    patchSeq.add(patch);
                    rebuildPatchTable();

                    UIUtils.showInfoToast("@import-patch.succeed", 3.0f, 4);
                }catch(Exception ignored){
                    UIUtils.showInfoToast("@import-patch.failed", 3.0f, 4);
                }
            }).disabled(b -> Core.app.getClipboardText() != null && Core.app.getClipboardText().isEmpty());
        }).pad(8f).padTop(4f).growX();
    }

    private void rebuildPatchTable(){
        patchTable.clearChildren();

        int index = 0;
        for(Patch patch : patchSeq){
            patchTable.table(MinTex.whiteuiRegion, t -> {
                t.field(patch.name, text -> patch.name = text).growX();

                t.table(buttons -> {
                    buttons.defaults().size(32f).pad(4f);

                    buttons.button(Icon.cancelSmall, Styles.clearNonei, () -> {
                        patchSeq.remove(patch);
                        rebuildPatchTable();
                    }).with(b -> {
                        ElementUtils.addTooltip(b, "@patch.remove", true);
                    });

                    buttons.button(Icon.copySmall, Styles.clearNonei, () -> {
                        Core.app.setClipboardText(patch.json);
                        UIUtils.showInfoToast("[green]Copy: []" + patch.name, 3.0f, 4);
                    }).with(b -> {
                        ElementUtils.addTooltip(b, "@patch.copy", true);
                    });

                    buttons.button(Icon.editSmall, Styles.clearNonei, () -> {
                        editor.edit(patch);
                    }).with(b -> {
                        ElementUtils.addTooltip(b, "@patch.edit", true);
                    });
                }).pad(4f);

                t.image().width(4f).color(Color.darkGray).growY().right();
                t.row();
                Cell<?> horizontalLine = t.image().height(4f).color(Color.darkGray).growX();
                horizontalLine.colspan(t.getColumns());
            }).pad(8f).color(EPalettes.gray);

            if(++index % 3 == 0){
                patchTable.row();
            }
        }
    }

    private String findPatchName(){
        String base = "Patch";

        int[] index = {0};
        while(patchSeq.contains(p -> p.name.equals(base + index[0]))){
            index[0]++;
        }
        return base + index[0];
    }

    public static class Patch implements Poolable{
        public String name;
        public String json;

        public Patch set(String name, String json){
            this.name = name;
            this.json = json;
            return this;
        }

        @Override
        public void reset(){
            name = null;
            json = null;
        }

        public JsonValue getJsonData(){
            return new JsonReader().parse(json);
        }
    }
}
