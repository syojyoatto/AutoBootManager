package com.syojyoatto.autobootmanager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.DisplayMetrics;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Utils {


    public static ArrayList<ReceiverInfo> readReceiver(Context context, final String apkPath) {
        if (apkPath == null)
            return null;
        File file = new File(apkPath);
        if (file.exists()) {
            String PATH_PackageParser = "android.content.pm.PackageParser";
            // 反射得到pkgParserCls对象并实例化,有参数
            try {
                Class<?> pkgParserCls = Class.forName(PATH_PackageParser);
                Class<?>[] typeArgs = null;
                Object[] valueArgs = null;
                Object pkgParser = null;
                try {
                    typeArgs = new Class<?>[]{String.class};
                    Constructor<?> pkgParserCt = pkgParserCls.getConstructor(typeArgs);
                    valueArgs = new Object[]{apkPath};
                    pkgParser = pkgParserCt.newInstance(valueArgs);
                } catch (Exception e1) {
                    Constructor<?> pkgParserCt = pkgParserCls.getConstructor();
                    pkgParser = pkgParserCt.newInstance();
                }
                if (pkgParser == null) {
                    return null;
                }

                Object pkgParserPkg = null;
                try {
                    DisplayMetrics metrics = new DisplayMetrics();
                    metrics.setToDefaults();
                    typeArgs = new Class<?>[]{File.class, String.class, DisplayMetrics.class, int.class};
                    Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", typeArgs);
                    valueArgs = new Object[]{new File(apkPath), apkPath, metrics, 0};
                    pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);//Package
                } catch (Exception e1) {
                    typeArgs = new Class<?>[]{File.class, int.class};
                    Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage", typeArgs);
                    valueArgs = new Object[]{new File(apkPath), 0};
                    pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);
                }

                // 从返回的对象得到名为"applicationInfo"的字段对象
                if (pkgParserPkg != null) {
                    Field PkgParserActivityFld = pkgParserPkg.getClass().getDeclaredField("receivers");
                    ArrayList<ReceiverInfo> autoStartReceivers = new ArrayList<ReceiverInfo>();
                    ArrayList<String> receiverClass = new ArrayList<String>();
                    //获取到receivers集合
                    ArrayList<Object> PkgParserActivitys = (ArrayList<Object>) PkgParserActivityFld.get(pkgParserPkg);

                    for (int i = 0; i < PkgParserActivitys.size(); i++) {

                        Object receiver = PkgParserActivitys.get(i);
                        if (receiver != null) {

                            Field IntentsFld = receiver.getClass().getField("intents");
                            ArrayList<Object> intents = (ArrayList<Object>) IntentsFld.get(receiver);

                            Field ActivityInfoFld = receiver.getClass().getDeclaredField("info");
                            ActivityInfo activityInfo = (ActivityInfo) ActivityInfoFld.get(receiver);

                            if (intents != null && intents.size() > 0) {
                                for (int j = 0; j < intents.size(); j++) {

                                    Object mActivityIntentInfo = intents.get(j);
                                    if (mActivityIntentInfo != null) {

                                        Method countActions_method = mActivityIntentInfo.getClass().getMethod("countActions");
                                        int size = (Integer) countActions_method.invoke(mActivityIntentInfo);
                                        for (int k = 0; k < size; k++) {

                                            Method getAction_method = mActivityIntentInfo.getClass().getMethod("getAction", int.class);
                                            final String action = (String) getAction_method.invoke(mActivityIntentInfo, k);


                                            if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
                                                ReceiverInfo mReceiverInfo = new ReceiverInfo();
                                                mReceiverInfo.className = activityInfo.name;
                                                int component_enabled_state = get_component_enabled_state(context, activityInfo.packageName, activityInfo.name);
                                                switch (component_enabled_state) {
                                                    case PackageManager.COMPONENT_ENABLED_STATE_DISABLED:
                                                        mReceiverInfo.enable = false;
                                                        break;
                                                    case PackageManager.COMPONENT_ENABLED_STATE_DEFAULT:
                                                    case PackageManager.COMPONENT_ENABLED_STATE_ENABLED:
                                                    default:
                                                        mReceiverInfo.enable = true;
                                                        break;
                                                }
                                                if (!receiverClass.contains(mReceiverInfo.className)) {
                                                    autoStartReceivers.add(mReceiverInfo);
                                                    receiverClass.add(mReceiverInfo.className);
                                                }
                                            }
                                        }

                                    }
                                }
                            }
                        }
                    }
                    if (autoStartReceivers.size() > 0) {
                        return autoStartReceivers;
                    }
                    return null;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return null;
    }


    /**
     * @param context
     * @param packageName
     * @param className
     * @return PackageManager.COMPONENT_ENABLED_STATE_DEFAULT = 0
     * PackageManager.COMPONENT_ENABLED_STATE_ENABLED = 1
     * PackageManager.COMPONENT_ENABLED_STATE_DISABLED = 2
     */
    private static int get_component_enabled_state(Context context, final String packageName, final String className) {
        try {
            return context.getPackageManager().getComponentEnabledSetting(new ComponentName(packageName, className));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return PackageManager.COMPONENT_ENABLED_STATE_DEFAULT;
    }


    public static ArrayList<AppInfo> getAllApp(Context context) {

        ArrayList<AppInfo> lists_appinfos = new ArrayList<AppInfo>();
        ArrayList<AppInfo> system_apinfos = new ArrayList<AppInfo>();
        ArrayList<AppInfo> users_appinfos = new ArrayList<AppInfo>();

        PackageManager packageManager = context.getPackageManager();

        List<PackageInfo> installedPackages = packageManager.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);

        for (PackageInfo packageInfo : installedPackages) {

            AppInfo appInfo = new AppInfo();
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;


            appInfo.path = applicationInfo.sourceDir;


            if (appInfo.path.contains("/system/")) {
                appInfo.issystem = true;
            } else {
                appInfo.issystem = false;
            }

            appInfo.icon = applicationInfo.loadIcon(packageManager);
            appInfo.name = applicationInfo.loadLabel(packageManager).toString();
            appInfo.packagename = applicationInfo.packageName;
            appInfo.versionname = packageInfo.versionName;


            ArrayList<ReceiverInfo> readReceiver = readReceiver(context, appInfo.path);

            if (readReceiver == null || readReceiver.size() <= 0) continue;

            //如果一个应用中有一个开机启动项可以使用，那么该应用开机时就会接收到广播，并启动，所以要循环判断所有的启动项
            boolean autoflag = false;
            for (ReceiverInfo receiverInfo : readReceiver) {
                if (receiverInfo.enable) {
                    autoflag = true;
                    break;
                }
            }

            appInfo.receivers = readReceiver;
            if (autoflag) {

                appInfo.isenable = true;
            } else {
                appInfo.isenable = false;
            }


            if (appInfo.issystem) {
                system_apinfos.add(appInfo);
            } else {
                users_appinfos.add(appInfo);
            }


        }

        lists_appinfos.addAll(users_appinfos);
        lists_appinfos.addAll(system_apinfos);

        return lists_appinfos;

    }

}
