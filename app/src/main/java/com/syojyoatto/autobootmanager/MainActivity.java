package com.syojyoatto.autobootmanager;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends Activity {

    private ListView lv_apps;
    private Context mContext;

    private AppsAdapter appsAdapter;

    Handler handler = new Handler() {

        public void handleMessage(android.os.Message msg) {

            switch (msg.what) {
                case 1:

                    ArrayList<AppInfo> lists_apps = (ArrayList<AppInfo>) msg.obj;

                    if (lists_apps != null && lists_apps.size() > 0) {

                        appsAdapter = new AppsAdapter(lists_apps);

                        lv_apps.setAdapter(appsAdapter);
                    }

                    break;

                default:
                    break;
            }

        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;
        lv_apps = (ListView) findViewById(R.id.lv_apps);

        new Thread(new Runnable() {

            @Override
            public void run() {


                ArrayList<AppInfo> lists_apps = Utils.getAllApp(mContext);
                Message message = new Message();
                message.what = 1;
                message.obj = lists_apps;

                handler.sendMessage(message);


            }
        }).start();
    }


    class ViewHold {
        public TextView tv_name;
        public TextView tv_enable;
        public TextView tv_issystem;
        public Button btn_open;
        public ImageView img_icon;

    }

    class AppsAdapter extends BaseAdapter {
        ArrayList<AppInfo> lists_apps = null;

        public AppsAdapter(ArrayList<AppInfo> lists_apps) {
            this.lists_apps = lists_apps;
        }

        @Override
        public int getCount() {
            return lists_apps.size();
        }

        @Override
        public Object getItem(int position) {
            return lists_apps.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {


            ViewHold hold = null;
            if (convertView == null) {

                hold = new ViewHold();
                convertView = View.inflate(mContext, R.layout.item_autoboot, null);

                hold.tv_enable = (TextView) convertView.findViewById(R.id.tv_enable);
                hold.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                hold.tv_issystem = (TextView) convertView.findViewById(R.id.tv_issystem);
                hold.btn_open = (Button) convertView.findViewById(R.id.btn_open);
                hold.img_icon = (ImageView) convertView.findViewById(R.id.img_icon);

                convertView.setTag(hold);

            } else {
                hold = (ViewHold) convertView.getTag();
            }


            final AppInfo appInfo = lists_apps.get(position);
            hold.img_icon.setImageDrawable(appInfo.icon);

            hold.tv_name.setText(appInfo.name);


            if (appInfo.issystem) {
                hold.tv_issystem.setText("系统应用，禁止开机启动请多加小心。。。");
                hold.tv_issystem.setTextColor(Color.parseColor("#ff0000"));
            } else {
                hold.tv_issystem.setText("第三方应用");
                hold.tv_issystem.setTextColor(Color.parseColor("#666666"));
            }


            if (appInfo.isenable) {
                hold.tv_enable.setText("已开启");
                hold.tv_enable.setTextColor(Color.parseColor("#ff0000"));

                hold.btn_open.setText("禁止");
                hold.btn_open.setBackgroundColor(Color.parseColor("#00ff00"));
            } else {
                hold.tv_enable.setText("已禁止");
                hold.tv_enable.setTextColor(Color.parseColor("#666666"));

                hold.btn_open.setText("开启");
                hold.btn_open.setBackgroundColor(Color.parseColor("#666666"));
            }


            hold.btn_open.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {


                    String cmd = "";

                    for (ReceiverInfo receiverinfo : appInfo.receivers) {

                        if (appInfo.isenable) {
                            cmd = cmd + "pm disable " + appInfo.packagename + "/" + receiverinfo.className + ";";
                        } else {
                            cmd = cmd + "pm enable " + appInfo.packagename + "/" + receiverinfo.className + ";";
                        }

                    }

                    ExeCmd.execmd(cmd);


                    ArrayList<ReceiverInfo> readReceiver = Utils.readReceiver(mContext, appInfo.path);

                    boolean autoflag = false;
                    for (ReceiverInfo receiverInfo : readReceiver) {
                        if (receiverInfo.enable) {
                            autoflag = true;
                            break;
                        }
                    }

                    if (autoflag) {
                        appInfo.isenable = true;
                    } else {
                        appInfo.isenable = false;
                    }
                    appsAdapter.notifyDataSetChanged();


                }
            });


            return convertView;
        }


    }


}
