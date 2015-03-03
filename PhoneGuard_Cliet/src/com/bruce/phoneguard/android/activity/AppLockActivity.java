package com.bruce.phoneguard.android.activity;

import android.app.Activity;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.bruce.phoneguard.android.R;
import com.bruce.phoneguard.android.dao.AppLockDao;
import com.bruce.phoneguard.android.model.AppInfo;
import com.bruce.phoneguard.android.model.parser.AppInfoParser;

import java.util.List;

public class AppLockActivity extends BaseActivity {
    private ListView lv;
	private List<AppInfo> appinfos;
	private AppLockAdapter adapter;
	private AppLockDao dao;
	private LinearLayout ll_app_manager_loading;
	
	private List<String> lockappinfos ;
	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			ll_app_manager_loading.setVisibility(View.INVISIBLE);
			adapter = new AppLockAdapter();
			lv.setAdapter(adapter);
		}

	};

    public AppLockActivity() {
    }

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.app_lock);
        initView();
        initData();
		initUI();
        setListener();
	}

    private void setListener() {
        lv.setOnItemClickListener(new OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                TranslateAnimation ta = new TranslateAnimation(
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.0f,
                        Animation.RELATIVE_TO_SELF, 0.0f);
                ta.setDuration(500);
                view.startAnimation(ta);
                ImageView iv = (ImageView) view.findViewById(R.id.iv_app_lock_status);

                // 传递当前要锁定程序的包名
                AppInfo info = (AppInfo) lv.getItemAtPosition(position);
                String packname = info.getPackname();
                if(dao.find(packname)){
                    // 移除这个条目
                    //dao.delete(packname);
                    getContentResolver().delete(Uri.parse("content://com.bruce.applockprovider/delete"), null, new String[]{packname});
                    lockappinfos.remove(packname);
                    iv.setImageResource(R.drawable.strongbox_app_lock_ic_unlock);
                }else{
                    //dao.add(packname);
                    lockappinfos.add(packname);
                    ContentValues values = new ContentValues();
                    values.put("packname", packname);
                    getContentResolver().insert(Uri.parse("content://com.bruce.applockprovider/insert"), values);
                    iv.setImageResource(R.drawable.strongbox_app_lock_ic_locked);
                }

            }
        });
    }

    @Override
    public void initView() {
        ll_app_manager_loading = (LinearLayout) this.findViewById(R.id.ll_app_manager_loading);
        lv = (ListView) this.findViewById(R.id.lv_app_lock);

    }

    @Override
    public void initData() {
        mActionBar.setTitle("程序加锁");
        mActionBar.setIcon(R.drawable.selector_back_imagview);
        dao = new AppLockDao(this);
        lockappinfos = dao.getAllApps();
    }

    private void initUI() {
		ll_app_manager_loading.setVisibility(View.VISIBLE);
		new Thread() {
			@Override
			public void run() {
				appinfos = AppInfoParser.getAppInfos(AppLockActivity.this);
				handler.sendEmptyMessage(0);
			}
		}.start();

	}

	private class AppLockAdapter extends BaseAdapter {

		public int getCount() {

			return appinfos.size();
		}

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return appinfos.get(position);
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			View view = null;
			if (convertView == null) {
				view = View.inflate(getApplicationContext(),
						R.layout.lock_app_item, null);
			} else {
				view = convertView;
			}
			//更改view对象的状态
			AppInfo info = appinfos.get(position);
			ImageView iv = (ImageView) view.findViewById(R.id.iv_app_icon);
			TextView tv = (TextView) view.findViewById(R.id.tv_app_name);
			ImageView iv_lock_status = (ImageView) view.findViewById(R.id.iv_app_lock_status);
			TextView tv_pack_name =  (TextView) view.findViewById(R.id.tv_app_packname);
			tv_pack_name.setText(info.getPackname());
			if(lockappinfos.contains(info.getPackname())){
				iv_lock_status.setImageResource(R.drawable.strongbox_app_lock_ic_locked);
			}else{
				iv_lock_status.setImageResource(R.drawable.strongbox_app_lock_ic_unlock);
			}
			iv.setImageDrawable(info.getIcon());
			tv.setText(info.getName());
			return view;
		}

	}
}
