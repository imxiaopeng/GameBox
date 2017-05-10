package com.mxth.gamebox;

import android.app.ProgressDialog;
import android.content.pm.PackageInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.lody.virtual.client.core.InstallStrategy;
import com.lody.virtual.client.core.VirtualCore;
import com.lody.virtual.remote.InstallResult;
import com.mxth.gamebox.modles.AppInfo;

import java.io.File;
import java.util.List;

import static com.flurry.sdk.ma.n;
import static com.mxth.gamebox.R.id.listView;

/**
 * Created by Administrator on 2017/4/28.
 */

public class FragmentUnInstall extends Fragment {
    private View view;
    private ProgressDialog dialog;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (view == null) {
            view = inflater.inflate(R.layout.fragment_uninstalled, container, false);
        }
        dialog = new ProgressDialog(getActivity());
        dialog.setCanceledOnTouchOutside(false);
        ListView listView = (ListView) view.findViewById(R.id.listView);
        MyAdapter adapter = new MyAdapter();
        listView.setAdapter(adapter);
        File storageFir = Environment.getExternalStorageDirectory();
        adapter.setmList(FileUtils.convertPackageInfoToAppData(getActivity(), FileUtils.findAndParseAPKs(getActivity(), storageFir), false));
        VirtualCore.get().registerObserver(new VirtualCore.PackageObserver() {
            @Override
            public void onPackageInstalled(String packageName) throws RemoteException {
                Log.e("message","onPackageInstalled");
                if(dialog!=null && !getActivity().isFinishing()){
                    dialog.dismiss();
                }
            }

            @Override
            public void onPackageUninstalled(String packageName) throws RemoteException {
                Log.e("message","onPackageUninstalled");
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

    private class MyAdapter extends BaseAdapter {
        public List<AppInfo> mList;

        public void setmList(List<AppInfo> mList) {
            this.mList = mList;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mList == null ? 0 : mList.size();
        }

        @Override
        public AppInfo getItem(int i) {
            return mList.get(i);
        }

        @Override
        public long getItemId(int i) {
            return i;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            ViewHolder vh = null;
            AppInfo appInfo = mList.get(i);
            if (view == null) {
                vh = new ViewHolder();
                view = View.inflate(getActivity(),R.layout.item_uninstall,null);
                vh.imageView = (ImageView) view.findViewById(R.id.image);
                vh.btn = (Button) view.findViewById(R.id.btn_install);
                vh.tv_name = (TextView) view.findViewById(R.id.tv_name);
                view.setTag(vh);
            } else {
                vh = (ViewHolder) view.getTag();
            }
            vh.imageView.setImageDrawable(appInfo.icon);
            vh.tv_name.setText(appInfo.name);
            vh.btn.setOnClickListener(v-> {
                    if(dialog!=null && !getActivity().isFinishing()){
                        dialog.setMessage("正在安装"+appInfo.name+"...");
                        dialog.show();
                    }
                    new Thread(){
                        @Override
                        public void run() {
                            int flags = InstallStrategy.COMPARE_VERSION | InstallStrategy.ART_FLY_MODE;
                            InstallResult installResult = VirtualCore.get().installPackage(appInfo.path, flags);
                           getActivity().runOnUiThread(new Runnable() {
                               @Override
                               public void run() {
                                   if (!installResult.isSuccess) {
                                       if(dialog!=null && !getActivity().isFinishing()){
                                           dialog.dismiss();
                                       }
                                       Utils.toast("安装失败:"+installResult.error);
                                   }else{
                                       Utils.toast("安装成功");
                                   }
                               }
                           });
                        }
                    }.start();

            });
            return view;
        }
        class ViewHolder {
            ImageView imageView;
            TextView tv_name;
            Button btn;
        }
    }
}
