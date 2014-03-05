/*
 * HorizontalVariableListView.java v1.0
 * Handles horizontal variable widths item scrolling
 *
 */

package cn.com.xinli.android.doudian.ui.widget.HorizontalListView;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.content.Context;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.ViewTreeObserver.OnScrollChangedListener;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.TextView;
import cn.com.xinli.android.doudian.R;
import cn.com.xinli.android.doudian.ui.widget.HorizontalListView.IFlingRunnable.FlingRunnableView;
import cn.com.xinli.android.doudian.ui.widget.HorizontalListView.ReflectionUtils.ReflectionException;

// TODO: Auto-generated Javadoc
/**
 * The Class HorizontialFixedListView.
 */
public class HorizontalVariableListView extends AdapterView<ListAdapter> implements 
	OnGestureListener, FlingRunnableView {

	/** The Constant LOG_TAG. */
	protected static final String LOG_TAG = "horizontal-variable-list";

	private static final float WIDTH_THRESHOLD = 1.1f;

	/** The m adapter. */
	protected ListAdapter mAdapter;
	
	private int mAdapterItemCount;

	/** The m left view index. */
	private int mLeftViewIndex = -1;

	/** The m right view index. */
	private int mRightViewIndex = 0;

	/** The m gesture. */
	private GestureDetector mGesture;

	/** The m removed view queue. */
	private List<Queue<View>> mRecycleBin;

	private List<Integer> mChildWidths = new ArrayList<Integer>();
	
	private int mChildWidth;

	/** The m on item selected. */
	private OnItemSelectedListener mOnItemSelected;

	/** The m on item clicked. */
	private OnItemClickListener mOnItemClicked;

	/** The m data changed. */
	private boolean mDataChanged = false;

	/** The m fling runnable. */
	private IFlingRunnable mFlingRunnable;

	/** The m force layout. */
	private boolean mForceLayout;

	private int mDragTolerance = 0;

	private boolean mDragScrollEnabled;

	protected EdgeGlow mEdgeGlowLeft, mEdgeGlowRight;

	private int mOverScrollMode = OVER_SCROLL_NEVER;

	private ScrollNotifier mScrollNotifier;
	
	/**
	 * flat for no-loop and start from center
	 */
	private boolean mNoLoop; 
	
	private int mCountInLine;
	
	/**
	 * is user defined focus ?
	 */
	private boolean mPreFocus = false;
	
	/**
	 * if user defined focus, where it is
	 */
	private int mPreFocusPosition = -1;
	
	private int mRightViewIndexFilledMinValue = 0;
	
	/**
	 * position in list
	 */
	private int mPosition;
	
	private boolean mHasNextUpFocus;
	private boolean mHasNextDownFocus;
	private int mNextUpFocusId;
	private int mNextDownFocusId;
	private boolean mIsFirst;
	
	private boolean mFixFocus = false;
	private boolean mCenterFocusFounded = false;
	private boolean mFocusTextSelected=false;
	
	private int mItemWidth;
	private int mItemHeight;
	private int mDividerWidth;
	private int mScrollStartX;
	
	private OnUpAndDownListener mUpAndDownListener;
	
	public interface OnUpAndDownListener{
		public abstract boolean go(boolean upOrDown);
	}
	
	public void setOnUpandDownListener(OnUpAndDownListener upAndDownListener){
		mUpAndDownListener = upAndDownListener;
	}
	
	/**
	 * Interface definition for a callback to be invoked when an item in this view has been clicked and held.
	 */
	public interface OnItemDragListener {

		/**
		 * Callback method to be invoked when an item in this view has been dragged outside the vertical tolerance area.
		 * 
		 * Implementers can call getItemAtPosition(position) if they need to access the data associated with the selected item.
		 * 
		 * @param parent
		 *           The AbsListView where the click happened
		 * @param view
		 *           The view within the AbsListView that was clicked
		 * @param position
		 *           The position of the view in the list
		 * @param id
		 *           The row id of the item that was clicked
		 * 
		 * @return true if the callback consumed the long click, false otherwise
		 */
		boolean onItemStartDrag( AdapterView<?> parent, View view, int position, long id );
	}
	
	public interface OnLayoutChangeListener {
		void onLayoutChange( boolean changed, int left, int top, int right, int bottom );
	}

	private OnItemDragListener mItemDragListener;
	private OnScrollChangedListener mScrollListener;
	private OnLayoutChangeListener mLayoutChangeListener;

	public void setOnItemDragListener( OnItemDragListener listener ) {
		mItemDragListener = listener;
	}

	public void setOnScrollListener( OnScrollChangedListener listener ) {
		mScrollListener = listener;
	}
	
	public void setOnLayoutChangeListener( OnLayoutChangeListener listener ) {
		mLayoutChangeListener = listener;
	}

	public OnItemDragListener getOnItemDragListener() {
		return mItemDragListener;
	}

	/**
	 * Instantiates a new horizontial fixed list view.
	 * 
	 * @param context
	 *           the context
	 * @param attrs
	 *           the attrs
	 */
	public HorizontalVariableListView( Context context, AttributeSet attrs ) {
		super( context, attrs );
		initView();
	}

	public HorizontalVariableListView( Context context, AttributeSet attrs, int defStyle ) {
		super( context, attrs, defStyle );
		initView();
	}
	
	public HorizontalVariableListView( Context context) {
		super( context);
		initView();
	}
	
	/**
	 * Inits the view.
	 */
	private synchronized void initView() {

		if ( Build.VERSION.SDK_INT > 8 ) {
			try {
				mFlingRunnable = (IFlingRunnable) ReflectionUtils.newInstance( "cn.com.xinli.android.doudian.ui.widget.HorizontalListView.Fling9Runnable", new Class<?>[] { FlingRunnableView.class, int.class }, this, mAnimationDuration );
			} catch ( ReflectionException e ) {
				mFlingRunnable = new Fling8Runnable( this, mAnimationDuration );
			}
		} else {
			mFlingRunnable = new Fling8Runnable( this, mAnimationDuration );
		}

		mCenterFocusFounded = false;
		mLeftViewIndex = -1;
		mRightViewIndex = 0;
		mMaxX = Integer.MAX_VALUE;
		mMinX = Integer.MIN_VALUE;
//		mChildHeight = 0;
		mRightEdge = 0;
		mLeftEdge = 0;
		mGesture = new GestureDetector( getContext(), mGestureListener );
		mGesture.setIsLongpressEnabled( true );

		setFocusable(false);
		setFocusableInTouchMode(false);

		ViewConfiguration configuration = ViewConfiguration.get( getContext() );
		mTouchSlop = configuration.getScaledTouchSlop();
		mDragTolerance = mTouchSlop;
		mOverscrollDistance = 10;
		mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
		mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
		
//		mMaximumVelocity = 1000;
	}

	@Override
	public void setOverScrollMode( int mode ) {
		mOverScrollMode = mode;

		if ( mode != OVER_SCROLL_NEVER ) {
			if ( mEdgeGlowLeft == null ) {
				Drawable glow = getContext().getResources().getDrawable( R.drawable.overscroll_glow );
				Drawable edge = getContext().getResources().getDrawable( R.drawable.overscroll_edge );
				mEdgeGlowLeft = new EdgeGlow( edge, glow );
				mEdgeGlowRight = new EdgeGlow( edge, glow );
				mEdgeGlowLeft.setColorFilter( 0xFF33b5e5, Mode.MULTIPLY );
			}
		} else {
			mEdgeGlowLeft = mEdgeGlowRight = null;
		}
	}

	public void setEdgeHeight( int value ) {
		mEdgesHeight = value;
	}

	public void setEdgeGravityY( int value ) {
		mEdgesGravityY = value;
	}

	@Override
	public void trackMotionScroll( int newX ) {

		scrollTo( newX, 0 );
		mCurrentX = getScrollX();
		removeNonVisibleItems( mCurrentX );
		fillList( mCurrentX );
		invalidate();
		
		if (mRightViewIndex < mRightViewIndexFilledMinValue || mRightViewIndexFilledMinValue == 0){
			mRightViewIndexFilledMinValue = mRightViewIndex;
//			Log.d(LOG_TAG,"mRightViewIndexFilledMinValue = " + mRightViewIndexFilledMinValue);
		}
		
	}

	@Override
	protected void dispatchDraw( Canvas canvas ) {
		super.dispatchDraw( canvas );

		if ( getChildCount() > 0 ) {
			drawEdges( canvas );
		}
	}

	private Matrix mEdgeMatrix = new Matrix();

	/**
	 * Draw glow edges.
	 * 
	 * @param canvas
	 *           the canvas
	 */
	private void drawEdges( Canvas canvas ) {

		if ( mEdgeGlowLeft != null ) {
			if ( !mEdgeGlowLeft.isFinished() ) {
				final int restoreCount = canvas.save();
				final int height = mEdgesHeight;

				mEdgeMatrix.reset();
				mEdgeMatrix.postRotate( -90 );
				mEdgeMatrix.postTranslate( 0, height );

				if ( mEdgesGravityY == Gravity.BOTTOM ) {
					mEdgeMatrix.postTranslate( 0, getHeight() - height );
				}
				canvas.concat( mEdgeMatrix );

				mEdgeGlowLeft.setSize( height, height / 5 );

				if ( mEdgeGlowLeft.draw( canvas ) ) {
					postInvalidate();
				}
				canvas.restoreToCount( restoreCount );
			}
			if ( !mEdgeGlowRight.isFinished() ) {
				final int restoreCount = canvas.save();
				final int width = getWidth();
				final int height = mEdgesHeight;

				mEdgeMatrix.reset();
				mEdgeMatrix.postRotate( 90 );
				mEdgeMatrix.postTranslate( mCurrentX + width, 0 );

				if ( mEdgesGravityY == Gravity.BOTTOM ) {
					mEdgeMatrix.postTranslate( 0, getHeight() - height );
				}
				canvas.concat( mEdgeMatrix );

				mEdgeGlowRight.setSize( height, height / 5 );

				if ( mEdgeGlowRight.draw( canvas ) ) {
					postInvalidate();
				}
				canvas.restoreToCount( restoreCount );
			}
		}
	}

	/**
	 * Set if a vertical scroll movement will trigger a long click event
	 * 
	 * @param value
	 */
	public void setDragScrollEnabled( boolean value ) {
		mDragScrollEnabled = value;
	}

    public void setAutoAppendData(boolean value) {mAutoAppendData=value;}

	public boolean getDragScrollEnabled() {
		return mDragScrollEnabled;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.AdapterView#setOnItemSelectedListener(android.widget.AdapterView.OnItemSelectedListener)
	 */
	@Override
	public void setOnItemSelectedListener( AdapterView.OnItemSelectedListener listener ) {
		mOnItemSelected = listener;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.AdapterView#setOnItemClickListener(android.widget.AdapterView.OnItemClickListener)
	 */
	@Override
	public void setOnItemClickListener( AdapterView.OnItemClickListener listener ) {
		mOnItemClicked = listener;
	}

	private DataSetObserverExtended mDataObserverExtended = new DataSetObserverExtended() {

		@Override
		public void onAdded() {
			synchronized ( HorizontalVariableListView.this ) {
				mAdapterItemCount = mAdapter.getCount();
			}
			mDataChanged = true;
			mMaxX = Integer.MAX_VALUE;
			requestLayout();
		};

		@Override
		public void onRemoved() {
			this.onChanged();
		};

		@Override
		public void onChanged() {
			mAdapterItemCount = mAdapter.getCount();
			mDataChanged = true;
			reset();
		};

		@Override
		public void onInvalidated() {
			this.onChanged();
		};
	};

	/** The m data observer. */
	private DataSetObserver mDataObserver = new DataSetObserver() {

		@Override
		public void onChanged() {
			synchronized ( HorizontalVariableListView.this ) {
				mAdapterItemCount = mAdapter.getCount();
			}
			invalidate();
            if (!mAutoAppendData)
    			reset();
		}

		@Override
		public void onInvalidated() {
			mAdapterItemCount = mAdapter.getCount();
			invalidate();
			reset();
		}
	};

	public void requestFullLayout() {
		mForceLayout = true;
		invalidate();
		requestLayout();
	}

	/** The m height measure spec. */
	private int mHeightMeasureSpec;

	/** The m width measure spec. */
	private int mWidthMeasureSpec;

	/** The m left edge. */
	private int mRightEdge, mLeftEdge;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.AdapterView#getAdapter()
	 */
	@Override
	public ListAdapter getAdapter() {
		return mAdapter;
	}

	@Override
	public View getSelectedView() {
		return null;
	}

	@Override
	public void setAdapter( ListAdapter adapter ) {

		if ( mAdapter != null ) {

			if ( mAdapter instanceof BaseAdapterExtended ) {
				( (BaseAdapterExtended) mAdapter ).unregisterDataSetObserverExtended( mDataObserverExtended );
			} else {
				mAdapter.unregisterDataSetObserver( mDataObserver );
			}

			emptyRecycler();
			mAdapterItemCount = 0;
		}

		mAdapter = adapter;
		mChildWidths.clear();

		if ( mAdapter != null ) {
			mAdapterItemCount = mAdapter.getCount();

			if ( mAdapter instanceof BaseAdapterExtended ) {
				( (BaseAdapterExtended) mAdapter ).registerDataSetObserverExtended( mDataObserverExtended );
			} else {
				mAdapter.registerDataSetObserver( mDataObserver );
			}

			int total = mAdapter.getViewTypeCount();

			mRecycleBin = Collections.synchronizedList( new ArrayList<Queue<View>>() );
			for ( int i = 0; i < total; i++ ) {
				mRecycleBin.add( new LinkedList<View>() );
				mChildWidths.add( -1 );
			}
		}
		reset();
	}

	private void emptyRecycler() {
		if ( null != mRecycleBin ) {
			while ( mRecycleBin.size() > 0 ) {
				Queue<View> recycler = mRecycleBin.remove( 0 );
				recycler.clear();
			}
			mRecycleBin.clear();
		}
	}

	/**
	 * Reset.
	 */
	private synchronized void reset() {
//		mCurrentX = 0;
		initView();
		removeAllViewsInLayout();
		mForceLayout = true;
		requestLayout();
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		removeCallbacks( mScrollNotifier );
		emptyRecycler();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.AdapterView#setSelection(int)
	 */
	@Override
	public void setSelection( int position ) {
		// added by chen
		
		if (mAdapter == null)
			return;
		
		mPreFocus = true;
		mPreFocusPosition = position;
		mPosition = position / mAdapter.getViewTypeCount();
	}
	
	public void setFixPosition(boolean fixFocus){
		mFixFocus = fixFocus;
	}
	
	public void setSize(int width, int height, int count, float ratio){
		mItemHeight = height;
		mItemWidth = (int)(ratio * mItemHeight);
		mDividerWidth = (int)((width - ratio * mItemHeight * count)/(count - 1));
		mItemWidth = mItemWidth + mDividerWidth;
		mCountInLine = count;
	}
	public int getItemWidth() {
		return mItemWidth;
	}
	public int getItemHeight() {
		return mItemHeight;
	}
	
	/**
	 * 
	 * @param hasNextUpFocus
	 * @param nextUpFocusId
	 * @param hasNextDownFocus
	 * @param nextDownFocusId
	 * @param isFirst
	 */
	public void setUpAndDownFocus(boolean hasNextUpFocus, 
			int nextUpFocusId, boolean hasNextDownFocus, int nextDownFocusId,
			boolean isFirst){
		mHasNextUpFocus = hasNextUpFocus;
		mHasNextDownFocus = hasNextDownFocus;
		mNextUpFocusId = nextUpFocusId;
		mNextDownFocusId = nextDownFocusId;
		mIsFirst = isFirst;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View#onMeasure(int, int)
	 */
	@Override
	protected void onMeasure( int widthMeasureSpec, int heightMeasureSpec ) {
		
		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
		int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
		
		/*if( mChildHeight != 0 ) {
//			setMeasuredDimension( parentWidth, mChildHeight );
			setMeasuredDimension( parentWidth, parentHeight );
		} else {
			setMeasuredDimension( parentWidth, 0 );
		}*/
		
		setMeasuredDimension( parentWidth, parentHeight );

		mHeightMeasureSpec = heightMeasureSpec;
		mWidthMeasureSpec = widthMeasureSpec;
	}

	/**
	 * Adds the and measure child.
	 * 
	 * @param child
	 *           the child
	 * @param viewPos
	 *           the view pos
	 */
	private void addAndMeasureChild( final View child, int viewPos, int type ) {
		LayoutParams params = child.getLayoutParams();

		if ( params == null ) {
//			params = new LayoutParams( LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT );
			params = new LayoutParams(mItemWidth, mItemHeight);
				
		} else {
			params.height = mItemHeight;
			params.width = mItemWidth;
		}

		addViewInLayout( child, viewPos, params, false );
		forceChildLayout( child, params );
	}

	public void forceChildLayout( View child, LayoutParams params ) {
		int childHeightSpec = ViewGroup.getChildMeasureSpec( mHeightMeasureSpec, getPaddingTop() + getPaddingBottom(), params.height );
		int childWidthSpec = ViewGroup.getChildMeasureSpec( mWidthMeasureSpec, getPaddingLeft() + getPaddingRight(), params.width );
		child.setPadding(0, 0, mDividerWidth, 0);
		child.measure( childWidthSpec, childHeightSpec );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.widget.AdapterView#onLayout(boolean, int, int, int, int)
	 */
	@Override
	protected void onLayout( boolean changed, int left, int top, int right, int bottom ) {
		super.onLayout( changed, left, top, right, bottom );

		if ( mAdapter == null ) {
			return;
		}
		
		if ( !changed && !mDataChanged ) {
			layoutChildren();
		}

		// no loop when item count less then total in line
//		if (mAdapterItemCount < mCountInLine)
//			mNoLoop = true;
//		else
//			mNoLoop = false;
        mNoLoop = true;
        boolean alignLeft = true;
        if (mAdapterItemCount < mCountInLine)
            alignLeft = false;

		
		if ( changed ) {
//			mCurrentX = mOldX = 0;
			initView();
			removeAllViewsInLayout();
//			trackMotionScroll( 0 );
			
//			if (mNoLoop){
            if (!alignLeft){
				mScrollStartX = 0 - mItemWidth * (mCountInLine / 2);
				trackMotionScroll( mScrollStartX );
			}
			else
				trackMotionScroll( 0 );
			
		}

		if ( mDataChanged ) {
			trackMotionScroll( mCurrentX );
			mDataChanged = false;
		}

		if ( mForceLayout ) {
			mCurrentX=0;
			mOldX = mCurrentX;
			mScrollStartX = mCurrentX;
			initView();
			removeAllViewsInLayout();
//			trackMotionScroll( mOldX );
//			if (mNoLoop){
            if (!alignLeft){
				mScrollStartX = 0 - mItemWidth * (mCountInLine / 2);
				trackMotionScroll( mScrollStartX );
			}
			else
				trackMotionScroll( mOldX );
			
			mForceLayout = false;
		}
		
		postNotifyLayoutChange( changed, left, top, right, bottom );

	}

	public void layoutChildren() {

		int paddingTop = getPaddingTop();
		int left, right;

		for ( int i = 0; i < getChildCount(); i++ ) {
			View child = getChildAt( i );

			forceChildLayout( child, child.getLayoutParams() );

			left = child.getLeft();
			right = child.getRight();
			child.layout( left, paddingTop, right, paddingTop + child.getMeasuredHeight() );
		}
	}

	/**
	 * Fill list.
	 * 
	 * @param positionX
	 *           the position x
	 */
	private void fillList( final int positionX ) {
		
		int edge = 0;

		View child = getChildAt( getChildCount() - 1 );
		if ( child != null ) {
			edge = child.getRight();
		}
		fillListRight( mCurrentX, edge);

		edge = 0;
		child = getChildAt( 0 );
		if ( child != null ) {
			edge = child.getLeft();
		}
		fillListLeft( mCurrentX, edge);
	}

	/**
	 * Fill list left.
	 * 
	 * @param positionX
	 *           the position x
	 * @param leftEdge
	 *           the left edge
	 */
	private void fillListLeft( int positionX, int leftEdge ) {

		if ( mAdapter == null ) return;
		
//		while ( ( leftEdge - positionX ) > mLeftEdge && mLeftViewIndex >= 0 ) {    // not fix focus loop
		while ( ( leftEdge - positionX ) > mLeftEdge ) {
			// no loop when mMinLength has been set
			if (mNoLoop && mLeftViewIndex < 0)
				break;
			
			// for fix focus loop ..................................
			if (mLeftViewIndex < 0)
				mLeftViewIndex = mAdapter.getCount()-1;
			
			if (mLeftViewIndex > mAdapter.getCount()-1)
				mLeftViewIndex = 0;
			// for fix focus loop ..................................
			
			int viewType = mAdapter.getItemViewType( mLeftViewIndex );
//			View child = mAdapter.getView( mLeftViewIndex, mRecycleBin.get( viewType ).poll(), this );
			View child = null;
			if (mRecycleBin.size() > 0 )
				child = mAdapter.getView( mLeftViewIndex, mRecycleBin.get( viewType ).poll(), this );
			else
				child = mAdapter.getView( mLeftViewIndex, null, this );
			
			// to return focus on current tab view
			if (mNextUpFocusId != 0)
				child.setNextFocusUpId(mNextUpFocusId);
			
			final int tmPosition = ((mLeftViewIndex+mAdapter.getCount())%mAdapter.getCount()) / mAdapter.getViewTypeCount();
			child.setTag(R.string.position_key,tmPosition);// save position
			child.setFocusable(true);
			child.setFocusableInTouchMode(true);
			child.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus){
						mPosition = (Integer) v.getTag(R.string.position_key);
//							Log.d(LOG_TAG, "fillListLeft mPosition = " + mPosition);
						
						// scroll to center
						if (mFixFocus && !mCenterFocusFounded){
							if (v.getX()+mItemWidth/2 > getWidth()/2 - mItemWidth/2
									&& v.getX()+mItemWidth/2 < getWidth()/2 + mItemWidth/2){
								mCenterFocusFounded = true;
							}
							else{
								final int i = getChildAtPosition(getWidth()/2 , v.getY() + mItemHeight/2);
								View itemView = getChildAt(i); 
								if (itemView != null){
									itemView.requestFocus();
									mCenterFocusFounded = true;
								}
							}
							
						}
						
						if (mOnItemSelected != null)
	        				mOnItemSelected.onItemSelected( HorizontalVariableListView.this, v, mPosition, mAdapter.getItemId( mPosition ) );
						
						TextView textView = (TextView) v.findViewById(R.id.card_text);
						if (textView != null && !mFocusTextSelected){
							textView.setSelected(true);
							mFocusTextSelected = true;
						}
					} else {
						TextView textView = (TextView) v.findViewById(R.id.card_text);
						if (textView != null){
							textView.setSelected(false);
							mFocusTextSelected = false;
						}
					}
					
				}
			});
			
			addAndMeasureChild( child, 0, viewType);

			int childWidth = mChildWidths.get( viewType );
			int childTop = getPaddingTop();
			child.layout( leftEdge - childWidth, childTop, leftEdge, childTop + mChildHeight);
			leftEdge -= childWidth;
			mLeftViewIndex--;
			
		}
	}

	public View getItemAt( int position ) {
		return getChildAt( position - ( mLeftViewIndex + 1 ) );
	}

	public int getScreenPositionForView( View view ) {
		View listItem = view;
		try {
			View v;
			while ( !this.equals( ( v = (View) listItem.getParent() ) ) ) {
				listItem = v;
			}
		} catch ( ClassCastException e ) {
			// We made it up to the window without find this list view
			return INVALID_POSITION;
		} catch ( NullPointerException e ) {
			return INVALID_POSITION;
		}

		// Search the children for the list item
		final int childCount = getChildCount();
		for ( int i = 0; i < childCount; i++ ) {
			if ( getChildAt( i ).equals( listItem ) ) {
				return i;
			}
		}

		// Child not found!
		return INVALID_POSITION;
	}

	@Override
	public int getPositionForView( View view ) {
		View listItem = view;
		try {
			View v;
			while ( !( v = (View) listItem.getParent() ).equals( this ) ) {
				listItem = v;
			}
		} catch ( ClassCastException e ) {
			// We made it up to the window without find this list view
			return INVALID_POSITION;
		}

		// Search the children for the list item
		final int childCount = getChildCount();
		for ( int i = 0; i < childCount; i++ ) {
			if ( getChildAt( i ).equals( listItem ) ) {
				return mLeftViewIndex + i + 1;
			}
		}

		// Child not found!
		return INVALID_POSITION;
	}

	/**
	 * Fill list right.
	 * 
	 * @param positionX
	 *           the position x
	 * @param rightEdge
	 *           the right edge
	 */
	private void fillListRight( int positionX, int rightEdge ) {
        boolean firstChild = getChildCount() == 0 || mDataChanged || mForceLayout;
		
		if ( mAdapter == null ) return;

		final int realWidth = getWidth();
		int viewWidth = (int) ( (float) realWidth * WIDTH_THRESHOLD );

		while ( ( rightEdge - positionX ) < viewWidth || firstChild ) {
			
			// no loop when mMinLength has been set
			if (mNoLoop && mRightViewIndex > mAdapterItemCount - 1)
				break;

			if ( mRightViewIndex > mAdapterItemCount - 1 ) {
//				break;				// not fix focus loop
				mRightViewIndex = 0; // for fix focus loop ..................................
			}
			
			// for fix focus loop ..................................
			if (mRightViewIndex < 0)
				mRightViewIndex = mAdapter.getCount() - 1;
			// for fix focus loop ..................................

			int viewType = mAdapter.getItemViewType( mRightViewIndex );
				
//			View child = mAdapter.getView( mRightViewIndex, mRecycleBin.get( viewType ).poll(), this );
			View child = null;
			if (mRecycleBin.size() > 0 )
				child = mAdapter.getView( mRightViewIndex, mRecycleBin.get( viewType ).poll(), this );
			else
				child = mAdapter.getView( mRightViewIndex, null, this );
			
			// to return focus on current tab view
			if (mNextUpFocusId != 0)
				child.setNextFocusUpId(mNextUpFocusId);
			
			final int tmPosition = mRightViewIndex / mAdapter.getViewTypeCount();
			child.setTag(R.string.position_key,tmPosition);// save position
			child.setFocusable(true);
			child.setFocusableInTouchMode(true);
			child.setOnFocusChangeListener(new View.OnFocusChangeListener() {
				
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus){
						mPosition = (Integer) v.getTag(R.string.position_key);
	//							Log.d(LOG_TAG, "fillListRight mPosition = " + mPosition);
						
						// scroll to center
						if (mFixFocus && !mCenterFocusFounded){
							if (v.getX()+mItemWidth/2 > getWidth()/2 - mItemWidth/2
									&& v.getX()+mItemWidth/2 < getWidth()/2 + mItemWidth/2){
								mCenterFocusFounded = true;
							}
							else{
								final int i = getChildAtPosition(getWidth()/2, v.getY() + mItemHeight/2);
								View itemView = getChildAt(i);
								if (itemView != null){
									itemView.requestFocus();
									mCenterFocusFounded = true;
								}
							}
							
						}
						
	        			if (mOnItemSelected != null)
	        				mOnItemSelected.onItemSelected( HorizontalVariableListView.this
	        						, v
	        						, mPosition
	        						, mAdapter.getItemId( mPosition ) );
	        			
	        			TextView textView = (TextView) v.findViewById(R.id.card_text);
						if (textView != null && !mFocusTextSelected){
							textView.setSelected(true);
							mFocusTextSelected = true;
						}
					} else {
						TextView textView = (TextView) v.findViewById(R.id.card_text);
						if (textView != null){
							textView.setSelected(false);
							mFocusTextSelected = false;
						}
					}
				}
			});
			addAndMeasureChild( child, -1, viewType );

			int childWidth = mChildWidths.get( viewType );
			if ( childWidth == -1 ) {
				childWidth = child.getMeasuredWidth();
				mChildWidths.set( viewType, childWidth );
			}

			if ( firstChild ) {
				mChildHeight = child.getMeasuredHeight();
				
				if ( mEdgesHeight == -1 ) {
					mEdgesHeight = mChildHeight;
				}
				mRightEdge = viewWidth;
				mLeftEdge = ( realWidth - viewWidth );
				mMinX = Integer.MIN_VALUE;
				firstChild = false;
				
				rightEdge = getPaddingLeft();
			}

			int childTop = getPaddingTop();
			child.layout( rightEdge, childTop, rightEdge + childWidth, childTop + child.getMeasuredHeight());
			rightEdge += childWidth;
			mRightViewIndex++;
			
		}
		
		/* comment for fix focus loop
		if ( mRightViewIndex == mAdapterItemCount ) {
			if( rightEdge > realWidth ) {
				mMaxX = rightEdge - realWidth + getPaddingRight();
			} else {
				mMaxX = 0;
			}
		}*/		
		
	}

	/**
	 * Removes the non visible items.
	 * 
	 * @param positionX
	 *           the position x
	 */
	private void removeNonVisibleItems( final int positionX ) {
		View child = getChildAt( 0 );

		// remove to left...
		while ( child != null && child.getRight() - positionX <= mLeftEdge ) {


			if ( null != mAdapter ) {
				int position = getPositionForView( child );
				int viewType = mAdapter.getItemViewType( position );
				if (mRecycleBin.size() > 0)
					mRecycleBin.get( viewType ).offer( child );
			}
			removeViewInLayout( child );
			mLeftViewIndex++;
			
			// for fix focus loop ..................................
			if (mLeftViewIndex > mAdapter.getCount() - 1)
				mLeftViewIndex = 0;
			
			child = getChildAt( 0 );
		}

		// remove to right...
		child = getChildAt( getChildCount() - 1 );
		while ( child != null && child.getLeft() - positionX >= mRightEdge ) {

			if ( null != mAdapter ) {
				int position = getPositionForView( child );
				int viewType = mAdapter.getItemViewType( position );
				if (mRecycleBin.size() > 0)
					mRecycleBin.get( viewType ).offer( child );
			}

			removeViewInLayout( child );
			mRightViewIndex--;
			
			// for fix focus loop ..................................
			if (mRightViewIndex < 0)
				mRightViewIndex = mAdapter.getCount() - 1;
			
			child = getChildAt( getChildCount() - 1 );
		}
	}

	private float mTestDragX, mTestDragY;
	private boolean mCanCheckDrag;
	private boolean mWasFlinging;
	private WeakReference<View> mOriginalDragItem;

	@Override
	public boolean onDown( MotionEvent event ) {
		return true;
	}

	@Override
	public boolean onScroll( MotionEvent e1, MotionEvent e2, float distanceX, float distanceY ) {
		return true;
	}

	@Override
	public boolean onFling( MotionEvent event0, MotionEvent event1, float velocityX, float velocityY ) {
		return true; // no mouse move
		/*if ( mMaxX == 0 ) return false;
		mCanCheckDrag = false;
		mWasFlinging = true;
		mFlingRunnable.startUsingVelocity( mCurrentX, (int) -velocityX );
		return true;*/
	}

	@Override
	public void onLongPress( MotionEvent e ) {
		if ( mWasFlinging ) return;

		OnItemLongClickListener listener = getOnItemLongClickListener();
		if ( null != listener ) {

			if ( !mFlingRunnable.isFinished() ) return;

			int i = getChildAtPosition( e.getX(), e.getY() );
			if ( i > -1 ) {
				View child = getChildAt( i );
				fireLongPress( child, mLeftViewIndex + 1 + i, mAdapter.getItemId( mLeftViewIndex + 1 + i ) );
			}
		}
	}

	public int getChildAtPosition( float x, float y ) {
		Rect viewRect = new Rect();

		for ( int i = 0; i < getChildCount(); i++ ) {
			View child = getChildAt( i );
			int left = child.getLeft();
			int right = child.getRight();
			int top = child.getTop();
			int bottom = child.getBottom();
			viewRect.set( left, top, right, bottom );
			viewRect.offset( -mCurrentX, 0 );

			if ( viewRect.contains( (int) x, (int) y ) ) {
				return i;
			}
		}
		return -1;
	}

	private boolean fireLongPress( View item, int position, long id ) {
		if ( getOnItemLongClickListener().onItemLongClick( HorizontalVariableListView.this, item, position, id ) ) {
			performHapticFeedback( HapticFeedbackConstants.LONG_PRESS );
			return true;
		}
		return false;
	}

	private boolean fireItemDragStart( View item, int position, long id ) {

		mCanCheckDrag = false;
		mIsBeingDragged = false;

		if ( mItemDragListener.onItemStartDrag( HorizontalVariableListView.this, item, position, id ) ) {
			performHapticFeedback( HapticFeedbackConstants.LONG_PRESS );
			mIsDragging = true;
			return true;
		}
		return false;
	}

	private void fireOnScrollChanged() {
		if ( mScrollListener != null ) {
			mScrollListener.onScrollChanged();
		}
	}
	
	private void fireOnLayoutChangeListener( boolean changed, int left, int top, int right, int bottom ) {
		if( mLayoutChangeListener != null ) {
			mLayoutChangeListener.onLayoutChange( changed, left, top, right, bottom );
		}
	}

	private void postScrollNotifier() {
		if ( mScrollListener != null ) {
			if ( mScrollNotifier == null ) {
				mScrollNotifier = new ScrollNotifier();
			}
			post( mScrollNotifier );
		}
	}
	
	private void postNotifyLayoutChange( final boolean changed, final int left, final int top, final int right, final int bottom ) {
		post( new Runnable() {
			
			@Override
			public void run() {
				fireOnLayoutChangeListener( changed, left, top, right, bottom );
			}
		});
	}

	private class ScrollNotifier implements Runnable {

		@Override
		public void run() {
			fireOnScrollChanged();
		}
	}


	public void setIsDragging( boolean value ) {
		mIsDragging = value;
	}

	private int getItemIndex( View view ) {
		final int total = getChildCount();
		for ( int i = 0; i < total; i++ ) {
			if ( view == getChildAt( i ) ) {
				return i;
			}
		}
		return -1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.GestureDetector.OnGestureListener#onShowPress(android.view.MotionEvent)
	 */
	@Override
	public void onShowPress( MotionEvent arg0 ) {}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.GestureDetector.OnGestureListener#onSingleTapUp(android.view.MotionEvent)
	 */
	@Override
	public boolean onSingleTapUp( MotionEvent arg0 ) {
		return false;
	}

	private boolean mIsDragging = false;
	private boolean mIsBeingDragged = false;
	private int mActivePointerId = -1;
	private int mLastMotionX;
	private float mLastMotionX2;
	private VelocityTracker mVelocityTracker;
	private static final int INVALID_POINTER = -1;
	private int mOverscrollDistance;
	private int mMinimumVelocity;
	private int mMaximumVelocity;

	private void initOrResetVelocityTracker() {
		if ( mVelocityTracker == null ) {
			mVelocityTracker = VelocityTracker.obtain();
		} else {
			mVelocityTracker.clear();
		}
	}

	private void initVelocityTrackerIfNotExists() {
		if ( mVelocityTracker == null ) {
			mVelocityTracker = VelocityTracker.obtain();
		}
	}

	private void recycleVelocityTracker() {
		if ( mVelocityTracker != null ) {
			mVelocityTracker.recycle();
			mVelocityTracker = null;
		}
	}

	@Override
	public void requestDisallowInterceptTouchEvent( boolean disallowIntercept ) {
		if ( disallowIntercept ) {
			recycleVelocityTracker();
		}
		super.requestDisallowInterceptTouchEvent( disallowIntercept );
	}

	@Override
	public boolean onInterceptTouchEvent( MotionEvent ev ) {

		if ( mIsDragging ) return false;

		final int action = ev.getAction();
		mGesture.onTouchEvent( ev );

		/*
		 * Shortcut the most recurring case: the user is in the dragging state and he is moving his finger. We want to intercept this
		 * motion.
		 */
		if ( action == MotionEvent.ACTION_MOVE ) {
			if ( mIsBeingDragged ) {
				return true;
			}
		}

		switch ( action & MotionEvent.ACTION_MASK ) {
			case MotionEvent.ACTION_MOVE: {
				/*
				 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check whether the user has moved far enough
				 * from his original down touch.
				 */
				final int activePointerId = mActivePointerId;
				if ( activePointerId == INVALID_POINTER ) {
					// If we don't have a valid id, the touch down wasn't on content.
					break;
				}

				final int pointerIndex = ev.findPointerIndex( activePointerId );
				final int x = (int) ev.getX( pointerIndex );
				final int y = (int) ev.getY( pointerIndex );
				final int xDiff = Math.abs( x - mLastMotionX );
				mLastMotionX2 = x;

				if ( checkDrag( x, y ) ) {
					return false;
				}

				if ( xDiff > mTouchSlop ) {

					mIsBeingDragged = true;
					mLastMotionX = x;
					initVelocityTrackerIfNotExists();
					mVelocityTracker.addMovement( ev );
					final ViewParent parent = getParent();
					if ( parent != null ) {
						parent.requestDisallowInterceptTouchEvent( true );
					}
					postScrollNotifier();
				}
				break;
			}

			case MotionEvent.ACTION_DOWN: {

				final int x = (int) ev.getX();
				final int y = (int) ev.getY();

				mTestDragX = x;
				mTestDragY = y;

				/*
				 * Remember location of down touch. ACTION_DOWN always refers to pointer index 0.
				 */
				mLastMotionX = x;
				mLastMotionX2 = x;
				mActivePointerId = ev.getPointerId( 0 );

				initOrResetVelocityTracker();
				mVelocityTracker.addMovement( ev );

				/*
				 * If being flinged and user touches the screen, initiate drag; otherwise don't. mScroller.isFinished should be false
				 * when being flinged.
				 */
				mIsBeingDragged = !mFlingRunnable.isFinished();

				mWasFlinging = !mFlingRunnable.isFinished();
				mFlingRunnable.stop( false );
				mCanCheckDrag = isLongClickable() && getDragScrollEnabled() && ( mItemDragListener != null );

				if ( mCanCheckDrag ) {
					int i = getChildAtPosition( x, y );
					if ( i > -1 ) {
						mOriginalDragItem = new WeakReference<View>( getChildAt( i ) );
					}
				}
				break;
			}

			case MotionEvent.ACTION_CANCEL:
			case MotionEvent.ACTION_UP:
				/* Release the drag */
				mIsBeingDragged = false;
				mActivePointerId = INVALID_POINTER;
				recycleVelocityTracker();

				if ( mFlingRunnable.springBack( mCurrentX, 0, mMinX, mMaxX, 0, 0 ) ) {
					postInvalidate();
				}

				mCanCheckDrag = false;
				break;

			case MotionEvent.ACTION_POINTER_UP:
				onSecondaryPointerUp( ev );
				break;
		}

		return mIsBeingDragged;
	}

	@Override
	public boolean onTouchEvent( MotionEvent ev ) {

		initVelocityTrackerIfNotExists();
		mVelocityTracker.addMovement( ev );

		final int action = ev.getAction();

		switch ( action & MotionEvent.ACTION_MASK ) {

			case MotionEvent.ACTION_DOWN: { // DOWN

				if ( getChildCount() == 0 ) {
					return false;
				}

				if ( ( mIsBeingDragged = !mFlingRunnable.isFinished() ) ) {
					final ViewParent parent = getParent();
					if ( parent != null ) {
						parent.requestDisallowInterceptTouchEvent( true );
					}
				}

				/*
				 * If being flinged and user touches, stop the fling. isFinished will be false if being flinged.
				 */
				if ( !mFlingRunnable.isFinished() ) {
					mFlingRunnable.stop( false );
				}

				// Remember where the motion event started
				mTestDragX = ev.getX();
				mTestDragY = ev.getY();
				mLastMotionX2 = mLastMotionX = (int) ev.getX();
				mActivePointerId = ev.getPointerId( 0 );
				break;
			}

			case MotionEvent.ACTION_MOVE: {
				// MOVE
				final int activePointerIndex = ev.findPointerIndex( mActivePointerId );
				final int x = (int) ev.getX( activePointerIndex );
				final int y = (int) ev.getY( activePointerIndex );
				int deltaX = mLastMotionX - x;
				if ( !mIsBeingDragged && Math.abs( deltaX ) > mTouchSlop ) {
					final ViewParent parent = getParent();
					if ( parent != null ) {
						parent.requestDisallowInterceptTouchEvent( true );
					}
					mIsBeingDragged = true;
					if ( deltaX > 0 ) {
						deltaX -= mTouchSlop;
					} else {
						deltaX += mTouchSlop;
					}

					postScrollNotifier();

				}

				// first check if we can drag the item
				if ( checkDrag( x, y ) ) {
					return false;
				}

				if ( mIsBeingDragged ) {
					// Scroll to follow the motion event
					mLastMotionX = x;

					final float deltaX2 = mLastMotionX2 - x;
					final int oldX = getScrollX();
					final int range = getScrollRange();
					final int overscrollMode = mOverScrollMode;
					final boolean canOverscroll = overscrollMode == OVER_SCROLL_ALWAYS || ( overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && range > 0 );

					if ( overScrollingBy( deltaX, 0, mCurrentX, 0, range, 0, 0, mOverscrollDistance, true ) ) {
						mVelocityTracker.clear();
					}

					if ( canOverscroll && mEdgeGlowLeft != null ) {
						final int pulledToX = oldX + deltaX;
						
						if ( pulledToX < mMinX ) {
							float overscroll = ( (float) -deltaX2 * 1.5f ) / getWidth();
							mEdgeGlowLeft.onPull( overscroll );
							if ( !mEdgeGlowRight.isFinished() ) {
								mEdgeGlowRight.onRelease();
							}
						} else if ( pulledToX > mMaxX ) {
							float overscroll = ( (float) deltaX2 * 1.5f ) / getWidth();
							
							
							mEdgeGlowRight.onPull( overscroll );
							if ( !mEdgeGlowLeft.isFinished() ) {
								mEdgeGlowLeft.onRelease();
							}
						}
						if ( mEdgeGlowLeft != null && ( !mEdgeGlowLeft.isFinished() || !mEdgeGlowRight.isFinished() ) ) {
							postInvalidate();
						}
					}

				}
				break;
			}

			case MotionEvent.ACTION_UP: {

				if ( mIsBeingDragged ) {
					final VelocityTracker velocityTracker = mVelocityTracker;
					velocityTracker.computeCurrentVelocity( 1000, mMaximumVelocity );

					final float velocityY = velocityTracker.getYVelocity();
					final float velocityX = velocityTracker.getXVelocity();

					if ( getChildCount() > 0 ) {
						if ( ( Math.abs( velocityX ) > mMinimumVelocity ) ) {
							onFling( ev, null, velocityX, velocityY );
						} else {
							if ( mFlingRunnable.springBack( mCurrentX, 0, mMinX, mMaxX, 0, 0 ) ) {
								postInvalidate();
							}
						}
					}

					mActivePointerId = INVALID_POINTER;
					endDrag();

					mCanCheckDrag = false;
					if ( mFlingRunnable.isFinished() ) {
						scrollIntoSlots();
					}
				}
				break;
			}

			case MotionEvent.ACTION_CANCEL: {
				if ( mIsBeingDragged && getChildCount() > 0 ) {
					if ( mFlingRunnable.springBack( mCurrentX, 0, mMinX, mMaxX, 0, 0 ) ) {
						postInvalidate();
					}
					mActivePointerId = INVALID_POINTER;
					endDrag();
				}
				break;
			}

			case MotionEvent.ACTION_POINTER_UP: {
				onSecondaryPointerUp( ev );
				mTestDragX = mLastMotionX2 = mLastMotionX = (int) ev.getX( ev.findPointerIndex( mActivePointerId ) );
				mTestDragY = ev.getY( ev.findPointerIndex( mActivePointerId ) );
				break;
			}
		}
		return true;
	}

	private void onSecondaryPointerUp( MotionEvent ev ) {
		final int pointerIndex = ( ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK ) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		final int pointerId = ev.getPointerId( pointerIndex );
		if ( pointerId == mActivePointerId ) {
			final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
			mTestDragX = mLastMotionX2 = mLastMotionX = (int) ev.getX( newPointerIndex );
			mTestDragY = ev.getY( newPointerIndex );
			mActivePointerId = ev.getPointerId( newPointerIndex );
			if ( mVelocityTracker != null ) {
				mVelocityTracker.clear();
			}
		}
	}

	/**
	 * Check if the movement will fire a drag start event
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private boolean checkDrag( int x, int y ) {
		
		// no mouse move
		return true;

		/*if ( mCanCheckDrag && !mIsDragging ) {

			float dx = Math.abs( x - mTestDragX );

			if ( dx > mDragTolerance ) {
				mCanCheckDrag = false;
			} else {
				float dy = Math.abs( y - mTestDragY );
				if ( dy > ( (double) mDragTolerance * 1.5 ) ) {

					if ( mOriginalDragItem != null && mAdapter != null ) {

						View view = mOriginalDragItem.get();
						int position = getItemIndex( view );
						if ( null != view && position > -1 ) {
							getParent().requestDisallowInterceptTouchEvent( false );
							if ( mItemDragListener != null ) {
								fireItemDragStart( view, mLeftViewIndex + 1 + position, mAdapter.getItemId( mLeftViewIndex + 1 + position ) );
								return true;
							}
						}
					}
					mCanCheckDrag = false;
				}
			}
		}
		return false;*/
	}

	private void endDrag() {
		mIsBeingDragged = false;
		recycleVelocityTracker();

		if ( mEdgeGlowLeft != null ) {
			mEdgeGlowLeft.onRelease();
			mEdgeGlowRight.onRelease();
		}
	}

	/**
	 * Scroll the view with standard behavior for scrolling beyond the normal content boundaries. Views that call this method should
	 * override {@link #onOverScrolled(int, int, boolean, boolean)} to respond to the results of an over-scroll operation.
	 * 
	 * Views can use this method to handle any touch or fling-based scrolling.
	 * 
	 * @param deltaX
	 *           Change in X in pixels
	 * @param deltaY
	 *           Change in Y in pixels
	 * @param scrollX
	 *           Current X scroll value in pixels before applying deltaX
	 * @param scrollY
	 *           Current Y scroll value in pixels before applying deltaY
	 * @param scrollRangeX
	 *           Maximum content scroll range along the X axis
	 * @param scrollRangeY
	 *           Maximum content scroll range along the Y axis
	 * @param maxOverScrollX
	 *           Number of pixels to overscroll by in either direction along the X axis.
	 * @param maxOverScrollY
	 *           Number of pixels to overscroll by in either direction along the Y axis.
	 * @param isTouchEvent
	 *           true if this scroll operation is the result of a touch event.
	 * @return true if scrolling was clamped to an over-scroll boundary along either axis, false otherwise.
	 */
	protected boolean overScrollingBy( int deltaX, int deltaY, int scrollX, int scrollY, int scrollRangeX, int scrollRangeY, int maxOverScrollX, int maxOverScrollY, boolean isTouchEvent ) {

		final int overScrollMode = mOverScrollMode;
		final boolean toLeft = deltaX > 0;
		final boolean overScrollHorizontal = overScrollMode == OVER_SCROLL_ALWAYS;

		int newScrollX = scrollX + deltaX;
		if ( !overScrollHorizontal ) {
			maxOverScrollX = 0;
		}

		// Clamp values if at the limits and record
		final int left = mMinX - maxOverScrollX;
		final int right = mMaxX == Integer.MAX_VALUE ? mMaxX : ( mMaxX + maxOverScrollX );
		

		boolean clampedX = false;
		if ( newScrollX > right && toLeft ) {
			newScrollX = right;
			deltaX = mMaxX - scrollX;
			clampedX = true;
		} else if ( newScrollX < left && !toLeft ) {
			newScrollX = left;
			deltaX = mMinX - scrollX;
			clampedX = true;
		}

		onScrolling( newScrollX, deltaX, clampedX );
		return clampedX;
	}

	public boolean onScrolling( int scrollX, int deltaX, boolean clampedX ) {
		
		if ( mAdapter == null ) return true;

		if ( !mFlingRunnable.isFinished() ) {
			mCurrentX = getScrollX();
			if ( clampedX ) {
				mFlingRunnable.springBack( scrollX, 0, mMinX, mMaxX, 0, 0 );
			}
		} else {
			trackMotionScroll( scrollX );
		}

		return true;
	}

	@Override
	public void computeScroll() {

		if ( mFlingRunnable.computeScrollOffset() ) {
			int oldX = mCurrentX;
			int x = mFlingRunnable.getCurrX();

			if ( oldX != x ) {
				final int range = getScrollRange();
				final int overscrollMode = mOverScrollMode;
				final boolean canOverscroll = overscrollMode == OVER_SCROLL_ALWAYS || ( overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && range > 0 );
				
				overScrollingBy( x - oldX, 0, oldX, 0, range, 0, mOverscrollDistance, 0, false );

				if ( canOverscroll && mEdgeGlowLeft != null ) {
					if ( x < 0 && oldX >= 0 ) {
						mEdgeGlowLeft.onAbsorb( (int) mFlingRunnable.getCurrVelocity() );
					} else if ( x > range && oldX <= range ) {
						mEdgeGlowRight.onAbsorb( (int) mFlingRunnable.getCurrVelocity() );
					}
				}
			}
			postInvalidate();
		}
	}

	int getScrollRange() {
		if ( getChildCount() > 0 ) {
			return mMaxX - mMinX;
		}
		return 0;
	}

	/** The m animation duration. */
	int mAnimationDuration = 400;

	/** The m child height. */
	int mMaxX, mMinX, mChildHeight;
	
	/** The m should stop fling. */
	boolean mShouldStopFling;

	/** The m to left. */
	boolean mToLeft;

	/** The m current x. */
	int mCurrentX = 0;

	/** The m old x. */
	int mOldX = 0;

	/** The m touch slop. */
	int mTouchSlop;

	int mEdgesHeight = -1;

	int mEdgesGravityY = Gravity.CENTER;

    boolean mAutoAppendData = false;

	@Override
	public void scrollIntoSlots() {
		if ( !mFlingRunnable.isFinished() ) {
			return;
		}

		// boolean greater_enough = mItemCount * ( mChildWidth ) > getWidth();

		/*if ( mCurrentX > mMaxX || mCurrentX < mMinX ) {
			if ( mCurrentX > mMaxX ) {
				if ( mMaxX < 0 ) {
					mFlingRunnable.startUsingDistance( mCurrentX, mMinX - mCurrentX );
				} else {
					mFlingRunnable.startUsingDistance( mCurrentX, mMaxX - mCurrentX );
				}
				return;
			} else {
				mFlingRunnable.startUsingDistance( mCurrentX, mMinX - mCurrentX );
				return;
			}
		}*/
		
		onFinishedMovement();
	}

	/**
	 * On finished movement.
	 */
	protected void onFinishedMovement() {
	}

	/** The m gesture listener. */
	private OnGestureListener mGestureListener = new GestureDetector.SimpleOnGestureListener() {

		@Override
		public boolean onDoubleTap( MotionEvent e ) {
			return false;
		};

		@Override
		public boolean onSingleTapUp( MotionEvent e ) {
			return onItemClick( e );
		};

		@Override
		public boolean onDown( MotionEvent e ) {
			return false;
			// return HorizontalFixedListView.this.onDown( e );
		};

		@Override
		public boolean onFling( MotionEvent e1, MotionEvent e2, float velocityX, float velocityY ) {
			return false;
			// return HorizontalFixedListView.this.onFling( e1, e2, velocityX, velocityY );
		};

		@Override
		public void onLongPress( MotionEvent e ) {
			HorizontalVariableListView.this.onLongPress( e );
		};

		@Override
		public boolean onScroll( MotionEvent e1, MotionEvent e2, float distanceX, float distanceY ) {
			return false;
			// return HorizontalFixedListView.this.onScroll( e1, e2, distanceX, distanceY );
		};

		@Override
		public void onShowPress( MotionEvent e ) {};

		@Override
		public boolean onSingleTapConfirmed( MotionEvent e ) {
			return true;
		}

		private boolean onItemClick( MotionEvent ev ) {
			if ( !mFlingRunnable.isFinished() || mWasFlinging ) return false;

			Rect viewRect = new Rect();

			for ( int i = 0; i < getChildCount(); i++ ) {
				View child = getChildAt( i );
				int left = child.getLeft();
				int right = child.getRight();
				int top = child.getTop();
				int bottom = child.getBottom();
				viewRect.set( left, top, right, bottom );
				viewRect.offset( -mCurrentX, 0 );

				if ( viewRect.contains( (int) ev.getX(), (int) ev.getY() ) ) {
					if ( mOnItemClicked != null ) {
						playSoundEffect( SoundEffectConstants.CLICK );
						mOnItemClicked.onItemClick( HorizontalVariableListView.this, child, mLeftViewIndex + 1 + i, mAdapter.getItemId( mLeftViewIndex + 1 + i ) );
					}
					if ( mOnItemSelected != null ) {
						mOnItemSelected.onItemSelected( HorizontalVariableListView.this, child, mLeftViewIndex + 1 + i, mAdapter.getItemId( mLeftViewIndex + 1 + i ) );
					}
					break;
				}
			}
			return true;
		}
	};
	
	public void centerChildRequestFocus(){
		if ( !mFlingRunnable.isFinished() || mWasFlinging ) return;

		Rect viewRect = new Rect();

		for ( int i = 0; i < getChildCount(); i++ ) {
			View child = getChildAt( i );
			int left = child.getLeft();
			int right = child.getRight();
			int top = child.getTop();
			int bottom = child.getBottom();
			viewRect.set( left, top, right, bottom );
			viewRect.offset( -mCurrentX, 0 );

			if ( viewRect.contains( getWidth()/2, (int)(getY() + mItemHeight/2) ) ) {
				child.requestFocus();
				TextView textView = (TextView) child.findViewById(R.id.card_text);
				if (textView != null && !mFocusTextSelected){
					textView.setSelected(true);
					mFocusTextSelected = true;
				}
				break;
			}
		}
	}

	public View getChild( MotionEvent e ) {
		Rect viewRect = new Rect();
		for ( int i = 0; i < getChildCount(); i++ ) {
			View child = getChildAt( i );
			int left = child.getLeft();
			int right = child.getRight();
			int top = child.getTop();
			int bottom = child.getBottom();
			viewRect.set( left, top, right, bottom );
			viewRect.offset( -mCurrentX, 0 );

			if ( viewRect.contains( (int) e.getX(), (int) e.getY() ) ) {
				return child;
			}
		}
		return null;
	}

	@Override
	public int getMinX() {
		return mMinX;
	}

	@Override
	public int getMaxX() {
		return Integer.MAX_VALUE;
	}

	public void setDragTolerance( int value ) {
		mDragTolerance = value;
	}
	
	// below added by chen
	@Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // Dispatch in the normal way
        boolean handled = super.dispatchKeyEvent(event);
        if (!handled) {
            // If we didn't handle it...
            View focused = getFocusedChild();
            if (focused != null && event.getAction() == KeyEvent.ACTION_DOWN) {
                // ... and our focused child didn't handle it
                // ... give it to ourselves so we can scroll if necessary
                handled = onKeyDown(event.getKeyCode(), event);
            }
        }
        return handled;
    }
	
	@Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        /*if (getChildCount() > 0) {
            View focusedChild = getFocusedChild();
            if (focusedChild != null) {
                final int childPosition = mFirstPosition + indexOfChild(focusedChild);
                final int childBottom = focusedChild.getBottom();
                final int offset = Math.max(0, childBottom - (h - mPaddingTop));
                final int top = focusedChild.getTop() - offset;
                if (mFocusSelector == null) {
                    mFocusSelector = new FocusSelector();
                }
                post(mFocusSelector.setup(childPosition, top));
            }
        }*/
        super.onSizeChanged(w, h, oldw, oldh);
    }
	
	@Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return commonKey(keyCode, 1, event);
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
        return commonKey(keyCode, repeatCount, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        return commonKey(keyCode, 1, event);
    }

    private boolean commonKey(int keyCode, int count, KeyEvent event) {

    	try {
			Thread.sleep(50);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	if (mAdapter == null) {
            return false;
        }

        if (mDataChanged) {
            layoutChildren();
        }

    	int action = event.getAction();
    	
//    	Log.d(LOG_TAG, "mPosition = " + mPosition);
//    	Log.d(LOG_TAG, "mLeftViewIndex = " + mLeftViewIndex);
//    	Log.d(LOG_TAG, "mRightViewIndex = " + mRightViewIndex);
    	
    	if (action != KeyEvent.ACTION_UP) {
    		/*if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
    				|| keyCode == KeyEvent.KEYCODE_DPAD_RIGHT
    				|| keyCode == KeyEvent.KEYCODE_DPAD_UP
    				|| keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
	    		View v = getChildAt(mCountInLine + 1);
	    		if (v != null){
	    			TextView textView = (TextView) v.findViewById(R.id.card_text);
	    			if (textView != null){
	    				textView.setSelected(false);
	    				mFocusTextSelected = false;
	    			}
	    		}
    		}*/
    		
    		switch (keyCode) {
    		case KeyEvent.KEYCODE_DPAD_LEFT:
    			if (mFixFocus){
    				// comment for fix focus loop ..................................
////    				if (mPosition > 0 ){
////    					trackMotionScroll(mCurrentX - focused.getWidth() - mDividerWidth);
////    					if (!mFlingRunnable.isFinished())
////            				return true;
//            			
//    				// comment for endless scroll
////            			if ( event.getRepeatCount() < 8 ) {
//        					mFlingRunnable.startUsingDistance(mScrollStartX, -(mItemWidth + mDividerWidth));
//        					mScrollStartX = mScrollStartX - (mItemWidth + mDividerWidth);
////            			}
////            			else
////            				return true;
////    				}
////    				else
////    					return true;
        		
        			if (mNoLoop && mPosition <= 0){
        				return true;
        			} else {
        				mFlingRunnable.startUsingDistance(mScrollStartX, -mItemWidth);
//        				trackMotionScroll(mScrollStartX - mItemWidth - mDividerWidth);
        				mScrollStartX = mScrollStartX - mItemWidth;
        			}
        					
        					
    			} else {
	    			if (mPosition > 0){
	    			}
	    			else 
	    				return true;
	    			
	    			if (mPosition == mLeftViewIndex + 1 && mRightViewIndex > mRightViewIndexFilledMinValue ){
	    				trackMotionScroll(mCurrentX - mItemWidth);
	    			}
    			}
    			
                break;

            case KeyEvent.KEYCODE_DPAD_RIGHT:

            	if (mFixFocus){
            		// comment for fix focus loop ..................................
////            		if (mPosition * 2 < mAdapter.getRecentsCount() -2 ){
////            			trackMotionScroll(mCurrentX + mItemWidth + mDividerWidth);
////            			if (!mFlingRunnable.isFinished())
////            				return true;
//            			
//            			// comment for endless scroll
////            			if ( event.getRepeatCount() < 8 ) {
//        					mFlingRunnable.startUsingDistance(mScrollStartX,mItemWidth + mDividerWidth);
//        					mScrollStartX = mScrollStartX + mItemWidth + mDividerWidth;
////            			}
////            			else
////            				return true;
//            			
////            		}
////            		else
////            			return true;
        					
        			if (mNoLoop && mPosition >= mAdapter.getCount() - 1){
        				return true;
        			}else {
        				mFlingRunnable.startUsingDistance(mScrollStartX,mItemWidth);
//        				trackMotionScroll(mScrollStartX + mItemWidth + mDividerWidth);
        				mScrollStartX = mScrollStartX + mItemWidth;
        			}
            	} else {
	            	if (mPosition < mAdapter.getCount() - 1){
	    			}
	            	else
	            		return true;
	            	
	            	if (mPosition == mRightViewIndex - 1 && mLeftViewIndex < mAdapterItemCount-1-mRightViewIndexFilledMinValue){
	            		trackMotionScroll(mCurrentX + mItemWidth);
	            	}
            	}
            	
                break;
            
            case KeyEvent.KEYCODE_DPAD_UP:
            	if ( !mFlingRunnable.isFinished() ) {
            		return true;
        		}
            	mCenterFocusFounded = false;
            	if (mUpAndDownListener != null && mHasNextUpFocus && !mIsFirst)
            		mUpAndDownListener.go(true);
            	else if (mIsFirst && mNextUpFocusId != 0){
            		mOnItemSelected.onNothingSelected( HorizontalVariableListView.this);
            		return false; 
            	} else {
            		this.requestFocus();
            		centerChildRequestFocus();
            		return true;
            	}
            	
                break;
                
            case KeyEvent.KEYCODE_DPAD_DOWN:
            	if ( !mFlingRunnable.isFinished() ) {
        			return true;
        		}
            	if (mUpAndDownListener != null && mHasNextDownFocus )
            		mUpAndDownListener.go(false);
            	else{
            		centerChildRequestFocus();
            		return true;
            	}
                break;
                
            case KeyEvent.KEYCODE_BACK:
            	mOnItemSelected.onNothingSelected( HorizontalVariableListView.this);
            	break;
    		}
    	}
    	
    	return false;
    	
    	
        /*if (mAdapter == null || !mIsAttached) {
            return false;
        }

        if (mDataChanged) {
            layoutChildren();
        }

        boolean handled = false;
        int action = event.getAction();

        if (action != KeyEvent.ACTION_UP) {
            switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_UP:
                if (event.hasNoModifiers()) {
                    handled = resurrectSelectionIfNeeded();
                    if (!handled) {
                        while (count-- > 0) {
                            if (arrowScroll(FOCUS_UP)) {
                                handled = true;
                            } else {
                                break;
                            }
                        }
                    }
                } else if (event.hasModifiers(KeyEvent.META_ALT_ON)) {
                    handled = resurrectSelectionIfNeeded() || fullScroll(FOCUS_UP);
                }
                break;

            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (event.hasNoModifiers()) {
                    handled = resurrectSelectionIfNeeded();
                    if (!handled) {
                        while (count-- > 0) {
                            if (arrowScroll(FOCUS_DOWN)) {
                                handled = true;
                            } else {
                                break;
                            }
                        }
                    }
                } else if (event.hasModifiers(KeyEvent.META_ALT_ON)) {
                    handled = resurrectSelectionIfNeeded() || fullScroll(FOCUS_DOWN);
                }
                break;

            case KeyEvent.KEYCODE_DPAD_LEFT:
                if (event.hasNoModifiers()) {
                    handled = handleHorizontalFocusWithinListItem(View.FOCUS_LEFT);
                }
                break;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                if (event.hasNoModifiers()) {
                    handled = handleHorizontalFocusWithinListItem(View.FOCUS_RIGHT);
                }
                break;

            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
                if (event.hasNoModifiers()) {
                    handled = resurrectSelectionIfNeeded();
                    if (!handled
                            && event.getRepeatCount() == 0 && getChildCount() > 0) {
                        keyPressed();
                        handled = true;
                    }
                }
                break;

            case KeyEvent.KEYCODE_SPACE:
                if (mPopup == null || !mPopup.isShowing()) {
                    if (event.hasNoModifiers()) {
                        handled = resurrectSelectionIfNeeded() || pageScroll(FOCUS_DOWN);
                    } else if (event.hasModifiers(KeyEvent.META_SHIFT_ON)) {
                        handled = resurrectSelectionIfNeeded() || pageScroll(FOCUS_UP);
                    }
                    handled = true;
                }
                break;

            case KeyEvent.KEYCODE_PAGE_UP:
                if (event.hasNoModifiers()) {
                    handled = resurrectSelectionIfNeeded() || pageScroll(FOCUS_UP);
                } else if (event.hasModifiers(KeyEvent.META_ALT_ON)) {
                    handled = resurrectSelectionIfNeeded() || fullScroll(FOCUS_UP);
                }
                break;

            case KeyEvent.KEYCODE_PAGE_DOWN:
                if (event.hasNoModifiers()) {
                    handled = resurrectSelectionIfNeeded() || pageScroll(FOCUS_DOWN);
                } else if (event.hasModifiers(KeyEvent.META_ALT_ON)) {
                    handled = resurrectSelectionIfNeeded() || fullScroll(FOCUS_DOWN);
                }
                break;

            case KeyEvent.KEYCODE_MOVE_HOME:
                if (event.hasNoModifiers()) {
                    handled = resurrectSelectionIfNeeded() || fullScroll(FOCUS_UP);
                }
                break;

            case KeyEvent.KEYCODE_MOVE_END:
                if (event.hasNoModifiers()) {
                    handled = resurrectSelectionIfNeeded() || fullScroll(FOCUS_DOWN);
                }
                break;

            case KeyEvent.KEYCODE_TAB:
                // XXX Sometimes it is useful to be able to TAB through the items in
                //     a ListView sequentially.  Unfortunately this can create an
                //     asymmetry in TAB navigation order unless the list selection
                //     always reverts to the top or bottom when receiving TAB focus from
                //     another widget.  Leaving this behavior disabled for now but
                //     perhaps it should be configurable (and more comprehensive).
                if (false) {
                    if (event.hasNoModifiers()) {
                        handled = resurrectSelectionIfNeeded() || arrowScroll(FOCUS_DOWN);
                    } else if (event.hasModifiers(KeyEvent.META_SHIFT_ON)) {
                        handled = resurrectSelectionIfNeeded() || arrowScroll(FOCUS_UP);
                    }
                }
                break;
            }
        }

        if (handled) {
            return true;
        }

        if (sendToTextFilter(keyCode, count, event)) {
            return true;
        }
		
        switch (action) {
            case KeyEvent.ACTION_DOWN:
                return super.onKeyDown(keyCode, event);

            case KeyEvent.ACTION_UP:
                return super.onKeyUp(keyCode, event);

            case KeyEvent.ACTION_MULTIPLE:
                return super.onKeyMultiple(keyCode, count, event);

            default: // shouldn't happen
                return false;
        }
        
        */
        
    }

}
