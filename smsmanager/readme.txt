public class AutoCompleteTextView extends EditText implements Filter.FilterListener
过滤是由谁来完成？Filter.FilterListener

addTextChangedListener(new MyWatcher()); 输入改变事件的相应

doAfterTextChanged();

            if (mFilter != null) {
                mPopupCanBeUpdated = true;
                performFiltering(getText(), mLastKeyCode); 执行过滤
            }
            
    protected void performFiltering(CharSequence text, int keyCode) {
        mFilter.filter(text, this);
    }   
    
研究 mFilter是什么？
    mAdapter == ContactAdapter == CursorAdapter
	Filter mFilter;Filter是一个抽象类   mFilter == new CursorFilter(this);
	mFilter的初始化在
	void setAdapter(T adapter) {
	mFilter = ((Filterable) mAdapter).getFilter();
	}
	
研究CursorAdapter?
getFilter(){
        if (mCursorFilter == null) {
            mCursorFilter = new CursorFilter(this);
        }
        return mCursorFilter;
}


研究CursorFilter?
class CursorFilter extends Filter 

研究Filter?
在filter(text,FilterListener){
 开启了一个子线程，并且子线程里面创建了一个RequestHandler,并且发送消息
}

控制权就到了RequestHandler。
args.results = performFiltering(args.constraint);执行过滤，该方法是一个抽象方法，他的实现在CursorFilter


研究CursorFilter?
	Cursor cursor = mClient.runQueryOnBackgroundThread(constraint);

mClient是什么？
    CursorFilter(CursorFilterClient client) {
        mClient = client;
    }
    
   CursorFilterClient =  mClient = CursorAdapter == ContactAdapter
   
   
查询出来的数据是如何交给AutoCompleteTextView的？
                        message = mResultHandler.obtainMessage(what);
                        message.obj = args;
                        message.sendToTarget();
                        
                        控制权就交给了ResultHandler。
                        publishResults(args.constraint, args.results);出版结果，该方法是一个抽象方法，他的实现在CursorFilter
   


















