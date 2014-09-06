package chen.smsmanager;

import java.util.HashMap;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.PhoneLookup;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import chen.smsother.Sms;

public class FolderListActivity extends Activity implements OnClickListener{

	private Button bt_new_msg;
	private ListView mListView;
	private int type;
	private int name;
	private QueryHandler mQueryHandler;
	
	private final static int SMS_INBOX_TYPE = 0;
	//private final static int SMS_OUTBOX_TYPE = 1;
	private final static int SMS_SENT_TYPE = 1;
	//private final static int SMS_DRAFT_TYPE = 3;
	
	private final static String[] SMS_PROJECTION = new String[]{Sms._ID,Sms.ADDRESS,Sms.DATE,Sms.BODY};
	private final static int ID_COLUMN_INDEX = 0;
	private final static int ADDRESS_COLUMN_INDEX = 1;
	private final static int DATE_COLUMN_INDEX = 2;
	private final static int BODY_COLUMN_INDEX = 3;
	
	private FolderListAdapter mAdapter;
	
	private HashMap<Integer, String> mDatePositionMap = new HashMap<Integer, String>();
	private HashMap<Integer, Integer> mPositionMap = new HashMap<Integer, Integer>();
	
	private Cursor mCursor;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.folder_list);
		
		bt_new_msg = (Button) findViewById(R.id.bt_new_msg);
		mListView = (ListView) findViewById(R.id.listview);
		
		type = getIntent().getIntExtra("type", 0);
		name = getIntent().getIntExtra("name", R.string.inbox);
		
		initTitle();
		
		mQueryHandler = new QueryHandler(this);
		
		mAdapter = new FolderListAdapter(this, null);
		
		mListView.setAdapter(mAdapter);
		
		startQuery();
		
		mListView.setOnItemClickListener(new MyOnItemClickListener());
	}
	
	private final class MyOnItemClickListener implements OnItemClickListener{

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			Cursor cursor = (Cursor) mAdapter.getItem(mPositionMap.get(position));
			String idStr = cursor.getString(ID_COLUMN_INDEX);
			
			Intent intent = new Intent(getApplicationContext(),SmsDetailActivity.class);
			intent.putExtra("_id", idStr);
			startActivity(intent);
		}
		
	}
	
	/**
	 * 初始化标题
	 */
	private void initTitle() {
		// TODO Auto-generated method stub
		setTitle(name);
	}


	private void startQuery() {
		// TODO Auto-generated method stub
		Uri uri = null;
		switch (type) {
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
	
			default:
				break;
		}
		
		mQueryHandler.startQuery(0, null, uri, SMS_PROJECTION, null, null, Sms.DATE + " desc");
	}

	private final class FolderListViews{
		ImageView header;
		TextView tv_name;
		TextView tv_date;
		TextView tv_body;
	}
	
	private final class FolderListAdapter extends CursorAdapter{

		private LayoutInflater mInflater;
		private long firstSecondOfToday;
		private Context mContext;
		
		public FolderListAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
			this.mContext = context;
			mInflater = LayoutInflater.from(context);
			
			Time time = new Time();
			time.setToNow();
			time.hour = 0;
			time.minute = 0;
			time.second = 0;
			
			firstSecondOfToday = time.toMillis(false);
		}
		
		
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			// TODO Auto-generated method stub
			if(mDatePositionMap.containsKey(position)){
				TextView tv = (TextView) mInflater.inflate(R.layout.list_separator, parent, false);
				tv.setText(mDatePositionMap.get(position));
				//tv.setFocusable(false);
				//tv.setClickable(false);
				tv.setOnClickListener(null);
				return tv;
			}
	        if (!mCursor.moveToPosition(mPositionMap.get(position))) {
	            throw new IllegalStateException("couldn't move cursor to position " + position);
	        }
	        View v;
	        if (convertView == null || convertView.getTag() == null) {
	            v = newView(mContext, mCursor, parent);
	        } else {
	            v = convertView;
	        }
	        bindView(v, mContext, mCursor);
	        return v;
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			View view = mInflater.inflate(R.layout.conversation_item, parent, false);
			
			FolderListViews views = new FolderListViews();
			views.header = (ImageView) view.findViewById(R.id.header);
			views.tv_name = (TextView) view.findViewById(R.id.tv_name);
			views.tv_date = (TextView) view.findViewById(R.id.tv_date);
			views.tv_body = (TextView) view.findViewById(R.id.tv_body);
			
			
			view.setTag(views);
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub
			FolderListViews views = (FolderListViews) view.getTag();
			
			
			String address = cursor.getString(ADDRESS_COLUMN_INDEX);
			long date = cursor.getLong(DATE_COLUMN_INDEX);
			String body = cursor.getString(BODY_COLUMN_INDEX);
			
			//根据电话号码查询联系人
			String name = null;
			Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
			Cursor contact_cursor = getContentResolver().query(uri, new String[]{PhoneLookup.DISPLAY_NAME}, null, null, null);
			if(contact_cursor.moveToFirst()){
				name = contact_cursor.getString(0);
			}
			contact_cursor.close();
			
			if(name != null){
				views.header.setImageResource(R.drawable.ic_contact_picture);
				views.tv_name.setText(name);
			}else{
				views.header.setImageResource(R.drawable.ic_unknown_picture_normal);
				views.tv_name.setText(address);
			}
			
			String dateStr = null;
			if((date - firstSecondOfToday > 0) && (date - firstSecondOfToday < DateUtils.DAY_IN_MILLIS)){
				//show time
				dateStr = DateFormat.getTimeFormat(context).format(date);
			}else{
				// show date
				dateStr = DateFormat.getDateFormat(context).format(date);
			}
			
			views.tv_date.setText(dateStr);
			
			views.tv_body.setText(body);
			
		}
		
		
		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return super.getCount() + mDatePositionMap.size();
		}
		
	}

	private final class QueryHandler extends AsyncQueryHandler{

		private Context context;
		public QueryHandler(Context context) {
			super(context.getContentResolver());
			// TODO Auto-generated constructor stub
			this.context = context;
		}
		
		
		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			// TODO Auto-generated method stub
			super.onQueryComplete(token, cookie, cursor);
			if(cursor == null){
				return ;
			}
			
			//在这里完成短信数据的迭代
			int size = 0;
			while(cursor.moveToNext()){
				//item原来的位置
				int position = cursor.getPosition();
				//取出日期
				long date = cursor.getLong(DATE_COLUMN_INDEX);
				String dateStr = DateFormat.getDateFormat(context).format(date);
				if(!mDatePositionMap.containsValue(dateStr)){
					mDatePositionMap.put(position + size, dateStr);
					size++;
				}
				mPositionMap.put(position + size, position);
			}
			//把cursor还原
			cursor.move(-1);
			mCursor = cursor;
			mAdapter.changeCursor(cursor);
			
			
			
		}
	}
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Cursor cursor = mAdapter.getCursor();
		if(cursor != null && !cursor.isClosed()){
			cursor.close();
		}
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch (id) {
			case R.id.bt_new_msg:
				Intent intent = new Intent(this,NewMessageActivity.class);
				startActivity(intent);
				break;
	
			default:
				break;
		}
	}
}
