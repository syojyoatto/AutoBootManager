package com.syojyoatto.autobootmanager;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;

public class AppInfo {

	
	public Drawable icon;
	public String name;
	public String packagename;
	public String versionname ;
	public String path;
	public boolean issystem;
	public boolean isenable;
	
	public ArrayList<ReceiverInfo> receivers ;
}
