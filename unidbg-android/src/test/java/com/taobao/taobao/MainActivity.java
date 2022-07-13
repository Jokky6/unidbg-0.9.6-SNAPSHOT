package com.taobao.taobao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Emulator;
import com.github.unidbg.Module;
import com.github.unidbg.file.FileResult;
import com.github.unidbg.file.IOResolver;
import com.github.unidbg.file.linux.AndroidFileIO;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.SystemPropertyHook;
import com.github.unidbg.linux.android.SystemPropertyProvider;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.array.ArrayObject;
import com.github.unidbg.linux.android.dvm.jni.ProxyDvmObject;
import com.github.unidbg.linux.android.dvm.wrapper.DvmBoolean;
import com.github.unidbg.linux.android.dvm.wrapper.DvmInteger;
import com.github.unidbg.memory.Memory;
import com.github.unidbg.virtualmodule.android.AndroidModule;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class MainActivity extends AbstractJni implements IOResolver<AndroidFileIO> {
    private final AndroidEmulator emulator;
    private final VM vm;
    public String dataAppPath = "/data/app/~~CKbhGinjY0UVgQQeVfhovA==";
    public String APK_INSTALL_PATH = dataAppPath + "/base.apk";
    public String SGMANAGER_DATA2;
    public String path = "unidbg-android/src/test/resources/taobao/";
    public String methodSign = "doCommandNative(I[Ljava/lang/Object;)Ljava/lang/Object;";
    public String packageName = "com.taobao.taobao";

    public long libsgmainsoBase;
    public long libsgmainsoSize;
    public long libsecbodyBase;
    public long libsecbodySize;
    public long libsgmiddletierBase;
    public long libsgmiddletierSize;

    public long slot;

    public String libsgmain = path + "libsgmainso-6.5.33.so";
    public File libsgmainso = new File(libsgmain);
    public String libsgsecuritybody = path + "libsgsecuritybodyso-6.5.39.so";
    public File libsgsecuritybodyso = new File(libsgsecuritybody);
    public String libsgmiddletier = path + "libsgmiddletierso-6.5.30.so";
    public File libsgmiddletierso = new File(libsgmiddletier);

    public DvmClass JNICLibrary;
    public DvmObject<?> context;
    public DvmObject<?> ret;

    public File APK_FILE = new File("unidbg-android/src/test/resources/taobao/com.taobao.taobao-v10.0.0.apk");

    private static AndroidEmulator createARMEmulator() {
        return AndroidEmulatorBuilder
                .for32Bit()
                .setRootDir(new File("unidbg-android/src/test/resources/taobao/com.taobao.taobao"))
                .setProcessName("com.taobao.taobao")
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
                switch (key){
                    case "ro.build.fingerprint":{
                        return "google/redfin/redfin:12/SP2A.220505.002/8353555:user/release-keys";
                    }
                    case "ro.build.version.sdk":{
                        return "23";
                    }
                    case "ro.product.cpu.abi":{
                        return "arm64-v8a";
                    }
                }
                return null;
            }
        });
        memory.addHookListener(systemPropertyHook);
        emulator.getSyscallHandler().addIOResolver(this);
        emulator.getSyscallHandler().setVerbose(true);
        vm.setVerbose(true); // 设置是否打印Jni调用细节
        vm.setJni(this);
        new AndroidModule(emulator, vm).register(memory);
        try {
            SGMANAGER_DATA2= FileUtils.readFileToString(new File(path + "/com.taobao.taobao/data/user/0/com.taobao.taobao/files/SGMANAGER_DATA2"),"UTF-8");
        }catch (Exception e){
            System.exit(1);
        }

        JNICLibrary = vm.resolveClass("com/taobao/wireless/security/adapter/JNICLibrary");
        context = vm.resolveClass("android/content/Context").newObject(null);
    }

    public void initMain(){
        DalvikModule dm = vm.loadLibrary(libsgmainso,true);
        Module module = dm.getModule();

        libsgmainsoBase = module.base;
        libsgmainsoSize = module.size;
        System.out.println("libsgmainso base "+module.base);

        dm.callJNI_OnLoad(emulator);

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

        ret = JNICLibrary.callStaticJniMethodObject(
                emulator, methodSign, 10102,
                new ArrayObject(
                        new StringObject(vm, "main"),
                        new StringObject(vm, "6.5.33"),
                        new StringObject(vm, "/data/user/0/com.taobao.taobao/app_SGLib/app_1657506303/main/libsgmainso-6.5.33.so")
                ));
        System.out.println("xiayu, initMain.ret-10102: " + ret.getValue().toString());
    }

    public void initSecurityBody(){
        DalvikModule securityBody = vm.loadLibrary(libsgsecuritybodyso,true);
        Module module = securityBody.getModule();
        libsecbodyBase = module.base;
        libsecbodySize = module.size;
        securityBody.callJNI_OnLoad(emulator);

        ret = JNICLibrary.callStaticJniMethodObject(
                emulator, methodSign, 10102,
                new ArrayObject(
                        new StringObject(vm, "securitybody"),
                        new StringObject(vm, "6.5.39"),
                        new StringObject(vm, "/data/user/0/com.taobao.taobao/app_SGLib/app_1657506303/main/libsgsecuritybodyso-6.5.39.so")
                ));
        System.out.println("xiayu, initSecurityBody.ret-10102: " + ret.getValue().toString());
    }

    public void initSgmiddletier(){
        DalvikModule sgmiddletier = vm.loadLibrary(libsgmiddletierso,true);
        Module module = sgmiddletier.getModule();
        libsgmiddletierBase = module.base;
        libsgmiddletierSize = module.size;
        sgmiddletier.callJNI_OnLoad(emulator);

        ret = JNICLibrary.callStaticJniMethodObject(
                emulator,methodSign,10102,
                new ArrayObject(
                        new StringObject(vm, "middletier"),
                        new StringObject(vm, "6.5.30"),
                        new StringObject(vm, "/data/user/0/com.taobao.taobao/app_SGLib/app_1657506303/main/libsgmiddletierso-6.5.30.so")
                ));
        System.out.println("xiayu, initSecurityBody.ret-10102: " + ret.getValue().toString());
    }

    public void getRes(){
        String input = "YrwYCQ811eADANRnGLGtyjdY&&&21646297&0f9b086f82fde02629194c32668300a3&1657506386&mtop.alibaba.cro.ds.reptg&1.0&&1609139228860@taobao_android_10.0.0&ArPVJhRsVSOs0ib4gagqWKFxv02hSj5MH21dL7DTvfij&&&openappkey=DEFAULT_AUTH&27&&&&&&&";
        DvmObject<?> ret = JNICLibrary.callStaticJniMethodObject(
                emulator,methodSign,70102,
                new ArrayObject(new StringObject(vm,"21646297"),
                        new StringObject(vm,input),
                        DvmBoolean.valueOf(vm,false),
                        DvmInteger.valueOf(vm,0),
                        new StringObject(vm,"mtop.alibaba.cro.ds.reptg"),
                        new StringObject(vm,"pageId=http%3A%2F%2Fm.taobao.com%2Findex.htm&pageName=com.taobao.tao.TBMainActivity"),
                        null,
                        null,
                        null,
                        new StringObject(vm,"r_8"))
        );
        System.out.println("result: " + ret.getValue().toString());
    }

    @Override
    public DvmObject<?> callObjectMethod(BaseVM vm, DvmObject<?> dvmObject, String signature, VarArg varArg) {
        if ("android/content/Context->getPackageCodePath()Ljava/lang/String;".equals(signature)) {
            return new StringObject(vm, APK_INSTALL_PATH);
        }

        if("android/content/Context->getFilesDir()Ljava/io/File;".equals(signature)){
            return vm.resolveClass("java/io/File").newObject(new File("/data/user/0/" + packageName + "/files"));
        }

        if("java/lang/Class->getAbsolutePath()Ljava/lang/String;".equals(signature)){
            return new StringObject(vm,"/data/user/0/" + packageName + "/files");
        }

        if("java/lang/ClassLoader->findClass(Ljava/lang/String;)Ljava/lang/Class;".equals(signature)){
            return vm.resolveClass("java/lang/Class");
        }

        if("java/io/File->getAbsolutePath()Ljava/lang/String;".equals(signature)){
            return new StringObject(vm,"/data/user/0/" + packageName + "/files");
        }

        if("java/lang/Thread->getStackTrace()[Ljava/lang/StackTraceElement;".equals(signature)){
            StackTraceElement[] elements = Thread.currentThread().getStackTrace();
            DvmObject[] objs = new DvmObject[elements.length];
            for (int i = 0; i < elements.length; i++) {
                objs[i] = vm.resolveClass("java/lang/StackTraceElement").newObject(elements[i]);
            }
            return new ArrayObject(objs);
        }

        if("java/lang/StackTraceElement->toString()Ljava/lang/String;".equals(signature)){
            StackTraceElement element = (StackTraceElement) dvmObject.getValue();
            return new StringObject(vm, element.toString());
        }

        if("java/util/HashMap->put(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;".equals(signature)){
            return ProxyDvmObject.createObject(vm, ((HashMap<Object, Object>) dvmObject.getValue())
                    .put(varArg.getObjectArg(0).getValue(), varArg.getObjectArg(1).getValue()));
        }

        return super.callObjectMethod(vm,dvmObject,signature,varArg);
    }

    @Override
    public DvmObject<?> getObjectField(BaseVM vm, DvmObject<?> dvmObject, String signature) {
        if ("android/content/pm/ApplicationInfo->nativeLibraryDir:Ljava/lang/String;".equals(signature)) {
            return new StringObject(vm, dataAppPath + "/lib/arm");
        }
        if("android/content/pm/ApplicationInfo->sourceDir:Ljava/lang/String;".equals(signature)){
            return new StringObject(vm,dataAppPath);
        }
        return super.getObjectField(vm, dvmObject, signature);
    }

    @Override
    public DvmObject<?> callStaticObjectMethod(BaseVM vm, DvmClass dvmClass, String signature, VarArg varArg) {
        switch (signature){
            case "com/alibaba/wireless/security/mainplugin/SecurityGuardMainPlugin->getMainPluginClassLoader()Ljava/lang/ClassLoader;":
            case "com/alibaba/wireless/security/securitybody/SecurityGuardSecurityBodyPlugin->getPluginClassLoader()Ljava/lang/ClassLoader;": {
                return vm.resolveClass("java/lang/ClassLoader").newObject(signature);
            }
            case "com/taobao/dp/util/CallbackHelper->getInstance()Lcom/taobao/dp/util/CallbackHelper;":{
                return vm.resolveClass("com/taobao/dp/util/CallbackHelper").newObject(signature);
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
                if(ar1 == 122){
                    return new StringObject(vm,"com.taobao.taobao");
                }
                if(ar1 == 135) {
                    return new StringObject(vm,"2nOAztFLPNENjgKB6yEG7wX8FAa0167w");
                }
                if(ar1 == 123) {
                    return new StringObject(vm,"10.0.0");
                }
                System.out.println("doCommandForString 参数是:" + ar1);
                return null;
            }
            case "com/alibaba/wireless/security/securitybody/SecurityBodyAdapter->doAdapter(I)Ljava/lang/String;":{
                return new StringObject(vm,"com/taobao/wireless/security/adapter/umid/UmidAdapter");
            }
            case "java/lang/Thread->currentThread()Ljava/lang/Thread;":{
                return vm.resolveClass("java/lang/Thread").newObject(signature);
            }
        }
        return super.callStaticObjectMethod(vm,dvmClass,signature,varArg);
    }

    @Override
    public int callStaticIntMethod(BaseVM vm, DvmClass dvmClass, String signature, VarArg varArg) {
        switch (signature){
            case "com/alibaba/wireless/security/framework/utils/UserTrackMethodJniBridge->utAvaiable()I":
//                return 1;
            case "com/uc/crashsdk/JNIBridge->registerInfoCallback(Ljava/lang/String;IJI)I":{
                return 1;
            }
        }
       return super.callStaticIntMethod(vm,dvmClass,signature,varArg);
    }

    @Override
    public boolean callStaticBooleanMethod(BaseVM vm, DvmClass dvmClass, String signature, VarArg varArg) {
        if("com/taobao/android/ab/api/ABGlobal->isFeatureOpened(Landroid/content/Context;Ljava/lang/String;)Z".equals(signature)){
            return true;
        }
        return super.callStaticBooleanMethod(vm,dvmClass,signature,varArg);
    }

    @Override
    public int callIntMethod(BaseVM vm, DvmObject<?> dvmObject, String signature, VarArg varArg) {
        if("java/lang/ClassLoader->findClass(Ljava/lang/String;)Ljava/lang/Class;".equals(signature)){
            DvmInteger integer = (DvmInteger) dvmObject;
            return integer.getValue();
        }
        return super.callIntMethod(vm,dvmObject,signature,varArg);
    }

    @Override
    public void setStaticLongField(BaseVM vm, DvmClass dvmClass, String signature, long value) {
        if("com/alibaba/wireless/security/framework/SGPluginExtras->slot:J".equals(signature)){
            this.slot = value;
            return;
        }
        super.setStaticLongField(vm,dvmClass,signature,value);
    }

    @Override
    public long getStaticLongField(BaseVM vm, DvmClass dvmClass, String signature) {
        if("com/alibaba/wireless/security/framework/SGPluginExtras->slot:J".equals(signature)){
            return slot;
        }
        return super.getStaticLongField(vm,dvmClass,signature);
    }

    @Override
    public int getStaticIntField(BaseVM vm, DvmClass dvmClass, String signature) {
        if("android/os/Build$VERSION->SDK_INT:I".equals(signature)){
            // current 32
            return 23;
        }
        return super.getStaticIntField(vm,dvmClass,signature);
    }

    @Override
    public DvmObject<?> newObject(BaseVM vm, DvmClass dvmClass, String signature, VarArg varArg) {
        if("java/lang/Integer-><init>(I)V".equals(signature)){
            return DvmInteger.valueOf(vm, varArg.getIntArg(0));
        }
        if("java/util/HashMap-><init>(I)V".equals(signature)){
            return ProxyDvmObject.createObject(vm, new HashMap<>());
        }
        return super.newObject(vm,dvmClass,signature,varArg);
    }

    public void destroy() throws IOException {
        emulator.close();
    }

    public static void main(String[] args) throws IOException {
        MainActivity mainActivity = new MainActivity();
        mainActivity.initMain();
        mainActivity.initSecurityBody();
        mainActivity.initSgmiddletier();
        mainActivity.getRes();
        mainActivity.destroy();
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
}
