package com.mxth.gamebox;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.remote.InstalledAppInfo;
import com.mxth.gamebox.modles.AppInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static android.R.attr.path;

/**
 * Created by Administrator on 2017/4/28.
 */

public class FileUtils {
    // File storageFir = Environment.getExternalStorageDirectory();
    public static  List<PackageInfo> findAndParseAPKs(Context context, File rootDir) {
        List<PackageInfo> packageList = new ArrayList<>();
        if(rootDir==null || !rootDir.exists()){
            return packageList;
        }
            File[] dirFiles = rootDir.listFiles();
            if (dirFiles != null){
                for (File f : dirFiles) {
                    if (!f.getName().toLowerCase().endsWith(".apk"))
                        continue;
                    PackageInfo pkgInfo = null;
                    try {
                        pkgInfo = context.getPackageManager().getPackageArchiveInfo(f.getAbsolutePath(), 0);
                        pkgInfo.applicationInfo.sourceDir = f.getAbsolutePath();
                        pkgInfo.applicationInfo.publicSourceDir = f.getAbsolutePath();
                    } catch (Exception e) {
                        // Ignore
                    }
                    if (pkgInfo != null)
                        packageList.add(pkgInfo);
                }
            }
        return packageList;
    }
    public static List<AppInfo> convertPackageInfoToAppData(Context context, List<PackageInfo> pkgList, boolean fastOpen) {
        PackageManager pm = context.getPackageManager();
        List<AppInfo> list = new ArrayList<>(pkgList.size());
        String hostPkg = VirtualCore.get().getHostPkg();
        for (PackageInfo pkg : pkgList) {
            // ignore the host package
            if (hostPkg.equals(pkg.packageName)) {
                continue;
            }
            // ignore the System package
            if (isSystemApplication(pkg)) {
                continue;
            }
            ApplicationInfo ai = pkg.applicationInfo;
            String path = ai.publicSourceDir != null ? ai.publicSourceDir : ai.sourceDir;
            if (path == null) {
                continue;
            }
            AppInfo info = new AppInfo();
            info.packageName = pkg.packageName;
            info.fastOpen = fastOpen;
            info.path = path;
            info.icon = ai.loadIcon(pm);
            info.name = ai.loadLabel(pm);
            InstalledAppInfo installedAppInfo = VirtualCore.get().getInstalledAppInfo(pkg.packageName, 0);
            if (installedAppInfo != null) {
                info.cloneCount = installedAppInfo.getInstalledUsers().length;
            }
            list.add(info);
        }
        return list;
    }
    private static boolean isSystemApplication(PackageInfo packageInfo) {
        return (packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }
}
