package com.example.globaljumpblocker;

import android.app.*;
import android.os.*;
import android.provider.Settings;
import android.content.*;
import android.content.pm.*;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    static final String P="settings";
    SharedPreferences sp;
    LinearLayout root;
    CheckBox enabled, appOnly, notifyLog;
    Spinner mode;
    EditText black, white;
    TextView logs;

    String[] modes={"仅黑名单拦截","拦截所有第三方 App（白名单除外）"};

    @Override public void onCreate(Bundle b){
        super.onCreate(b); sp=getSharedPreferences(P,MODE_PRIVATE); build();
    }
    TextView label(String s,int size){
        TextView t=new TextView(this); t.setText(s); t.setTextSize(size);
        t.setTextColor(Color.DKGRAY); t.setPadding(0,10,0,10); return t;
    }
    void build(){
        ScrollView sv=new ScrollView(this);
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(30,28,30,40); sv.addView(root);

        TextView title=label("全局 App 跳转拦截器 V3",27); title.setTextColor(Color.BLACK); root.addView(title);
        root.addView(label("适用于 Android 15 / Z Fold6。建议先用黑名单模式测试。",14));

        enabled=new CheckBox(this); enabled.setText("启用拦截");
        enabled.setTextSize(18); enabled.setChecked(sp.getBoolean("enabled",false)); root.addView(enabled);

        appOnly=new CheckBox(this); appOnly.setText("仅 App → App");
        appOnly.setTextSize(18); appOnly.setChecked(sp.getBoolean("appOnly",true)); root.addView(appOnly);

        notifyLog=new CheckBox(this); notifyLog.setText("记录跳转日志");
        notifyLog.setChecked(sp.getBoolean("log",true)); root.addView(notifyLog);

        root.addView(label("拦截模式",18));
        mode=new Spinner(this);
        mode.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,modes));
        mode.setSelection(sp.getInt("mode",0)); root.addView(mode);

        Button apps=new Button(this); apps.setText("查看已安装 App / 复制包名提示");
        apps.setOnClickListener(v->showApps()); root.addView(apps);

        root.addView(label("黑名单：每行一个包名",18));
        black=new EditText(this); black.setMinLines(4); black.setHint("com.example.app");
        black.setText(sp.getString("black","")); root.addView(black);

        root.addView(label("白名单：每行一个包名（优先级最高）",18));
        white=new EditText(this); white.setMinLines(4); white.setHint("com.android.chrome");
        white.setText(sp.getString("white","")); root.addView(white);

        Button save=new Button(this); save.setText("保存设置");
        save.setOnClickListener(v->save()); root.addView(save);

        Button access=new Button(this); access.setText("打开无障碍设置");
        access.setOnClickListener(v->startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        root.addView(access);

        root.addView(label("跳转日志",20));
        logs=label("",14); root.addView(logs); refreshLogs();

        Button clear=new Button(this); clear.setText("清空日志");
        clear.setOnClickListener(v->{sp.edit().remove("logs").apply(); refreshLogs();}); root.addView(clear);
        setContentView(sv);
    }

    void save(){
        sp.edit().putBoolean("enabled",enabled.isChecked())
        .putBoolean("appOnly",appOnly.isChecked())
        .putBoolean("log",notifyLog.isChecked())
        .putInt("mode",mode.getSelectedItemPosition())
        .putString("black",black.getText().toString())
        .putString("white",white.getText().toString()).apply();
        Toast.makeText(this,"设置已保存",Toast.LENGTH_SHORT).show();
    }
    void refreshLogs(){
        String x=sp.getString("logs","");
        logs.setText(x.isEmpty()?"暂无日志":x);
    }
    void showApps(){
        PackageManager pm=getPackageManager();
        List<ApplicationInfo> list=pm.getInstalledApplications(PackageManager.GET_META_DATA);
        Collections.sort(list,(a,b)->pm.getApplicationLabel(a).toString()
            .compareToIgnoreCase(pm.getApplicationLabel(b).toString()));
        LinearLayout box=new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL);
        ScrollView sc=new ScrollView(this); sc.addView(box);
        int count=0;
        for(ApplicationInfo a:list){
            if(pm.getLaunchIntentForPackage(a.packageName)==null) continue;
            String name=pm.getApplicationLabel(a).toString();
            TextView t=new TextView(this);
            t.setText(name+"\\n"+a.packageName);
            t.setTextSize(15); t.setPadding(20,14,20,14);
            t.setOnClickListener(v->{
                String p=a.packageName;
                new AlertDialog.Builder(this).setTitle(name)
                    .setMessage(p)
                    .setPositiveButton("加入黑名单",(d,w)->appendRule("black",p))
                    .setNeutralButton("加入白名单",(d,w)->appendRule("white",p))
                    .setNegativeButton("关闭",null).show();
            });
            box.addView(t); count++;
        }
        new AlertDialog.Builder(this).setTitle("已安装可启动 App ("+count+")")
            .setView(sc).setNegativeButton("关闭",null).show();
    }
    void appendRule(String key,String p){
        String k=key.equals("black")?"black":"white";
        EditText e=k.equals("black")?black:white;
        String s=e.getText().toString();
        if(!s.contains(p)){ if(!s.isEmpty()) s+="\n"; s+=p; e.setText(s); }
        save();
    }
}