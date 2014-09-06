package chen.smsmanager;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract.PhoneLookup;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import chen.smsother.Sms;

public class SearchableActivity extends ListActivity {

	
	private ListView mListView;
	
	private QueryHandler mQueryHandler;
	
	private SearchAdapter mAdapter;
	
	private final static String[] SMS_PROJECTION = new String[]{Sms._ID,Sms.ADDRESS,Sms.DATE,Sms.BODY};
	private final static int ID_COLUMN_INDEX = 0;
	private final static int ADDRESS_COLUMN_INDEX = 1;
	private final static int DATE_COLUMN_INDEX = 2;
	private final static int BODY_COLUMN_INDEX = 3;
	
	
	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	    Intent intent = getIntent();
	    
	    mListView = getListView();
	    
	    mQueryHandler = new QueryHandler(getContentResolver());
	    
	    mAdapter = new SearchAdapter(this, null);
	    
	    mListView.setAdapter(mAdapter);
	    
	    mListView.setBackgroundColor(Color.WHITE);
	    
	    if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
	      String query = intent.getStringExtra(SearchManager.QUERY);
	      doMySearch(query);
	      
	      Log.i("i", " query " + query);
	    }
	    
	    
	    mListView.setOnItemClickListener(new MyOnItemClickListener());
	}
	
	private final class MyOnItemClickListener implements OnItemClickListener{

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			Cursor cursor = (Cursor) mAdapter.getItem(position);
			String idStr = cursor.getString(ID_COLUMN_INDEX);
			
			Intent intent = new Intent(getApplicationContext(),SmsDetailActivity.class);
			intent.putExtra("_id", idStr);
			startActivity(intent);
		}
		
	}

	/**
	 * 执行搜索功能
	 * @param query
	 */
	private void doMySearch(String query) {
		// TODO Auto-generated method stub
		Uri uri = Sms.CONTENT_URI;
		// select * from table where body like '%love%'
		String selection = Sms.BODY + " like '%" + query + "%'";
		mQueryHandler.startQuery(0, null, uri, SMS_PROJECTION, selection, null, Sms.DATE + " desc");
	};

	
	private final class SearchViews{
		ImageView header;
		TextView tv_name;
		TextView tv_date;
		TextView tv_body;
	}
	
	private final class SearchAdapter extends CursorAdapter{
		
		private LayoutInflater mInflater;
		
		private long firstSecondOfToday;

		public SearchAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
			mInflater = LayoutInflater.from(context);
			
			Time time = new Time();
			time.setToNow();
			time.hour = 0;
			time.minute = 0;
			time.second = 0;
			
			firstSecondOfToday = time.toMillis(false);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = mInflater.inflate(R.layout.conversation_item, parent, false);
			
			SearchViews views = new SearchViews();
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
			SearchViews views = (SearchViews) view.getTag();
			
			
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
			
			mAdapter.changeCursor(cursor);
			
			initTitle();
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

	/**
	 * 初始化标题
	 */
	public void initTitle() {
		// TODO Auto-generated method stub
		int count = mAdapter.getCount();
		setTitle("查询的结果记录有" + count + "条");
	}
}
