package com.xhs;

import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Emulator;
import com.github.unidbg.Module;
import com.github.unidbg.file.FileResult;
import com.github.unidbg.file.IOResolver;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.file.SimpleFileIO;
import com.github.unidbg.memory.Memory;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class xhs_my extends AbstractJni implements IOResolver {
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;
    xhs_my(){
        emulator = AndroidEmulatorBuilder.for32Bit().setProcessName("com.xhs").build(); // 创建模拟器实例，要模拟32位或者64位，在这里区分
        final Memory memory = emulator.getMemory(); // 模拟器的内存操作接口
        memory.setLibraryResolver(new AndroidResolver(23)); // 设置系统类库解析
        vm = emulator.createDalvikVM(new File("D:\\unidbg-master-11-30\\unidbg-master\\unidbg-android\\src\\test\\resources\\xhs\\xhs6.97.apk"));
        vm.setVerbose(true);

        DalvikModule dm = vm.loadLibrary(new File("D:\\unidbg-master-11-30\\unidbg-master\\unidbg-android\\src\\test\\resources\\xhs\\libshield.so"), true);

        vm.setJni(this);
        module = dm.getModule();
        emulator.getSyscallHandler().addIOResolver(this);
        System.out.println("call JNIOnLoad");
        dm.callJNI_OnLoad(emulator);
    }

    public static void main(String[] args) {
        xhs_my test = new xhs_my();
        test.callinitializeNative();
    }
    // 第一个初始化函数
    public void callinitializeNative(){
        List<Object> list = new ArrayList<>(10);
        list.add(vm.getJNIEnv()); // 第一个参数是env
        list.add(0); // 第二个参数，实例方法是jobject，静态方法是jclazz，直接填0，一般用不到。
        module.callFunction(emulator, 0x6c11d, list.toArray());
    };

    @Override
    public void callVoidMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
        switch (signature){
            case "com/xingin/shield/http/ShieldLogger->nativeInitializeStart()V":{
                return;
            }
        }
        super.callVoidMethodV(vm, dvmObject, signature, vaList);
    }
    @Override
    public int getIntField(BaseVM vm, DvmObject<?> dvmObject, String signature) {
        switch (signature){
            case "android/content/pm/PackageInfo->versionCode:I":{
                return 6970181;
            }
        }
        return super.getIntField(vm, dvmObject, signature);
    }
    @Override
    public DvmObject<?> callStaticObjectMethodV(BaseVM vm, DvmClass dvmClass, String signature, VaList vaList) {
        switch (signature){
            case "java/nio/charset/Charset->defaultCharset()Ljava/nio/charset/Charset;":{
                return vm.resolveClass("java/nio/charset/Charset").newObject(Charset.defaultCharset());
            }
        }
        return super.callStaticObjectMethodV(vm, dvmClass, signature, vaList);
    }
    @Override
    public int getStaticIntField(BaseVM vm, DvmClass dvmClass, String signature) {
        switch (signature){
            case "com/xingin/shield/http/ContextHolder->sAppId:I":{
                return -319115519;
            }
        }
        return super.getStaticIntField(vm, dvmClass, signature);
    }
    @Override
    public DvmObject<?> getStaticObjectField(BaseVM vm, DvmClass dvmClass, String signature){
        switch (signature){
            case "com/xingin/shield/http/ContextHolder->sLogger:Lcom/xingin/shield/http/ShieldLogger;":{
                return vm.resolveClass("com/xingin/shield/http/ShieldLogger").newObject(signature);
            }
            case "com/xingin/shield/http/ContextHolder->sDeviceId:Ljava/lang/String;":{
                return new StringObject(vm, "1d41ebdc-86dd-33ea-9ceb-e9210babd74e");
            }
        }
        return super.getStaticObjectField(vm, dvmClass, signature);
    }
    @Override
    public FileResult resolve(Emulator emulator, String pathname, int oflags) {
        System.out.println("fuckpath:"+pathname);
        return null;
    }
}
