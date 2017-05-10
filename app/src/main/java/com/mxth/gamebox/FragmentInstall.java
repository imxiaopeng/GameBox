package com.mxth.gamebox;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.client.ipc.VActivityManager;
import com.lody.virtual.remote.InstalledAppInfo;
import com.mxth.gamebox.modles.AppData;
import com.mxth.gamebox.modles.MultiplePackageAppData;
import com.mxth.gamebox.modles.PackageAppData;

import java.util.ArrayList;
import java.util.List;

import static android.R.attr.data;
import static com.mxth.gamebox.R.id.gridView;

/**
 * Created by Administrator on 2017/4/28.
 */

public class FragmentInstall extends Fragment {
    private View view;
    Handler handler = new Handler();
    private MyAdapter adapter;
    private ProgressDialog dialog;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_installed, container, false);
        }
        dialog =new ProgressDialog(getActivity());
        dialog.setMessage("正在卸载...");
        dialog.setCanceledOnTouchOutside(false);
        SwipeRefreshLayout refresh = (SwipeRefreshLayout) view.findViewById(R.id.refresh);
        refresh.setColorSchemeColors(getResources().getColor(android.R.color.holo_blue_light),
                getResources().getColor(android.R.color.holo_red_light),
//                getResources().getColor(android.R.color.holo_orange_light),
                getResources().getColor(android.R.color.holo_green_light));
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                flushApp();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        refresh.setRefreshing(false);
                    }
                },3000);
            }
        });
        GridView gridView = (GridView) view.findViewById(R.id.gridView);
        adapter = new MyAdapter();
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                AppData item = adapter.getItem(i);
                ProgressDialog dialog = new ProgressDialog(getActivity());
                dialog.setCanceledOnTouchOutside(false);
                launchApp(item,dialog);
            }
        });
        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                AppData item = adapter.getItem(i);
                AlertDialog.Builder adb = new AlertDialog.Builder(getActivity());
                adb.setTitle("是否要卸载该应用?");
                adb.setNegativeButton("否", null);
                adb.setPositiveButton("是", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(dialog!=null && !getActivity().isFinishing()){
                            dialog.show();
                        }
                        if (item instanceof PackageAppData) {
                            PackageAppData data = (PackageAppData) item;
                            if (VirtualCore.get().uninstallPackage(data.packageName)) {
                                flushApp();
                                Utils.toast("卸载成功!");
                            } else {
                                Utils.toast("卸载失败!");
                                if(dialog!=null && !getActivity().isFinishing()){
                                    dialog.dismiss();
                                }
                            }
                        } else {
                            if(dialog!=null && !getActivity().isFinishing()){
                                dialog.dismiss();
                            }
                            Utils.toast("该应用无法卸载!");
                        }
                    }
                });
                AlertDialog dialog = adb.create();
                dialog.setCanceledOnTouchOutside(false);
                dialog.show();
                return true;
            }
        });
        new Thread() {
            @Override
            public void run() {
                flushApp();
            }
        }.start();
        VirtualCore.get().registerObserver(new VirtualCore.PackageObserver() {
            @Override
            public void onPackageInstalled(String packageName) throws RemoteException {
                Log.e("message","onPackageInstalled"+FragmentInstall.class);
                flushApp();
            }

            @Override
            public void onPackageUninstalled(String packageName) throws RemoteException {
                Log.e("message","onPackageUninstalled");
                if(dialog!=null && !getActivity().isFinishing()){
                    dialog.dismiss();
                }
            }

            @Override
            public void onPackageInstalledAsUser(int userId, String packageName) throws RemoteException {
                Log.e("message","onPackageInstalledAsUser");
            }

            @Override
            public void onPackageUninstalledAsUser(int userId, String packageName) throws RemoteException {
                Log.e("message","onPackageUninstalledAsUser");
            }
        });
        return view;
    }

    private void flushApp() {
        new Thread(){
            @Override
            public void run() {
                List<AppData> installApps = getInstallApps();
                if (handler != null && adapter != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            adapter.setList(installApps);
                        }
                    });
                }
            }
        }.start();
    }


    private class MyAdapter extends BaseAdapter {
        private List<AppData> mList;

        public void setList(List<AppData> installApps) {
            this.mList = installApps;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mList == null ? 0 : mList.size();
        }

        @Override
        public AppData getItem(int i) {
            return mList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            AppData appData = mList.get(i);
            ViewHolder vh = null;
            if (view == null) {
                vh = new ViewHolder();
                view = android.view.View.inflate(getActivity(), R.layout.item_grid, null);
                vh.app_icon = (ImageView) view.findViewById(R.id.app_icon);
                vh.app_name = (TextView) view.findViewById(R.id.app_name);
                view.setTag(vh);
            } else {
                vh = (ViewHolder) view.getTag();
            }
            vh.app_icon.setImageDrawable(appData.getIcon());
            vh.app_name.setText(appData.getName());
            return view;
        }
    }

    private class ViewHolder {
        private ImageView app_icon;
        private TextView app_name;

        public ViewHolder() {

        }
    }

    private List<AppData> getInstallApps() {
        List<InstalledAppInfo> infos = VirtualCore.get().getInstalledApps(0);
        List<AppData> models = new ArrayList<>();
        for (InstalledAppInfo info : infos) {
            if (!VirtualCore.get().isPackageLaunchable(info.packageName)) {
                continue;
            }
            PackageAppData data = new PackageAppData(getActivity(), info);
            if (VirtualCore.get().isAppInstalledAsUser(0, info.packageName)) {
                models.add(data);
            }
            int[] userIds = info.getInstalledUsers();
            for (int userId : userIds) {
                if (userId != 0) {
                    models.add(new MultiplePackageAppData(data, userId));
                }
            }
        }
        return models;
    }

    public void launchApp(AppData data,ProgressDialog dialog) {

        try {
            if (data instanceof PackageAppData) {
                Log.e("message","aaa"+System.currentTimeMillis());
                PackageAppData appData = (PackageAppData) data;
                dialog.setMessage("正在打开"+appData.getName()+"...");
                dialog.show();
                Log.e("message","bbb"+System.currentTimeMillis());
                new Thread(){
                    @Override
                    public void run() {
                        Intent launchIntent = VirtualCore.get().getLaunchIntent(appData.packageName, 0);
                        VirtualCore.get().setUiCallback(launchIntent, new VirtualCore.UiCallback() {
                            @Override
                            public void onAppOpened(String packageName, int userId) throws RemoteException {
                                getActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Log.e("message","ddd"+System.currentTimeMillis());
                                        dialog.dismiss();
                                    }
                                });
                            }
                        });
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.e("message","ccc"+System.currentTimeMillis());
                                VActivityManager.get().startActivity(launchIntent, 0);
                            }
                        });
                    }
                }.start();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

}
