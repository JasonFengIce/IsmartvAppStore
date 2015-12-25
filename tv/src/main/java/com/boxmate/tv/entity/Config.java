package com.boxmate.tv.entity;

public class Config {
	static public String baseApi = "http://api.qjhtv.com/api/";
	static public String appBase = Config.baseApi + "lenovo/?";
	static public String UPDATE_URL=baseApi +"lenovo/?action=get_product_by_package&package=";
	static public String Cookie = "Cookie";
	
	static public String BOARD_URL=baseApi+"lenovo/?action=get_top_data";
	
	static public String apkDir = "/data/data/com.boxmate.tv";
	static public String LOCAL_HOST = "127.0.0.1";
	static public String TV_ClEAN_APK_NAME = "tv_clean2.desk";
	static public String TV_ClEAN_PKG = "com.boxmate.clean";
	static public String MARKET_PKG_OLD = "com.reco.tv";
	static public String TV_ClEAN_PKG_OLD = "com.reco.clean";
	static public String SETTING = "UserSetting";
	static public String SETTING_FIRST_LAUNCHER = "FirstLaunhcer";
	static public String SETTING_BACKGROUND_FLAG = "BackgroundAvailable";//设备是否允许后台进程
	static public String SETTING_ADB_FLAG = "AdbFlag";
	static public String SETTING_UPDATE_HINT_FLAG = "UpdateHintFlag";
	static public String SETTING_VIDEO_FLAG = "VideoFlag";
	static public String SETTING_DESK_SPEED_FLAG = "DeskSpeedFlag";
	static public String USER_DATA = "UserData";
	static public String CLEAR_GABAGE_COUNT = "ClearGabageCount";
	static public String WIFI_NAME = "WifiName";
	static public String VIDEO_PKG_LIST = "VideoPkgList";
	static public String UPGRADE_CHECK_DATE = "UpgradeCheckDate";
	static public String PKG_PREFIX = "prefix_";
	static public String LAST_TOP_PKG = "LastTopPkg";
	
	//INTENT参数名
	static public String JUMP_PARAM = "JumpParam";
	static public int PAGE_HOME = 1;
	static public int PAGE_MANAGE = 0;
	static public int PAGE_GAME = 2;
	static public int PAGE_APP = 3;
	static public String[] params={"manage","home","game","app"};
	static public String[] deskParams = {"gabage", "uninstall", "more"};
	static public int PAGE_INDEX = 1;
	static public int PAGE_COUNT=4;
	static public String INTENT_VIDEO_OBSERVER_OPEN = "VideoObserverOpen";
	static public String INTENT_VIDEO_OBSERVER_CLOSE = "VideoObserverClose";

	// 影音分类
	static public int VIDEO_CAT_LIVE = 20;
	static public int VIDEO_CAT_VEDIO = 21;
	static public int VIDEO_CAT_SPECIAL = 22;
	static public int VIDEO_CAT_MUSIC = 23;

	// app分类
	static public int APP_CAT_VEDIO = 8;
	static public int APP_CAT_ENTERTAINMENT = 6;
	static public int APP_CAT_LIVE = 5;
	static public int APP_CAT_HEALTH = 3;
	static public int APP_CAT_TOOLS = 9;
	static public int APP_CAT_EDUCATION = 4;

	// game分类
	static public int APP_CAT_SIMULATOR = 14;
	static public int APP_CAT_IQ = 13;
	static public int APP_CAT_ADVENTURE = 15;
	static public int APP_CAT_SPORT = 12;
	static public int APP_CAT_TABLE = 11;
	static public int APP_CAT_MANAGE = 16;
	static public int APP_CAT_SHOT = 17;
	static public int APP_CAT_RPG = 18;

	// 手柄分类
	static public int CONTROL_REMOTE = 1;
	static public int CONTROL_HANDLE = 2;
	static public int CONTROL_BODY = 3;
	static public int CONTROL_PHONE = 4;
	static public int CONTROL_MOUSE = 5;

}
