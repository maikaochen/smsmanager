package chen.smsmanager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.ContactsContract.PhoneLookup;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import chen.smsother.Groups;
import chen.smsother.Sms;
import chen.smsother.Thread_Groups;

public class ConversationActivity extends Activity implements OnClickListener{
	
	private Button bt_new_msg;
	private Button bt_all_select;
	private Button bt_cancel_selected;
	private Button bt_delete;
	
	private LinearLayout mEdit;
	private ListView mListView;
	private TextView tv_empty;
	
	private QueryHandler mQueryHandler;
	private ConversationAdapter mAdapter;
	
	//短信会话的投影
	private final static String[] CONVERSATION_PROJECTION = new String[]{"sms.thread_id as _id",
		"snippet",
		"msg_count",
		"sms.address as address",
		"sms.date as date"};
	
	private final static int ID_COLUMN_INDEX = 0;
	private final static int SNIPPET_COLUMN_INDEX = 1;
	private final static int MSG_COUNT_COLUMN_INDEX = 2;
	private final static int ADDRESS_COLUMN_INDEX = 3;
	private final static int DATE_COLUMN_INDEX = 4;
	
	//联系人的投影
	private final static String[] CONTACT_PROJECTION = new String[]{PhoneLookup.DISPLAY_NAME};
	private final static int DISPLAY_NAME_COLUMN_INDEX = 0;
	
	
	//群组的投影
	private final static String[] GROUP_PROJECTION = new String[]{Groups._ID,Groups.GROUP_NAME};
	private final static int GROUP_NAME_COLUMN_INDEX = 1;
	
	
	private static final int MENU_SEARCH_ID = Menu.NONE + 1;
	private static final int MENU_DELETE_ID = Menu.NONE + 2;
	private static final int MENU_BACK_ID = Menu.NONE + 3;
	
	private MenuItem menu_item_search;
	private MenuItem menu_item_delete;
	private MenuItem menu_item_back;
	
	//Activity的显示模式
	enum DISPLAYMODE{
		list,edit
	};
	private DISPLAYMODE mode = DISPLAYMODE.list;
	
	private HashSet<String> mMultSelected = new HashSet<String>();
	
	private ProgressDialog mProgressDialog;
	
	private boolean delete = true;
	
	private String thread_ids;
	private String group_name;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.conversation);
		
		thread_ids = getIntent().getStringExtra("thread_ids");
		group_name = getIntent().getStringExtra("group_name");
		
		initTitle();
		
		initView();
		
		startQuery();
	}

	/**
	 * 初始化标题
	 */
    private void initTitle() {
		// TODO Auto-generated method stub
		if(group_name != null){
			setTitle(group_name);
		}
	}

	/**
     * 查询短信的数据
     * getContentResolver().query();
     * managedQuery(uri, projection, selection, selectionArgs, sortOrder) 不用手动的去管理cursor,让activity去帮我们管理
     * 如果直接查询，就是在主线程进行的。
     * 可以采用android提供的异步框架,
     * 使用范围：只能去访问我们的ContentProvider所提供的数据
     */
    private void startQuery() {
		// TODO Auto-generated method stub
    	Uri uri = Sms.CONVERSATION_URI;
    	
    	// select * from table where thread_id in (1,2,4)
    	if(thread_ids != null){
    		String where = Sms.THREAD_ID + " in " + thread_ids;
        	mQueryHandler.startQuery(0, null, uri, CONVERSATION_PROJECTION, where, null, " date desc");
    	}else{
        	mQueryHandler.startQuery(0, null, uri, CONVERSATION_PROJECTION, null, null, " date desc");
    	}

		
	}


	/**
     * 初始化控件
     */
	private void initView() {
		// TODO Auto-generated method stub
		bt_new_msg = (Button) findViewById(R.id.bt_new_msg);
		bt_all_select = (Button) findViewById(R.id.bt_all_select);
		bt_cancel_selected = (Button) findViewById(R.id.bt_cancel_selected);
		bt_delete = (Button) findViewById(R.id.bt_delete);
		
		//设置点击监听事件
		bt_new_msg.setOnClickListener(this);
		bt_all_select.setOnClickListener(this);
		bt_cancel_selected.setOnClickListener(this);
		bt_delete.setOnClickListener(this);
		
		mEdit = (LinearLayout) findViewById(R.id.edit);
		
		mListView = (ListView) findViewById(R.id.listview);
		tv_empty = (TextView) findViewById(R.id.empty);
		
		if (thread_ids != null) {
			bt_new_msg.setVisibility(View.GONE);
		}
		
		mEdit.setVisibility(View.GONE);
		bt_delete.setVisibility(View.GONE);
		
		//当listview里面数据为空的时候，显示一个布局
		mListView.setEmptyView(tv_empty);
		
		mQueryHandler = new QueryHandler(getContentResolver());
		
		mAdapter = new ConversationAdapter(this, null);
		
		mListView.setAdapter(mAdapter);
		
		//给listview设置条目点击事件
		mListView.setOnItemClickListener(new MyOnItemClickListener());
		
		//设置listview长按事件
		mListView.setOnItemLongClickListener(new MyOnItemLongClickListener());
		
	}
	
	
	private final class MyOnItemLongClickListener implements OnItemLongClickListener{

		public boolean onItemLongClick(AdapterView<?> parent, View view,
				int position, long id) {
			// TODO Auto-generated method stub
			if(mode == DISPLAYMODE.list){
				//得到会话的id
				Cursor cursor = (Cursor) mAdapter.getItem(position);
				final String thread_id = cursor.getString(ID_COLUMN_INDEX);
				//查询群组数据
				final HashMap<String,String> groupsMap = new HashMap<String,String>();
				Uri uri = Groups.CONTENT_URI;
				Cursor group_cursor = getContentResolver().query(uri, GROUP_PROJECTION, null, null, null);
				if(group_cursor.getCount() > 0){
					while(group_cursor.moveToNext()){
						String group_id = group_cursor.getString(ID_COLUMN_INDEX);
						String group_name = group_cursor.getString(GROUP_NAME_COLUMN_INDEX);
						groupsMap.put(group_name, group_id);
					}
					group_cursor.close();
				}else{
					group_cursor.close();
					Toast.makeText(getApplicationContext(), R.string.please_create_group, Toast.LENGTH_LONG).show();
					return true;
				}
				//构建群组数组
				int i = 0;
				final String[] groups = new String[groupsMap.size()];
				for(Map.Entry<String, String> entry:groupsMap.entrySet()){
					groups[i] = entry.getKey();
					i++;
				}
				
				AlertDialog.Builder builder = new AlertDialog.Builder(ConversationActivity.this);
				
				builder.setTitle(R.string.select_collection_group);
				builder.setItems(groups, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						String group_name = groups[which];
						String group_id = groupsMap.get(group_name);
						
						//把会话收藏到群组
						/**
						 * 1 先判断会话是否已经收藏到了该群组
						 * 
						 */
						Uri uri = Thread_Groups.CONTENT_URI;
						String selection = Thread_Groups.THREAD_ID + " = ? and " + Thread_Groups.GROUP_ID + " = ?";
						String[] selectionArgs = new String[]{thread_id,group_id};
						Cursor exist_cursor = getContentResolver().query(uri, null, selection, selectionArgs, null);
						if(exist_cursor.moveToFirst()){
							Toast.makeText(getApplicationContext(), R.string.exist_collection_group, Toast.LENGTH_SHORT).show();
							exist_cursor.close();
						}else{
							//不存在
							ContentValues values = new ContentValues();
							values.put(Thread_Groups.THREAD_ID, thread_id);
							values.put(Thread_Groups.GROUP_ID, group_id);
							getContentResolver().insert(uri, values);
							Toast.makeText(getApplicationContext(), R.string.success_collection_group, Toast.LENGTH_SHORT).show();
							exist_cursor.close();
						}
					}
				});
				
				AlertDialog dialog = builder.create();
				dialog.show();
			}
			return true;
		}
		
	}
	
	
	private final class MyOnItemClickListener implements OnItemClickListener{

		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// TODO Auto-generated method stub
			Cursor cursor = (Cursor) mAdapter.getItem(position);
			String idStr = cursor.getString(ID_COLUMN_INDEX);
			if(mode == DISPLAYMODE.edit){
				ConversationViews views = (ConversationViews) view.getTag();
				CheckBox checkbox = views.checkbox;
				if(mMultSelected.contains(idStr)){
					mMultSelected.remove(idStr);
					checkbox.setChecked(false);
				}else{
					mMultSelected.add(idStr);
					checkbox.setChecked(true);
				}
				
				if(mMultSelected.size() > 0){
					bt_cancel_selected.setEnabled(true);
					bt_delete.setEnabled(true);
				}else{
					bt_cancel_selected.setEnabled(false);
					bt_delete.setEnabled(false);
				}
				
				if(mMultSelected.size() == mAdapter.getCount()){
					bt_all_select.setEnabled(false);
				}else{
					bt_all_select.setEnabled(true);
				}
			}else{
				Intent intent = new Intent(ConversationActivity.this,ConversastionListActivity.class);
				intent.putExtra("thread_id", idStr);
				startActivity(intent);
			}

		}
		
	}
	
	private final class ConversationViews{
		CheckBox checkbox;
		ImageView header;
		TextView tv_name;
		TextView tv_body;
		TextView tv_date;
	}
	
	private final class ConversationAdapter extends CursorAdapter{
		
		private LayoutInflater mInflater;
		private long firstSecondOfToday;//今天起始时间的毫秒数

		public ConversationAdapter(Context context, Cursor c) {
			super(context, c);
			// TODO Auto-generated constructor stub
//			mInflater = getLayoutInflater();
//			mInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mInflater = LayoutInflater.from(context);
			
			Time time = new Time();
			time.setToNow();
			time.hour = 0;
			time.minute = 0;
			time.second = 0;
			//false:如果我们修改的是time对象的时间，那么转化为毫秒数，才准确   true:我们修改了time对象的日期
			firstSecondOfToday = time.toMillis(false);
		}

		/**
		 * 创建ListView的item布局,只会被调用一次
		 */
		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			// TODO Auto-generated method stub
			/**
			 * 加载布局的时候，如果指定true，就是把加载进来的布局添加到父元素身上
			 * 如果指定的是false,就是不添加到父元素身上，如果我们希望使用布局不会本身的宽高，还是需要给定parent
			 */
			View view = mInflater.inflate(R.layout.conversation_item, parent, false);
			//View view = View.inflate(context, R.layout.conversation_item, parent);
			ConversationViews views = new ConversationViews();
			views.checkbox= (CheckBox) view.findViewById(R.id.checkbox);
			views.header = (ImageView) view.findViewById(R.id.header);
			views.tv_name = (TextView) view.findViewById(R.id.tv_name);
			views.tv_date = (TextView) view.findViewById(R.id.tv_date);
			views.tv_body = (TextView) view.findViewById(R.id.tv_body);
			
			view.setTag(views);
			
			return view;
		}

		/**
		 * 给item绑定数据
		 */
		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// TODO Auto-generated method stub
			//得到控件
/*			未优化之前的代码
 *          CheckBox checkBox = (CheckBox) view.findViewById(R.id.checkbox);
			ImageView header = (ImageView) view.findViewById(R.id.header);
			TextView tv_name = (TextView) view.findViewById(R.id.tv_name);
			TextView tv_date = (TextView) view.findViewById(R.id.tv_date);
			TextView tv_body = (TextView) view.findViewById(R.id.tv_body);*/
			
			ConversationViews views = (ConversationViews) view.getTag();
			CheckBox checkBox = views.checkbox;
			ImageView header = views.header;
			TextView tv_name = views.tv_name;
			TextView tv_date = views.tv_date;
			TextView tv_body = views.tv_body;
			
			//得到数据
			String idStr = cursor.getString(ID_COLUMN_INDEX);
			String address = cursor.getString(ADDRESS_COLUMN_INDEX);
			int msg_count = cursor.getInt(MSG_COUNT_COLUMN_INDEX);
			long date = cursor.getLong(DATE_COLUMN_INDEX);
			String body = cursor.getString(SNIPPET_COLUMN_INDEX);
			
			//根据电话号码去查询联系人的姓名
			String name = null;
			Uri uri = Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address));
			Cursor contactCursor = getContentResolver().query(uri, CONTACT_PROJECTION, null, null, null);
			if(contactCursor.moveToFirst()){
				name = contactCursor.getString(DISPLAY_NAME_COLUMN_INDEX);
			}
			contactCursor.close();
			
			if(mode == DISPLAYMODE.list){
				checkBox.setVisibility(View.GONE);
			}else{
				checkBox.setVisibility(View.VISIBLE);
				if(mMultSelected.contains(idStr)){
					checkBox.setChecked(true);
				}else{
					checkBox.setChecked(false);
				}
			}
			
			
			
			//把数据绑定给控件
			if(name != null){
				header.setImageResource(R.drawable.ic_contact_picture);
				if(msg_count > 1){
					tv_name.setText(name + "(" + msg_count + ")");
				}else{
					tv_name.setText(name);
				}
			}else{
				header.setImageResource(R.drawable.ic_unknown_picture_normal);
				if(msg_count > 1){
					tv_name.setText(address + "(" + msg_count + ")");
				}else{
					tv_name.setText(address);
				}
			}
						
			


			//绑定日期：1 如果是今天的日期，那么我们就显示时间，如果不是就显示日期。2 显示的风格应该和系统保持一致
			String dateStr = null;
			if((date - firstSecondOfToday > 0) && (date - firstSecondOfToday < DateUtils.DAY_IN_MILLIS)){
				//显示时间
				dateStr = DateFormat.getTimeFormat(context).format(date);
			}else{
				//显示日期
				dateStr = DateFormat.getDateFormat(context).format(date);
			}
			tv_date.setText(dateStr);
			
			
			tv_body.setText(body);
		}
		
	}
	
	/**
	 * 当第一次点击menu的时候调用
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		menu_item_search = menu.add(0, MENU_SEARCH_ID, 0, R.string.search);
		menu_item_delete = menu.add(0, MENU_DELETE_ID, 0, R.string.delete);
		menu_item_back = menu.add(0, MENU_BACK_ID, 0, R.string.back);
		return super.onCreateOptionsMenu(menu);
	}
	
	
	/**
	 * 该方法是每次menu键被按下都会被调用
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		Log.i("i", " onPrepareOptionsMenu ");
		//一种是通过配置文件 MenuInflater
		//通过代码直接显示 
		if(mode == DISPLAYMODE.list){
			menu_item_search.setVisible(true);
			menu_item_delete.setVisible(true);
			menu_item_back.setVisible(false);
			
			if(mAdapter.getCount() > 0){
				menu_item_delete.setEnabled(true);
			}else{
				menu_item_delete.setEnabled(false);
			}
		}else{
			menu_item_search.setVisible(false);
			menu_item_delete.setVisible(false);
			menu_item_back.setVisible(true);
		}
		return super.onPrepareOptionsMenu(menu);
	}
	
	/**
	 * menuItem被点击的时候调用
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		int id = item.getItemId();
		switch (id) {
			case MENU_SEARCH_ID:
				//激活系统浮动搜索框
				onSearchRequested();
				break;
			case MENU_DELETE_ID:
				changeMode(DISPLAYMODE.edit);
				break;
			case MENU_BACK_ID:
				changeMode(DISPLAYMODE.list);
				break;
	
			default:
				break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	/**
	 * 改变activity的显示模式
	 * @param mode 
	 * 有listView位置发生改变，导致listview重绘，所有就出现了checkbox
	 */
	private void changeMode(DISPLAYMODE mode) {
		// TODO Auto-generated method stub
		this.mode = mode;
		if(mode == DISPLAYMODE.edit){
			bt_new_msg.setVisibility(View.GONE);
			mEdit.setVisibility(View.VISIBLE);
			bt_delete.setVisibility(View.VISIBLE);
			
			bt_all_select.setEnabled(true);
			bt_cancel_selected.setEnabled(false);
			bt_delete.setEnabled(false);
		}else{
			bt_new_msg.setVisibility(View.VISIBLE);
			mEdit.setVisibility(View.GONE);
			bt_delete.setVisibility(View.GONE);
			mMultSelected.clear();
		}

	}

	/**
	 * 构造异步查询类
	 *
	 */
	private final class QueryHandler extends AsyncQueryHandler{

		public QueryHandler(ContentResolver cr) {
			super(cr);
			// TODO Auto-generated constructor stub
		}

		@Override
		protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
			// TODO Auto-generated method stub
			super.onQueryComplete(token, cookie, cursor);
//			String[] names = cursor.getColumnNames();
//			while(cursor.moveToNext()){
//				for(String name:names){
//					Log.i("i", name + ":"  + cursor.getString(cursor.getColumnIndex(name)));
//				}
//			}
			mAdapter.changeCursor(cursor);//通过adapter数据发生改变
		}
		
		
	}

	public void onClick(View v) {
		// TODO Auto-generated method stub
		int id = v.getId();
		switch (id) {
			case R.id.bt_new_msg:
				Intent intent = new Intent(this,NewMessageActivity.class);
				startActivity(intent);
				//mListView.setSelectionFromTop(2, 1);
				break;
			case R.id.bt_all_select:
				for(int i = 0;i< mAdapter.getCount();i++){
					Cursor cursor = (Cursor) mAdapter.getItem(i);
					String idStr = cursor.getString(ID_COLUMN_INDEX);
					mMultSelected.add(idStr);
				}
				mAdapter.notifyDataSetChanged();//让listView自动刷新
				
				bt_all_select.setEnabled(false);
				bt_cancel_selected.setEnabled(true);
				bt_delete.setEnabled(true);
				break;
			case R.id.bt_cancel_selected:
				mMultSelected.clear();
				mAdapter.notifyDataSetChanged();
				bt_all_select.setEnabled(true);
				bt_cancel_selected.setEnabled(false);
				bt_delete.setEnabled(false);
				break;
			case R.id.bt_delete:
				/**
				 * 1 创建builder
				 * 2 给builder设置属性 标题、提示信息、按钮
				 * 3创建dialog
				 * 4显示dialog
				 */
				
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setIcon(android.R.drawable.ic_dialog_alert);
				builder.setCancelable(false);//屏蔽回退键
				builder.setTitle(R.string.delete);
				builder.setMessage(R.string.delete_alert);
				builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						mProgressDialog = new ProgressDialog(ConversationActivity.this);
						mProgressDialog.setIcon(android.R.drawable.ic_dialog_alert);
						mProgressDialog.setTitle(R.string.delete);
						//设置进度条对话框的风格
						mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
						//设置进度条的大小
						mProgressDialog.setMax(mMultSelected.size());
						mProgressDialog.setCancelable(false);
						mProgressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
							
							public void onDismiss(DialogInterface dialog) {
								// TODO Auto-generated method stub
								changeMode(DISPLAYMODE.list);
								//delete = true;
							}
						});
						mProgressDialog.setButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
							
							public void onClick(DialogInterface dialog, int which) {
								// TODO Auto-generated method stub
								delete = false;
								mProgressDialog.dismiss();
							}
						});
						mProgressDialog.show();
						
						delete = true;
						
						//删除会话
						new Thread(new DeleteTask()).start();
					}
				});
				builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						
					}
				});
				AlertDialog dialog = builder.create();
				dialog.show();
				break;
	
			default:
				break;
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if(keyCode == KeyEvent.KEYCODE_BACK){
			if(mode == DISPLAYMODE.edit){
				changeMode(DISPLAYMODE.list);
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
	}
	
	
	
	//删除会话的任务
	private final class DeleteTask implements Runnable{

		public void run() {
			// TODO Auto-generated method stub
			
			ArrayList<String> list = new ArrayList<String>(mMultSelected);
			
			for(int i = 0;i< list.size();i++){
				
				if(!delete){
					return;
				}
				Uri uri = Uri.withAppendedPath(Sms.CONVERSATION_URI, list.get(i));
				getContentResolver().delete(uri, null, null);
				
				//更新进度条的显示
				mProgressDialog.incrementProgressBy(1);
				
				SystemClock.sleep(2000);
			}
			
			mProgressDialog.dismiss();
			//子线程不能对显示进行操作
			//changeMode(DISPLAYMODE.list);
		}
		
	}
	
	/**
	 * 应该把cursor关闭
	 */
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
