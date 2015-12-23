package com.boxmate.tv.ui;

import com.boxmate.tv.R;
import com.boxmate.tv.R.id;
import com.boxmate.tv.R.layout;
import com.boxmate.tv.R.menu;
import com.boxmate.tv.entity.Config;
import com.boxmate.tv.entity.RankType;
import com.boxmate.tv.util.CommonUtil;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;

public class MoreCatActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_more_cat);
		
		
		
		CommonUtil.bindGoAppListEvent(this,
				this.findViewById(R.id.tb_iq), "益智休闲",
				"get_list_by_cid_rank&cid=" + Config.APP_CAT_IQ, RankType.RANK);
		
		CommonUtil.bindGoAppListEvent(this,
				this.findViewById(R.id.tb_simulate), "模拟器",
				"get_list_by_cid_rank&cid=" + Config.APP_CAT_SIMULATOR, RankType.RANK);
		
		CommonUtil.bindGoAppListEvent(this,
				this.findViewById(R.id.tb_action), "动作冒险",
				"get_list_by_cid_rank&cid=" + Config.APP_CAT_ADVENTURE, RankType.RANK);
		
		CommonUtil.bindGoAppListEvent(this,
				this.findViewById(R.id.tb_sports), "体育竞技",
				"get_list_by_cid_rank&cid=" + Config.APP_CAT_SPORT,
				RankType.RANK);

		CommonUtil.bindGoAppListEvent(this,
				this.findViewById(R.id.tb_desk), "棋牌桌游",
				"get_list_by_cid_rank&cid=" + Config.APP_CAT_TABLE,
				RankType.RANK);
		
		CommonUtil.bindGoAppListEvent(this,
				this.findViewById(R.id.tb_manage), "经营策略",
				"get_list_by_cid_rank&cid=" + Config.APP_CAT_MANAGE, RankType.RANK);
		CommonUtil.bindGoAppListEvent(this,
				this.findViewById(R.id.tb_war), "射击飞行",
				"get_list_by_cid_rank&cid=" + Config.APP_CAT_SHOT, RankType.RANK);
		CommonUtil.bindGoAppListEvent(this,
				this.findViewById(R.id.tb_role), "角色扮演",
				"get_list_by_cid_rank&cid=" + Config.APP_CAT_RPG, RankType.RANK);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.more_cat, menu);
		return false;
	}

}
