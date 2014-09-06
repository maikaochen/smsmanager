package chen.smsmanager;

import java.util.HashMap;

import android.app.ListActivity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import chen.smsother.Sms;

public class FolderActivity extends ListActivity {

	private ListView mListView;
	private FolderAdapter mAdapter;
	private QueryHandler mQueryHandler;
	
	//private final static int[] images = new int[]{R.drawable.a_f_inbox,R.drawable.a_f_outbox,R.drawable.a_f_sent,R.drawable.a_f_draft};
	//private final static int[] names = new int[]{R.string.inbox,R.string.outbox,R.string.sent,R.string.draft};
	
	private final static int[] images = new int[]{R.drawable.a_f_inbox,R.drawable.a_f_sent};
	private final static int[] names = new int[]{R.string.inbox,R.string.sent};
	
	private final static int SMS_INBOX_TYPE = 0;
	//private final static int SMS_OUTBOX_TYPE = 1;
	private final static int SMS_SENT_TYPE = 1;
	//private final static int SMS_DRAFT_TYPE = 3;
	private static final int MENU_SEARCH_ID = 0;
	
	private HashMap<Integer,Integer> sizeMap = new HashMap<Integer, Integer>();
	
	private MyContentObserver observer;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mListView = getListView();
		mAdapter = new FolderAdapter();
		mListView.setAdapter(mAdapter);
		mQueryHandler = new QueryHandler(getContentResolver());
		
		observer = new MyContentObserver(new Handler());
		
		initSizeMap();
		
		startQuery();
		
		mListView.setOnItemClickListener(new MyOnItemClickListener());
	}
	
	private final class MyOnItemClickListener implements OnItemClickListener{

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			//如果里面有数据，就进入
			int count = sizeMap.get(position);
			if(count > 0){
				Intent intent = new Intent(getApplicationContext(),FolderListActivity.class);
				intent.putExtra("type", position);
				intent.putExtra("name", names[position]);
				startActivity(intent);
			}
		}
		
	}
	
	
	//初始化size集合的大小
	private void initSizeMap() {
		// TODO Auto-generated method stub
		for(int i = 0;i< images.length;i++){
			sizeMap.put(i, 0);
		}
		
	}

	//执行查询，分四次进行查询
	private void startQuery() {
		// TODO Auto-generated method stub
		for(int i = 0;i< images.length;i++ ){
			Uri uri = null;
			switch (i) {
				case SMS_INBOX_TYPE:
					uri = Sms.Inbox.CONTENT_URI;
					break;
				//case SMS_OUTBOX_TYPE:
				//	uri = Sms.Outbox.CONTENT_URI;
				//	break;
				case SMS_SENT_TYPE:
					uri = Sms.Sent.CONTENT_URI;
					break;
				//case SMS_DRAFT_TYPE:
				//	uri = Sms.Draft.CONTENT_URI;
				//	break;
			}
			mQueryHandler.startQuery(i, null, uri, null, null, null, null);
		}
		
	}
	
	
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		
		//注册短信的内容观察者
		//content://sms    
		Uri uri = Uri.parse("content://mms-sms/conversations/");
		getContentResolver().registerContentObserver(uri, false, observer);
	}
	
	
	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		getContentResolver().unregisterContentObserver(observer);
	}
	
	
	
	private final class MyContentObserver extends ContentObserver{

		public MyContentObserver(Handler handler) {
			super(handler);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void onChange(boolean selfChange) {
			// TODO Auto-generated method stub
			super.onChange(selfChange);
			Log.i("i", " on change");
			startQuery();
		}
		
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu.add(0, MENU_SEARCH_ID, 0, R.string.search);
		return super.onCreateOptionsMenu(menu);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		int id = item.getItemId();
		switch (id) {
			case MENU_SEARCH_ID:
				onSearchRequested();
				break;
	
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	

	private final class FolderViews{
		ImageView header;
		TextView tv_name;
		TextView tv_size;
	}
	
	private final class FolderAdapter extends BaseAdapter{

		public int getCount() {
			// TODO Auto-generated method stub
			return images.length;
		}

		public Object getItem(int position) {
			// TODO Auto-generated method stub
			return names[position];
		}

		public long getItemId(int position) {
			// TODO Auto-generated method stub
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			//初始化控件
			View view = null;
			FolderViews views = null;
			if(convertView != null){
				view = convertView;
				views = (FolderViews) view.getTag();
			}else{
				view = getLayoutInflater().inflate(R.layout.folder_item, parent, false);
				views = new FolderViews();
				views.header = (ImageView) view.findViewById(R.id.header);
				views.tv_name = (TextView) view.findViewById(R.id.tv_name);
				views.tv_size = (TextView) view.findViewById(R.id.tv_size);
				view.setTag(views);
			}

			//绑定数据
			views.header.setImageResource(images[position]);
			views.tv_name.setText(names[position]);
			
			views.tv_size.setText(String.valueOf(sizeMap.get(position)));
			
			
			return view;
		}
		
	}
	
	private final class QueryHandler extends AsyncQueryHandler{

		public QueryHandler(ContentResolver cr) {
			super(cr);
			// TODO Auto-generated constructor stub
		}
		
		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			// TODO Auto-generated method stub
			super.onQueryComplete(token, cookie, cursor);
			if(cursor.getCount() > 0){
				int count = cursor.getCount();
				sizeMap.put(token, count);
				mAdapter.notifyDataSetChanged();
			}
			cursor.close();
		}
	}
	
}
