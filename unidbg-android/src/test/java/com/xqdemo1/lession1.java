package com.xqdemo1;
import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.AbstractJni;
import com.github.unidbg.linux.android.dvm.DalvikModule;
import com.github.unidbg.linux.android.dvm.VM;
import com.github.unidbg.memory.Memory;
import java.io.File;

public class lession1 extends AbstractJni {
    private final AndroidEmulator emulator;
    private final VM vm;
    lession1(){
        emulator = AndroidEmulatorBuilder.for32Bit().build(); // 创建模拟器实例，要模拟32位或者64位，在这里区分
        final Memory memory = emulator.getMemory(); // 模拟器的内存操作接口
        memory.setLibraryResolver(new AndroidResolver(23)); // 设置系统类库解析
        vm=emulator.createDalvikVM(new File("unidbg-android/src/test/resources/lession1/xc 8-38-2.apk"));
        vm.setVerbose(true); // 设置是否打印Jni调用细节
        DalvikModule dm = vm.loadLibrary(new File("unidbg-android/src/test/resources/lession1/libscmain.so"), true);
        vm.setJni(this);
        dm.callJNI_OnLoad(emulator);

    }

    public static void main(String[] args) {
        lession1 xc=new lession1();
    }
}
