package com.innerdrawer.Ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.content.res.TypedArray
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.Log
import android.view.*
import android.widget.*
import androidx.annotation.IntDef
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.innerdrawer.Helpers.OnSwipeTouchListener
import com.innerdrawer.Model.NavMenuItem
import com.maurya.navdrawer.R
import pl.droidsonroids.gif.GifImageView
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy


class InnerDrawer : RelativeLayout {
    private var TAG = this.javaClass.simpleName

    //Context
    protected var mContext: Context? = null
    protected var mLayoutInflater: LayoutInflater? = null

    //Layouts
    protected var menuItemsList: ArrayList<NavMenuItem>? = null
    protected var headersItem: View? = null
    protected var footersItem: View? = null
    protected var rootLayout: RelativeLayout? = null
    protected var main_ll: CardView? = null
    protected var swipeLayout: LinearLayout? = null
    protected var mainswipeLayout: LinearLayout? = null
    protected var appbar_title: TextView? = null
    protected var nav_icon: ImageView? = null
    protected var drawer_ll: LinearLayout? = null
    protected var background_ll: GifImageView? = null
    protected var menu_ll: LinearLayout? = null
    protected var containerLL: LinearLayout? = null

    protected var background_bitmaps: Bitmap? = null
    protected var background_url: String? = null
    protected var background_alpha: Float = 1f
    protected var clear_cache: Boolean = false
    protected var activity: Activity? = null

    //Customization Variables
    private var menuItemFont: Typeface? = null
    private var appbarColor: Int = R.color.White
    private var appbarTitleTextColor: Int = R.color.Black
    private var navigationDrawerBackgroundColor: Int = R.color.White
    private var menuItemTextColor: Int = R.color.White
    private var menuItemIconColor: Int = R.color.White
    private var navIconTintColor: Int = R.color.Black

    private var menuItemTextSize = 15f
    private var menuItemIconSize = 20f

    //To check if drawer is open or not
    var isDrawerOpen = false
        private set

    //Other stuff
    private var menuItemGravity = Gravity.TOP
        private set
    private var isStatusBarIconDark = false
        private set
    private var isheaderInside = false
        private set
    private var isfooterInside = false
        private set
    private var closeDrawerOnMenuClick = true
        private set
    private var blurContent = true
        private set
    private var isAppBar = false
        private set
    private var centerX = 0f
    private var centerY = 0f
    private var shrink_value = 0.8f
    private var header_present = false
    private var footer_present = false

    @IntDef(STATE_OPEN, STATE_CLOSED, STATE_OPENING, STATE_CLOSING)
    @Retention(RetentionPolicy.SOURCE)
    private annotation class State

    //Listeners
    private var onnavIconClickListener: OnNavIconClickListener? = null
    private var onmenuItemClickListener: OnMenuItemClickListener? = null
    private var drawerListener: DrawerListener? = null

    constructor(context: Context?) : super(context) {}
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context)
        val a: TypedArray = context.getTheme().obtainStyledAttributes(
            attrs,
            R.styleable.InnerDrawer,
            0, 0
        )
        setAttributes(a)
        a.recycle()
    }

    //Adding the child views inside CardView LinearLayout
    override fun addView(child: View?, index: Int, params: ViewGroup.LayoutParams?) {
        if (containerLL == null) {
            super.addView(child, index, params)
        } else {
            //Forward these calls to the content view
            containerLL!!.addView(child, index, params)
        }
    }

    //Initialization
    fun init(context: Context?) {
        mContext = context
        mLayoutInflater = LayoutInflater.from(context)
        //Load RootView from xml
        val rootView: View =
            mLayoutInflater!!.inflate(R.layout.inner_drawer, this, true)
        rootLayout = rootView.findViewById(R.id.rootLayout)
        main_ll = rootView.findViewById(R.id.main_ll)
        swipeLayout = rootView.findViewById(R.id.swipe_layout)
        mainswipeLayout = rootView.findViewById(R.id.main_swipe_layout)
        drawer_ll = rootView.findViewById(R.id.drawer_ll)
        background_ll = rootView.findViewById(R.id.background_ll)
        menu_ll = rootView.findViewById(R.id.menu_ll)
        containerLL = rootView.findViewById(R.id.containerLL)
        menuItemsList = ArrayList()


        swipeLayout!!.setOnTouchListener(object : OnSwipeTouchListener(mContext!!) {
            override fun onSwipeRight(view: View?) {
                if (!isDrawerOpen) {
                    openDrawer()
                }
            }

            override fun onSwipeLeft(view: View?) {
                if (isDrawerOpen) {
                    closeDrawer()
                }
            }

            override fun onSwipeBottom(view: View?) {

            }

            override fun onSwipeTop(view: View?) {

            }

            override fun onClick(view: View?) {
            }

            override fun onLongClick(view: View?): Boolean {
                return false
            }

        })



        mainswipeLayout!!.setOnTouchListener(object : OnSwipeTouchListener(mContext!!) {
            override fun onSwipeRight(view: View?) {
                if (!isDrawerOpen) {
                    openDrawer()
                }
            }

            override fun onSwipeLeft(view: View?) {
                if (isDrawerOpen) {
                    closeDrawer()
                }
            }

            override fun onSwipeBottom(view: View?) {

            }

            override fun onSwipeTop(view: View?) {

            }

            override fun onClick(view: View?) {
                if (isDrawerOpen) {
                    closeDrawer()
                } else {
                    openDrawer()
                }
            }

            override fun onLongClick(view: View?): Boolean {
                return false
            }

        })

    }

    @SuppressLint("ResourceAsColor")
    protected fun setAllViews(activity: Activity) {
        this.activity = activity
        transparentStatusBar(activity)

        if (background_bitmaps != null) {
            //background_ll!!.background = BitmapDrawable(resources, background_bitmaps)
            if (clear_cache) {
                Glide.with(mContext!!).load(background_bitmaps)
                    .apply(getrequestopt()).into(background_ll!!)
            } else {
                Glide.with(mContext!!).load(background_bitmaps).into(background_ll!!)
            }
            background_ll!!.alpha = background_alpha
        }
        if (background_url != null) {
            //background_ll!!.background = BitmapDrawable(resources, background_bitmaps)
            if (clear_cache) {
                Glide.with(mContext!!).load(background_url).apply(getrequestopt())
                    .into(background_ll!!)
            } else {
                Glide.with(mContext!!).load(background_url).into(background_ll!!)
            }
            background_ll!!.alpha = background_alpha
        }

        if (headersItem != null) {
            header_present = true
            if (isheaderInside) {
                menu_ll!!.addView(headersItem)
            } else {
                drawer_ll!!.removeView(headersItem)
                drawer_ll!!.addView(headersItem, 0)
            }
        }

        rootLayout!!.setBackgroundColor(navigationDrawerBackgroundColor)
        main_ll!!.setCardBackgroundColor(navigationDrawerBackgroundColor)
        containerLL!!.setBackgroundColor(navigationDrawerBackgroundColor)

        setmenuLayout()

    }

    @SuppressLint("ResourceAsColor")
    private fun setmenuLayout() {
        (menu_ll!!.layoutParams as FrameLayout.LayoutParams).gravity = menuItemGravity

        menu_ll!!.removeAllViews()
        for (i in menuItemsList!!.indices) {
            val view: View = LayoutInflater.from(context).inflate(R.layout.menu_item, null)
            val menu_title: TextView = view.findViewById(R.id.menu_title)
            val menu_icon: GifImageView = view.findViewById(R.id.menu_icon)

            menu_title.setTextColor(menuItemTextColor)
            menu_title.textSize = menuItemTextSize
            val rootRL: LinearLayout = view.findViewById(R.id.rootRL)

            rootRL.tag = i
            rootRL.setOnClickListener { view ->
                menuItemClicked(menuItemsList!![i].title)
                if (closeDrawerOnMenuClick) {
                    closeDrawer()
                }
            }
            if (menuItemsList!![i].drawableId != null) {
                Glide.with(mContext!!).load(menuItemsList!![i].drawableId).into(menu_icon)
                menu_icon.setColorFilter(menuItemIconColor)
            } else if (menuItemsList!![i].iconUrl != null) {
                if (menuItemsList!![i].iconCache) {
                    Glide.with(mContext!!).load(menuItemsList!![i].iconUrl).apply(getrequestopt())
                        .into(menu_icon)
                } else {
                    Glide.with(mContext!!).load(menuItemsList!![i].iconUrl).into(menu_icon)
                }
            }

            menu_title.setText(menuItemsList!![i].title)
            if (menuItemFont != null) {
                menu_title.typeface = menuItemFont
            }

            menu_ll!!.addView(view)
        }
    }

    fun getrequestopt(): RequestOptions {
        return RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .skipMemoryCache(true)
    }

    fun transparentStatusBar(activity: Activity) {
        var icon_color = true
        if (isDarkMode(mContext!!)) {
            if (isDrawerOpen) {
                icon_color = !isStatusBarIconDark
            } else {
                icon_color = false
            }
        } else {
            if (isDrawerOpen) {
                icon_color = !isStatusBarIconDark
            } else {
                icon_color = true
            }
        }

        WindowInsetsControllerCompat(
            activity.window,
            activity.window.decorView
        ).isAppearanceLightStatusBars = icon_color

        val window: Window = activity.window
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(false)
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
            )
        }
        window.setStatusBarColor(Color.TRANSPARENT)
    }

    fun isDarkMode(context: Context): Boolean {
        val darkModeFlag =
            context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return darkModeFlag == Configuration.UI_MODE_NIGHT_YES
    }

    //Hamburger button Click Listener
    interface OnNavIconClickListener {
        fun onNavIconClicked()
    }

    //Listener for menu item click
    interface OnMenuItemClickListener {
        fun onMenuItemClicked(title: String)
    }

    //Listener for monitoring events about drawer.
    interface DrawerListener {
        //Called when a drawer is opening.
        fun onDrawerOpening()

        //Called when a drawer is closing.
        fun onDrawerClosing()

        //Called when a drawer has settled in a completely open state.
        fun onDrawerOpened()

        //Called when a drawer has settled in a completely closed state.
        fun onDrawerClosed()

        //Called when the drawer motion state changes. The new state will
        fun onDrawerStateChanged(@State newState: Int)
    }

    protected fun navIconClicked() {
        if (onnavIconClickListener != null) {
            onnavIconClickListener!!.onNavIconClicked()
        }
    }

    protected fun menuItemClicked(title: String) {
        if (onmenuItemClickListener != null) {
            onmenuItemClickListener!!.onMenuItemClicked(title)
        }
    }

    protected fun drawerOpened() {
        if (drawerListener != null) {
            drawerListener!!.onDrawerOpened()
            drawerListener!!.onDrawerStateChanged(STATE_OPEN)
        }
    }

    protected fun drawerClosed() {
        if (drawerListener != null) {
            drawerListener!!.onDrawerClosed()
            drawerListener!!.onDrawerStateChanged(STATE_CLOSED)
        }
    }

    protected fun drawerOpening() {
        if (drawerListener != null) {
            drawerListener!!.onDrawerOpening()
            drawerListener!!.onDrawerStateChanged(STATE_OPENING)
        }
    }

    protected fun drawerClosing() {
        if (drawerListener != null) {
            drawerListener!!.onDrawerClosing()
            drawerListener!!.onDrawerStateChanged(STATE_CLOSING)
        }
    }

    //Closes drawer
    fun closeDrawer() {
        drawerClosing()
        isDrawerOpen = false
        val stateSet = intArrayOf(android.R.attr.state_checked * if (isDrawerOpen) 1 else -1)
        nav_icon!!.setImageState(stateSet, true)
        appbar_title!!.animate().translationX(centerX).start()

        main_ll!!.animate().translationX(rootLayout!!.x)
            .scaleY(1f)
            .setDuration(500).start()

        //swipeLayout!!.visibility = View.GONE
        mainswipeLayout!!.visibility = View.GONE
        mainswipeLayout!!.setBackgroundColor(
            ContextCompat.getColor(
                mContext!!,
                R.color.transparent
            )
        )

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(Runnable {
            drawerClosed()
            main_ll!!.cardElevation = 0f
            main_ll!!.radius = 0f

            transparentStatusBar(activity!!)
        }, 500)
    }

    //Opens Drawer
    fun openDrawer() {
        drawerOpening()
        isDrawerOpen = true
        val stateSet = intArrayOf(android.R.attr.state_checked * if (isDrawerOpen) 1 else -1)
        nav_icon!!.setImageState(stateSet, true)
        main_ll!!.cardElevation = 10.0.toFloat()
        main_ll!!.radius = 60.0.toFloat()


        main_ll!!.animate()
            .translationX(rootLayout!!.x + rootLayout!!.width / 8 + rootLayout!!.width / 2)
            .scaleY(shrink_value).setDuration(500).start()


        //swipeLayout!!.visibility = View.VISIBLE
        mainswipeLayout!!.visibility = View.VISIBLE
        if (blurContent) {
            mainswipeLayout!!.setBackgroundColor(
                ContextCompat.getColor(
                    mContext!!,
                    R.color.transparent_White
                )
            )
        } else {
            mainswipeLayout!!.setBackgroundColor(
                ContextCompat.getColor(
                    mContext!!,
                    R.color.transparent
                )
            )
        }

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed(Runnable {
            drawerOpened()
            transparentStatusBar(activity!!)
        }, 250)
    }

    //set Attributes from xml
    @SuppressLint("ResourceAsColor")
    protected fun setAttributes(attrs: TypedArray) {
        setAppbarColor(
            attrs.getColor(
                R.styleable.InnerDrawer_appbarColor,
                ContextCompat.getColor(mContext!!, appbarColor)
                //resources.getColor(appbarColor)
            )
        )
        navIconTintColor = attrs.getColor(
            R.styleable.InnerDrawer_HamMenuIconTintColor,
            ContextCompat.getColor(mContext!!, navIconTintColor)
            // resources.getColor(menuIconTintColor)
        )

        setNavigationDrawerBackgroundColor(
            attrs.getColor(
                R.styleable.InnerDrawer_navigationDrawerBackgroundColor,
                ContextCompat.getColor(mContext!!, navigationDrawerBackgroundColor)
                // resources.getColor(navigationDrawerBackgroundColor)
            )
        )
        setMenuItemTextColor(
            attrs.getColor(
                R.styleable.InnerDrawer_menuItemTextColor,
                ContextCompat.getColor(mContext!!, menuItemTextColor)
                //resources.getColor(primaryMenuItemTextColor)
            )
        )



        setmenuItemTextSize(
            attrs.getDimension(
                R.styleable.InnerDrawer_menuItemTextSize,
                20f
            )
        )

        menuItemIconColor = attrs.getColor(
            R.styleable.InnerDrawer_menuItemIconColor,
            ContextCompat.getColor(mContext!!, menuItemIconColor)
            //resources.getColor(primaryMenuItemTextColor)
        )


        val header_lay = attrs.getResourceId(R.styleable.InnerDrawer_headerLayout, 0)
        if (header_lay > 0) {
            headersItem = LayoutInflater.from(context).inflate(
                header_lay, null
            )
            header_present = true
        } else {
            headersItem = null
        }

        val footer_lay = attrs.getResourceId(R.styleable.InnerDrawer_footerlayout, 0)
        if (footer_lay > 0) {
            footersItem = LayoutInflater.from(context).inflate(
                footer_lay, null
            )
            footer_present = true
        } else {
            footersItem = null
        }


        /*menuItemIconSize = attrs.getDimension(
            R.styleable.InnerDrawer_menuItemIconSize,
            20f
        )*/

        shrink_value = attrs.getFloat(
            R.styleable.InnerDrawer_shrinkValue,
            0.8f
        )

        isStatusBarIconDark = attrs.getBoolean(
            R.styleable.InnerDrawer_statusBarDark,
            false
        )

        isfooterInside = attrs.getBoolean(
            R.styleable.InnerDrawer_footerInsideMenuList,
            false
        )

        isheaderInside = attrs.getBoolean(
            R.styleable.InnerDrawer_headerInsideMenuList,
            false
        )

        menuItemGravity = attrs.getInt(
            R.styleable.InnerDrawer_menuItemGravity,
            Gravity.CENTER
        )

        isAppBar = attrs.getBoolean(
            R.styleable.InnerDrawer_appBarEnabled,
            false
        )

        closeDrawerOnMenuClick = attrs.getBoolean(
            R.styleable.InnerDrawer_closeDrawerOnMenuClick,
            true
        )

        blurContent = attrs.getBoolean(
            R.styleable.InnerDrawer_blurContent,
            true
        )
    }

    /**
     *set AppBar Title
     */
    fun setAppbarTitle(name: String?) {
        appbar_title!!.text = name
    }

    /**
     *set status bar icon color dark
     */
    fun setstatusbarIconDark(enable: Boolean?) {
        this.isStatusBarIconDark = enable!!
    }

    /**
     *set gravity for menu items
     */
    fun setMenuItemGravity(value: Int) {
        this.menuItemGravity = value
        setmenuLayout()
    }

    /**
     *add menu to drawer
     */
    fun addMenuItem(menuItem: NavMenuItem) {
        if (menuItemsList != null) {
            menuItemsList!!.add(menuItem)
            setmenuLayout()
        }
    }

    /**
     *remove menu from drawer
     */
    fun removeMenuItem(pos: Int) {
        if (menuItemsList != null) {
            if (menuItemsList!!.size > 0) {
                if (pos in menuItemsList!!.indices) {
                    menuItemsList!!.removeAt(pos)
                    setmenuLayout()
                } else {
                    Log.e(TAG, "removeMenuItem: No Item at that position")
                }
            } else {
                Log.e(TAG, "removeMenuItem: No Items Present")
            }
        }
    }

    /**
     *get list of Menu Items
     * @return arrayList<NavMenuItem>
     */
    fun getMenuItemList(): ArrayList<NavMenuItem>? {
        return menuItemsList
    }

    /**
     *set the list of Menu Items
     * NavMenuItem(title="Title of Menu Item",drawableId="res drawable",iconUrl="icon url",iconCache="clear cache for icon url with same path but different images")
     * in NavMenuItem set either drawable or icon url
     */
    fun setMenuItemList(menuItemList: ArrayList<NavMenuItem>?) {
        this.menuItemsList = menuItemList
        setmenuLayout()
    }

    /**
     *remove all Menu Items
     */
    fun removeAllMenuItem() {
        this.menuItemsList = ArrayList()
        setmenuLayout()
    }

    /**
     *Call this after setting all views
     */
    fun setupNavigationDrawer(activity: Activity) {
        setAllViews(activity)
    }

    /**
     *get Header View
     * @return View
     */
    fun getHeaderView(): View? {
        return headersItem
    }

    /**
     *set Header View
     */
    fun setHeaderView(view: View?) {
        if (!header_present) {
            header_present = true
            this.headersItem = view
            if (headersItem != null) {
                if (isheaderInside) {
                    menu_ll!!.addView(headersItem, 0)
                } else {
                    drawer_ll!!.addView(headersItem, 0)
                }
            }
        } else {
            Log.e(TAG, "setHeaderView: Header already present")
        }
    }

    /**
     *remove Header View
     */
    fun removeHeaderView() {
        if (header_present) {
            if (headersItem != null) {
                if (isheaderInside) {
                    menu_ll!!.removeView(headersItem)
                } else {
                    drawer_ll!!.removeView(headersItem)
                }
                header_present = false
            } else {
                Log.e(TAG, "removeHeaderView: Header view not present")
            }
        } else {
            Log.e(TAG, "removeHeaderView: No Header Present")
        }
    }


    /*get background image bitmap or image url of drawer layout, null if none is set
    * @ return bitmap
    */
    fun getBackgroundItem(): Any? {
        if (background_bitmaps != null) {
            return background_bitmaps
        } else if (background_url != null) {
            return background_url
        } else {
            return null
        }
    }


    /**
     *set background image of drawer layout
     * alpha value between 0F and 1F
     */
    fun setBackgroundItem(bitmap: Bitmap?, alpha: Float = 1f) {
        this.background_bitmaps = bitmap
        this.background_alpha = alpha

        if (background_bitmaps != null) {
            if (clear_cache) {
                Glide.with(mContext!!).load(background_bitmaps)
                    .apply(getrequestopt()).into(background_ll!!)
            } else {
                Glide.with(mContext!!).load(background_bitmaps).into(background_ll!!)
            }
            background_ll!!.alpha = this.background_alpha
        }
    }

    /**
     *set background image of drawer layout
     * alpha value between 0F and 1F
     * clear_cache for image url with same path but different images
     */

    fun setBackgroundItem(url: String?, alpha: Float = 1f, clear_cache: Boolean = false) {
        this.background_url = url
        this.background_alpha = alpha

        if (background_url != null) {
            if (clear_cache) {
                Glide.with(mContext!!).load(background_url).apply(getrequestopt())
                    .into(background_ll!!)
            } else {
                Glide.with(mContext!!).load(background_url).into(background_ll!!)
            }
            background_ll!!.alpha = this.background_alpha
        }
    }

    /**
     *enable deafult app bar
     */
    fun setAppBarEnabled(enabled: Boolean?) {
        this.isAppBar = enabled!!
    }

    /**
     *enable blur content
     */
    fun setBlurContentEnabled(enabled: Boolean?) {
        this.blurContent = enabled!!
    }

    /**
     *close drawer on menu click
     */
    fun setCloseDrawerOnMenuClick(enabled: Boolean?) {
        this.closeDrawerOnMenuClick = enabled!!
    }

    /**
     *set Header View inside menu list
     */
    fun setHeaderInside(enabled: Boolean?) {
        this.isheaderInside = enabled!!
    }

    /**
     *set Footer View inside menu list
     */
    fun setFooterInside(enabled: Boolean?) {
        this.isfooterInside = enabled!!
    }

    /**
     *get menu item font
     * @return Typeface
     */

    fun getmenuItemFont(): Typeface? {
        return menuItemFont
    }

    /**
     *set menu item font
     */
    fun setmenuItemFont(font: Typeface?) {
        this.menuItemFont = font
        setmenuLayout()
    }

    /**
     *set main content shrink value
     */

    fun setshrinkValue(value: Float?) {
        this.shrink_value = value!!
    }

    /**
     *set appbar item text size
     */


    /**
     *set appbar title font
     */

    /**
     *set appbar color
     */
    fun setAppbarColor(appbarColor: Int) {
        this.appbarColor = appbarColor
    }

    /**
     *get appbar color
     * @return Int
     */
    fun getAppbarColor(): Int {
        return appbarColor
    }


    /**
     *set drawer layout background color
     */
    fun setNavigationDrawerBackgroundColor(navigationDrawerBackgroundColor: Int) {
        this.navigationDrawerBackgroundColor = navigationDrawerBackgroundColor
    }

    /**
     *get drawer layout background color
     * @return Int
     */
    fun getNavigationDrawerBackgroundColor(): Int {
        return navigationDrawerBackgroundColor
    }

    /**
     *set menu item text color
     */
    fun setMenuItemTextColor(menuItemTextColor: Int) {
        this.menuItemTextColor = menuItemTextColor
        setmenuLayout()
    }

    /**
     *get menu item text color
     * @return Int
     */
    fun getMenuItemTextColor(): Int {
        return menuItemTextColor
    }

    /**
     *set menu item text size
     */
    fun setmenuItemTextSize(menuItemTextSize: Float) {
        this.menuItemTextSize = menuItemTextSize
        setmenuLayout()
    }

    /**
     *get menu item text size
     * @return Float
     */
    fun getMenuItemTextSize(): Float {
        return menuItemTextSize
    }


    /**
     *set menu item icon color
     */
    fun setMenuItemIconColor(menuItemIconColor: Int) {
        this.menuItemIconColor = menuItemIconColor
        setmenuLayout()
    }

    /**
     *get menu item icon color
     * @return Int
     */
    fun getMenuItemIconColor(): Int {
        return menuItemIconColor
    }

    /**
     *get onmenuItemClickListener
     * @return OnMenuItemClickListener
     */
    fun getOnMenuItemClickListener(): OnMenuItemClickListener? {
        return onmenuItemClickListener
    }

    /**
     *set onMenuItemClickListener
     */
    fun setOnMenuItemClickListener(onMenuItemClickListener: OnMenuItemClickListener?) {
        this.onmenuItemClickListener = onMenuItemClickListener
    }

    /**
     *get onnavIconClickListener
     * @return OnNavIconClickListener
     */
    fun getOnNavIconClickListener(): OnNavIconClickListener? {
        return onnavIconClickListener
    }

    /**
     *set onnavIconClickListener
     */
    fun setOnNavIconClickListener(onNavIconClickListener: OnNavIconClickListener?) {
        this.onnavIconClickListener = onNavIconClickListener
    }


    /**
     *get drawerListener
     * @return drawerListener
     */
    fun getOnDrawerListener(): DrawerListener? {
        return drawerListener
    }

    /**
     *set drawerListener
     */
    fun setOnDrawerListener(DrawerListener: DrawerListener?) {
        this.drawerListener = DrawerListener
    }

    companion object {
        //Indicates that any drawer is open. No animation is in progress.
        const val STATE_OPEN = 0

        //Indicates that any drawer is closed. No animation is in progress.
        const val STATE_CLOSED = 1

        //Indicates that a drawer is in the process of opening.
        const val STATE_OPENING = 2

        //Indicates that a drawer is in the process of closing.
        const val STATE_CLOSING = 3
    }
}