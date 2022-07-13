package com.dianpingSeekNumber;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Module;
import com.github.unidbg.debugger.Debugger;
import com.github.unidbg.debugger.DebuggerType;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.dvm.AbstractJni;
import com.github.unidbg.linux.android.dvm.DalvikModule;
import com.github.unidbg.linux.android.dvm.StringObject;
import com.github.unidbg.linux.android.dvm.VM;
import com.github.unidbg.linux.android.dvm.array.ByteArray;
import com.github.unidbg.memory.Memory;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class seekNumber extends AbstractJni {
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;
    seekNumber(){
        emulator = AndroidEmulatorBuilder.for32Bit().build(); // 创建模拟器实例，要模拟32位或者64位，在这里区分
        final Memory memory = emulator.getMemory(); // 模拟器的内存操作接口
        memory.setLibraryResolver(new AndroidResolver(23)); // 设置系统类库解析
        vm=emulator.createDalvikVM(new File("unidbg-android/src/test/resources/dianping/点评10.29.3.apk"));
        vm.setVerbose(true); // 设置是否打印Jni调用细节
        DalvikModule dm = vm.loadLibrary(new File("unidbg-android/src/test/resources/dianping/libdpobj.so"), true);
        module = dm.getModule();
        vm.setJni(this);
        Debugger MyDbg = emulator.attach(DebuggerType.CONSOLE);
        MyDbg.addBreakPoint(module.base + 0xe0a+1);
        dm.callJNI_OnLoad(emulator);

    }

    public long callSeek() throws IOException {
        String res=FileUtils.readFileToString(new File("unidbg-android/src/test/java/com/dianpingSeekNumber/bytesFile"),"UTF-8");
//        System.out.println(res);
        String res2=res;
        JSONArray res3=JSONObject.parseArray(res2);
        byte[] bytes = new byte[res3.size()];
        for (int i=0;i<res3.size();i++){
            int one= (int) res3.get(i);
            if (one>=128){
                one=one-256;
            }
            bytes[i]=Byte.parseByte(String.valueOf(one));
        }
        List<Object> list = new ArrayList<>(10);
        list.add(vm.getJNIEnv()); // 第一个参数是env
        list.add(0); // 第二个参数，实例方法是jobject，静态方法是jclazz，直接填0，
        list.add(vm.addLocalObject(new ByteArray(vm,bytes)));
        list.add(0);
        list.add(bytes.length);
        list.add(11012);
        Number number = module.callFunction(emulator, 0xEDA+1, list.toArray())[0];
//        String result = vm.getObject(number.intValue()).getValue().toString();
        System.out.println("result:"+number.intValue());
        return number.longValue();

    }
    public static void main(String[] args) throws IOException {
        seekNumber xc=new seekNumber();
        xc.callSeek();
        System.out.println(1);



    }
//    public int nativeSeekMember(){
//        List<Object> list = new ArrayList<>(10);
//        list.add(vm.getJNIEnv()); // 第一个参数是env
//        list.add(0); // 第二个参数，实例方法是jobject，静态方法是jclazz，直接填0，一般用不到。
////        new ByteArray(vm,)
//    }
}
