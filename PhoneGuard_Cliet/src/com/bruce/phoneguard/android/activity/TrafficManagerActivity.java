package com.bruce.phoneguard.android.activity;

import java.util.ArrayList;
import java.util.List;

import com.bruce.phoneguard.android.R;
import com.bruce.phoneguard.android.model.AppInfo;
import com.bruce.phoneguard.android.model.parser.AppInfoParser;

import android.net.TrafficStats;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class TrafficManagerActivity extends BaseActivity {

	private ListView listView = null;
	private List<AppInfo> infos;
	private TrafficAdapter adapter = null;
	private LinearLayout loadingView = null;

	private LayoutInflater inflater;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_traffic_manager);
		initView();
		initData();
	}

	@Override
	public void initView() {
		listView = (ListView) findViewById(R.id.lv_traffic_manger);
		loadingView = (LinearLayout) findViewById(R.id.ll_loading);
	}

	@Override
	public void initData() {
		mActionBar.setTitle("流量管理");
		mActionBar.setIcon(R.drawable.selector_back_imagview);

		adapter = new TrafficAdapter();
		infos = new ArrayList<AppInfo>();
		listView.setAdapter(adapter);
		inflater = getLayoutInflater();
		fillData();
		showList(false);
	}

	private void fillData() {
		new Thread() {
			public void run() {
				infos = AppInfoParser.getAppInfos(TrafficManagerActivity.this);
				handler.sendEmptyMessage(0);
			};
		}.start();
	}

	private void showList(boolean isShow) {
		if (isShow) {
			loadingView.setVisibility(View.INVISIBLE);
			listView.setVisibility(View.VISIBLE);
		} else {
			loadingView.setVisibility(View.VISIBLE);
			listView.setVisibility(View.INVISIBLE);
		}

	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			adapter.notifyDataSetChanged();
			showList(true);
		};
	};

	class TrafficAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return infos.size();
		}

		@Override
		public Object getItem(int position) {
			return infos.get(position);
		}

		@Override
		public long getItemId(int position) {
			return infos.get(position).getUid();
		}

		@Override
		public View getView(int position, View v, ViewGroup parent) {
			ViewHolder holder = null;
			if (v == null) {
				holder = new ViewHolder();
				v = inflater.inflate(R.layout.item_traffic_info, null);
				holder.imgView = (ImageView) v.findViewById(R.id.iv_app_icon);
				holder.titleTxt = (TextView) v.findViewById(R.id.tv_app_name);
				holder.trafficTxt = (TextView) v.findViewById(R.id.tv_traffic_size);
				holder.uploadTxt = (TextView) v.findViewById(R.id.tv_upload);
				holder.downloadTxt = (TextView) v.findViewById(R.id.tv_download);
				v.setTag(holder);
			} else {
				holder = (ViewHolder) v.getTag();
			}
			AppInfo info = infos.get(position);
			double tx = TrafficStats.getUidTxBytes(info.getUid()) / (1024 * 1024) * 1000.0 / 1000.0;
			double rx = TrafficStats.getUidRxBytes(info.getUid()) / (1024 * 1024) * 1000.0 / 1000.0;
			holder.imgView.setImageDrawable(info.getIcon());
			holder.titleTxt.setText(info.getName());
			holder.trafficTxt.setText((tx + rx) + " M");
			holder.uploadTxt.setText(tx + "	M");
			holder.downloadTxt.setText(rx + " M");

			return v;
		}

	}

	class ViewHolder {
		private ImageView imgView;
		private TextView titleTxt;
		private TextView trafficTxt;
		private TextView uploadTxt;
		private TextView downloadTxt;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
