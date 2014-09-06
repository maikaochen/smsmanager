package chen.smsmanager;

import android.app.ListActivity;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import chen.smsother.Groups;
import chen.smsother.Thread_Groups;

public class GroupActivity extends ListActivity {
	
	private ListView mListView;
	private QueryHandler mQueryHandler;
	private GroupsAdapter mAdapter;
	private final static String[] GROUP_PROJECTION = new String[]{Groups._ID,Groups.GROUP_NAME};
	private final static int ID_COLUMN_INDEX = 0;
	private final static int GROUP_NAME_COLUMN_INDEX = 1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mListView = getListView();
		
		mQueryHandler = new QueryHandler(getContentResolver());
		
		mAdapter = new GroupsAdapter(this, null);
		
		mListView.setAdapter(mAdapter);
		
		startQuery();
		
		
		mListView.setOnItemClickListener(new MyOnItemClickListener());
	}
	
	private final class MyOnItemClickListener implements OnItemClickListener{

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
		   	// TODO Auto-generated method stub
			
			Cursor cursor = (Cursor) mAdapter.getItem(position);
			String group_id = cursor.getString(ID_COLUMN_INDEX);
			String group_name = cursor.getString(GROUP_NAME_COLUMN_INDEX);
			
			Uri uri = Thread_Groups.CONTENT_URI;
			String selection = Thread_Groups.GROUP_ID + " = ?";
			String[] selectionArgs = new String[]{group_id};
			Cursor collection_cursor = getContentResolver().query(uri, new String[]{Thread_Groups.THREAD_ID}, selection, selectionArgs, null);
			if(collection_cursor.getCount() > 0){
				// v构造 (1,2,4) 
				StringBuilder sb = new StringBuilder("(");
				//(
				while(collection_cursor.moveToNext()){
					String thread_id = collection_cursor.getString(0);
					sb.append(thread_id);
					sb.append(",");
				}
				//(1,2,4,
				
				sb.deleteCharAt(sb.length() - 1);
				//(1,2,4
				
				sb.append(")");
				//(1,2,4)
				collection_cursor.close();
				Intent intent = new Intent(getApplicationContext(),ConversationActivity.class);
				intent.putExtra("thread_ids", sb.toString());
				intent.putExtra("group_name", group_name);
				startActivity(intent);
			}else{
				collection_cursor.close();
				Toast.makeText(getApplicationContext(), R.string.no_collection_convesation, Toast.LENGTH_SHORT).show();
			}

		}
		
	}
	
	
	/**
	 * 异步的查询群组
	 */
	private void startQuery() {
		// TODO Auto-generated method stub
		Uri uri = Groups.CONTENT_URI;
		mQueryHandler.startQuery(0, null, uri, GROUP_PROJECTION, null, null, null);
	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		MenuInflater mInflater = getMenuInflater();
		
		mInflater.inflate(R.menu.group_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		int id = item.getItemId();
		
		switch (id) {
			case R.id.menu_new_group:
				Intent intent = new Intent(this,NewGroupActivity.class);
				startActivity(intent);
				break;
	
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	
	private final class GroupsViews{
		ImageView header;
		TextView tv_name;
	}
	
	private final class GroupsAdapter extends CursorAdapter{

		private LayoutInflater mInflater;
		public GroupsAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
			
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			View view = mInflater.inflate(R.layout.folder_item, parent, false);
			GroupsViews views = new GroupsViews();
			views.header = (ImageView) view.findViewById(R.id.header);
			views.tv_name = (TextView) view.findViewById(R.id.tv_name);
			view.setTag(views);
			return view;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub
			GroupsViews views = (GroupsViews) view.getTag();
			
			String group_name = cursor.getString(GROUP_NAME_COLUMN_INDEX);
			
			views.header.setImageResource(R.drawable.tab_folder);
			views.tv_name.setText(group_name);
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
}
