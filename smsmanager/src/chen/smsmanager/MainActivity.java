package chen.smsmanager;

import android.app.TabActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Window;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;

public class MainActivity extends TabActivity {
	
	private TabHost mTabHost;
	private SharedPreferences sp;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);//没有标题
        setContentView(R.layout.main);
        
        mTabHost = getTabHost();
        
        sp = getSharedPreferences("config", Context.MODE_PRIVATE);
        
        setupConversationTab();
        setupFolderTab();
        setupGroupTab();
        
        //设置tab切换的监听
        mTabHost.setOnTabChangedListener(new MyOnTabChangedListener());
        
        String tag = sp.getString("tag", "");
        if("".equals(tag)){
        	mTabHost.setCurrentTab(0);
        }else{
            mTabHost.setCurrentTabByTag(tag);
        }


    }
    

	private final class MyOnTabChangedListener implements OnTabChangeListener{

    	/**
    	 * tabId 就是tab所对应的tag值
    	 */
		public void onTabChanged(String tabId) {
			// TODO Auto-generated method stub
			Log.i("i", tabId);
			String tag = mTabHost.getCurrentTabTag();
			Editor editor = sp.edit();
			editor.putString("tag", tag);
			editor.commit();
		}
    	
    }

    /**
     * 添加会话tab
     */
	private void setupConversationTab() {
		// TODO Auto-generated method stub
		TabSpec mTabSpec = mTabHost.newTabSpec("convesation");
		mTabSpec.setIndicator(getString(R.string.conversation), getResources().getDrawable(R.drawable.tab_conversation));
		Intent intent = new Intent(this,ConversationActivity.class);
		mTabSpec.setContent(intent);
		mTabHost.addTab(mTabSpec);
	}

	/**
	 * 添加文件夹tab
	 */
	private void setupFolderTab() {
		// TODO Auto-generated method stub
		TabSpec mTabSpec = mTabHost.newTabSpec("folder");
		mTabSpec.setIndicator(getString(R.string.folder), getResources().getDrawable(R.drawable.tab_folder));
		Intent intent = new Intent(this,FolderActivity.class);
		mTabSpec.setContent(intent);
		mTabHost.addTab(mTabSpec);
	}

	/**
	 * 添加群组tab
	 */
	private void setupGroupTab() {
		// TODO Auto-generated method stub
		TabSpec mTabSpec = mTabHost.newTabSpec("group");
		mTabSpec.setIndicator(getString(R.string.group), getResources().getDrawable(R.drawable.tab_group));
		Intent intent = new Intent(this,GroupActivity.class);
		mTabSpec.setContent(intent);
		mTabHost.addTab(mTabSpec);
	}
}