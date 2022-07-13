package com.tmall.wireless;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Emulator;
import com.github.unidbg.Module;
import com.github.unidbg.arm.backend.Backend;
import com.github.unidbg.arm.backend.CodeHook;
import com.github.unidbg.arm.backend.UnHook;
import com.github.unidbg.arm.context.Arm32RegisterContext;
import com.github.unidbg.debugger.BreakPointCallback;
import com.github.unidbg.file.FileResult;
import com.github.unidbg.file.IOResolver;
import com.github.unidbg.file.linux.AndroidFileIO;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.SystemPropertyHook;
import com.github.unidbg.linux.android.SystemPropertyProvider;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.array.ArrayObject;
import com.github.unidbg.linux.android.dvm.wrapper.DvmBoolean;
import com.github.unidbg.linux.android.dvm.wrapper.DvmInteger;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.virtualmodule.android.AndroidModule;
import com.sun.jna.Pointer;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class MainActivity extends AbstractJni implements IOResolver<AndroidFileIO> {
    private final AndroidEmulator emulator;
    private final VM vm;

    public String methodSign = "doCommandNative(I[Ljava/lang/Object;)Ljava/lang/Object;";
    public String packageName = "com.tmall.wireless";
    public DvmClass JNICLibrary;
    public DvmObject<?> context;
    public DvmObject<?> ret;
    public String dataAppPath = "/data/app/com.tmall.wireless-jd3waLD9hTRNT6FDdzlKyw==";
    public String APK_INSTALL_PATH = dataAppPath + "/base.apk";
    public File APK_FILE = new File("unidbg-android/src/test/resources/tmall/com.tmall.wireless-v8.11.0.apk");
    public String SGMANAGER_DATA2;
    public long slot;
    public long libsgmainsoBase;
    public long libsecbodyBase;
    public LinkedHashMap<String,String> aesres=new LinkedHashMap<>();
    public long libsgmainsoSize;
    public long libsecbodySize;

    public String path = "unidbg-android/src/test/resources/tmall/";
    public String libsgmain = path + "libsgmainso-6.4.156.so";
    public File libsgmainso = new File(libsgmain);
    public String libsgsecuritybody = path + "libsgsecuritybodyso-6.4.90.so";
    public File libsgsecuritybodyso = new File(libsgsecuritybody);
    public String libsgavmp = path + "libsgavmpso-6.4.34.so";
    public File libsgavmpso = new File(libsgavmp);

    private static AndroidEmulator createARMEmulator() {
        return AndroidEmulatorBuilder
                .for32Bit()
                .setRootDir(new File("unidbg-android/src/test/resources/tmall/com.tmall.wireless"))
                .setProcessName("com.tmall.wireless")
                .build();
    }

    MainActivity(){
        emulator = createARMEmulator();
        Memory memory = emulator.getMemory();
        memory.setLibraryResolver(new AndroidResolver(23));
        vm = emulator.createDalvikVM(APK_FILE);
        SystemPropertyHook systemPropertyHook = new SystemPropertyHook(emulator);
        systemPropertyHook.setPropertyProvider(new SystemPropertyProvider() {
            @Override
            public String getProperty(String key) {
//                System.out.println("fuckkey:"+key);
                switch (key){
                    case "ro.build.fingerprint":{
                        return "ro.build.fingerprint=Xiaomi/dipper/dipper:9/PKQ1.180729.001/9.7.4:user/release-keys";
                    }
                    case "ro.build.version.sdk":{
                        return "23";
                    }
                    case "ro.product.cpu.abi":{
                        return "arm64-v8a";
                    }
//                    case "persist.sys.dalvik.vm.lib.2":{
//                        return "libart.so";
//                    }
//                    case "persist.sys.dalvik.vm.lib":{
//                        return "libart.so";
//                    }
                }
                return "";
            };
        });
        memory.addHookListener(systemPropertyHook);
        emulator.getSyscallHandler().addIOResolver(this);
        emulator.getSyscallHandler().setVerbose(true);
        vm.setVerbose(true); // 设置是否打印Jni调用细节
        vm.setJni(this);
        new AndroidModule(emulator, vm).register(memory);
//        new UserEnvModule(emulator).register(memory);

        try {
            SGMANAGER_DATA2=FileUtils.readFileToString(new File(path + "/com.tmall.wireless/data/user/0/com.tmall.wireless/files/SGMANAGER_DATA2"),"UTF-8");
        }catch (Exception e){
            System.exit(1);
        }
        JNICLibrary = vm.resolveClass("com/taobao/wireless/security/adapter/JNICLibrary");
        context = vm.resolveClass("android/content/Context").newObject(null);

//        40742300
//        emulator.traceRead(0x40419700L,0x40419700L+16);
        emulator.traceRead(0x40742300L,0x40742300L+16);
    }

    public static void main(String[] args) throws IOException {
        MainActivity mainActivity = new MainActivity();

        mainActivity.initMain();
        mainActivity.initSecurityBody();
        mainActivity.initAvMp();
//        tm2.getXSign();
        mainActivity.getMiniWua();
        mainActivity.destroy();
    }

    public void HookByConsoleDebugger(){
        emulator.attach().addBreakPoint(libsgmainsoBase+0x9EF50+1, new BreakPointCallback() {
            @Override
            public boolean onHit(Emulator<?> emulator, long address) {
                Arm32RegisterContext context = emulator.getContext();
                int r1=context.getR1Int();
                String r116=Integer.toHexString(r1);

                Pointer r1p=context.getR1Pointer();

                byte[] r1a=r1p.getByteArray(0,122);

                try {
                    FileWriter fileWriter=new FileWriter("unidbg-android/src/test/java/com/tmall/wireless/log/extandkey.txt",true);
                    fileWriter.write(r116+"\n");
                    fileWriter.write(hex(r1a)+"\n");
                    fileWriter.write("========\n");
                    fileWriter.close();
                }catch (Exception e){
                    System.out.println(e);
                }
                return true;
            }
        });
    }

    public void getXSign() {
        Map<String, String> map = new HashMap<>();
//        map.put("INPUT", "Yl/IcPmQsTYDAFntrZADj14x&&&23181017&b322e589530f672eceb3f0fc96642a2f&1650510408&mtop.taobao.detail.getrecomservice&1.0&&231200@tmall_android_8.11.0&AmT4AwKvHELBOebleyQn8MJxiiREKuIjVb4aCKqsyI7R&&&1051&&&&&&&");
        map.put("INPUT", "aaaaa");
        DvmObject<?> ret = JNICLibrary.callStaticJniMethodObject(
                emulator, methodSign, 10401,
                new ArrayObject(
                        vm.resolveClass("java/util/HashMap").newObject(map),
                        new StringObject(vm, "23181017"),
                        DvmInteger.valueOf(vm, 7),
                        null,
                        DvmBoolean.valueOf(vm, true)
                ));

        System.out.println("xiayu, getXSign.ret-10401: " + ret.getValue().toString());
    }

    public static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            int decimal = (int) aByte & 0xff;               // bytes widen to int, need mask, prevent sign extension
            // get last 8 bits
            String hex = Integer.toHexString(decimal);
            if (hex.length() % 2 == 1) {                    // if half hex, pad with zero, e.g \t
                hex = "0" + hex;
            }
            result.append(hex);
        }
        return result.toString();
    }

    public void getMiniWua() {
//        Debugger MyDbg = emulator.attach(DebuggerType.CONSOLE);
//        MyDbg.addBreakPoint(lissecbodyBase + 0xc4a3);
//        MyDbg.addBreakPoint(lissecbodyBase + 0xb1a7);
//        MyDbg.addBreakPoint(libsgmainsoBase + 0x9EF50+1);
//        HookByConsoleDebugger();
//        emulator.traceCode(libsgmainsoBase,libsgmainsoBase+libsgmainsoSize);
//        emulator.traceCode(lissecbodyBase,lissecbodyBase+lissecbodySize);
//        emulator.traceWrite(0x402d1100L,0x402d1100L+16);
//        emulator.traceRead(0x402d1100L,0x402d1100L+32);
//        emulator.traceWrite(0xbfffefd4L,0xbfffefd4L);
//        emulator.traceWrite(0xbffff000L,0xbffff000L+4);
        Map<String, String> map = new HashMap<>();
        //map.put("INPUT", "Yl/IcPmQsTYDAFntrZADj14x&&&23181017&b322e589530f672eceb3f0fc96642a2f&1650510408&mtop.taobao.detail.getrecomservice&1.0&&231200@tmall_android_8.11.0&AmT4AwKvHELBOebleyQn8MJxiiREKuIjVb4aCKqsyI7R&&&1051&&&&&&&");
        map.put("INPUT", "aaaaa");
        DvmObject<?> ret = JNICLibrary.callStaticJniMethodObject(
                emulator, methodSign, 20102,
                new ArrayObject(new StringObject(vm, "1651917030"),
                        new StringObject(vm, "23181017"),
                        DvmInteger.valueOf(vm, 8),
                        null,
                        new StringObject(vm, "pageId=&pageName="),
                        DvmInteger.valueOf(vm, 0)
                ));

        System.out.println("xiayu, getMiniWua.ret-20102: " + ret.getValue().toString());
    }

    public void inlineHookAes() {
        emulator.getBackend().hook_add_new(new CodeHook() {
            @Override
            public void hook(Backend backend, long address, int size, Object user) {
                if (address == (libsgmainsoBase + 0x999dc)) {
                    Arm32RegisterContext ctx = emulator.getContext();

                    String aesIv = QLUtils.bytesToHexString(ctx.getR9Pointer().getByteArray(0, 16));
                    if (Objects.equals(aesIv, "35323861383066383539356433316563")) {
                        emulator.getBackend().mem_write(ctx.getR9Long(), "cdfc7e0a1154b409".getBytes());
                    }

                    System.out.println("aes decrypt iv: " + QLUtils.bytesToHexString(
                            ctx.getR9Pointer().getByteArray(0, 16)));
                }
            }

            @Override
            public void onAttach(UnHook unHook) {
            }

            @Override
            public void detach() {
            }
        }, libsgmainsoBase + 0x999dc, libsgmainsoBase + 0x999dc, null);

        emulator.getBackend().hook_add_new(new CodeHook() {
            @Override
            public void hook(Backend backend, long address, int size, Object user) {
                if (address == (libsgmainsoBase + 0x999b4)) {
                    Arm32RegisterContext ctx = emulator.getContext();

                    int r2 = ctx.getR2Int();
                    String aesKey = QLUtils.bytesToHexString(ctx.getR1Pointer().getByteArray(0, r2));
                    //303033303831383930323831383130303834303631323566
                    //35323861383066383539356433316563
                    //38366531303163623131376533633535
                    if (Objects.equals(aesKey, "35323861383066383539356433316563")) {
                          emulator.getBackend().mem_write(ctx.getR1Long(), "cdfc7e0a1154b409".getBytes());
                        // HHnB_+OC/lyDVlzWduiC0aqq1G2IBnIlJfRIyBzthSwthezU=
                        // HHnB_+OC/lyDVlzWduiC0aqq1G2IBnIlJfRIyBzthSwthezU=
                        // HHnB_ehVppJ1w4h646ckuMQCQN0Qn97eB4TeBm5g47L2LMbA=
                    }

                    System.out.println("aes decrypt key: " + QLUtils.bytesToHexString(
                            ctx.getR1Pointer().getByteArray(0, r2))
                    );
                }
            }

            @Override
            public void onAttach(UnHook unHook) {
            }

            @Override
            public void detach() {
            }
        }, libsgmainsoBase + 0x999b4, libsgmainsoBase + 0x999b4, null);
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte aByte : bytes) {
            String hex = Integer.toHexString(aByte & 0xFF);
            if (hex.length() < 2) {
                sb.append(0);
            }
            sb.append(hex);
        }
        return sb.toString();
    }
    public void initMain() {
        DalvikModule dm = vm.loadLibrary(libsgmainso, true);
        Module module = dm.getModule();
        libsgmainsoBase = module.base;
        libsgmainsoSize = module.size;
        System.out.println("libsgmainso base "+module.base);
//        Debugger MyDbg = emulator.attach(DebuggerType.CONSOLE);
//        MyDbg.addBreakPoint(libsgmainsoBase + 0x999FA  +1);
        HookByConsoleDebugger();
        dm.callJNI_OnLoad(emulator);

        inlineHookAes();

        ret = JNICLibrary.callStaticJniMethodObject(
                emulator, methodSign, 10101,
                new ArrayObject(
                        context,
                        DvmInteger.valueOf(vm, 3),
                        new StringObject(vm, ""),
                        new StringObject(vm, "/data/user/0/" + packageName + "/app_SGLib"),
                        new StringObject(vm, "")
                ));
        System.out.println("xiayu, initMain.ret-10101: " + ret.getValue().toString());
//
        ret = JNICLibrary.callStaticJniMethodObject(
                emulator, methodSign, 10102,
                new ArrayObject(
                        new StringObject(vm, "main"),
                        new StringObject(vm, "6.4.156"),
                        new StringObject(vm, "/data/user/0/com.tmall.wireless/app_SGLib/app_1650422279/main")
                ));
        System.out.println("xiayu, initMain.ret-10102: " + ret.getValue().toString());
    }

    public void initSecurityBody() {
        DalvikModule securityBody = vm.loadLibrary(libsgsecuritybodyso, true);
        Module module = securityBody.getModule();
        libsecbodyBase=module.base;
        libsecbodySize=module.size;
        System.out.println("securityBody base "+module.base);
        securityBody.callJNI_OnLoad(emulator);
        ret = JNICLibrary.callStaticJniMethodObject(
                emulator, methodSign, 10102,
                new ArrayObject(
                        new StringObject(vm, "securitybody"),
                        new StringObject(vm, "6.4.90"),
                        new StringObject(vm, "/data/user/0/com.tmall.wireless/app_SGLib/app_1650422279/main/libsgsecuritybodyso-6.4.90.so")
                ));
        System.out.println("xiayu, initSecurityBody.ret-10102: " + ret.getValue().toString());
    }

    public void initAvMp() {
        DalvikModule avMp = vm.loadLibrary(libsgavmpso, true);
        Module module = avMp.getModule();
        System.out.println("avMp base "+module.base);
        avMp.callJNI_OnLoad(emulator);
        ret = JNICLibrary.callStaticJniMethodObject(
                emulator, methodSign, 10102,
                new ArrayObject(
                        new StringObject(vm, "avmp"),
                        new StringObject(vm, "6.4.34"),
                        new StringObject(vm, "/data/user/0/com.tmall.wireless/app_SGLib/app_1650422279/main/libsgavmpso-6.4.34.so")
                ));
        System.out.println("xiayu, initAvMp.ret-10102: " + ret.getValue().toString());
    }

    public void destroy() throws IOException {
        emulator.close();
    }

    @Override
    public FileResult<AndroidFileIO> resolve(Emulator<AndroidFileIO> emulator, String pathname, int oflags) {
        System.out.println("resolve.pathname: " + pathname);
//        FileResult.success()
        if (pathname.equals(APK_INSTALL_PATH)){
            return FileResult.success(emulator.getFileSystem().createSimpleFileIO(APK_FILE,oflags,pathname));
        }
        return null;
    }

    @Override
    public DvmObject<?> callStaticObjectMethod(BaseVM vm, DvmClass dvmClass, String signature, VarArg varArg) {
        switch (signature){
            case "com/alibaba/wireless/security/mainplugin/SecurityGuardMainPlugin->getMainPluginClassLoader()Ljava/lang/ClassLoader;":{
                return vm.resolveClass("java/lang/ClassLoader").newObject(signature);
            }
            case "com/taobao/wireless/security/adapter/common/SPUtility2->readFromSPUnified(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;":{
                String key0= (String) varArg.getObjectArg(0).getValue();
                System.out.println("0==="+key0);
                String key1= (String) varArg.getObjectArg(1).getValue();
                System.out.println("1==="+key1);
                if(varArg.getObjectArg(2) ==null){
                    System.out.println("2===null");
                }else {
                    System.out.println("2==="+varArg.getObjectArg(2).getValue());
                }
                JSONObject ss = JSON.parseObject(SGMANAGER_DATA2);
                String temp=ss.getString(key0+"_"+key1);
                return temp==null?varArg.getObjectArg(2):new StringObject(vm,temp);
            }
            case "com/taobao/wireless/security/adapter/datacollection/DeviceInfoCapturer->doCommandForString(I)Ljava/lang/String;":{
                int ar1=varArg.getIntArg(0);
                switch (ar1){
                    case 122:{
                        return new StringObject(vm,"com.tmall.wireless");
                    }
                    case 11:{
                        return new StringObject(vm,"0");
                    }
                    case 114:{
                        return new StringObject(vm,"1080*2028");
                    }
                    case 115:{
                        return new StringObject(vm,"118982303744");
                    }
                    default:{
                        System.out.println("doCommandForString 参数是:"+ar1);
                        System.exit(1111111);
                    }
                }
            }
            case "com/alibaba/wireless/security/securitybody/LifeCycle->getCurrentActivity()Landroid/app/Activity;":{
                return vm.resolveClass("android/app/Activity").newObject(null);
            }
        }
        return super.callStaticObjectMethod(vm, dvmClass, signature, varArg);
    }

    @Override
    public DvmObject<?> callObjectMethod(BaseVM vm, DvmObject<?> dvmObject, String signature, VarArg varArg) {
        switch (signature){
            case "android/content/Context->getPackageCodePath()Ljava/lang/String;":{
                return new StringObject(vm,APK_INSTALL_PATH);
            }
            case "android/content/Context->getFilesDir()Ljava/io/File;":{
                return vm.resolveClass("java/io/File");
            }
            case "java/lang/Class->getAbsolutePath()Ljava/lang/String;":{
                return new StringObject(vm,"/data/user/0/" + packageName + "/files");
            }
            case "java/util/HashMap->keySet()Ljava/util/Set;":{
                HashMap map= (HashMap) dvmObject.getValue();
                return vm.resolveClass("java/util/Set").newObject(map.keySet());
            }
            case "java/util/Set->toArray()[Ljava/lang/Object;":{
                Set set= (Set) dvmObject.getValue();
                Object[] array=set.toArray();
                DvmObject[] objects=new DvmObject[array.length];
                for (int i = 0; i < array.length; i++) {
                    if (array[i] instanceof String) {
                        objects[i] = new StringObject(vm, (String) array[i]);
                    } else {
                        throw new IllegalStateException("array=" + array[i]);
                    }
                }
                return new ArrayObject(objects);
            }
            case "java/util/HashMap->get(Ljava/lang/Object;)Ljava/lang/Object;":{
                HashMap map= (HashMap) dvmObject.getValue();
                Object key=varArg.getObjectArg(0).getValue();
                Object obj=map.get(key);
                if (obj instanceof String) {
                    return new StringObject(vm, (String) obj);
                } else {
                    throw new IllegalStateException("array=" + obj);
                }
            }
            case "android/app/Activity->getWindow()Landroid/view/Window;":{
                return vm.resolveClass("android/view/Window").newObject(null);
            }
            case "android/view/Window->getDecorView()Landroid/view/View;":{
                return vm.resolveClass("android/view/View").newObject(null);
            }
            case "java/lang/Class->getDeclaredMethod(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;":{
                String methodName= (String) varArg.getObjectArg(0).getValue();
                return vm.resolveClass("java/lang/reflect/Method").newObject(methodName);
            }
            case "java/lang/reflect/Method->invoke(Ljava/lang/Object;[Ljava/lang/Object;)Ljava/lang/Object;":{
                DvmObject[] args;
                args= (DvmObject[]) varArg.getObjectArg(1).getValue();
                if(args[0].getValue().equals("mAttachInfo")){
//                    return vm.resolveClass("android/view/View").newObject(null);
                    return null;
                }
                System.exit(2222);
//                return null;

            }
        }
        return super.callObjectMethod(vm, dvmObject, signature, varArg);
    }

    @Override
    public DvmObject<?> getObjectField(BaseVM vm, DvmObject<?> dvmObject, String signature) {
        if ("android/content/pm/ApplicationInfo->nativeLibraryDir:Ljava/lang/String;".equals(signature)) {
            return new StringObject(vm, dataAppPath + "/lib/arm");
        }
        return super.getObjectField(vm, dvmObject, signature);
    }

    @Override
    public DvmObject<?> newObject(BaseVM vm, DvmClass dvmClass, String signature, VarArg varArg){
        switch (signature){
            case "com/alibaba/wireless/security/open/SecException-><init>(Ljava/lang/String;I)V":{
                StringObject msg=varArg.getObjectArg(0);
                int value=varArg.getIntArg(1);
                System.out.println("SecException========"+msg);
                System.out.println("SecException========"+value);
                return dvmClass.newObject(msg.getValue()+"["+value+"]");
            }
            case "java/lang/Integer-><init>(I)V": {
                return DvmInteger.valueOf(vm, varArg.getIntArg(0));
            }
        }
        return super.newObject(vm, dvmClass, signature, varArg);
    }

    @Override
    public void callStaticVoidMethod(BaseVM vm, DvmClass dvmClass, String signature, VarArg varArg){
        switch (signature){
            case "com/alibaba/wireless/security/securitybody/LifeCycle->registerCallBack()V":
            case "com/alibaba/wireless/security/open/edgecomputing/ECMiscInfo->registerAppLifeCyCleCallBack()V":{
                return;
            }
        }
        super.callStaticVoidMethod(vm, dvmClass, signature, varArg);
    }

    @Override
    public int callStaticIntMethod(BaseVM vm, DvmClass dvmClass, String signature, VarArg varArg){
        switch (signature){
            case "com/alibaba/wireless/security/framework/utils/UserTrackMethodJniBridge->utAvaiable()I":
//            没hook到
            case "com/uc/crashsdk/JNIBridge->registerInfoCallback(Ljava/lang/String;IJI)I": {
                return 1;
            }
        }
        return super.callStaticIntMethod(vm, dvmClass, signature, varArg);
    }

    @Override
    public void setStaticLongField(BaseVM vm, DvmClass dvmClass, String signature, long value) {
        if ("com/alibaba/wireless/security/framework/SGPluginExtras->slot:J".equals(signature)) {
            this.slot = value;
            return;
        }
        super.setStaticLongField(vm, dvmClass, signature, value);
    }

    @Override
    public int getStaticIntField(BaseVM vm, DvmClass dvmClass, String signature) {
        if ("android/content/pm/PackageManager->PERMISSION_GRANTED:I".equals(signature)) {
            return 1;
        }
        return super.getStaticIntField(vm, dvmClass, signature);
    }

    @Override
    public long getStaticLongField(BaseVM vm, DvmClass dvmClass, String signature) {
        if ("com/alibaba/wireless/security/framework/SGPluginExtras->slot:J".equals(signature)) {
            return this.slot;
        }
        return super.getStaticLongField(vm, dvmClass, signature);
    }

    public long callStaticLongMethod(BaseVM vm, DvmClass dvmClass, String signature, VarArg varArg) {
        if ("mtopsdk/mtop/global/SDKUtils->getCorrectionTime()J".equals(signature)) {
            long now = System.currentTimeMillis();
//            System.out.println(Long.toHexString(1651917030896L));
//            return 1651743950896L;
            return 1651917030896L;
        }
        return super.callStaticLongMethod(vm, dvmClass, signature, varArg);
    }

}

// HHnB_ehVppJ1w4h646ckuMQCQNwhyF5KSnDhtjtNB2vlDyxs=
// HHnB_ehVppJ1w4h646ckuMQCQN0Qn97eB4TeBm5g47L2LMbA=