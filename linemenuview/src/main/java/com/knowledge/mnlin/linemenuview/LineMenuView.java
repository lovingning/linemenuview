package com.knowledge.mnlin.linemenuview;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.List;


/**
 * Created by admin on 2017/4/12.
 * <p>
 * 自定义横向的menu菜单
 */
public class LineMenuView extends LinearLayout implements Cloneable, View.OnClickListener {
    /**
     * 位置信息情况
     */
    public static final int TAG_POSITION = R.id.LINE_MENU_VIEW_TAG_POSITION;

    /**
     * 布局文件
     */
    public static final int LAYOUT_SELF = R.layout.layout_line_menu;

    /**
     * 设置图片最大为36dp
     */
    private static final int MAX_PICTURE_SIZE = 72;

    /**
     * 默认动画执行的时间:单位ms
     */
    private static final int DEFAULT_ANIMATOR_TIME = 300;

    /**
     * 手指点击的xy位置
     */
    protected float clickX;
    protected float clickY;

    /**
     * 左侧菜单按钮
     * 右侧消息菜单
     */
    MTextView mTvMenu;
    TextView mTvBriefInfo;
    SwitchCompat mScSwitch;
    RadioButton mRbCheck;
    ImageView mIvImage;
    ImageView mIconOpenClose;

    private AttributeSet attrs;

    @Nullable
    private LineMenuListener listener;

    /**
     * true表示on状态
     */
    private boolean transition;

    /**
     * 是否纳入计算体系
     * <p>
     * 0 不处理
     * 1 纳入
     * 2 不纳入
     */
    private int calculation;

    public LineMenuView(Context context) {
        this(context, null);
    }

    public LineMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LineMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.attrs = attrs;

        //加载布局
        inflate(context, LAYOUT_SELF, this);
        setParam();
        super.setOnClickListener(this);
        mTvMenu = findViewById(R.id.tv_menu_line_menu_view);
        mTvBriefInfo = findViewById(R.id.tv_brief_info_line_menu_view);
        mScSwitch = findViewById(R.id.sc_switch_line_menu_view);
        mRbCheck = findViewById(R.id.rb_check_line_menu_view);
        mIvImage = findViewById(R.id.iv_image_line_menu_view);
        mIconOpenClose = findViewById(R.id.icon_open_close_line_menu_view);

        //处理逻辑
        initData();
    }



    /**
     * 设置类自身必要的参数
     */
    protected void setParam() {
        setGravity(Gravity.CENTER_VERTICAL);
        setOrientation(HORIZONTAL);
        if (getMinimumHeight() <= 0) {
            //只有获知最小高度未赋值时,才进行处理
            setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.prefer_view_height_48dp));
        }
    }

    /**
     * 处理xml属性
     */
    protected void initData() {
        TypedArray params = getContext().obtainStyledAttributes(attrs, R.styleable.LineMenuView);

        //查看当前是否需要显示switch,默认显示为off状态
        //查看当前icon资源文件，如果未设定则默认不显示
        //获取menu需要显示的内容
        int plugin;
        int resId;
        for (int i = params.getIndexCount() - 1; i >= 0; i--) {
            resId = params.getIndex(i);
            if (resId == R.styleable.LineMenuView_LineMenuView_plugin) {
                plugin = params.getInt(resId, 0);
                switch (plugin) {
                    case 0:
                        break;
                    case 1:
                        mTvBriefInfo.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        mScSwitch.setVisibility(View.VISIBLE);
                        break;
                    case 3:
                        mRbCheck.setVisibility(View.VISIBLE);
                        break;
                    case 4:
                        mIvImage.setVisibility(View.VISIBLE);
                        break;
                    case 5:
                        mIconOpenClose.setVisibility(View.VISIBLE);
                        break;
                }

                //当transition处于显示状态,进行显示/隐藏处理
            } else if (resId == R.styleable.LineMenuView_LineMenuView_transition) {
                int switchValue = params.getInt(resId, 0);
                switch (switchValue) {
                    case 1: {
                        TransitionDrawable drawable = (TransitionDrawable) mIconOpenClose.getDrawable();
                        drawable.setCrossFadeEnabled(true);
                        transition = true;
                        drawable.startTransition(0);
                        break;
                    }
                }

                //当switch处于显示状态,进行显示/隐藏处理
            } else if (resId == R.styleable.LineMenuView_LineMenuView_switch) {
                int switchValue = params.getInt(resId, 0);
                switch (switchValue) {
                    case 1: {
                        mScSwitch.setChecked(true);
                        break;
                    }
                    case 0: {
                        mScSwitch.setChecked(false);
                        break;
                    }
                }

                //是否纳入计算体系
            } else if (resId == R.styleable.LineMenuView_LineMenuView_for_calculation) {
                calculation = params.getInt(resId, 0);

                //当radio处于显示状态,进行显示/隐藏处理
            } else if (resId == R.styleable.LineMenuView_LineMenuView_radio) {
                int radioValue = params.getInt(resId, 0);
                switch (radioValue) {
                    case 1: {
                        mRbCheck.setChecked(true);
                        break;
                    }
                    case 0: {
                        mRbCheck.setChecked(false);
                        break;
                    }
                }

                //处理badge图标
            } else if (resId == R.styleable.LineMenuView_LineMenuView_badge) {
                Drawable d = params.getDrawable(resId);
                if (d != null) {
                    d.setBounds(0, 0, Math.min(d.getMinimumWidth(), MAX_PICTURE_SIZE), Math.min(d.getMinimumHeight(), MAX_PICTURE_SIZE));
                    Drawable[] compoundDrawables = mTvBriefInfo.getCompoundDrawables();
                    mTvBriefInfo.setCompoundDrawables(d, compoundDrawables[1], compoundDrawables[2], compoundDrawables[3]);
                }

                //处理icon图标
            } else if (resId == R.styleable.LineMenuView_LineMenuView_icon) {
                Drawable d = params.getDrawable(resId);
                if (d != null) {
                    d.setBounds(0, 0, Math.min(d.getMinimumWidth(), MAX_PICTURE_SIZE), Math.min(d.getMinimumHeight(), MAX_PICTURE_SIZE));
                    Drawable[] compoundDrawables = mTvMenu.getCompoundDrawables();
                    mTvMenu.setCompoundDrawables(d, compoundDrawables[1], compoundDrawables[2], compoundDrawables[3]);
                }

                //处理navigation图标
            } else if (resId == R.styleable.LineMenuView_LineMenuView_navigation) {
                Drawable d = params.getDrawable(resId);
                if (d != null) {
                    d.setBounds(0, 0, Math.min(d.getMinimumWidth(), MAX_PICTURE_SIZE), Math.min(d.getMinimumHeight(), MAX_PICTURE_SIZE));
                    Drawable[] compoundDrawables = mTvBriefInfo.getCompoundDrawables();
                    mTvBriefInfo.setCompoundDrawables(compoundDrawables[0], compoundDrawables[1], d, compoundDrawables[3]);
                }

                //处理menu标题
            } else if (resId == R.styleable.LineMenuView_LineMenuView_menu) {
                mTvMenu.setText(params.getString(resId));

                //处理brief标题
            } else if (resId == R.styleable.LineMenuView_LineMenuView_brief) {
                mTvBriefInfo.setText(params.getString(resId));

                //处理menu标题
            } else if (resId == R.styleable.LineMenuView_LineMenuView_menu_text_color) {
                mTvMenu.setTextColor(params.getColorStateList(resId));

                //处理brief标题
            } else if (resId == R.styleable.LineMenuView_LineMenuView_brief_text_color) {
                mTvBriefInfo.setTextColor(params.getColorStateList(resId));

                //处理menu标题
            } else if (resId == R.styleable.LineMenuView_LineMenuView_menu_text_size) {
                mTvMenu.setTextSize(TypedValue.COMPLEX_UNIT_PX, params.getDimensionPixelSize(resId, (int) mTvMenu.getTextSize()));

                //处理brief标题
            } else if (resId == R.styleable.LineMenuView_LineMenuView_brief_text_size) {
                mTvBriefInfo.setTextSize(TypedValue.COMPLEX_UNIT_PX, params.getDimensionPixelSize(resId, (int) mTvMenu.getTextSize()));
            }
        }

        //如果发现右侧的文本drawable设置了两个,则判断文字是否存在,不存在则将drawablePadding设置一半
        Drawable[] compoundDrawables = mTvBriefInfo.getCompoundDrawables();
        int count = (compoundDrawables[0] == null ? 0 : 1) + (compoundDrawables[0] == null ? 0 : 1);
        if (count > 1) {
            mTvBriefInfo.setCompoundDrawablePadding(mTvBriefInfo.getCompoundDrawablePadding() / 2);
        }

        mIvImage.getDrawable().setVisible(false, false);

        //如果当前view所在的context对象声明了该接口,那么就直接进行绑定,如果未纳入计数体系,则不会自动添加listener
        if (getContext() instanceof LineMenuListener && setListenerIsSelf() && calculation != 2) {
            setOnClickListener((LineMenuListener) getContext());
        }

        params.recycle();
    }

    /**
     * 是否设置onClickLisener为自身this
     *
     * @return true表示设置
     */
    protected boolean setListenerIsSelf() {
        return true;
    }

    ///////////////////////////////////////// 设置属性值方法-begin

    /**
     * @param plugin 0 表示不显示任何插件
     *               1 表示显示textView
     *               2 表示显示switch
     *               3 表示显示radio
     *               4 表示select
     *               5 表示transition
     */
    public LineMenuView setPlugin(int plugin) {
        mTvBriefInfo.setVisibility(plugin == 1 ? View.VISIBLE : View.GONE);
        mScSwitch.setVisibility(plugin == 2 ? View.VISIBLE : View.GONE);
        mRbCheck.setVisibility(plugin == 3 ? View.VISIBLE : View.GONE);
        mIvImage.setVisibility(plugin == 4 ? View.VISIBLE : View.GONE);
        mIconOpenClose.setVisibility(plugin == 5 ? View.VISIBLE : View.GONE);
        return this;
    }

    public boolean getSwitch() {
        return mScSwitch.isChecked();
    }

    public LineMenuView setSwitch(boolean checked) {
        mScSwitch.setChecked(checked);
        return this;
    }

    public boolean getRadio() {
        return mRbCheck.isChecked();
    }

    public LineMenuView setRadio(boolean checked) {
        mRbCheck.setChecked(checked);
        return this;
    }

    public LineMenuView setMenu(String title) {
        mTvMenu.setText(title);
        return this;
    }

    public LineMenuView setIcon(Drawable d) {
        if (d != null) {
            d.setBounds(0, 0, Math.min(d.getMinimumWidth(), MAX_PICTURE_SIZE), Math.min(d.getMinimumHeight(), MAX_PICTURE_SIZE));
            Drawable[] compoundDrawables = mTvMenu.getCompoundDrawables();
            mTvMenu.setCompoundDrawables(d, compoundDrawables[1], compoundDrawables[2], compoundDrawables[3]);
        }
        return this;
    }

    public LineMenuView setMenuTextColor(int color) {
        mTvMenu.setTextColor(color);
        return this;
    }

    public LineMenuView setMenuTextSize(int unit, int size) {
        mTvMenu.setTextSize(unit, size);
        return this;
    }

    public String getBrief() {
        return mTvBriefInfo.getText().toString();
    }

    public LineMenuView setBrief(int brief) {
        mTvBriefInfo.setText(String.valueOf(brief));
        return this;
    }

    public LineMenuView setBrief(String brief) {
        mTvBriefInfo.setText(brief);
        return this;
    }

    public LineMenuView setBriefSize(int unit, int size) {
        mTvBriefInfo.setTextSize(unit, size);
        return this;
    }

    public LineMenuView setNavigation(Drawable drawable) {
        Drawable[] compoundDrawables = mTvBriefInfo.getCompoundDrawables();
        mTvBriefInfo.setCompoundDrawables(compoundDrawables[0], compoundDrawables[1], drawable, compoundDrawables[3]);
        return this;
    }

    public LineMenuView setBadge(Drawable drawable) {
        Drawable[] compoundDrawables = mTvBriefInfo.getCompoundDrawables();
        drawable.setBounds(0, 0, MAX_PICTURE_SIZE, MAX_PICTURE_SIZE);
        mTvBriefInfo.setCompoundDrawables(drawable, compoundDrawables[1], compoundDrawables[2], compoundDrawables[3]);
        return this;
    }

    public LineMenuView setBriefColor(@ColorInt int color) {
        mTvBriefInfo.setTextColor(color);
        return this;
    }

    public LineMenuView setMenuColor(@ColorInt int color) {
        mTvMenu.setTextColor(color);
        return this;
    }

    public boolean getRightSelect() {
        return mIvImage.getVisibility() == View.VISIBLE;
    }

    public LineMenuView setRightSelect(boolean select) {
        mIvImage.setVisibility(select ? View.VISIBLE : View.INVISIBLE);
        return this;
    }

    public boolean getTransition() {
        return transition;
    }

    /**
     * @param toggle true表示显示打开的开关
     * @return 返回自身
     */
    public LineMenuView setTransition(boolean toggle) {
        if (toggle != transition) {
            TransitionDrawable drawable = (TransitionDrawable) mIconOpenClose.getDrawable();
            drawable.setCrossFadeEnabled(true);
            if (toggle) {
                drawable.startTransition(DEFAULT_ANIMATOR_TIME);
            } else {
                drawable.reverseTransition(DEFAULT_ANIMATOR_TIME);
            }
            transition = toggle;
        }
        return this;
    }

    /**
     * 获取menu菜单文字
     */
    public CharSequence getMenuText() {
        return mTvMenu.getText();
    }

    /**
     * 获取brief
     */
    public CharSequence getBriefText() {
        return mTvBriefInfo.getText();
    }

    /**
     * 如果child的calculation为on,则纳入统计
     * 如果child的calculation为default,且child可见,则纳入统计
     * 否则不会纳入统计
     *
     * @return 返回当前LineMenuView所在的兄弟布局(同一个viewParent)
     */
    public List<LineMenuView> getFriendsWithSelf() {
        LinkedList<LineMenuView> childs = new LinkedList<>();

        ViewGroup parent = (ViewGroup) getParent();
        if (parent == null) {
            childs.add(this);
        } else {
            int childCount = parent.getChildCount();
            View child;
            for (int i = 0; i < childCount; i++) {
                child = parent.getChildAt(i);
                if (child instanceof LineMenuView && (((LineMenuView) child).calculation == 1 || ((LineMenuView) child).calculation == 0 && child.getVisibility() == View.VISIBLE)) {
                    childs.add((LineMenuView) child);
                }
            }
        }
        return childs;
    }

    /**
     * 所有可见不可见的child都会被纳入统计
     *
     * @return 返回当前LineMenuView所在的兄弟布局(同一个viewParent)
     */
    public List<LineMenuView> getFriendsEveryOne() {
        LinkedList<LineMenuView> childs = new LinkedList<>();

        ViewGroup parent = (ViewGroup) getParent();
        if (parent == null) {
            childs.add(this);
        } else {
            int childCount = parent.getChildCount();
            View child;
            for (int i = 0; i < childCount; i++) {
                child = parent.getChildAt(i);
                if (child instanceof LineMenuView) {
                    childs.add((LineMenuView) child);
                }
            }
        }
        return childs;
    }

    //////////////////////////////////////////设置属性值方法-end
    /**
     * 如果当前有对自身的监听事件
     * 则在手指触摸屏幕时就保存手指坐标
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //若监听了自身view,则记录手指抬起时的坐标
        if (listener != null && event.getAction() == MotionEvent.ACTION_UP) {
            clickX = event.getX();
            clickY = event.getY();
        }
        return super.onTouchEvent(event);
    }

    /**
     * 拦截子类的事件处理
     */
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    /**
     * 处理点击事件,如果LineMenuView没有被外部重写clickListener接口,则可以判断是右侧还是左侧的文字相应点击事件
     * <p>
     * 如果是放在AdapterView中的话,可以在onItemClick时,通过performClick方法让onclick得以执行,当然,可以在此之前通过setTag(int key)方法将position信息传递过来,这样就可以实现具体的点击事件处理
     * <p>
     * 点击判断是以X轴坐标来判断的,可以通过重写onClick方法实现不同的逻辑
     */
    @Override
    public void onClick(View v) {
        //如果没有被监听,则不做任何处理
        if (listener == null) {
            return;
        }

        //判断手指抬起时的位置,由此来判断是由左侧或者右侧的文本相应点击事件
        int dividerX = mTvMenu.getRight();
        boolean consume;
        boolean setPosition = setTagPosition(((ViewGroup) getParent()));
        int position = getFriendsWithSelf().indexOf(this);
        if (clickX <= dividerX) {
            mTvMenu.setTag(R.id.LINE_MENU_VIEW_TAG_POSITION, setPosition ? position : -1);
            consume = listener.performClickLeft(mTvMenu);
        } else {
            mTvBriefInfo.setTag(R.id.LINE_MENU_VIEW_TAG_POSITION, setPosition ? position : -1);
            consume = listener.performClickRight(mTvBriefInfo);
        }

        //如果都没有被消费,则默认由performSelf来处理
        if (!consume) {
            this.setTag(R.id.LINE_MENU_VIEW_TAG_POSITION, setPosition ? position : -1);
            listener.performSelf(this);
        }
    }

    /**
     * 是否有必须在tag中设置位置信息
     * <p>
     * 如果为true那么可以通过getTag方法获取到当前this在父布局中所处的位置
     * 如果为false，那么位置就是-1（避免真正使用时的空指针异常）
     *
     * @param parent 父布局
     * @return true表示有必要
     */
    protected boolean setTagPosition(ViewGroup parent) {
        return parent != null && !(parent instanceof AdapterView) && !(parent instanceof RecyclerView);
    }

    /**
     * 屏蔽点击事件的设置,自身会处理点击事件
     * <p>
     * 该方法不可调用,因为调用也不会有任何操作
     */
    @Override
    @Deprecated
    public final void setOnClickListener(@Nullable OnClickListener onClickListener) {
        throw new UnsupportedOperationException("不支持设置OnClick监听");
    }

    /**
     * 重载父类的onclick处理方法
     */
    public final void setOnClickListener(@Nullable LineMenuListener listener) {
        this.listener = listener;
    }

    /**
     * 控件监听
     */
    public interface LineMenuListener {
        /**
         * 点击左侧文本
         *
         * @param v 被点击到的v;此时应该是左侧的TextView
         * @return 是否消费该点击事件, 如果返回true, 则performSelf将不会被调用
         */
        default boolean performClickLeft(TextView v) {
            return false;
        }

        /**
         * 注：该放置主要针对 text 插件设计，但即便是其他插件模式，也可以通过 v.getTag()方法获取到位置信息
         *
         * @param v 被点击到的v;此时应该是右侧的TextView
         * @return 是否消费该点击事件, 如果返回true, 则performSelf将不会被调用
         */
        default boolean performClickRight(TextView v) {
            return false;
        }

        /**
         * @param v 被点击到的v;此时应该是该view自身:LineMenuView
         */
        default void performSelf(LineMenuView v) {

        }
    }
}
