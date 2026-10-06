package com.example.globaljumpblocker;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.view.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.widget.*;
import android.content.*;
import android.os.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class JumpBlockAccessibilityService extends AccessibilityService {
    static final String P="settings";
    SharedPreferences sp;
    String current="";
    View overlay;
    Handler h=new Handler(Looper.getMainLooper());
    long last=0;

    @Override public void onServiceConnected(){sp=getSharedPreferences(P,MODE_PRIVATE);}
    @Override public void onAccessibilityEvent(AccessibilityEvent e){
        if(e==null||e.getPackageName()==null)return;
        String pkg=e.getPackageName().toString();
        if(pkg.equals(getPackageName()))return;
        long n=System.currentTimeMillis();
        if(n-last<350)return; last=n;
        if(!pkg.equals(current)){
            String from=current; current=pkg;
            if(from.length()>0 && enabled() && isUserApp(pkg)){
                if(sp.getBoolean("log",true)) log(from,pkg);
                if(block(pkg)) show(from,pkg); else remove();
            }
        }
    }
    boolean enabled(){return sp.getBoolean("enabled",false);}
    boolean isUserApp(String p){
        return !p.startsWith("android") &&
               !p.equals("com.android.systemui") &&
               !p.equals("com.android.settings") &&
               !p.equals(getPackageName());
    }
    Set<String> rules(String key){
        Set<String>s=new HashSet<>();
        for(String x:sp.getString(key,"").split("\\R"))
            if(!x.trim().isEmpty())s.add(x.trim());
        return s;
    }
    boolean block(String pkg){
        if(rules("white").contains(pkg))return false;
        if(sp.getInt("mode",0)==1)return true;
        return rules("black").contains(pkg);
    }
    void log(String from,String to){
        String t=new SimpleDateFormat("MM-dd HH:mm:ss",Locale.getDefault()).format(new Date());
        String out=t+"  "+from+"  →  "+to+"\n"+sp.getString("logs","")+"";
        if(out.length()>16000)out=out.substring(0,16000);
        sp.edit().putString("logs",out).apply();
    }
    void show(String from,String pkg){
        remove();
        LinearLayout box=new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL); box.setGravity(Gravity.CENTER);
        box.setPadding(40,40,40,40);
        GradientDrawable bg=new GradientDrawable(); bg.setColor(Color.WHITE); bg.setCornerRadius(40);
        box.setBackground(bg);

        TextView title=new TextView(this); title.setText("App 跳转拦截");
        title.setTextSize(25); title.setTextColor(Color.BLACK); title.setGravity(Gravity.CENTER);
        TextView info=new TextView(this);
        info.setText("\n来源：\n"+from+"\n\n目标：\n"+pkg+"\n");
        info.setTextSize(15); info.setTextColor(Color.DKGRAY); info.setGravity(Gravity.CENTER);

        LinearLayout row=new LinearLayout(this); row.setGravity(Gravity.CENTER);
        Button once=new Button(this); once.setText("允许一次");
        once.setOnClickListener(v->remove());
        Button white=new Button(this); white.setText("白名单");
        white.setOnClickListener(v->{add("white",pkg);remove();});
        Button deny=new Button(this); deny.setText("拦截");
        deny.setOnClickListener(v->{performGlobalAction(GLOBAL_ACTION_BACK);h.postDelayed(this::remove,200);});
        row.addView(once);row.addView(white);row.addView(deny);
        box.addView(title);box.addView(info);box.addView(row);

        overlay=box;
        WindowManager.LayoutParams lp=new WindowManager.LayoutParams(
            -1,-1,WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|
            WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT);
        lp.gravity=Gravity.CENTER;
        try{getSystemService(WindowManager.class).addView(overlay,lp);}
        catch(Exception e){overlay=null;}
    }
    void add(String key,String pkg){
        String s=sp.getString(key,"");
        if(!s.contains("\n"+pkg+"\n"))s=s+"\n"+pkg+"\n";
        sp.edit().putString(key,s).apply();
    }
    void remove(){
        if(overlay!=null){
            try{getSystemService(WindowManager.class).removeView(overlay);}catch(Exception ignored){}
            overlay=null;
        }
    }
    @Override public void onInterrupt(){remove();}
    @Override public void onDestroy(){remove();super.onDestroy();}
}