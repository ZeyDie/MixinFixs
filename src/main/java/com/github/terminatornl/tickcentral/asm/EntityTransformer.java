//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.github.terminatornl.tickcentral.asm;

import com.github.terminatornl.tickcentral.TickCentral;
import com.github.terminatornl.tickcentral.api.ClassDebugger;
import com.github.terminatornl.tickcentral.api.ClassSniffer;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.Map;

public class EntityTransformer implements IClassTransformer {
    public static final String ENTITY_CLASS_NON_OBF = "net.minecraft.entity.Entity";
    public static final String ENTITY_CLASS_OBF;
    public static final String TRUE_ONUPDATE_TICK_NAME = "TickCentral_TrueOnUpdateTick";
    public static Map.Entry<String, String> ONUPDATE_TICK_METHOD;

    public EntityTransformer() {
    }

    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        //TODO ZeyCodeStart
        if (name.startsWith("com/zeydie/mixinfixs/mixin") || transformedName.startsWith("com.zeydie.mixinfixs.mixin"))
            return basicClass;
        //TODO ZeyCodeEnd

        try {
            if (ONUPDATE_TICK_METHOD == null) {
                ClassNode classNode = (ClassNode) ClassSniffer.performOnSource(ENTITY_CLASS_OBF, (k) -> {
                    ClassNode node = new ClassNode();
                    k.accept(node, 0);
                    return node;
                });
                String targetTargetMethod = null;
                Iterator var6 = classNode.methods.iterator();

                MethodNode node;
                while (var6.hasNext()) {
                    node = (MethodNode) var6.next();
                    if (node.desc.equals("()V") && Utilities.usesConstant(node.instructions, "entityBaseTick")) {
                        TickCentral.LOGGER.info("Found onEntityUpdate as " + FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(classNode.name, node.name, node.desc) + FMLDeobfuscatingRemapper.INSTANCE.mapDesc(node.desc));
                        targetTargetMethod = node.name;
                        break;
                    }
                }

                if (targetTargetMethod == null) {
                    TickCentral.LOGGER.fatal("Unable to find the entity onEntityUpdate method! (Stage 1)");
                    FMLCommonHandler.instance().exitJava(1, false);
                    throw new RuntimeException();
                }

                var6 = classNode.methods.iterator();

                while (var6.hasNext()) {
                    node = (MethodNode) var6.next();
                    if (Utilities.usesMethodInstruction(182, classNode.name, targetTargetMethod, "()V", node.instructions)) {
                        ONUPDATE_TICK_METHOD = new AbstractMap.SimpleEntry(FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(classNode.name, node.name, node.desc), FMLDeobfuscatingRemapper.INSTANCE.mapDesc(node.desc));
                        TickCentral.LOGGER.info("Found onUpdate as " + (String) ONUPDATE_TICK_METHOD.getKey() + (String) ONUPDATE_TICK_METHOD.getValue());
                    }
                }

                if (ONUPDATE_TICK_METHOD == null) {
                    TickCentral.LOGGER.fatal("Unable to find the entity onUpdate method! (Stage 2)");
                    FMLCommonHandler.instance().exitJava(1, false);
                    throw new RuntimeException();
                }
            }

            if (basicClass == null) {
                return null;
            } else {
                ClassReader reader = new ClassReader(basicClass);
                if (!ClassSniffer.isInstanceOf(reader, ENTITY_CLASS_OBF)) {
                    return basicClass;
                } else {
                    boolean dirty = false;
                    String className = reader.getClassName();
                    if (TickCentral.CONFIG.DEBUG) {
                        TickCentral.LOGGER.info("Entity found: " + className + " (" + transformedName + ")");
                    }

                    ClassNode classNode = new ClassNode();
                    reader.accept(classNode, 0);
                    MethodNode newUpdateTick = null;
                    Iterator var9 = classNode.methods.iterator();

                    MethodNode method;
                    while (var9.hasNext()) {
                        method = (MethodNode) var9.next();
                        if ((method.access & 1024) == 0 && ((String) ONUPDATE_TICK_METHOD.getKey()).equals(method.name) && ((String) ONUPDATE_TICK_METHOD.getValue()).equals(method.desc)) {
                            newUpdateTick = Utilities.CopyMethodAppearanceAndStripOtherFromFinal(method);
                            newUpdateTick.instructions = new InsnList();
                            newUpdateTick.instructions.add(new FieldInsnNode(178, "com/github/terminatornl/tickcentral/api/TickHub", "INTERCEPTOR", "Lcom/github/terminatornl/tickcentral/api/TickInterceptor;"));
                            newUpdateTick.instructions.add(new VarInsnNode(25, 0));
                            newUpdateTick.instructions.add(new MethodInsnNode(185, "com/github/terminatornl/tickcentral/api/TickInterceptor", "redirectOnUpdate", "(Lnet/minecraft/entity/Entity;)V", true));
                            newUpdateTick.instructions.add(new InsnNode(177));
                            method.name = "TickCentral_TrueOnUpdateTick";
                            dirty = true;
                        }
                    }

                    if (newUpdateTick != null) {
                        classNode.methods.add(newUpdateTick);
                    }

                    for (var9 = classNode.methods.iterator(); var9.hasNext(); dirty = Utilities.convertSuperInstructions((String) ONUPDATE_TICK_METHOD.getKey(), (String) ONUPDATE_TICK_METHOD.getValue(), "TickCentral_TrueOnUpdateTick", method.instructions) || dirty) {
                        method = (MethodNode) var9.next();
                        dirty = Utilities.convertTargetInstruction(className, (String) ONUPDATE_TICK_METHOD.getKey(), (String) ONUPDATE_TICK_METHOD.getValue(), className, "TickCentral_TrueOnUpdateTick", method.instructions) || dirty;
                    }

                    if (dirty) {
                        return ClassDebugger.WriteClass(classNode, transformedName);
                    } else {
                        return basicClass;
                    }
                }
            }
        } catch (Throwable var11) {
            TickCentral.LOGGER.fatal("An error has occurred", var11);
            FMLCommonHandler.instance().exitJava(1, false);
            throw new RuntimeException(var11);
        }
    }

    static {
        ENTITY_CLASS_OBF = FMLDeobfuscatingRemapper.INSTANCE.unmap("net.minecraft.entity.Entity".replace(".", "/"));
        ONUPDATE_TICK_METHOD = null;
    }
}
