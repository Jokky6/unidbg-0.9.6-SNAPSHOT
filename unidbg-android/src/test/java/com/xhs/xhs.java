package com.xhs;
import com.github.unidbg.AndroidEmulator;
import com.github.unidbg.Module;
import com.github.unidbg.debugger.Debugger;
import com.github.unidbg.debugger.DebuggerType;
import com.github.unidbg.linux.android.AndroidEmulatorBuilder;
import com.github.unidbg.linux.android.AndroidResolver;
import com.github.unidbg.linux.android.SystemPropertyHook;
import com.github.unidbg.linux.android.SystemPropertyProvider;
import com.github.unidbg.linux.android.dvm.*;
import com.github.unidbg.linux.android.dvm.array.ByteArray;
import com.github.unidbg.memory.Memory;
import okhttp3.*;
import okio.Buffer;
import okio.BufferedSink;
import org.apache.commons.codec.binary.Base64;

import java.awt.geom.RectangularShape;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class xhs extends AbstractJni {
    private final AndroidEmulator emulator;
    private final VM vm;
    private final Module module;
    private Headers headers;
    private Request request;
    private String url;

    xhs(){
        emulator = AndroidEmulatorBuilder.for32Bit().setProcessName("com.xhs").build(); // 创建模拟器实例，要模拟32位或者64位，在这里区分
        final Memory memory = emulator.getMemory(); // 模拟器的内存操作接口
        memory.setLibraryResolver(new AndroidResolver(23)); // 设置系统类库解析
        vm = emulator.createDalvikVM(new File("D:\\unidbg-master-11-30\\unidbg-master\\unidbg-android\\src\\test\\resources\\xhs\\xhs6.97.apk"));
        vm.setVerbose(true);

        DalvikModule dm = vm.loadLibrary(new File("D:\\unidbg-master-11-30\\unidbg-master\\unidbg-android\\src\\test\\resources\\xhs\\libshield.so"), true);

        vm.setJni(this);
        module = dm.getModule();
        System.out.println("call JNIOnLoad");
        Debugger MyDbg = emulator.attach(DebuggerType.CONSOLE);
//        MyDbg.addBreakPoint(module.base + 0x2486C +1);
        MyDbg.addBreakPoint(module.base + 0x1F85C  +1);
        dm.callJNI_OnLoad(emulator);


        url = "https://edith.xiaohongshu.com/api/sns/v10/search/notes?keyword=%E7%A9%BA%E6%B0%94%E7%82%B8%E9%94%85%E9%A3%9F%E8%B0%B1&filters=%5B%5D&sort=&page=1&page_size=20&source=explore_feed&search_id=29qx5zcgyi67c5v68ey2o&session_id=29qwiqzuuthiaekwfrta8&api_extra=&page_pos=0&pin_note_id=&allow_rewrite=1&geo=eyJsYXRpdHVkZSI6MC4wMDAwMDAsImxvbmdpdHVkZSI6MC4wMDAwMDB9%0A&word_request_id=&loaded_ad=&query_extra_info=&preview_ad=";

        request = new Request.Builder()
                .url(url)
                .addHeader("X-B3-TraceId", "4a4a3a065c180b0f")
                .addHeader("xy-common-params", "fid=1645512721109eb9702836fb4d9e86e3e9a41ca2c165&device_fingerprint=20220107165923175eeb343651598c275906af9927c35f01ebac22e3b2caa2&device_fingerprint1=20220107165923175eeb343651598c275906af9927c35f01ebac22e3b2caa2&launch_id=1645512881&tz=Asia%2FShanghai&channel=PMgdt19935737&versionName=6.97.0.1&deviceId=d448b648-6f21-36a0-9e4a-59d132d4efac&platform=android&sid=session.1645512726660327178543&identifier_flag=4&t=1645515467&project_id=ECFAAF&build=6970181&x_trace_page_current=search_entry&lang=zh-Hans&app_id=ECFAAF01&uis=light")
                .addHeader("User-Agent", "Dalvik/2.1.0 (Linux; U; Android 9; MI 8 MIUI/9.7.4) Resolution/1080*2158 Version/6.97.0.1 Build/6970181 Device/(Xiaomi;MI 8) discover/6.97.0.1 NetType/WiFi")
                .build();

    }

    // 第一个初始化函数
    public void callinitializeNative(){
        List<Object> list = new ArrayList<>(10);
        list.add(vm.getJNIEnv()); // 第一个参数是env
        list.add(0); // 第二个参数，实例方法是jobject，静态方法是jclazz，直接填0，一般用不到。
        module.callFunction(emulator, 0x6c11d, list.toArray());
    };

    // 第二个初始化函数
    public long callinitialize(){
        List<Object> list = new ArrayList<>(10);
        list.add(vm.getJNIEnv()); // 第一个参数是env
        list.add(0); // 第二个参数，实例方法是jobject，静态方法是jclazz，直接填0，一般用不到。
        list.add(vm.addLocalObject(new StringObject(vm, "main")));
        Number number = module.callFunction(emulator, 0x6b801, list.toArray())[0];
        return number.longValue();
    }

    // 目标函数
    public void callintercept(long ptr){
        List<Object> list = new ArrayList<>(10);
        list.add(vm.getJNIEnv()); // 第一个参数是env
        list.add(0); // 第二个参数，实例方法是jobject，静态方法是jclazz，直接填0，一般用不到。
        DvmObject<?> chain = vm.resolveClass("okhttp3/Interceptor$Chain").newObject(null);
        list.add(vm.addLocalObject(chain));
        list.add(ptr);
        module.callFunction(emulator, 0x6b9e9, list.toArray());
    };
    public static void main(String[] args) {
        xhs test = new xhs();
        test.callinitializeNative();
        long ptr = test.callinitialize();
        System.out.println("call intercept");
        test.callintercept(ptr);


    }

    @Override
    public DvmObject<?> getStaticObjectField(BaseVM vm, DvmClass dvmClass, String signature) {
        switch (signature){
            case "com/xingin/shield/http/ContextHolder->sLogger:Lcom/xingin/shield/http/ShieldLogger;":{
                return vm.resolveClass("com/xingin/shield/http/ShieldLogger").newObject(signature);
            }
            case "com/xingin/shield/http/ContextHolder->sDeviceId:Ljava/lang/String;":{
                return new StringObject(vm, "d448b648-6f21-36a0-9e4a-59d132d4efac");
            }
        }
        return super.getStaticObjectField(vm, dvmClass, signature);
    }

    @Override
    public void callVoidMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
        switch (signature){
            case "com/xingin/shield/http/ShieldLogger->nativeInitializeStart()V":{
                return;
            }
            case "com/xingin/shield/http/ShieldLogger->nativeInitializeEnd()V": {
                return;
            }
            case "com/xingin/shield/http/ShieldLogger->initializeStart()V": {
                return;
            }
            case "com/xingin/shield/http/ShieldLogger->initializedEnd()V": {
                return;
            }
            case "com/xingin/shield/http/ShieldLogger->buildSourceStart()V": {
                return;
            }
            case "okhttp3/RequestBody->writeTo(Lokio/BufferedSink;)V": {
                BufferedSink bufferedSink = (BufferedSink) vaList.getObjectArg(0).getValue();
                RequestBody requestBody = (RequestBody) dvmObject.getValue();
                if(requestBody != null){
                    try {
                        requestBody.writeTo(bufferedSink);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return;
            }
            case "com/xingin/shield/http/ShieldLogger->buildSourceEnd()V": {
                return;
            }
            case "com/xingin/shield/http/ShieldLogger->calculateStart()V": {
                System.out.println("calculateStart —— 开始计算");
                return;
            }
            case "com/xingin/shield/http/ShieldLogger->calculateEnd()V": {
                System.out.println("calculateEnd —— 结束计算");
                return;
            }
        }
        super.callVoidMethodV(vm, dvmObject, signature, vaList);
    }

    @Override
    public DvmObject<?> callStaticObjectMethodV(BaseVM vm, DvmClass dvmClass, String signature, VaList vaList) {
        switch (signature){
            case "java/nio/charset/Charset->defaultCharset()Ljava/nio/charset/Charset;":{
                return vm.resolveClass("java/nio/charset/Charset").newObject(Charset.defaultCharset());
            }
            case "com/xingin/shield/http/Base64Helper->decode(Ljava/lang/String;)[B":{
                String input = (String) vaList.getObjectArg(0).getValue();
                byte[] result = Base64.decodeBase64(input);
                return new ByteArray(vm, result);
            }
        }
        return super.callStaticObjectMethodV(vm, dvmClass, signature, vaList);
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
    public int getStaticIntField(BaseVM vm, DvmClass dvmClass, String signature) {
        switch (signature){
            case "com/xingin/shield/http/ContextHolder->sAppId:I":{
                return -319115519;
            }
        }
        return super.getStaticIntField(vm, dvmClass, signature);
    }

    @Override
    public DvmObject<?> callObjectMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
        switch (signature) {
            case "android/content/Context->getSharedPreferences(Ljava/lang/String;I)Landroid/content/SharedPreferences;":
                vaList.getObjectArg(0);
                return vm.resolveClass("android/content/SharedPreferences").newObject(vaList.getObjectArg(0));
            case "android/content/SharedPreferences->getString(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;": {
                if(((StringObject) dvmObject.getValue()).getValue().equals("s")){
                    System.out.println("getString :"+vaList.getObjectArg(0).getValue());
                    if (vaList.getObjectArg(0).getValue().equals("main")) {
                        return new StringObject(vm, "");
                    }
                    if(vaList.getObjectArg(0).getValue().equals("main_hmac")){
                        return  new StringObject(vm, "DfCysQY5P2wB7Zz76clHM7QCxDTVnvSPQPPRxxmHlaOrUpQDB5ZfH2xdvtML50mYCqWtZs8iaSXvhdqExV6yhyMOEnRDUNEJchMmPynykEDgVLoV9TYeXpb3RDfB454V");
                    }
                }
            }
            case "okhttp3/Interceptor$Chain->request()Lokhttp3/Request;": {
                DvmClass clazz = vm.resolveClass("okhttp3/Request");
                return clazz.newObject(request);
            }
            case "okhttp3/Request->url()Lokhttp3/HttpUrl;": {
                DvmClass clazz = vm.resolveClass("okhttp3/HttpUrl");
                Request request = (Request) dvmObject.getValue();
                return clazz.newObject(request.url());
            }
            case "okhttp3/HttpUrl->encodedPath()Ljava/lang/String;": {
                HttpUrl httpUrl = (HttpUrl) dvmObject.getValue();
                return new StringObject(vm, httpUrl.encodedPath());
            }
            case "okhttp3/HttpUrl->encodedQuery()Ljava/lang/String;": {
                HttpUrl httpUrl = (HttpUrl) dvmObject.getValue();
                return new StringObject(vm, httpUrl.encodedQuery());
            }
            case "okhttp3/Request->body()Lokhttp3/RequestBody;": {
                Request request = (Request) dvmObject.getValue();
                return vm.resolveClass("okhttp3/RequestBody").newObject(request.body());
            }
            case "okhttp3/Request->headers()Lokhttp3/Headers;": {
                Request request = (Request) dvmObject.getValue();
                return vm.resolveClass("okhttp3/Headers").newObject(request.headers());
            }
            case "okio/Buffer->writeString(Ljava/lang/String;Ljava/nio/charset/Charset;)Lokio/Buffer;": {
                System.out.println("write to my buffer:"+vaList.getObjectArg(0).getValue());
                Buffer buffer = (Buffer) dvmObject.getValue();
                buffer.writeString(vaList.getObjectArg(0).getValue().toString(), (Charset) vaList.getObjectArg(1).getValue());
                return dvmObject;
            }
            case "okhttp3/Headers->name(I)Ljava/lang/String;": {
                Headers headers = (Headers) dvmObject.getValue();
                return new StringObject(vm, headers.name(vaList.getIntArg(0)));
            }
            case "okhttp3/Headers->value(I)Ljava/lang/String;": {
                Headers headers = (Headers) dvmObject.getValue();
                return new StringObject(vm, headers.value(vaList.getIntArg(0)));
            }
            case "okio/Buffer->clone()Lokio/Buffer;": {
                Buffer buffer = (Buffer) dvmObject.getValue();
                return vm.resolveClass("okio/Buffer").newObject(buffer.clone());
            }
            case "okhttp3/Request->newBuilder()Lokhttp3/Request$Builder;": {
                Request request = (Request) dvmObject.getValue();
                return vm.resolveClass("okhttp3/Request$Builder").newObject(request.newBuilder());
            }
            case "okhttp3/Request$Builder->header(Ljava/lang/String;Ljava/lang/String;)Lokhttp3/Request$Builder;": {
                Request.Builder builder = (Request.Builder) dvmObject.getValue();
                builder.header(vaList.getObjectArg(0).getValue().toString(), vaList.getObjectArg(1).getValue().toString());
                return dvmObject;
            }
            case "okhttp3/Request$Builder->build()Lokhttp3/Request;": {
                Request.Builder builder = (Request.Builder) dvmObject.getValue();
                return vm.resolveClass("okhttp3/Request").newObject(builder.build());
            }
            case "okhttp3/Interceptor$Chain->proceed(Lokhttp3/Request;)Lokhttp3/Response;": {
                return vm.resolveClass("okhttp3/Response").newObject(null);
            }
        }

        return super.callObjectMethodV(vm, dvmObject, signature, vaList);
    }

    @Override
    public DvmObject<?> newObjectV(BaseVM vm, DvmClass dvmClass, String signature, VaList vaList) {
        switch (signature){
            case "okio/Buffer-><init>()V":
                return dvmClass.newObject(new Buffer());
        }
        return super.newObjectV(vm, dvmClass, signature, vaList);
    }

    @Override
    public int callIntMethodV(BaseVM vm, DvmObject<?> dvmObject, String signature, VaList vaList) {
        switch (signature){
            case "okhttp3/Headers->size()I":
                Headers headers = (Headers) dvmObject.getValue();
                return headers.size();
            case "okhttp3/Response->code()I":
                return 200;
            case "okio/Buffer->read([B)I":
                Buffer buffer = (Buffer) dvmObject.getValue();
                return buffer.read((byte[]) vaList.getObjectArg(0).getValue());
        }
        return super.callIntMethodV(vm, dvmObject, signature, vaList);
    }

}